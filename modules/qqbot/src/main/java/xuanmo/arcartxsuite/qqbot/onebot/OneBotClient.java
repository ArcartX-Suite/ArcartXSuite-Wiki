package xuanmo.arcartxsuite.qqbot.onebot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class OneBotClient {

    private final URI wsUri;
    private final String accessToken;
    private final int reconnectIntervalSeconds;
    private final int heartbeatIntervalSeconds;
    private final Logger logger;
    private final Consumer<OneBotEvent> eventHandler;
    private final Runnable onConnected;
    private final Runnable onDisconnected;

    private final HttpClient httpClient;
    private final ScheduledExecutorService scheduler;
    private final AtomicReference<WebSocket> wsRef = new AtomicReference<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private ScheduledFuture<?> reconnectFuture;
    private ScheduledFuture<?> heartbeatFuture;
    private int reconnectAttempts = 0;
    private static final int MAX_BACKOFF_SECONDS = 300; // 最大 5 分钟
    private boolean firstFailureLogged = false;

    public OneBotClient(
        String wsUrl,
        String accessToken,
        int reconnectIntervalSeconds,
        int heartbeatIntervalSeconds,
        Logger logger,
        Consumer<OneBotEvent> eventHandler,
        Runnable onConnected,
        Runnable onDisconnected
    ) {
        this.wsUri = URI.create(wsUrl);
        this.accessToken = accessToken == null ? "" : accessToken;
        this.reconnectIntervalSeconds = Math.max(5, reconnectIntervalSeconds);
        this.heartbeatIntervalSeconds = Math.max(10, heartbeatIntervalSeconds);
        this.logger = logger;
        this.eventHandler = eventHandler;
        this.onConnected = onConnected;
        this.onDisconnected = onDisconnected;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AXS-QQBot-WS");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        if (running.getAndSet(true)) return;
        connect();
    }

    public void shutdown() {
        running.set(false);
        connected.set(false);
        if (reconnectFuture != null) {
            reconnectFuture.cancel(false);
            reconnectFuture = null;
        }
        if (heartbeatFuture != null) {
            heartbeatFuture.cancel(false);
            heartbeatFuture = null;
        }
        WebSocket ws = wsRef.getAndSet(null);
        if (ws != null) {
            try {
                ws.sendClose(WebSocket.NORMAL_CLOSURE, "shutdown")
                    .orTimeout(3, TimeUnit.SECONDS)
                    .join();
            } catch (Exception ignored) {}
        }
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean isConnected() {
        return connected.get();
    }

    public void send(String json) {
        WebSocket ws = wsRef.get();
        if (ws != null && connected.get()) {
            ws.sendText(json, true);
        }
    }

    public void sendGroupMessage(long groupId, String message) {
        send(OneBotAction.sendGroupMsg(groupId, message));
    }

    private void connect() {
        if (!running.get()) return;
        if (reconnectAttempts <= 1) {
            logger.info("[QQBot] 正在连接 OneBot: " + wsUri);
        }

        WebSocket.Builder builder = httpClient.newWebSocketBuilder()
            .connectTimeout(Duration.ofSeconds(10));

        if (!accessToken.isEmpty()) {
            builder.header("Authorization", "Bearer " + accessToken);
        }

        builder.buildAsync(wsUri, new WebSocket.Listener() {
            private final StringBuilder buffer = new StringBuilder();

            @Override
            public void onOpen(WebSocket webSocket) {
                logger.info("[QQBot] WebSocket 已连接: " + wsUri);
                wsRef.set(webSocket);
                connected.set(true);
                reconnectAttempts = 0;
                startHeartbeatCheck();
                if (onConnected != null) onConnected.run();
                // 获取登录信息
                webSocket.sendText(OneBotAction.getLoginInfo(), true);
                webSocket.request(1);
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                buffer.append(data);
                if (last) {
                    String text = buffer.toString();
                    buffer.setLength(0);
                    handleMessage(text);
                }
                webSocket.request(1);
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
                webSocket.sendPong(message);
                webSocket.request(1);
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                logger.warning("[QQBot] WebSocket 断开: code=" + statusCode + " reason=" + reason);
                handleDisconnect();
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                logger.warning("[QQBot] WebSocket 错误: " + error.getMessage());
                handleDisconnect();
            }
        }).exceptionally(ex -> {
            if (reconnectAttempts == 0) {
                logger.warning("[QQBot] 连接失败: " + ex.getCause().getClass().getSimpleName());
            }
            scheduleReconnect();
            return null;
        });
    }

    private void handleMessage(String text) {
        try {
            JsonObject json = JsonParser.parseString(text).getAsJsonObject();
            // API 响应（有 echo 字段）
            if (json.has("echo")) {
                String echo = json.get("echo").getAsString();
                if ("get_login_info".equals(echo) && json.has("data")) {
                    JsonObject data = json.getAsJsonObject("data");
                    String nick = data.has("nickname") ? data.get("nickname").getAsString() : "未知";
                    long qq = data.has("user_id") ? data.get("user_id").getAsLong() : 0;
                    logger.info("[QQBot] 登录账号: " + nick + " (" + qq + ")");
                }
                return;
            }
            // 事件
            OneBotEvent event = new OneBotEvent(json);
            if (!event.isHeartbeat()) {
                eventHandler.accept(event);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "[QQBot] 消息解析失败: " + e.getMessage(), e);
        }
    }

    private void handleDisconnect() {
        connected.set(false);
        wsRef.set(null);
        if (heartbeatFuture != null) {
            heartbeatFuture.cancel(false);
            heartbeatFuture = null;
        }
        if (onDisconnected != null) onDisconnected.run();
        scheduleReconnect();
    }

    private void scheduleReconnect() {
        if (!running.get()) return;
        if (reconnectFuture != null && !reconnectFuture.isDone()) return;
        reconnectAttempts++;
        // 指数退避: base * 2^(attempt-1)，上限 MAX_BACKOFF_SECONDS
        int delay = (int) Math.min(
            reconnectIntervalSeconds * Math.pow(2, reconnectAttempts - 1),
            MAX_BACKOFF_SECONDS
        );
        if (!firstFailureLogged) {
            firstFailureLogged = true;
            logger.warning("[QQBot] ══════════════════════════════════════════════════");
            logger.warning("[QQBot]  OneBot 实现端未运行或地址不正确!");
            logger.warning("[QQBot]  当前目标: " + wsUri);
            logger.warning("[QQBot] ");
            logger.warning("[QQBot]  解决方法:");
            logger.warning("[QQBot]    1. 安装 Lagrange / NapCat / LLOneBot");
            logger.warning("[QQBot]    2. 启动并登录 QQ 机器人账号");
            logger.warning("[QQBot]    3. 确认 WebSocket 监听地址与配置一致");
            logger.warning("[QQBot] ");
            logger.warning("[QQBot]  如暂不使用，请在 config.yml 设置:");
            logger.warning("[QQBot]    modules.qqbot.enabled: false");
            logger.warning("[QQBot] ══════════════════════════════════════════════════");
        }
        if (reconnectAttempts <= 3) {
            logger.info("[QQBot] 将在 " + delay + " 秒后尝试重连 (第 " + reconnectAttempts + " 次)...");
        } else if (reconnectAttempts % 5 == 0) {
            // 超过 3 次后每 5 次才打印一次，避免刷屏
            logger.info("[QQBot] 将在 " + delay + " 秒后尝试重连 (第 " + reconnectAttempts + " 次)...");
        }
        reconnectFuture = scheduler.schedule(this::connect, delay, TimeUnit.SECONDS);
    }

    private void startHeartbeatCheck() {
        if (heartbeatFuture != null) {
            heartbeatFuture.cancel(false);
        }
        // 定期发送空 ping 保活（Java HttpClient WS 会自动处理 pong）
        heartbeatFuture = scheduler.scheduleAtFixedRate(() -> {
            WebSocket ws = wsRef.get();
            if (ws == null || !connected.get()) return;
            try {
                ws.sendPing(ByteBuffer.allocate(0));
            } catch (Exception e) {
                logger.fine("[QQBot] Ping 发送失败: " + e.getMessage());
            }
        }, heartbeatIntervalSeconds, heartbeatIntervalSeconds, TimeUnit.SECONDS);
    }
}
