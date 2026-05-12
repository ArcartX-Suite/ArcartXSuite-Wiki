package xuanmo.arcartxsuite.command;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xuanmo.arcartxsuite.ArcartXSuitePlugin;
import xuanmo.arcartxsuite.api.ModuleCommandHandler;
import xuanmo.arcartxsuite.module.ModuleRegistry;
import xuanmo.arcartxsuite.bossbar.config.PluginConfiguration;
import xuanmo.arcartxsuite.bossbar.tracker.ActiveBossSessionView;
import xuanmo.arcartxsuite.bossbar.tracker.BossDamageRewardDispatchResult;
import xuanmo.arcartxsuite.bossbar.tracker.BossDamageSettlementEntry;
import xuanmo.arcartxsuite.bossbar.tracker.BossDamageSettlementRecord;
import xuanmo.arcartxsuite.bossbar.tracker.BossSessionRankingView;
import xuanmo.arcartxsuite.bossbar.tracker.BossTrackerService;
import xuanmo.arcartxsuite.mail.model.MailCdkDefinition;
import xuanmo.arcartxsuite.onlinerewards.model.OnlineRewardsLeaderboardScope;
import xuanmo.arcartxsuite.security.ModulePasswordAuthenticator.ModuleKey;
import xuanmo.arcartxsuite.security.ModulePasswordAuthenticator.ValidationResult;
import xuanmo.arcartxsuite.title.TitleDurationParser;
import xuanmo.arcartxsuite.title.service.TitleOperationResult;

public final class ArcartXSuiteCommand implements CommandExecutor, TabCompleter {

    private static final String PREFIX = ChatColor.AQUA + "[arcartxsuite] " + ChatColor.RESET;
    private static final ThreadLocal<DecimalFormat> DECIMAL_FORMAT = ThreadLocal.withInitial(() -> new DecimalFormat("0.##"));
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<String> MODULE_IDS = List.of(
        "entitytracker",
        "combateffect",
        "eventpacket",
        "tab",
        "title",
        "conversation",
        "announcer",
        "pickup",
        "prop",
        "rgb",
        "onlinerewards",
        "loginview",
        "warehouse",
        "mail",
        "chat",
        "questgps",
        "map"
    );
    private static final List<String> ROOT_SUBCOMMANDS = List.of(
        "help",
        "status",
        "reload",
        "entitytracker",
        "combateffect",
        "eventpacket",
        "tab",
        "title",
        "conversation",
        "announcer",
        "pickup",
        "prop",
        "rgb",
        "onlinerewards",
        "loginview",
        "warehouse",
        "mail",
        "chat",
        "questgps",
        "map"
    );
    private static final List<String> ENTITY_TRACKER_ACTIONS = List.of("help", "status", "reload", "sessions", "rank", "settlements", "settlement", "reissue");
    private static final List<String> MODULE_ACTIONS = List.of("help", "status", "reload");
    private static final List<String> EVENT_PACKET_ACTIONS = List.of("help", "status", "reload", "fire");
    private static final List<String> QUEST_GPS_ACTIONS = List.of("help", "status", "reload", "open");
    private static final List<String> MAP_ACTIONS = List.of("help", "status", "reload", "open", "list", "anchors");
    private static final List<String> PROP_ACTIONS = List.of("help", "status", "reload", "set");
    private static final List<String> TITLE_ACTIONS = List.of("help", "status", "reload", "give", "revoke", "open");
    private static final List<String> ANNOUNCER_ACTIONS = List.of("help", "status", "reload", "subtitle");
    private static final List<String> MAIL_ACTIONS = List.of("help", "status", "reload", "open", "preset", "cdk");
    private static final List<String> CHAT_ACTIONS = List.of("help", "status", "reload", "mute", "unmute", "spy");
    private static final List<String> LOGIN_VIEW_ACTIONS = List.of("help", "status", "reload", "open", "migrate-authme", "migration-commands");
    private static final List<String> ONLINE_REWARD_ACTIONS = List.of("help", "status", "reload", "add", "remove", "set", "card");
    private final ArcartXSuitePlugin plugin;

    public ArcartXSuiteCommand(ArcartXSuitePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!hasPermission(sender, args)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "你没有权限执行这个命令。");
            return true;
        }

        if (args.length == 0 || "status".equalsIgnoreCase(args[0])) {
            sendStatus(sender);
            return true;
        }
        if ("help".equalsIgnoreCase(args[0])) {
            if (args.length >= 2) {
                sendModuleHelp(sender, label, args[1]);
            } else {
                sendHelpOverview(sender, label);
            }
            return true;
        }
        if ("reload".equalsIgnoreCase(args[0])) {
            return handleReload(sender, label, args);
        }
        if (args.length >= 2 && "help".equalsIgnoreCase(args[1])) {
            sendModuleHelp(sender, label, args[0]);
            return true;
        }
        if ("entitytracker".equalsIgnoreCase(args[0])) {
            return handleBossBarCommand(sender, label, args);
        }
        if ("combateffect".equalsIgnoreCase(args[0])) {
            return handleKillEffectCommand(sender, label, args);
        }
        if ("eventpacket".equalsIgnoreCase(args[0])) {
            return handleEventPacketCommand(sender, label, args);
        }
        if ("tab".equalsIgnoreCase(args[0])) {
            return handleTabCommand(sender, label, args);
        }
        if ("title".equalsIgnoreCase(args[0])) {
            return handleTitleCommand(sender, label, args);
        }
        if ("conversation".equalsIgnoreCase(args[0])) {
            return handleConversationCommand(sender, label, args);
        }
        if ("announcer".equalsIgnoreCase(args[0])) {
            return handleAnnouncerCommand(sender, label, args);
        }
        if ("pickup".equalsIgnoreCase(args[0])) {
            return handlePickupCommand(sender, label, args);
        }
        if ("prop".equalsIgnoreCase(args[0])) {
            return handlePropCommand(sender, label, args);
        }
        if ("rgb".equalsIgnoreCase(args[0])) {
            return handleRgbCommand(sender, label, args);
        }
        if ("onlinerewards".equalsIgnoreCase(args[0])) {
            return handleOnlineRewardsCommand(sender, label, args);
        }
        if ("loginview".equalsIgnoreCase(args[0])) {
            return handleLoginViewCommand(sender, label, args);
        }
        if ("warehouse".equalsIgnoreCase(args[0])) {
            return handleWarehouseCommand(sender, label, args);
        }
        if ("mail".equalsIgnoreCase(args[0])) {
            return handleMailCommand(sender, label, args);
        }
        if ("chat".equalsIgnoreCase(args[0])) {
            return handleChatCommand(sender, label, args);
        }
        if ("questgps".equalsIgnoreCase(args[0])) {
            return handleQuestGpsCommand(sender, label, args);
        }
        if ("map".equalsIgnoreCase(args[0])) {
            return handleMapCommand(sender, label, args);
        }

        // 动态委托给外部模块命令处理器
        ModuleRegistry registry = plugin.getModuleRegistry();
        if (registry != null) {
            Optional<ModuleCommandHandler> handler = registry.getCommandHandler(args[0].toLowerCase(java.util.Locale.ROOT));
            if (handler.isPresent()) {
                return handler.get().onCommand(sender, label, args);
            }
        }

        sendUsage(sender, label);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull CommandSender sender,
        @NotNull Command command,
        @NotNull String alias,
        @NotNull String[] args
    ) {
        if (args.length == 1) {
            List<String> combined = new ArrayList<>(ROOT_SUBCOMMANDS);
            ModuleRegistry registry = plugin.getModuleRegistry();
            if (registry != null) {
                for (String extId : registry.externalModuleIds()) {
                    if (!combined.contains(extId)) {
                        combined.add(extId);
                    }
                }
            }
            return filter(combined, args[0]);
        }
        if (args.length == 2) {
            if ("help".equalsIgnoreCase(args[0])) {
                return filter(MODULE_IDS, args[1]);
            }
            if ("reload".equalsIgnoreCase(args[0])) {
                List<String> reloadTargets = new ArrayList<>();
                reloadTargets.add("all");
                reloadTargets.addAll(MODULE_IDS);
                return filter(reloadTargets, args[1]);
            }
            if ("title".equalsIgnoreCase(args[0])) {
                return filter(TITLE_ACTIONS, args[1]);
            }
            if ("mail".equalsIgnoreCase(args[0])) {
                return filter(MAIL_ACTIONS, args[1]);
            }
            if ("chat".equalsIgnoreCase(args[0])) {
                return filter(CHAT_ACTIONS, args[1]);
            }
            if ("onlinerewards".equalsIgnoreCase(args[0])) {
                return filter(ONLINE_REWARD_ACTIONS, args[1]);
            }
            if ("questgps".equalsIgnoreCase(args[0])) {
                return filter(QUEST_GPS_ACTIONS, args[1]);
            }
            if ("map".equalsIgnoreCase(args[0])) {
                return filter(MAP_ACTIONS, args[1]);
            }
            if ("loginview".equalsIgnoreCase(args[0])) {
                return filter(LOGIN_VIEW_ACTIONS, args[1]);
            }
            if ("entitytracker".equalsIgnoreCase(args[0])) {
                return filter(ENTITY_TRACKER_ACTIONS, args[1]);
            }
            if ("eventpacket".equalsIgnoreCase(args[0])) {
                return filter(EVENT_PACKET_ACTIONS, args[1]);
            }
            if ("announcer".equalsIgnoreCase(args[0])) {
                return filter(ANNOUNCER_ACTIONS, args[1]);
            }
            if ("combateffect".equalsIgnoreCase(args[0])
                || "tab".equalsIgnoreCase(args[0])
                || "conversation".equalsIgnoreCase(args[0])
                || "pickup".equalsIgnoreCase(args[0])
                || "prop".equalsIgnoreCase(args[0])
                || "rgb".equalsIgnoreCase(args[0])) {
                return filter("prop".equalsIgnoreCase(args[0]) ? PROP_ACTIONS : MODULE_ACTIONS, args[1]);
            }
            if ("warehouse".equalsIgnoreCase(args[0])) {
                return filter(List.of("help", "status", "reload", "open", "info", "password", "bank"), args[1]);
            }
        }
        if (args.length == 3 && "announcer".equalsIgnoreCase(args[0]) && "subtitle".equalsIgnoreCase(args[1])) {
            return filter(List.of("play", "stop", "list"), args[2]);
        }
        if (args.length == 4 && "announcer".equalsIgnoreCase(args[0]) && "subtitle".equalsIgnoreCase(args[1])
            && ("play".equalsIgnoreCase(args[2]) || "stop".equalsIgnoreCase(args[2]))) {
            return filter(onlinePlayerNames(), args[3]);
        }
        if (args.length == 5 && "announcer".equalsIgnoreCase(args[0]) && "subtitle".equalsIgnoreCase(args[1]) && "play".equalsIgnoreCase(args[2])) {
            return filter(plugin.getSubtitleGroupIds(), args[4]);
        }
        if (args.length == 3 && "mail".equalsIgnoreCase(args[0]) && "open".equalsIgnoreCase(args[1])) {
            return filter(onlinePlayerNames(), args[2]);
        }
        if (args.length == 3 && "onlinerewards".equalsIgnoreCase(args[0]) && ("add".equalsIgnoreCase(args[1]) || "remove".equalsIgnoreCase(args[1]) || "set".equalsIgnoreCase(args[1]))) {
            return filter(List.of("30m", "1h", "2h", "1d"), args[2]);
        }
        if (args.length == 4 && "onlinerewards".equalsIgnoreCase(args[0]) && ("add".equalsIgnoreCase(args[1]) || "remove".equalsIgnoreCase(args[1]) || "set".equalsIgnoreCase(args[1]))) {
            return filter(onlinePlayerNames(), args[3]);
        }
        if (args.length == 3 && "onlinerewards".equalsIgnoreCase(args[0]) && "card".equalsIgnoreCase(args[1])) {
            return filter(List.of("add", "remove", "set"), args[2]);
        }
        if (args.length == 4 && "onlinerewards".equalsIgnoreCase(args[0]) && "card".equalsIgnoreCase(args[1])) {
            return filter(List.of("1", "5", "10"), args[3]);
        }
        if (args.length == 5 && "onlinerewards".equalsIgnoreCase(args[0]) && "card".equalsIgnoreCase(args[1])) {
            return filter(onlinePlayerNames(), args[4]);
        }
        if (args.length == 3 && "entitytracker".equalsIgnoreCase(args[0])) {
            if ("sessions".equalsIgnoreCase(args[1])) {
                PluginConfiguration configuration = plugin.getBossBarConfiguration();
                return filter(configuration == null ? List.of() : configuration.getTrackedBossIds(), args[2]);
            }
            if ("rank".equalsIgnoreCase(args[1]) && plugin.getBossTrackerService() != null) {
                return filter(plugin.getBossTrackerService().sessionEntityIds(), args[2]);
            }
            if (("settlement".equalsIgnoreCase(args[1]) || "reissue".equalsIgnoreCase(args[1])) && plugin.getBossTrackerService() != null) {
                return filter(plugin.getBossTrackerService().settlementIds(), args[2]);
            }
        }
        if (args.length == 3 && "chat".equalsIgnoreCase(args[0]) && ("mute".equalsIgnoreCase(args[1]) || "unmute".equalsIgnoreCase(args[1]) || "spy".equalsIgnoreCase(args[1]))) {
            return filter(onlinePlayerNames(), args[2]);
        }
        if (args.length == 3 && "questgps".equalsIgnoreCase(args[0]) && "open".equalsIgnoreCase(args[1])) {
            return filter(onlinePlayerNames(), args[2]);
        }
        if (args.length == 3 && "eventpacket".equalsIgnoreCase(args[0]) && "fire".equalsIgnoreCase(args[1])) {
            return filter(configuredEventPacketSignals(), args[2]);
        }
        if (args.length == 4 && "eventpacket".equalsIgnoreCase(args[0]) && "fire".equalsIgnoreCase(args[1])) {
            return filter(onlinePlayerNames(), args[3]);
        }
        if (args.length >= 5 && "eventpacket".equalsIgnoreCase(args[0]) && "fire".equalsIgnoreCase(args[1])) {
            return filter(List.of("quest-id=", "task-id=", "reason=", "value="), args[args.length - 1]);
        }
        if (args.length == 3 && "map".equalsIgnoreCase(args[0])) {
            if ("open".equalsIgnoreCase(args[1])) {
                return filter(onlinePlayerNames(), args[2]);
            }
            if ("anchors".equalsIgnoreCase(args[1]) && plugin.getMapConfiguration() != null) {
                return filter(new ArrayList<>(plugin.getMapConfiguration().worlds().keySet()), args[2]);
            }
        }
        if (args.length == 3 && "loginview".equalsIgnoreCase(args[0]) && "open".equalsIgnoreCase(args[1])) {
            return filter(onlinePlayerNames(), args[2]);
        }
        if (args.length == 3 && "loginview".equalsIgnoreCase(args[0]) && "migrate-authme".equalsIgnoreCase(args[1])) {
            return filter(List.of("dry-run"), args[2]);
        }
        // 动态委托给外部模块 Tab 补全
        ModuleRegistry registry = plugin.getModuleRegistry();
        if (registry != null && args.length >= 2) {
            Optional<ModuleCommandHandler> handler = registry.getCommandHandler(args[0].toLowerCase(java.util.Locale.ROOT));
            if (handler.isPresent()) {
                List<String> result = handler.get().onTabComplete(sender, args);
                if (result != null) {
                    return result;
                }
                if (args.length == 2) {
                    return filter(handler.get().actions(), args[1]);
                }
            }
        }
        if (args.length == 4 && "map".equalsIgnoreCase(args[0]) && "open".equalsIgnoreCase(args[1]) && plugin.getMapConfiguration() != null) {
            return filter(new ArrayList<>(plugin.getMapConfiguration().worlds().keySet()), args[3]);
        }
        if (args.length == 3 && "mail".equalsIgnoreCase(args[0]) && "preset".equalsIgnoreCase(args[1])) {
            return filter(List.of("send"), args[2]);
        }
        if (args.length == 3 && "mail".equalsIgnoreCase(args[0]) && "cdk".equalsIgnoreCase(args[1])) {
            return filter(List.of("create", "info", "delete", "list"), args[2]);
        }
        if (args.length == 4 && "mail".equalsIgnoreCase(args[0]) && "preset".equalsIgnoreCase(args[1]) && "send".equalsIgnoreCase(args[2]) && plugin.getMailService() != null) {
            return filter(plugin.getMailService().presetIds(), args[3]);
        }
        if (args.length == 4 && "mail".equalsIgnoreCase(args[0]) && "cdk".equalsIgnoreCase(args[1]) && "create".equalsIgnoreCase(args[2]) && plugin.getMailService() != null) {
            return filter(plugin.getMailService().presetIds(), args[3]);
        }
        if (args.length == 5 && "mail".equalsIgnoreCase(args[0]) && "preset".equalsIgnoreCase(args[1]) && "send".equalsIgnoreCase(args[2])) {
            List<String> targets = new ArrayList<>(onlinePlayerNames());
            targets.add("all-online");
            targets.add("all-registered");
            return filter(targets, args[4]);
        }
        if (args.length == 5 && "mail".equalsIgnoreCase(args[0]) && "cdk".equalsIgnoreCase(args[1]) && "create".equalsIgnoreCase(args[2])) {
            return filter(List.of("auto"), args[4]);
        }
        if (args.length == 6 && "mail".equalsIgnoreCase(args[0]) && "cdk".equalsIgnoreCase(args[1]) && "create".equalsIgnoreCase(args[2])) {
            return filter(List.of("1", "10", "100"), args[5]);
        }
        if (args.length == 7 && "mail".equalsIgnoreCase(args[0]) && "cdk".equalsIgnoreCase(args[1]) && "create".equalsIgnoreCase(args[2])) {
            return filter(List.of("1d", "7d", "30d", "permanent"), args[6]);
        }
        if (args.length == 4 && "mail".equalsIgnoreCase(args[0]) && "cdk".equalsIgnoreCase(args[1]) && "list".equalsIgnoreCase(args[2])) {
            return filter(List.of("1", "2", "3"), args[3]);
        }
        if (args.length == 4 && "chat".equalsIgnoreCase(args[0]) && "spy".equalsIgnoreCase(args[1])) {
            return filter(List.of("on", "off"), args[3]);
        }
        if (args.length == 4 && "chat".equalsIgnoreCase(args[0]) && "mute".equalsIgnoreCase(args[1])) {
            return filter(List.of("30m", "12h", "7d", "permanent"), args[3]);
        }
        if (args.length == 3 && "warehouse".equalsIgnoreCase(args[0]) && ("info".equalsIgnoreCase(args[1]) || "open".equalsIgnoreCase(args[1]) || "password".equalsIgnoreCase(args[1]) || "bank".equalsIgnoreCase(args[1]))) {
            return filter(onlinePlayerNames(), args[2]);
        }
        if (args.length == 3 && "prop".equalsIgnoreCase(args[0]) && "set".equalsIgnoreCase(args[1]) && plugin.getPropService() != null) {
            return filter(plugin.getPropService().propIds(), args[2]);
        }
        if (args.length == 4 && "warehouse".equalsIgnoreCase(args[0]) && "bank".equalsIgnoreCase(args[1]) && plugin.getWarehouseService() != null) {
            return filter(new ArrayList<>(plugin.getWarehouseService().currencyIds()), args[3]);
        }
        if (args.length == 5 && "warehouse".equalsIgnoreCase(args[0]) && "bank".equalsIgnoreCase(args[1])) {
            return filter(List.of("set", "add", "take"), args[4]);
        }
        if (args.length == 3 && "title".equalsIgnoreCase(args[0]) && ("give".equalsIgnoreCase(args[1]) || "revoke".equalsIgnoreCase(args[1]) || "open".equalsIgnoreCase(args[1]))) {
            return filter(onlinePlayerNames(), args[2]);
        }
        if (args.length == 4 && "title".equalsIgnoreCase(args[0]) && ("give".equalsIgnoreCase(args[1]) || "revoke".equalsIgnoreCase(args[1])) && plugin.getTitleConfiguration() != null) {
            return filter(new ArrayList<>(plugin.getTitleConfiguration().titles().keySet()), args[3]);
        }
        if (args.length == 4 && "entitytracker".equalsIgnoreCase(args[0])) {
            if ("reissue".equalsIgnoreCase(args[1]) && plugin.getBossTrackerService() != null) {
                BossDamageSettlementRecord record = plugin.getBossTrackerService().settlement(args[2]);
                List<String> ranks = new ArrayList<>();
                if (record != null) {
                    for (BossDamageSettlementEntry entry : record.rankedEntries()) {
                        ranks.add(Integer.toString(entry.rank()));
                    }
                }
                return filter(ranks, args[3]);
            }
            if ("rank".equalsIgnoreCase(args[1]) || "settlements".equalsIgnoreCase(args[1]) || "settlement".equalsIgnoreCase(args[1])) {
                return filter(List.of("1", "2", "3"), args[3]);
            }
        }
        if (args.length == 5 && "title".equalsIgnoreCase(args[0]) && "give".equalsIgnoreCase(args[1])) {
            return filter(List.of("permanent", "30m", "12h", "7d"), args[4]);
        }
        if (args.length == 5 && "entitytracker".equalsIgnoreCase(args[0]) && "reissue".equalsIgnoreCase(args[1])) {
            return filter(onlinePlayerNames(), args[4]);
        }
        return List.of();
    }

    private boolean handleBossBarCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendBossBarStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1])) {
            boolean success = isExternalModule("bossbar") ? reloadModuleByJarId("bossbar") : plugin.reloadBossBarState(true);
            sendModuleReloadFeedback(sender, ModuleKey.ENTITY_TRACKER, success);
            return true;
        }
        BossTrackerService trackerService = plugin.getBossTrackerService();
        if ("sessions".equalsIgnoreCase(args[1])) {
            if (trackerService == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "EntityTracker 模块当前未启用。");
                return true;
            }
            String mobIdFilter = args.length >= 3 ? args[2] : "";
            List<ActiveBossSessionView> sessions = trackerService.activeSessions(mobIdFilter);
            if (sessions.isEmpty()) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "当前没有匹配的 Boss 会话。");
                return true;
            }
            sender.sendMessage(PREFIX + ChatColor.GRAY + "Boss 会话数量: " + ChatColor.WHITE + sessions.size());
            for (ActiveBossSessionView session : sessions) {
                sender.sendMessage(
                    PREFIX + ChatColor.WHITE
                        + session.entityUuid()
                        + ChatColor.GRAY + " | " + ChatColor.GOLD + session.mythicMobId()
                        + ChatColor.GRAY + " | " + ChatColor.WHITE + session.displayName()
                        + ChatColor.GRAY + " | HP " + ChatColor.WHITE + formatDecimal(session.health()) + "/" + formatDecimal(session.maxHealth())
                        + ChatColor.GRAY + " | 排行 " + ChatColor.WHITE + session.participantCount() + "/" + session.trackedPlayerCount()
                        + ChatColor.GRAY + " | 总伤害 " + ChatColor.WHITE + formatDecimal(session.totalDamage())
                );
            }
            return true;
        }
        if ("rank".equalsIgnoreCase(args[1])) {
            if (trackerService == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "EntityTracker 模块当前未启用。");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " entitytracker rank <entityUuid> [page]");
                return true;
            }
            UUID entityUuid;
            try {
                entityUuid = UUID.fromString(args[2]);
            } catch (IllegalArgumentException exception) {
                sender.sendMessage(PREFIX + ChatColor.RED + "无效的实体 UUID: " + args[2]);
                return true;
            }
            BossSessionRankingView rankingView = trackerService.sessionRanking(entityUuid);
            if (rankingView == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "找不到该 Boss 会话: " + entityUuid);
                return true;
            }
            int page = args.length >= 4 ? Math.max(1, parseInt(args[3], 1)) : 1;
            List<BossDamageSettlementEntry> trackedEntries = rankingView.trackedEntries().stream()
                .map(entry -> BossDamageSettlementEntry.fromRankingEntry(entry, false, "", List.of()))
                .toList();
            sendSettlementLikePage(
                sender,
                "实时排行",
                rankingView.displayName(),
                rankingView.mythicMobId(),
                entityUuid.toString(),
                rankingView.participantCount(),
                rankingView.trackedPlayerCount(),
                rankingView.totalDamage(),
                trackedEntries,
                page,
                false
            );
            return true;
        }
        if ("settlements".equalsIgnoreCase(args[1])) {
            if (trackerService == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "EntityTracker 模块当前未启用。");
                return true;
            }
            List<BossDamageSettlementRecord> settlements = trackerService.settlements();
            if (settlements.isEmpty()) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "当前没有伤害结算记录。");
                return true;
            }
            int page = args.length >= 3 ? Math.max(1, parseInt(args[2], 1)) : 1;
            int pageSize = 10;
            int totalPages = Math.max(1, (settlements.size() + pageSize - 1) / pageSize);
            int safePage = Math.min(page, totalPages);
            int fromIndex = (safePage - 1) * pageSize;
            int toIndex = Math.min(settlements.size(), fromIndex + pageSize);
            sender.sendMessage(
                PREFIX + ChatColor.GRAY + "伤害结算记录 第 " + ChatColor.WHITE + safePage + "/" + totalPages
                    + ChatColor.GRAY + " 页，共 " + ChatColor.WHITE + settlements.size() + ChatColor.GRAY + " 条"
            );
            for (BossDamageSettlementRecord record : settlements.subList(fromIndex, toIndex)) {
                BossDamageSettlementEntry topEntry = record.topEntry();
                sender.sendMessage(
                    PREFIX + ChatColor.GOLD + record.settlementId()
                        + ChatColor.GRAY + " | " + ChatColor.WHITE + record.mythicMobId()
                        + ChatColor.GRAY + " | " + ChatColor.WHITE + record.bossDisplayName()
                        + ChatColor.GRAY + " | 时间 " + ChatColor.WHITE + formatDateTime(record.settledAtMillis())
                        + ChatColor.GRAY + " | 第一名 " + ChatColor.WHITE + (topEntry.rank() > 0 ? topEntry.playerName() : "-")
                        + ChatColor.GRAY + " | " + ChatColor.WHITE + record.rewardSummary()
                );
            }
            return true;
        }
        if ("settlement".equalsIgnoreCase(args[1])) {
            if (trackerService == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "EntityTracker 模块当前未启用。");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " entitytracker settlement <settlementId> [page]");
                return true;
            }
            BossDamageSettlementRecord record = trackerService.settlement(args[2]);
            if (record == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "找不到结算记录: " + args[2]);
                return true;
            }
            int page = args.length >= 4 ? Math.max(1, parseInt(args[3], 1)) : 1;
            sendSettlementLikePage(
                sender,
                "结算详情",
                record.bossDisplayName(),
                record.mythicMobId(),
                record.entityUuid(),
                record.participantCount(),
                record.trackedPlayerCount(),
                record.totalDamage(),
                record.trackedEntries(),
                page,
                true
            );
            return true;
        }
        if ("reissue".equalsIgnoreCase(args[1])) {
            if (trackerService == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "EntityTracker 模块当前未启用。");
                return true;
            }
            if (args.length < 4) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " entitytracker reissue <settlementId> <rank> [player]");
                return true;
            }
            int rank = parseInt(args[3], -1);
            if (rank <= 0) {
                sender.sendMessage(PREFIX + ChatColor.RED + "无效名次: " + args[3]);
                return true;
            }
            OfflinePlayer overridePlayer = null;
            if (args.length >= 5) {
                overridePlayer = resolveOfflinePlayer(args[4]);
                if (overridePlayer == null || overridePlayer.getUniqueId() == null) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "找不到玩家: " + args[4]);
                    return true;
                }
            }
            BossDamageRewardDispatchResult result = trackerService.reissueSettlementReward(args[2], rank, overridePlayer);
            sender.sendMessage(PREFIX + (result.success() ? ChatColor.GREEN : ChatColor.RED) + result.message());
            return true;
        }
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " entitytracker status|reload|sessions [mobId]|rank <entityUuid> [page]|settlements [page]|settlement <settlementId> [page]|reissue <settlementId> <rank> [player]");
        return true;
    }

    private boolean handleKillEffectCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendKillEffectStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1])) {
            boolean success = plugin.reloadKillEffectState(true);
            sendModuleReloadFeedback(sender, ModuleKey.COMBAT_EFFECT, success);
            return true;
        }
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " combateffect status|reload");
        return true;
    }

    private boolean handleEventPacketCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendEventPacketStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1])) {
            boolean success = plugin.reloadEventPacketState(true);
            sendModuleReloadFeedback(sender, ModuleKey.EVENT_PACKET, success);
            return true;
        }
        if ("fire".equalsIgnoreCase(args[1])) {
            if (args.length < 4) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " eventpacket fire <signal> <player> [key=value...]");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[3]);
            if (target == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "目标玩家必须在线。");
                return true;
            }
            Map<String, String> variables = new LinkedHashMap<>();
            for (int index = 4; index < args.length; index++) {
                String raw = args[index];
                int split = raw.indexOf('=');
                if (split <= 0) {
                    continue;
                }
                variables.put(raw.substring(0, split), raw.substring(split + 1));
            }
            plugin.dispatchEventPacketSignal(args[2], target, variables);
            sender.sendMessage(PREFIX + ChatColor.GREEN + "已触发 EventPacket signal=" + args[2] + " -> " + target.getName());
            return true;
        }
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " eventpacket status|reload|fire <signal> <player> [key=value...]");
        return true;
    }

    private boolean handleTabCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendTabStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1])) {
            boolean success = plugin.reloadTabState(true);
            sendModuleReloadFeedback(sender, ModuleKey.TAB, success);
            return true;
        }
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " tab status|reload");
        return true;
    }

    private boolean handleTitleCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendTitleStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1])) {
            boolean success = plugin.reloadTitleState(true);
            sendModuleReloadFeedback(sender, ModuleKey.TITLE, success);
            return true;
        }
        if (plugin.getTitleService() == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Title 模块当前未启用。");
            return true;
        }

        if ("open".equalsIgnoreCase(args[1])) {
            if (args.length < 3) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " title open <player>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "目标玩家必须在线。");
                return true;
            }
            plugin.getTitleService().openMenu(target);
            sender.sendMessage(PREFIX + ChatColor.GREEN + "已为 " + target.getName() + " 打开称号界面。");
            return true;
        }

        if ("give".equalsIgnoreCase(args[1])) {
            if (args.length < 5) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " title give <player> <titleId> <permanent|30m|12h|7d>");
                return true;
            }
            OfflinePlayer target = resolveOfflinePlayer(args[2]);
            if (target == null || target.getUniqueId() == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "找不到玩家: " + args[2]);
                return true;
            }
            Optional<TitleDurationParser.TitleDurationSpec> durationSpec = TitleDurationParser.parse(args[4]);
            if (durationSpec.isEmpty()) {
                sender.sendMessage(PREFIX + ChatColor.RED + "无效时长: " + args[4]);
                return true;
            }
            TitleOperationResult result = plugin.getTitleService().giveTitle(target.getUniqueId(), args[3], durationSpec.get(), sender.getName());
            sender.sendMessage(PREFIX + (result.success() ? ChatColor.GREEN : ChatColor.RED) + result.message());
            return true;
        }

        if ("revoke".equalsIgnoreCase(args[1])) {
            if (args.length < 4) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " title revoke <player> <titleId>");
                return true;
            }
            OfflinePlayer target = resolveOfflinePlayer(args[2]);
            if (target == null || target.getUniqueId() == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "找不到玩家: " + args[2]);
                return true;
            }
            TitleOperationResult result = plugin.getTitleService().revokeTitle(target.getUniqueId(), args[3]);
            sender.sendMessage(PREFIX + (result.success() ? ChatColor.GREEN : ChatColor.RED) + result.message());
            return true;
        }

        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " title status|reload|give|revoke|open");
        return true;
    }


    private boolean handleConversationCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendConversationStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1])) {
            boolean success = plugin.reloadConversationState(true);
            sendModuleReloadFeedback(sender, ModuleKey.CONVERSATION, success);
            return true;
        }
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " conversation status|reload");
        return true;
    }

    private boolean handleAnnouncerCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendAnnouncerStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1])) {
            boolean success = plugin.reloadAnnouncerState(true);
            sendModuleReloadFeedback(sender, ModuleKey.ANNOUNCER, success);
            return true;
        }
        if ("subtitle".equalsIgnoreCase(args[1])) {
            return handleAnnouncerSubtitle(sender, label, args);
        }
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " announcer status|reload|subtitle <play|stop|list>");
        return true;
    }

    private boolean handleAnnouncerSubtitle(CommandSender sender, String label, String[] args) {
        if (args.length == 2 || "list".equalsIgnoreCase(args[2])) {
            List<String> groupIds = plugin.getSubtitleGroupIds();
            sender.sendMessage(PREFIX + ChatColor.GRAY + "字幕组: " + ChatColor.WHITE + (groupIds.isEmpty() ? "无" : String.join(", ", groupIds)));
            return true;
        }
        if ("play".equalsIgnoreCase(args[2])) {
            if (args.length < 5) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " announcer subtitle play <player> <group>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[3]);
            if (target == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "目标玩家必须在线。");
                return true;
            }
            boolean success = plugin.playSubtitleGroup(target, args[4]);
            sender.sendMessage(PREFIX + (success ? ChatColor.GREEN + "已开始播放字幕组。" : ChatColor.RED + "播放失败，请检查模块状态和字幕组名称。"));
            return true;
        }
        if ("stop".equalsIgnoreCase(args[2])) {
            if (args.length < 4) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " announcer subtitle stop <player>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[3]);
            if (target == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "目标玩家必须在线。");
                return true;
            }
            plugin.stopSubtitle(target);
            sender.sendMessage(PREFIX + ChatColor.GREEN + "已停止该玩家的字幕会话。");
            return true;
        }
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " announcer subtitle play <player> <group>|stop <player>|list");
        return true;
    }

    private boolean handleAttackTargetCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendAttackTargetStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1])) {
            boolean success = plugin.reloadAttackTargetState(true);
            sendModuleReloadFeedback(sender, ModuleKey.ENTITY_TRACKER, success);
            return true;
        }
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " entitytracker status|reload");
        return true;
    }

    private boolean handlePickupCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendPickupStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1])) {
            boolean success = plugin.reloadPickupState(true);
            sendModuleReloadFeedback(sender, ModuleKey.PICKUP, success);
            return true;
        }
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " pickup status|reload");
        return true;
    }

    private boolean handlePropCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendPropStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1])) {
            boolean success = plugin.reloadPropState(true);
            sendModuleReloadFeedback(sender, ModuleKey.PROP, success);
            return true;
        }
        if ("set".equalsIgnoreCase(args[1])) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(PREFIX + ChatColor.RED + "只有玩家可以执行该命令。");
                return true;
            }
            if (plugin.getPropService() == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "Prop 模块当前未启用。");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " prop set <propId>");
                return true;
            }
            var result = plugin.getPropService().applyPropToMainHand(player, joinTail(args, 2));
            sender.sendMessage(PREFIX + (result.success() ? ChatColor.GREEN : ChatColor.RED) + result.message());
            return true;
        }
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " prop status|reload|set <propId>");
        return true;
    }

    private boolean handleDigisDisplayCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendDigisDisplayStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1])) {
            boolean success = plugin.reloadDigisDisplayState(true);
            sendModuleReloadFeedback(sender, ModuleKey.COMBAT_EFFECT, success);
            return true;
        }
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " combateffect status|reload");
        return true;
    }

    private boolean handleRgbCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendRgbStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1])) {
            boolean success = plugin.reloadRgbState(true);
            sendModuleReloadFeedback(sender, ModuleKey.RGB, success);
            return true;
        }
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " rgb status|reload");
        return true;
    }

    private boolean handleOnlineRewardsCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendOnlineRewardsStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1])) {
            boolean success = plugin.reloadOnlineRewardsState(true);
            sendModuleReloadFeedback(sender, ModuleKey.ONLINE_REWARDS, success);
            return true;
        }
        if (plugin.getOnlineRewardsService() == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "OnlineRewards 模块当前未启用。");
            return true;
        }
        if ("add".equalsIgnoreCase(args[1]) || "remove".equalsIgnoreCase(args[1]) || "set".equalsIgnoreCase(args[1])) {
            if (args.length < 4) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " " + args[0] + " add|remove|set <time> <player>");
                return true;
            }
            Optional<Integer> minutes = parseOnlineRewardMinutes(args[2]);
            if (minutes.isEmpty()) {
                sender.sendMessage(PREFIX + ChatColor.RED + "无效时长: " + args[2] + "，示例 30m、2h、1d 或整数分钟。");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[3]);
            if (target == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "目标玩家必须在线。");
                return true;
            }
            var result = plugin.getOnlineRewardsService().adjustOnlineTime(target, args[1], minutes.get());
            sender.sendMessage(PREFIX + (result.success() ? ChatColor.GREEN : ChatColor.RED) + result.message());
            return true;
        }
        if ("card".equalsIgnoreCase(args[1])) {
            if (args.length < 5) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " " + args[0] + " card add|remove|set <amount> <player>");
                return true;
            }
            int amount = parseInt(args[3], -1);
            if (amount < 0) {
                sender.sendMessage(PREFIX + ChatColor.RED + "无效补签卡数量: " + args[3]);
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[4]);
            if (target == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "目标玩家必须在线。");
                return true;
            }
            var result = plugin.getOnlineRewardsService().adjustMakeupCards(target, args[2], amount);
            sender.sendMessage(PREFIX + (result.success() ? ChatColor.GREEN : ChatColor.RED) + result.message());
            return true;
        }
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " " + args[0] + " status|reload|add|remove|set|card");
        return true;
    }

    // handlePacketCommandCommand removed — merged into EventPacket

    private boolean handleLoginViewCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendLoginViewStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1])) {
            boolean success = plugin.reloadLoginViewState(true);
            sendModuleReloadFeedback(sender, ModuleKey.LOGIN_VIEW, success);
            return true;
        }
        if ("migration-commands".equalsIgnoreCase(args[1])) {
            sendAuthMeRemovalInstructions(sender, label);
            return true;
        }
        if (plugin.getLoginViewService() == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "LoginView 模块当前未启用。");
            return true;
        }
        if ("open".equalsIgnoreCase(args[1])) {
            if (args.length < 3) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " loginview open <player>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "目标玩家必须在线。");
                return true;
            }
            plugin.getLoginViewService().openFor(target);
            sender.sendMessage(PREFIX + ChatColor.GREEN + "已向 " + target.getName() + " 打开 LoginView。");
            return true;
        }
        if ("migrate-authme".equalsIgnoreCase(args[1])) {
            boolean dryRun = args.length >= 3 && "dry-run".equalsIgnoreCase(args[2]);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    var result = plugin.getLoginViewService().migrateAuthMe(null, dryRun);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        sender.sendMessage(PREFIX + ChatColor.GREEN + "AuthMe 数据库迁移完成: 扫描="
                            + result.scanned() + " | 导入=" + result.imported() + " | 跳过=" + result.skipped()
                            + (result.dryRun() ? " | dry-run 未写入" : ""));
                        if (!result.dryRun()) {
                            sendAuthMeRemovalInstructions(sender, label);
                        }
                    });
                } catch (Exception exception) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                        sender.sendMessage(PREFIX + ChatColor.RED + "AuthMe 迁移失败: " + exception.getMessage())
                    );
                }
            });
            sender.sendMessage(PREFIX + ChatColor.YELLOW + "AuthMe 迁移任务已开始，数据库 hash 会原样复制，不会破解明文密码。");
            return true;
        }
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " loginview status|reload|open <player>|migrate-authme [dry-run]|migration-commands");
        return true;
    }

    private boolean handleMailCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendMailStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1])) {
            boolean success = plugin.reloadMailState(true);
            sendModuleReloadFeedback(sender, ModuleKey.MAIL, success);
            return true;
        }
        if (plugin.getMailService() == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Mail 模块当前未启用。");
            return true;
        }
        if ("open".equalsIgnoreCase(args[1])) {
            if (args.length < 3) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " mail open <player>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "目标玩家必须在线。");
                return true;
            }
            var result = plugin.getMailService().openInbox(target);
            sender.sendMessage(PREFIX + (result.success() ? ChatColor.GREEN : ChatColor.RED) + result.message());
            return true;
        }
        if ("preset".equalsIgnoreCase(args[1])) {
            if (args.length < 5 || !"send".equalsIgnoreCase(args[2])) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " mail preset send <presetId> <player|all-online|all-registered>");
                return true;
            }
            var result = plugin.getMailService().dispatchPreset(args[3], args[4], sender.getName());
            sender.sendMessage(PREFIX + (result.success() ? ChatColor.GREEN : ChatColor.RED) + result.message());
            return true;
        }
        if ("cdk".equalsIgnoreCase(args[1])) {
            if (args.length < 3) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " mail cdk create|info|list|delete ...");
                return true;
            }
            if ("list".equalsIgnoreCase(args[2])) {
                int page = args.length >= 4 ? parseInt(args[3], 1) : 1;
                var definitions = plugin.getMailService().listCdks(page, 10);
                sender.sendMessage(PREFIX + ChatColor.GOLD + "CDK 列表 第 " + Math.max(1, page) + " 页");
                if (definitions.isEmpty()) {
                    sender.sendMessage(PREFIX + ChatColor.YELLOW + "没有可显示的 CDK。");
                    return true;
                }
                Instant now = Instant.now();
                for (var definition : definitions) {
                    sender.sendMessage(
                        PREFIX + ChatColor.AQUA + definition.code()
                            + ChatColor.GRAY + " | 预设: " + ChatColor.WHITE + definition.presetId()
                            + ChatColor.GRAY + " | 领取: " + ChatColor.WHITE + definition.claimedCount() + "/" + definition.maxClaims()
                            + ChatColor.GRAY + " | 过期: " + ChatColor.WHITE + formatCdkExpiry(definition.expiresAt(), now)
                            + ChatColor.GRAY + " | 状态: " + ChatColor.WHITE + formatCdkState(definition, now)
                    );
                }
                return true;
            }
            if ("create".equalsIgnoreCase(args[2])) {
                if (args.length < 7) {
                    sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " mail cdk create <presetId> <code|auto> <maxClaims> <ttl>");
                    return true;
                }
                int maxClaims = parseInt(args[5], -1);
                if (maxClaims <= 0) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "maxClaims 必须大于 0。");
                    return true;
                }
                Instant expiresAt = parseTtl(args[6]);
                if (expiresAt == Instant.MIN) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "无效 TTL，示例: 1d / 12h / 30m / permanent");
                    return true;
                }
                var result = plugin.getMailService().createCdk(args[3], args[4], maxClaims, expiresAt, sender.getName());
                sender.sendMessage(PREFIX + (result.success() ? ChatColor.GREEN : ChatColor.RED) + result.message());
                return true;
            }
            if ("info".equalsIgnoreCase(args[2])) {
                if (args.length < 4) {
                    sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " mail cdk info <code>");
                    return true;
                }
                var definition = plugin.getMailService().loadCdk(args[3].toUpperCase());
                if (definition.isEmpty()) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "CDK 不存在。");
                    return true;
                }
                sender.sendMessage(PREFIX + ChatColor.GRAY + "代码: " + ChatColor.WHITE + definition.get().code());
                sender.sendMessage(PREFIX + ChatColor.GRAY + "预设: " + ChatColor.WHITE + definition.get().presetId());
                sender.sendMessage(PREFIX + ChatColor.GRAY + "领取: " + ChatColor.WHITE + definition.get().claimedCount() + "/" + definition.get().maxClaims());
                sender.sendMessage(PREFIX + ChatColor.GRAY + "到期: " + ChatColor.WHITE + formatCdkExpiry(definition.get().expiresAt(), Instant.now()));
                sender.sendMessage(PREFIX + ChatColor.GRAY + "状态: " + ChatColor.WHITE + formatCdkState(definition.get(), Instant.now()));
                return true;
            }
            if ("delete".equalsIgnoreCase(args[2])) {
                if (args.length < 4) {
                    sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " mail cdk delete <code>");
                    return true;
                }
                var result = plugin.getMailService().deleteCdk(args[3]);
                sender.sendMessage(PREFIX + (result.success() ? ChatColor.GREEN : ChatColor.RED) + result.message());
                return true;
            }
        }

        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " mail status|reload|open|preset|cdk");
        return true;
    }

    private boolean handleChatCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendChatStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1])) {
            boolean success = plugin.reloadChatState(true);
            sendModuleReloadFeedback(sender, ModuleKey.CHAT, success);
            return true;
        }
        if (plugin.getChatService() == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Chat 模块当前未启用。");
            return true;
        }
        if ("mute".equalsIgnoreCase(args[1])) {
            if (args.length < 4) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " chat mute <player> <30m|12h|7d|permanent> [reason]");
                return true;
            }
            Instant expiresAt = parseTtl(args[3]);
            if (expiresAt == Instant.MIN) {
                sender.sendMessage(PREFIX + ChatColor.RED + "无效时长，示例: 30m / 12h / 7d / permanent");
                return true;
            }
            var result = plugin.getChatService().mutePlayer(args[2], expiresAt, joinTail(args, 4), sender.getName());
            sender.sendMessage(PREFIX + (result.success() ? ChatColor.GREEN : ChatColor.RED) + result.message());
            return true;
        }
        if ("unmute".equalsIgnoreCase(args[1])) {
            if (args.length < 3) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " chat unmute <player>");
                return true;
            }
            var result = plugin.getChatService().unmutePlayer(args[2]);
            sender.sendMessage(PREFIX + (result.success() ? ChatColor.GREEN : ChatColor.RED) + result.message());
            return true;
        }
        if ("spy".equalsIgnoreCase(args[1])) {
            if (args.length < 4) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " chat spy <player> <on|off>");
                return true;
            }
            Boolean enabled = "on".equalsIgnoreCase(args[3]) ? Boolean.TRUE : ("off".equalsIgnoreCase(args[3]) ? Boolean.FALSE : null);
            if (enabled == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "状态只能是 on 或 off。");
                return true;
            }
            var result = plugin.getChatService().setSocialSpy(args[2], enabled.booleanValue(), sender.getName());
            sender.sendMessage(PREFIX + (result.success() ? ChatColor.GREEN : ChatColor.RED) + result.message());
            return true;
        }

        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " chat status|reload|mute|unmute|spy");
        return true;
    }

    private boolean handleQuestGpsCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendQuestGpsStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1])) {
            boolean success = plugin.reloadQuestGpsState(true);
            sendModuleReloadFeedback(sender, ModuleKey.QUEST_GPS, success);
            return true;
        }
        if (plugin.getQuestGpsService() == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "QuestGPS 模块当前未启用。");
            return true;
        }
        if ("open".equalsIgnoreCase(args[1])) {
            if (args.length < 3) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " questgps open <player>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "目标玩家必须在线。");
                return true;
            }
            plugin.getQuestGpsService().openMenu(target);
            sender.sendMessage(PREFIX + ChatColor.GREEN + "已为 " + target.getName() + " 打开任务界面。");
            return true;
        }

        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " questgps status|reload|open <player>");
        return true;
    }

    private boolean handleMapCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendMapStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1])) {
            boolean success = plugin.reloadMapState(true);
            sendModuleReloadFeedback(sender, ModuleKey.MAP, success);
            return true;
        }
        if (plugin.getMapService() == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Map 模块当前未启用。");
            return true;
        }
        if ("open".equalsIgnoreCase(args[1])) {
            if (args.length < 3) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " map open <player> [world]");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "目标玩家必须在线。");
                return true;
            }
            String worldId = args.length >= 4 ? args[3] : "";
            var result = plugin.getMapService().openMenuFor(target, worldId);
            sender.sendMessage(PREFIX + (result.success() ? ChatColor.GREEN : ChatColor.RED) + result.message());
            return true;
        }
        if ("list".equalsIgnoreCase(args[1])) {
            if (plugin.getMapConfiguration() == null || plugin.getMapConfiguration().worlds().isEmpty()) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "当前没有已配置的地图世界。");
                return true;
            }
            sender.sendMessage(PREFIX + ChatColor.GRAY + "已配置地图世界: " + ChatColor.WHITE + String.join(", ", plugin.getMapConfiguration().worlds().keySet()));
            return true;
        }
        if ("anchors".equalsIgnoreCase(args[1])) {
            String worldId = args.length >= 3 ? args[2].toLowerCase() : "";
            if (plugin.getMapConfiguration() == null) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "Map 配置尚未初始化。");
                return true;
            }
            List<String> anchorIds = plugin.getMapConfiguration().anchors().values().stream()
                .filter(anchor -> worldId.isBlank() || anchor.worldId().equalsIgnoreCase(worldId))
                .map(anchor -> anchor.id() + "@" + anchor.worldId())
                .toList();
            if (anchorIds.isEmpty()) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "没有匹配的已配置锚点。");
                return true;
            }
            sender.sendMessage(PREFIX + ChatColor.GRAY + "已配置锚点: " + ChatColor.WHITE + String.join(", ", anchorIds));
            return true;
        }

        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " map status|reload|open <player> [world]|list|anchors [world]");
        return true;
    }

    private boolean handleWarehouseCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1 || "status".equalsIgnoreCase(args[1])) {
            sendWarehouseStatus(sender);
            return true;
        }
        if ("reload".equalsIgnoreCase(args[1])) {
            boolean success = plugin.reloadWarehouseState(true);
            sendModuleReloadFeedback(sender, ModuleKey.WAREHOUSE, success);
            return true;
        }
        if (plugin.getWarehouseService() == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Warehouse 模块当前未启用。");
            return true;
        }
        if ("open".equalsIgnoreCase(args[1])) {
            if (args.length < 3) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " warehouse open <player>");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[2]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(PREFIX + ChatColor.RED + "玩家不在线: " + args[2]);
                return true;
            }
            var result = plugin.getWarehouseService().openMenu(target);
            sender.sendMessage(PREFIX + (result.success() ? ChatColor.GREEN : ChatColor.RED) + result.message());
            return true;
        }
        if ("info".equalsIgnoreCase(args[1])) {
            if (args.length < 3) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " warehouse info <player>");
                return true;
            }
            OfflinePlayer target = resolveOfflinePlayer(args[2]);
            if (target == null || target.getUniqueId() == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "找不到玩家: " + args[2]);
                return true;
            }
            plugin.getWarehouseService().describePlayer(
                target.getUniqueId(),
                target.getName() == null ? args[2] : target.getName(),
                lines -> lines.forEach(line -> sender.sendMessage(PREFIX + line)),
                error -> sender.sendMessage(PREFIX + ChatColor.RED + error)
            );
            return true;
        }
        if ("password".equalsIgnoreCase(args[1])) {
            if (args.length < 3) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " warehouse password <player> clear");
                return true;
            }
            OfflinePlayer target = resolveOfflinePlayer(args[2]);
            if (target == null || target.getUniqueId() == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "找不到玩家: " + args[2]);
                return true;
            }
            if (args.length >= 4 && "clear".equalsIgnoreCase(args[3])) {
                plugin.getWarehouseService().adminClearSecondaryPassword(target.getUniqueId(), result -> sender.sendMessage(PREFIX + (result.success() ? ChatColor.GREEN : ChatColor.RED) + result.message()));
            } else {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "AXUI 版本不再提供管理员明文查询，请使用 clear 重置。");
            }
            return true;
        }
        if ("bank".equalsIgnoreCase(args[1])) {
            if (args.length < 6) {
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " warehouse bank <player> <currency> <set|add|take> <amount>");
                return true;
            }
            OfflinePlayer target = resolveOfflinePlayer(args[2]);
            if (target == null || target.getUniqueId() == null) {
                sender.sendMessage(PREFIX + ChatColor.RED + "找不到玩家: " + args[2]);
                return true;
            }
            plugin.getWarehouseService().adminAdjustWallet(
                target.getUniqueId(),
                args[3],
                args[4],
                args[5],
                result -> sender.sendMessage(PREFIX + (result.success() ? ChatColor.GREEN : ChatColor.RED) + result.message())
            );
            return true;
        }
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "用法: /" + label + " warehouse status|reload|open|info|password|bank");
        return true;
    }

    private boolean isExternalModule(String moduleJarId) {
        ModuleRegistry registry = plugin.getModuleRegistry();
        return registry != null && registry.isModuleLoaded(moduleJarId);
    }

    private boolean reloadModuleByJarId(String moduleJarId) {
        ModuleRegistry registry = plugin.getModuleRegistry();
        return registry != null && registry.reloadModule(moduleJarId);
    }

    private boolean handleReload(CommandSender sender, String label, String[] args) {
        String target = args.length < 2 ? "all" : args[1].toLowerCase();
        switch (target) {
            case "all" -> {
                boolean killEffectSuccess = isExternalModule("killeffect") ? reloadModuleByJarId("killeffect") : plugin.reloadKillEffectState(true);
                boolean eventPacketSuccess = isExternalModule("eventpacket") ? reloadModuleByJarId("eventpacket") : plugin.reloadEventPacketState(true);
                boolean tabSuccess = isExternalModule("tab") ? reloadModuleByJarId("tab") : plugin.reloadTabState(true);
                boolean titleSuccess = isExternalModule("title") ? reloadModuleByJarId("title") : plugin.reloadTitleState(true);
                boolean conversationSuccess = isExternalModule("conversation") ? reloadModuleByJarId("conversation") : plugin.reloadConversationState(true);
                boolean announcerSuccess = isExternalModule("announcer") ? reloadModuleByJarId("announcer") : plugin.reloadAnnouncerState(true);
                boolean pickupSuccess = isExternalModule("pickup") ? reloadModuleByJarId("pickup") : plugin.reloadPickupState(true);
                boolean propSuccess = isExternalModule("prop") ? reloadModuleByJarId("prop") : plugin.reloadPropState(true);
                boolean rgbSuccess = isExternalModule("rgb") ? reloadModuleByJarId("rgb") : plugin.reloadRgbState(true);
                boolean loginViewSuccess = isExternalModule("loginview") ? reloadModuleByJarId("loginview") : plugin.reloadLoginViewState(true);
                boolean onlineRewardsSuccess = isExternalModule("onlinerewards") ? reloadModuleByJarId("onlinerewards") : plugin.reloadOnlineRewardsState(true);
                boolean warehouseSuccess = isExternalModule("warehouse") ? reloadModuleByJarId("warehouse") : plugin.reloadWarehouseState(true);
                boolean mailSuccess = isExternalModule("mail") ? reloadModuleByJarId("mail") : plugin.reloadMailState(true);
                boolean chatSuccess = isExternalModule("chat") ? reloadModuleByJarId("chat") : plugin.reloadChatState(true);
                boolean questGpsSuccess = isExternalModule("questgps") ? reloadModuleByJarId("questgps") : plugin.reloadQuestGpsState(true);
                boolean mapSuccess = isExternalModule("map") ? reloadModuleByJarId("map") : plugin.reloadMapState(true);
                boolean bossBarSuccess = isExternalModule("bossbar") ? reloadModuleByJarId("bossbar") : plugin.reloadBossBarState(true);
                if (bossBarSuccess
                    && killEffectSuccess
                    && eventPacketSuccess
                    && tabSuccess
                    && titleSuccess
                    && conversationSuccess
                    && announcerSuccess
                    && rgbSuccess
                    && propSuccess
                    && loginViewSuccess
                    && onlineRewardsSuccess
                    && warehouseSuccess
                    && mailSuccess
                    && chatSuccess
                    && questGpsSuccess
                    && mapSuccess
                    && pickupSuccess) {
                    sender.sendMessage(PREFIX + ChatColor.GREEN + "全部模块重载完成。");
                    return true;
                }
                List<String> authFailures = collectPasswordFailures(
                    ModuleKey.COMBAT_EFFECT,
                    ModuleKey.EVENT_PACKET,
                    ModuleKey.TAB,
                    ModuleKey.TITLE,
                    ModuleKey.CONVERSATION,
                    ModuleKey.ANNOUNCER,
                    ModuleKey.PICKUP,
                    ModuleKey.PROP,
                    ModuleKey.RGB,
                    ModuleKey.LOGIN_VIEW,
                    ModuleKey.ONLINE_REWARDS,
                    ModuleKey.WAREHOUSE,
                    ModuleKey.MAIL,
                    ModuleKey.CHAT,
                    ModuleKey.QUEST_GPS,
                    ModuleKey.MAP,
                    ModuleKey.ENTITY_TRACKER
                );
                if (!authFailures.isEmpty()) {
                    sender.sendMessage(PREFIX + ChatColor.RED + "鉴权未通过: " + ChatColor.WHITE + String.join(" | ", authFailures));
                }
                sender.sendMessage(
                    PREFIX + ChatColor.YELLOW + "部分模块重载完成。"
                        + ChatColor.GRAY + " CombatEffect=" + killEffectSuccess
                        + " EventPacket=" + eventPacketSuccess
                        + " Tab=" + tabSuccess
                        + " Title=" + titleSuccess
                        + " Conversation=" + conversationSuccess
                        + " Announcer=" + announcerSuccess
                        + " Pickup=" + pickupSuccess
                        + " Prop=" + propSuccess
                        + " RGB=" + rgbSuccess
                        + " LoginView=" + loginViewSuccess
                        + " OnlineRewards=" + onlineRewardsSuccess
                        + " Warehouse=" + warehouseSuccess
                        + " Mail=" + mailSuccess
                        + " Chat=" + chatSuccess
                        + " QuestGPS=" + questGpsSuccess
                        + " Map=" + mapSuccess
                        + " EntityTracker=" + bossBarSuccess
                );
                return true;
            }
            case "entitytracker" -> {
                boolean success = isExternalModule("bossbar") ? reloadModuleByJarId("bossbar") : plugin.reloadBossBarState(true);
                sendModuleReloadFeedback(sender, ModuleKey.ENTITY_TRACKER, success);
                return true;
            }
            case "combateffect" -> {
                boolean success = isExternalModule("killeffect") ? reloadModuleByJarId("killeffect") : plugin.reloadKillEffectState(true);
                sendModuleReloadFeedback(sender, ModuleKey.COMBAT_EFFECT, success);
                return true;
            }
            case "eventpacket" -> {
                boolean success = isExternalModule("eventpacket") ? reloadModuleByJarId("eventpacket") : plugin.reloadEventPacketState(true);
                sendModuleReloadFeedback(sender, ModuleKey.EVENT_PACKET, success);
                return true;
            }
            case "tab" -> {
                boolean success = isExternalModule("tab") ? reloadModuleByJarId("tab") : plugin.reloadTabState(true);
                sendModuleReloadFeedback(sender, ModuleKey.TAB, success);
                return true;
            }
            case "title" -> {
                boolean success = isExternalModule("title") ? reloadModuleByJarId("title") : plugin.reloadTitleState(true);
                sendModuleReloadFeedback(sender, ModuleKey.TITLE, success);
                return true;
            }
            case "conversation" -> {
                boolean success = isExternalModule("conversation") ? reloadModuleByJarId("conversation") : plugin.reloadConversationState(true);
                sendModuleReloadFeedback(sender, ModuleKey.CONVERSATION, success);
                return true;
            }
            case "announcer" -> {
                boolean success = isExternalModule("announcer") ? reloadModuleByJarId("announcer") : plugin.reloadAnnouncerState(true);
                sendModuleReloadFeedback(sender, ModuleKey.ANNOUNCER, success);
                return true;
            }
            case "pickup" -> {
                boolean success = isExternalModule("pickup") ? reloadModuleByJarId("pickup") : plugin.reloadPickupState(true);
                sendModuleReloadFeedback(sender, ModuleKey.PICKUP, success);
                return true;
            }
            case "prop" -> {
                boolean success = isExternalModule("prop") ? reloadModuleByJarId("prop") : plugin.reloadPropState(true);
                sendModuleReloadFeedback(sender, ModuleKey.PROP, success);
                return true;
            }
            case "rgb" -> {
                boolean success = isExternalModule("rgb") ? reloadModuleByJarId("rgb") : plugin.reloadRgbState(true);
                sendModuleReloadFeedback(sender, ModuleKey.RGB, success);
                return true;
            }
            case "loginview" -> {
                boolean success = isExternalModule("loginview") ? reloadModuleByJarId("loginview") : plugin.reloadLoginViewState(true);
                sendModuleReloadFeedback(sender, ModuleKey.LOGIN_VIEW, success);
                return true;
            }
            case "onlinerewards" -> {
                boolean success = isExternalModule("onlinerewards") ? reloadModuleByJarId("onlinerewards") : plugin.reloadOnlineRewardsState(true);
                sendModuleReloadFeedback(sender, ModuleKey.ONLINE_REWARDS, success);
                return true;
            }
            case "warehouse" -> {
                boolean success = isExternalModule("warehouse") ? reloadModuleByJarId("warehouse") : plugin.reloadWarehouseState(true);
                sendModuleReloadFeedback(sender, ModuleKey.WAREHOUSE, success);
                return true;
            }
            case "mail" -> {
                boolean success = isExternalModule("mail") ? reloadModuleByJarId("mail") : plugin.reloadMailState(true);
                sendModuleReloadFeedback(sender, ModuleKey.MAIL, success);
                return true;
            }
            case "chat" -> {
                boolean success = isExternalModule("chat") ? reloadModuleByJarId("chat") : plugin.reloadChatState(true);
                sendModuleReloadFeedback(sender, ModuleKey.CHAT, success);
                return true;
            }
            case "questgps" -> {
                boolean success = isExternalModule("questgps") ? reloadModuleByJarId("questgps") : plugin.reloadQuestGpsState(true);
                sendModuleReloadFeedback(sender, ModuleKey.QUEST_GPS, success);
                return true;
            }
            case "map" -> {
                boolean success = isExternalModule("map") ? reloadModuleByJarId("map") : plugin.reloadMapState(true);
                sendModuleReloadFeedback(sender, ModuleKey.MAP, success);
                return true;
            }
            default -> {
                sender.sendMessage(
                    PREFIX + ChatColor.YELLOW
                        + "用法: /"
                        + label
                        + " reload [all|entitytracker|combateffect|eventpacket|tab|title|conversation|announcer|pickup|prop|rgb|onlinerewards|loginview|warehouse|mail|chat|questgps|map]"
                );
                return true;
            }
        }
    }

    private void sendStatus(CommandSender sender) {
        var bossBarConfiguration = plugin.getBossBarConfiguration();
        var killEffectConfiguration = plugin.getKillEffectConfiguration();
        var eventPacketConfiguration = plugin.getEventPacketConfiguration();
        var tabConfiguration = plugin.getTabConfiguration();
        var titleConfiguration = plugin.getTitleConfiguration();
        var conversationConfiguration = plugin.getConversationConfiguration();
        var subtitleConfiguration = plugin.getSubtitleConfiguration();
        var announcerConfiguration = plugin.getAnnouncerConfiguration();
        var attackTargetConfiguration = plugin.getAttackTargetConfiguration();
        var pickupConfiguration = plugin.getPickupConfiguration();
        var propConfiguration = plugin.getPropConfiguration();
        var digisDisplayConfiguration = plugin.getDigisDisplayConfiguration();
        var rgbConfiguration = plugin.getRgbConfiguration();
        var loginViewConfiguration = plugin.getLoginViewConfiguration();
        var onlineRewardsConfiguration = plugin.getOnlineRewardsConfiguration();
        var warehouseConfiguration = plugin.getWarehouseConfiguration();
        var mailConfiguration = plugin.getMailConfiguration();
        var chatConfiguration = plugin.getChatConfiguration();
        var questGpsConfiguration = plugin.getQuestGpsConfiguration();
        var mapConfiguration = plugin.getMapConfiguration();

        sender.sendMessage(PREFIX + ChatColor.GRAY + "服务端环境: " + ChatColor.WHITE + plugin.getServerPlatform().displayName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "ArcartX 桥接: " + ChatColor.WHITE + plugin.describePacketBridgeMode());
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "EntityTracker 模块: " + ChatColor.WHITE
                + (plugin.isBossBarModuleReady()
                    ? "运行中"
                    : (plugin.isPasswordGateLocked()
                        ? "已锁定"
                        : (plugin.isHybridBootstrapPending() ? "等待兼容启动" : "未运行")))
                + ChatColor.GRAY + " | 授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.ENTITY_TRACKER)
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "EntityTracker 配置数: " + ChatColor.WHITE + (bossBarConfiguration == null ? "0" : bossBarConfiguration.getTrackedBossCount()));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "CombatEffect 模块: " + ChatColor.WHITE
                + (plugin.isKillEffectModuleReady() ? "运行中" : "未运行")
                + ChatColor.GRAY + " | 授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.COMBAT_EFFECT)
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "CombatEffect 配置数: " + ChatColor.WHITE
                + (killEffectConfiguration == null ? "0/0" : killEffectConfiguration.enabledPacketCount() + "/" + killEffectConfiguration.packetDefinitions().size())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "EventPacket 模块: " + ChatColor.WHITE
                + (plugin.isEventPacketModuleReady() ? "运行中" : "未运行")
                + ChatColor.GRAY + " | 授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.EVENT_PACKET)
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "EventPacket 规则数: " + ChatColor.WHITE
                + (eventPacketConfiguration == null ? "0/0" : eventPacketConfiguration.enabledRuleCount() + "/" + eventPacketConfiguration.rules().size())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Tab 模块: " + ChatColor.WHITE
                + (plugin.isTabModuleReady() ? "运行中" : "未运行")
                + ChatColor.GRAY + " | 授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.TAB)
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Tab 配置数: " + ChatColor.WHITE + (tabConfiguration == null ? "0" : tabConfiguration.definitions().size()));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Title 模块: " + ChatColor.WHITE
                + (plugin.isTitleModuleReady() ? "运行中" : "未运行")
                + ChatColor.GRAY + " | 授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.TITLE)
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Title 配置数: " + ChatColor.WHITE + (titleConfiguration == null ? "0" : titleConfiguration.enabledTitleCount()));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Conversation 模块: " + ChatColor.WHITE
                + (plugin.isConversationModuleReady() ? "运行中" : "未运行")
                + ChatColor.GRAY + " | 授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.CONVERSATION)
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Conversation 摘要: " + ChatColor.WHITE
                + (conversationConfiguration == null
                    ? "未初始化"
                    : ("theme=" + conversationConfiguration.themeName()
                        + " | ui=" + plugin.getConversationRuntimeUiId()
                        + " | packet-id=" + conversationConfiguration.clientPacketId()
                        + " | sessions=" + plugin.getConversationActiveSessionCount()
                        + "/" + plugin.getConversationOpenedPlayerCount()))
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Announcer 字幕: " + ChatColor.WHITE
                + (plugin.isSubtitleModuleReady() ? "运行中" : "未运行")
                + " | groups=" + plugin.getSubtitleGroupIds().size()
                + (subtitleConfiguration != null ? " | ui=" + subtitleConfiguration.uiId() : "")
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Announcer 模块: " + ChatColor.WHITE
                + (plugin.isAnnouncerModuleReady() ? "运行中" : "未运行")
                + ChatColor.GRAY + " | 授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.ANNOUNCER)
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Announcer 摘要: " + ChatColor.WHITE
                + (announcerConfiguration == null
                    ? "未初始化"
                    : ("entries=" + plugin.getAnnouncerActiveEntryCount()
                        + "/" + announcerConfiguration.entries().size()
                        + " | auto-play=" + announcerConfiguration.autoPlay()))
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "EntityTracker 攻击目标: " + ChatColor.WHITE
                + (plugin.isAttackTargetModuleReady() ? "运行中" : "未运行")
                + (attackTargetConfiguration != null
                    ? " | ui=" + attackTargetConfiguration.uiId()
                        + " | tracked=" + plugin.getAttackTargetActiveTargetCount()
                        + "/" + plugin.getAttackTargetActiveViewerCount()
                    : "")
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Pickup 模块: " + ChatColor.WHITE
                + (plugin.isPickupModuleReady() ? "运行中" : "未运行")
                + ChatColor.GRAY + " | 授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.PICKUP)
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Pickup 摘要: " + ChatColor.WHITE
                + (pickupConfiguration == null
                    ? "未初始化"
                    : ("ui=" + pickupConfiguration.uiId()
                        + " | max-visible=" + pickupConfiguration.maxVisible()
                        + " | ttl=" + pickupConfiguration.entryTtlMs() + "ms"))
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Prop 模块: " + ChatColor.WHITE
                + (plugin.isPropModuleReady() ? "运行中" : "未运行")
                + ChatColor.GRAY + " | 授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.PROP)
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Prop 摘要: " + ChatColor.WHITE
                + (propConfiguration == null
                    ? "未初始化"
                    : ("props=" + (plugin.getPropService() == null ? 0 : plugin.getPropService().propCount())
                        + " | keys=" + (plugin.getPropService() == null ? 0 : plugin.getPropService().registeredKeyCount())
                        + " | category=" + (plugin.getPropService() == null ? "未初始化" : plugin.getPropService().keyCategory())
                        + " | prop-id-writer=" + plugin.describePropWriterBackend()))
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "CombatEffect 伤害飘字: " + ChatColor.WHITE
                + (plugin.isDigisDisplayModuleReady() ? "运行中" : "未运行")
                + (digisDisplayConfiguration != null
                    ? " | damage=" + digisDisplayConfiguration.damageEnabled()
                        + " | heal=" + digisDisplayConfiguration.healEnabled()
                        + " | source=" + plugin.getDigisDisplayActiveDamageSource()
                    : "")
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "ArcartRGB 模块: " + ChatColor.WHITE
                + (plugin.isRgbModuleReady() ? "运行中" : "未运行")
                + ChatColor.GRAY + " | 授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.RGB)
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "ArcartRGB 摘要: " + ChatColor.WHITE
                + (rgbConfiguration == null
                    ? "未初始化"
                    : ("entries=" + plugin.getRgbActiveEntryCount()
                        + "/"
                        + rgbConfiguration.entries().size()
                        + " | PAPI="
                        + (plugin.isPlaceholderApiAvailable() ? (plugin.isPlaceholderExpansionRegistered() ? "已注册" : "注册失败") : "未安装")))
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "PacketCommand(→EventPacket): " + ChatColor.WHITE
                + (plugin.isPacketCommandIntegrated() ? "已集成" : "未集成")
                + " | packet-id=" + plugin.getPacketCommandPacketId()
                + " | presets=" + plugin.getPacketCommandPresetCount()
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "LoginView 模块: " + ChatColor.WHITE
                + (plugin.isLoginViewModuleReady() ? "运行中" : "未运行")
                + ChatColor.GRAY + " | 授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.LOGIN_VIEW)
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "LoginView 摘要: " + ChatColor.WHITE
                + (loginViewConfiguration == null
                    ? "未初始化"
                    : ("mode=" + loginViewConfiguration.authMode().configKey()
                        + " | ui=" + plugin.getLoginViewRuntimeUiId()
                        + " | packet-id=" + loginViewConfiguration.ui().packetId()
                        + " | accounts=" + (plugin.getLoginViewService() == null ? 0 : plugin.getLoginViewService().accountCount())))
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "OnlineRewards 模块: " + ChatColor.WHITE
                + (plugin.isOnlineRewardsModuleReady() ? "运行中" : "未运行")
                + ChatColor.GRAY + " | 授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.ONLINE_REWARDS)
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "OnlineRewards 摘要: " + ChatColor.WHITE
                + (onlineRewardsConfiguration == null
                    ? "未初始化"
                    : ("stages=" + onlineRewardsConfiguration.rewards().size()
                        + " | cached=" + plugin.getOnlineRewardsCachedPlayerCount()
                        + " | storage=" + onlineRewardsConfiguration.storage().dialect().configKey()
                        + " | signin=" + onlineRewardsConfiguration.signIn().reminderOnJoin()
                        + " | vars=" + onlineRewardsConfiguration.progressVariableName()
                        + ", " + onlineRewardsConfiguration.titleVariableName()))
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Warehouse 模块: " + ChatColor.WHITE
                + (plugin.isWarehouseModuleReady() ? "运行中" : "未运行")
                + ChatColor.GRAY + " | 授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.WAREHOUSE)
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Warehouse 摘要: " + ChatColor.WHITE
                + (warehouseConfiguration == null
                    ? "未初始化"
                    : ("warehouses=" + warehouseConfiguration.warehouses().size()
                        + " | categories=" + warehouseConfiguration.categories().size()
                        + " | currencies=" + warehouseConfiguration.currencies().size()
                        + " | products=" + warehouseConfiguration.depositProducts().size()
                        + " | storage=" + warehouseConfiguration.storage().dialect().configKey()
                        + " | cached=" + (plugin.getWarehouseService() == null ? 0 : plugin.getWarehouseService().cachedPlayerCount())))
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Mail 模块: " + ChatColor.WHITE
                + (plugin.isMailModuleReady() ? "运行中" : "未运行")
                + ChatColor.GRAY + " | 授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.MAIL)
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Mail 摘要: " + ChatColor.WHITE
                + (mailConfiguration == null
                    ? "未初始化"
                    : ("storage=" + mailConfiguration.storage().dialect().configKey()
                        + " | presets=" + (plugin.getMailService() == null ? 0 : plugin.getMailService().presetCount())
                        + " | redis=" + (plugin.getMailService() != null && plugin.getMailService().redisActive())
                        + " | vault=" + plugin.isVaultEconomyAvailable()))
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Chat 模块: " + ChatColor.WHITE
                + (plugin.isChatModuleReady() ? "运行中" : "未运行")
                + ChatColor.GRAY + " | 授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.CHAT)
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Chat 摘要: " + ChatColor.WHITE
                + (chatConfiguration == null
                    ? "未初始化"
                    : ("channels=" + chatConfiguration.channels().size()
                        + " | storage=" + chatConfiguration.storage().dialect().configKey()
                        + " | transport=" + (plugin.getChatService() == null ? "none" : plugin.getChatService().transportName())
                        + " | states=" + (plugin.getChatService() == null ? 0 : plugin.getChatService().cachedStateCount())))
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "QuestGPS 模块: " + ChatColor.WHITE
                + (plugin.isQuestGpsModuleReady() ? "运行中" : "未运行")
                + ChatColor.GRAY + " | 授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.QUEST_GPS)
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "QuestGPS 摘要: " + ChatColor.WHITE
                + (questGpsConfiguration == null
                    ? "未初始化"
                    : ("packet-id=" + questGpsConfiguration.client().packetId()
                        + " | ui=" + plugin.getQuestGpsRuntimeUiId()
                        + " | navigation=" + questGpsConfiguration.navigation().enabled()))
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Map 模块: " + ChatColor.WHITE
                + (plugin.isMapModuleReady() ? "运行中" : "未运行")
                + ChatColor.GRAY + " | 授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.MAP)
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Map 摘要: " + ChatColor.WHITE
                + (mapConfiguration == null
                    ? "未初始化"
                    : ("packet-id=" + mapConfiguration.client().packetId()
                        + " | menu=" + plugin.getMapMenuRuntimeUiId()
                        + " | hud=" + plugin.getMapHudRuntimeUiId()
                        + " | worlds=" + mapConfiguration.worlds().size()
                        + " | anchors=" + mapConfiguration.anchors().size()))
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "PAPI 占位符: " + ChatColor.WHITE
                + (plugin.isPlaceholderApiAvailable() ? (plugin.isPlaceholderExpansionRegistered() ? "已注册" : "注册失败") : "未安装")
        );
    }

    private void sendBossBarStatus(CommandSender sender) {
        var configuration = plugin.getBossBarConfiguration();
        BossTrackerService trackerService = plugin.getBossTrackerService();
        String runtimeUiId = plugin.getBossBarRuntimeUiId().isBlank()
            ? (configuration == null ? "尚未初始化" : configuration.uiId())
            : plugin.getBossBarRuntimeUiId();
        String trackerStatus = trackerService == null
            ? (plugin.isHybridBootstrapPending() ? "等待兼容启动完成" : "未运行")
            : ("会话=" + trackerService.getActiveSessionCount() + "，显示玩家=" + trackerService.getActiveViewerCount());
        boolean bridgeReady = plugin.getPacketBridge() != null && plugin.getPacketBridge().isAvailable();

        sender.sendMessage(PREFIX + ChatColor.GRAY + "服务端环境: " + ChatColor.WHITE + plugin.getServerPlatform().displayName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置 UI 标识: " + ChatColor.WHITE + (configuration == null ? "尚未初始化" : configuration.uiId()));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "实际 UI 标识: " + ChatColor.WHITE + runtimeUiId);
        sender.sendMessage(PREFIX + ChatColor.GRAY + "已跟踪 Boss: " + ChatColor.WHITE + (configuration == null ? "尚未初始化" : configuration.getTrackedBossCount()));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "伤害排行 Boss: " + ChatColor.WHITE
                + (configuration == null ? "尚未初始化" : configuration.getDamageRankingBossCount() + "/" + configuration.getTrackedBossCount())
                + ChatColor.GRAY + " | AttributePlus: " + ChatColor.WHITE + (plugin.isBossBarAttributePlusHooked() ? "已挂钩" : "未挂钩")
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "排行最大名次: " + ChatColor.WHITE + (configuration == null ? "尚未初始化" : configuration.getMaxDamageRankingEntries()));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Boss 预览: " + ChatColor.WHITE + (configuration == null ? "尚未初始化" : configuration.summarizeTrackedBossIds(8)));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "最多显示血条: " + ChatColor.WHITE + (configuration == null ? "尚未初始化" : configuration.maxVisibleBars()));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "排序模式: " + ChatColor.WHITE + (configuration == null ? "尚未初始化" : configuration.sortMode().configKey()));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.ENTITY_TRACKER));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "首个 Boss 重载: " + ChatColor.WHITE + plugin.getFirstBossReloadStatus());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "ArcartX 桥接: " + ChatColor.WHITE + (bridgeReady ? "可用" : "离线"));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "PAPI 占位符: " + ChatColor.WHITE + (plugin.isPlaceholderApiAvailable() ? (plugin.isPlaceholderExpansionRegistered() ? "已注册" : "注册失败") : "未安装"));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "跟踪器状态: " + ChatColor.WHITE + trackerStatus);
    }

    private void sendKillEffectStatus(CommandSender sender) {
        var configuration = plugin.getKillEffectConfiguration();
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置文件: " + ChatColor.WHITE + plugin.getKillEffectConfigFile().getName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "CombatEffect 配置: " + ChatColor.WHITE + (configuration == null ? "0/0" : configuration.enabledPacketCount() + "/" + configuration.packetDefinitions().size()));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "实体战斗 / 玩家 / 非玩家: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.entityCombatEnabled()
                        + " / " + configuration.includePlayers()
                        + " / " + configuration.includeNonPlayerLiving())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "黑名单: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : "mm=" + configuration.blacklistedMythicMobIds().size()
                        + " entity=" + configuration.blacklistedEntityTypes().size())
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "ArcartX 模式: " + ChatColor.WHITE + plugin.describePacketBridgeMode());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.COMBAT_EFFECT));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Debug: " + ChatColor.WHITE + (configuration != null && configuration.debug()));
    }

    private void sendEventPacketStatus(CommandSender sender) {
        var configuration = plugin.getEventPacketConfiguration();
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置文件: " + ChatColor.WHITE + plugin.getEventPacketConfigFile().getName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "模块状态: " + ChatColor.WHITE + (plugin.isEventPacketModuleReady() ? "运行中" : "未运行"));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "规则配置: " + ChatColor.WHITE
                + (configuration == null ? "0/0" : configuration.enabledRuleCount() + "/" + configuration.rules().size())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "PAPI 监听: " + ChatColor.WHITE
                + (configuration == null ? "0 (未初始化)" : configuration.papiPacketCount() + (plugin.isEventPacketWatcherRunning() ? " (运行中)" : " (未运行)"))
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "轮询间隔: " + ChatColor.WHITE + (configuration == null ? "0" : configuration.refreshIntervalTicks()) + " ticks");
        sender.sendMessage(PREFIX + ChatColor.GRAY + "ArcartX 模式: " + ChatColor.WHITE + plugin.describePacketBridgeMode());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.EVENT_PACKET));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Debug: " + ChatColor.WHITE + (configuration != null && configuration.debug()));
    }

    private void sendTabStatus(CommandSender sender) {
        var configuration = plugin.getTabConfiguration();
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置文件: " + ChatColor.WHITE + plugin.getTabConfigFile().getName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "模块状态: " + ChatColor.WHITE + (plugin.isTabModuleReady() ? "运行中" : "未运行"));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "轮询间隔: " + ChatColor.WHITE + (configuration == null ? "0" : configuration.refreshIntervalTicks()) + " ticks");
        sender.sendMessage(PREFIX + ChatColor.GRAY + "定义数量: " + ChatColor.WHITE + (configuration == null ? "0" : configuration.definitions().size()));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.TAB));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Debug: " + ChatColor.WHITE + (configuration != null && configuration.debug()));
    }

    private void sendTitleStatus(CommandSender sender) {
        var configuration = plugin.getTitleConfiguration();
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置文件: " + ChatColor.WHITE + plugin.getTitleConfigFile().getName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "模块状态: " + ChatColor.WHITE + (plugin.isTitleModuleReady() ? "运行中" : "未运行"));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "称号数量: " + ChatColor.WHITE + (configuration == null ? "0" : configuration.enabledTitleCount()));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "缓存玩家: " + ChatColor.WHITE + (plugin.getTitleService() == null ? "0" : plugin.getTitleService().cachedPlayerCount()));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "存储模式: " + ChatColor.WHITE + (configuration == null ? "未初始化" : configuration.storage().dialect().configKey()));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "UI 标识: " + ChatColor.WHITE + (plugin.getTitleService() == null ? "未初始化" : plugin.getTitleService().runtimeUiId()));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "AP兼容 / AP挂钩 / MythicLib兼容 / MythicLib挂钩 / Crane兼容 / Crane挂钩: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.attributePlus().enabled()
                        + " / "
                        + (plugin.getTitleService() != null && plugin.getTitleService().attributePlusHooked())
                        + " / "
                        + configuration.mythicLib().enabled()
                        + " / "
                        + (plugin.getTitleService() != null && plugin.getTitleService().mythicLibHooked())
                        + " / "
                        + configuration.craneAttribute().enabled()
                        + " / "
                        + (plugin.getTitleService() != null && plugin.getTitleService().craneAttributeHooked()))
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.TITLE));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "PAPI 占位符: " + ChatColor.WHITE + (plugin.isPlaceholderApiAvailable() ? (plugin.isPlaceholderExpansionRegistered() ? "已注册" : "注册失败") : "未安装"));
    }

    private void sendConversationStatus(CommandSender sender) {
        var configuration = plugin.getConversationConfiguration();
        var service = plugin.getConversationService();
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置文件: " + ChatColor.WHITE + plugin.getConversationConfigFile().getName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "模块状态: " + ChatColor.WHITE + (plugin.isConversationModuleReady() ? "运行中" : "未运行"));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Chemdah / Adyeshach: " + ChatColor.WHITE + Bukkit.getPluginManager().isPluginEnabled("Chemdah") + " / " + Bukkit.getPluginManager().isPluginEnabled("Adyeshach"));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "主题 / DialogUI / SelectorUI / 回包: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.themeName()
                        + " / "
                        + plugin.getConversationRuntimeUiId()
                        + " / "
                        + plugin.getConversationSelectorRuntimeUiId()
                        + " / "
                        + configuration.clientPacketId())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "键位增强 / Selector HUD: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.interactionEnabled()
                        ? ((plugin.isConversationInteractionReady() ? "已就绪" : "已启用但降级")
                            + " / "
                            + plugin.getConversationSelectorRuntimeUiId())
                        : "已关闭")
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "KeyBind / KeyPress / NPC桥: " + ChatColor.WHITE
                + (service == null
                    ? "未运行"
                    : (service.keybindReady() ? "已就绪" : "未就绪")
                        + " / "
                        + (service.keyPressEventReady() ? "已就绪" : "未就绪")
                        + " / "
                        + (service.npcBridgeReady() ? "已就绪" : "未就绪"))
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "候选玩家 / 活动会话 / 对话HUD / SelectorHUD: " + ChatColor.WHITE
                + plugin.getConversationCandidatePlayerCount()
                + " / "
                + plugin.getConversationActiveSessionCount()
                + " / "
                + plugin.getConversationOpenedPlayerCount()
                + " / "
                + plugin.getConversationSelectorOpenedPlayerCount()
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Generation / 最近摘要: " + ChatColor.WHITE
                + (service == null ? "未运行 / 无" : service.generationToken() + " / " + service.latestDebugSummary())
        );
        if (service != null && !service.interactionReady() && configuration != null && configuration.interactionEnabled()) {
            sender.sendMessage(PREFIX + ChatColor.GRAY + "降级原因: " + ChatColor.WHITE + service.interactionDisabledReason());
        }
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.CONVERSATION));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Debug: " + ChatColor.WHITE + (configuration != null && configuration.debug()));
    }

    private void sendSubtitleStatus(CommandSender sender) {
        var configuration = plugin.getSubtitleConfiguration();
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置文件: " + ChatColor.WHITE + plugin.getSubtitleConfigFile().getName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "模块状态: " + ChatColor.WHITE + (plugin.isSubtitleModuleReady() ? "运行中" : "未运行"));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "字幕组数量: " + ChatColor.WHITE + plugin.getSubtitleGroupIds().size());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "活动会话: " + ChatColor.WHITE + plugin.getSubtitleActiveSessionCount());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "UI 标识: " + ChatColor.WHITE + (configuration == null ? "未初始化" : configuration.uiId()));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.ANNOUNCER));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Debug: " + ChatColor.WHITE + (configuration != null && configuration.debug()));
    }

    private void sendAnnouncerStatus(CommandSender sender) {
        var configuration = plugin.getAnnouncerConfiguration();
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置文件: " + ChatColor.WHITE + plugin.getAnnouncerConfigFile().getName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "模块状态: " + ChatColor.WHITE + (plugin.isAnnouncerModuleReady() ? "运行中" : "未运行"));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "公告数量: " + ChatColor.WHITE
                + (configuration == null ? "0/0" : plugin.getAnnouncerActiveEntryCount() + "/" + configuration.entries().size())
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "已初始化玩家: " + ChatColor.WHITE + plugin.getAnnouncerInitializedPlayerCount());
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "轮询/间隔: " + ChatColor.WHITE
                + (configuration == null
                    ? "0 / 0 / 0"
                    : configuration.checkIntervalTicks() + " ticks / "
                        + configuration.cooldownMs() + "ms / "
                        + configuration.betweenEntryIntervalMs() + "ms")
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "自动播放: " + ChatColor.WHITE + (configuration != null && configuration.autoPlay()));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.ANNOUNCER));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Debug: " + ChatColor.WHITE + (configuration != null && configuration.debug()));
    }

    private void sendAttackTargetStatus(CommandSender sender) {
        var configuration = plugin.getAttackTargetConfiguration();
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置文件: " + ChatColor.WHITE + plugin.getAttackTargetConfigFile().getName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "模块状态: " + ChatColor.WHITE + (plugin.isAttackTargetModuleReady() ? "运行中" : "未运行"));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "UI / 刷新 / 超时 / 距离: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : plugin.getAttackTargetRuntimeUiId()
                        + " / " + configuration.refreshIntervalTicks()
                        + " ticks / "
                        + configuration.targetTimeoutMs()
                        + "ms / "
                        + configuration.maxViewDistance())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "活动锁定 / 打开HUD: " + ChatColor.WHITE
                + plugin.getAttackTargetActiveTargetCount()
                + " / "
                + plugin.getAttackTargetActiveViewerCount()
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "黑名单 / EntityTracker联动: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : "mm=" + configuration.blacklistedMythicMobIds().size()
                        + " entity=" + configuration.blacklistedEntityTypes().size()
                        + " / " + configuration.ignoreBossBarConfiguredBosses())
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.ENTITY_TRACKER));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Debug: " + ChatColor.WHITE + (configuration != null && configuration.debug()));
    }

    private void sendPickupStatus(CommandSender sender) {
        var configuration = plugin.getPickupConfiguration();
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置文件: " + ChatColor.WHITE + plugin.getPickupConfigFile().getName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "模块状态: " + ChatColor.WHITE + (plugin.isPickupModuleReady() ? "运行中" : "未运行"));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "UI / 可见数 / 生存期: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.uiId() + " / " + configuration.maxVisible() + " / " + configuration.entryTtlMs() + "ms")
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.PICKUP));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Debug: " + ChatColor.WHITE + (configuration != null && configuration.debug()));
    }

    private void sendPropStatus(CommandSender sender) {
        var configuration = plugin.getPropConfiguration();
        var service = plugin.getPropService();
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置文件: " + ChatColor.WHITE + plugin.getPropConfigFile().getName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "模块状态: " + ChatColor.WHITE + (plugin.isPropModuleReady() ? "运行中" : "未运行"));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "道具 / 按键 / 分类: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : (service == null ? "0 / 0 / 未初始化" : service.propCount() + " / " + service.registeredKeyCount() + " / " + service.keyCategory()))
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "AP挂钩 / MythicLib兼容 / MythicLib挂钩: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : (service == null
                        ? "false / " + configuration.mythicLib().enabled() + " / false"
                        : service.attributePlusHooked() + " / " + service.mythicLibEnabled() + " / " + service.mythicLibHooked()))
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "prop-id-writer: " + ChatColor.WHITE + plugin.describePropWriterBackend());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.PROP));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Debug: " + ChatColor.WHITE + (configuration != null && configuration.debug()));
    }

    private void sendDigisDisplayStatus(CommandSender sender) {
        var configuration = plugin.getDigisDisplayConfiguration();
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置文件: " + ChatColor.WHITE + plugin.getDigisDisplayConfigFile().getName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "模块状态: " + ChatColor.WHITE + (plugin.isDigisDisplayModuleReady() ? "运行中" : "未运行"));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "普通伤害 / 玩家受伤: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.damageEnabled() + " / " + configuration.playerDamageEnabled())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "伤害来源模式 / 实际来源 / fallback: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.damageSourceMode()
                        + " / "
                        + plugin.getDigisDisplayActiveDamageSource()
                        + " / "
                        + configuration.damageSourceFallback())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "原版治疗 / Mythic治疗: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.healEnabled() + " / " + configuration.mythicHealEnabled())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "配置ID: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.damageConfigId()
                        + " / "
                        + configuration.playerDamageConfigId()
                        + " / "
                        + configuration.mythicLibDamageConfigId()
                        + " / "
                        + configuration.mythicLibPlayerDamageConfigId()
                        + " / "
                        + configuration.craneAttributeDamageConfigId()
                        + " / "
                        + configuration.craneAttributePlayerDamageConfigId()
                        + " / "
                        + configuration.healConfigId()
                        + " / "
                        + configuration.mythicHealConfigId())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "最小值 / exact-mode: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.damageMinAmount()
                        + ", "
                        + configuration.playerDamageMinAmount()
                        + ", "
                        + configuration.mythicLibDamageMinAmount()
                        + ", "
                        + configuration.mythicLibPlayerDamageMinAmount()
                        + ", "
                        + configuration.craneAttributeDamageMinAmount()
                        + ", "
                        + configuration.craneAttributePlayerDamageMinAmount()
                        + ", "
                        + configuration.healMinAmount()
                        + ", "
                        + configuration.mythicHealMinAmount()
                        + " / "
                        + configuration.mythicHealExactMode())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "MythicLib兼容 / MythicLib挂钩 / Crane兼容 / Crane挂钩 / AP兼容 / AP挂钩 / Mythic治疗挂钩: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.mythicLibDamageEnabled()
                        + " / "
                        + plugin.isDigisDisplayMythicLibDamageHooked()
                        + " / "
                        + configuration.craneAttributeDamageEnabled()
                        + " / "
                        + plugin.isDigisDisplayCraneAttributeHooked()
                        + " / "
                        + configuration.damageAttributePlusCompatible()
                        + " / "
                        + plugin.isDigisDisplayAttributePlusHooked()
                        + " / "
                        + plugin.isDigisDisplayMythicHealHooked())
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.COMBAT_EFFECT));
    }

    private void sendRgbStatus(CommandSender sender) {
        var configuration = plugin.getRgbConfiguration();
        var service = plugin.getRgbService();
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置文件: " + ChatColor.WHITE + plugin.getRgbConfigFile().getName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "模块状态: " + ChatColor.WHITE + (plugin.isRgbModuleReady() ? "运行中" : "未运行"));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "条目数量: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : (service == null ? 0 : service.activeEntryCount()) + "/" + configuration.entries().size())
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "PAPI 占位符: " + ChatColor.WHITE + "%arcartrgb_<id>%");
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "PAPI 状态: " + ChatColor.WHITE
                + (plugin.isPlaceholderApiAvailable()
                    ? (plugin.isPlaceholderExpansionRegistered() ? "已注册" : "注册失败")
                    : "未安装")
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.RGB));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Debug: " + ChatColor.WHITE + (configuration != null && configuration.debug()));
    }

    private void sendOnlineRewardsStatus(CommandSender sender) {
        var configuration = plugin.getOnlineRewardsConfiguration();
        var service = plugin.getOnlineRewardsService();
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置文件: " + ChatColor.WHITE + plugin.getOnlineRewardsConfigFile().getName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "模块状态: " + ChatColor.WHITE + (plugin.isOnlineRewardsModuleReady() ? "运行中" : "未运行"));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "奖励阶段 / 缓存玩家: " + ChatColor.WHITE
                + (configuration == null ? "未初始化" : configuration.rewards().size() + " / " + plugin.getOnlineRewardsCachedPlayerCount())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "存储 / 同步延迟: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.storage().dialect().configKey() + " / " + configuration.clientSyncDelayTicks() + " ticks")
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "变量名: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.progressVariableName() + " / " + configuration.titleVariableName())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "签到提醒 / 连续奖励 / 累计奖励 / 日期奖励: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.signIn().reminderOnJoin()
                        + " / " + configuration.signIn().streakRewards().size()
                        + " / " + configuration.signIn().totalRewards().size()
                        + " / " + configuration.signIn().dayOfMonthRewards().size())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "排行榜缓存(日/周/月/总): " + ChatColor.WHITE
                + (service == null
                    ? "0 / 0 / 0 / 0"
                    : service.leaderboardSnapshotSize(OnlineRewardsLeaderboardScope.DAILY)
                        + " / "
                        + service.leaderboardSnapshotSize(OnlineRewardsLeaderboardScope.WEEKLY)
                        + " / "
                        + service.leaderboardSnapshotSize(OnlineRewardsLeaderboardScope.MONTHLY)
                        + " / "
                        + service.leaderboardSnapshotSize(OnlineRewardsLeaderboardScope.TOTAL))
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "完成文案: " + ChatColor.WHITE
                + (configuration == null ? "未初始化" : configuration.doneMessage())
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.ONLINE_REWARDS));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "PAPI 占位符: " + ChatColor.WHITE + (plugin.isPlaceholderApiAvailable() ? (plugin.isPlaceholderExpansionRegistered() ? "已注册" : "注册失败") : "未安装"));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Debug: " + ChatColor.WHITE + (configuration != null && configuration.debug()));
    }

    // sendPacketCommandStatus removed — merged into EventPacket

    private void sendLoginViewStatus(CommandSender sender) {
        var configuration = plugin.getLoginViewConfiguration();
        var service = plugin.getLoginViewService();
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置文件: " + ChatColor.WHITE + (plugin.getLoginViewConfigFile() == null ? "未创建" : plugin.getLoginViewConfigFile().getName()));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "模块状态: " + ChatColor.WHITE + (plugin.isLoginViewModuleReady() ? "运行中" : "未运行"));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "模式 / AuthMe / 账户数: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.authMode().configKey()
                        + " / "
                        + (service != null && service.authMeAvailable())
                        + " / "
                        + (service == null ? 0 : service.accountCount()))
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "UI / packet-id: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : plugin.getLoginViewRuntimeUiId() + " / " + configuration.ui().packetId())
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.LOGIN_VIEW));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Debug: " + ChatColor.WHITE + (configuration != null && configuration.debug()));
    }

    private void sendAuthMeRemovalInstructions(CommandSender sender, String label) {
        sender.sendMessage(PREFIX + ChatColor.GOLD + "AuthMe 迁移后的安全切换步骤:");
        sender.sendMessage(PREFIX + ChatColor.GRAY + "1. 停服并备份 plugins/AuthMe 与 LoginView 数据库。");
        sender.sendMessage(PREFIX + ChatColor.GRAY + "2. 确认 ArcartXLoginView.yml 的 auth.mode 改为 standalone。");
        sender.sendMessage(PREFIX + ChatColor.GRAY + "3. 将 AuthMeReloaded jar 移出 plugins 目录，不要在运行中直接删除。");
        sender.sendMessage(PREFIX + ChatColor.GRAY + "4. 启服后执行 /" + label + " loginview reload，并让已迁移玩家登录一次完成 hash 重写。");
    }

    private void sendMailStatus(CommandSender sender) {
        var configuration = plugin.getMailConfiguration();
        var service = plugin.getMailService();
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置文件: " + ChatColor.WHITE + plugin.getMailConfigFile().getName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "模块状态: " + ChatColor.WHITE + (plugin.isMailModuleReady() ? "运行中" : "未运行"));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "存储 / Redis / Vault: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.storage().dialect().configKey()
                        + " / "
                        + (service != null && service.redisActive())
                        + " / "
                        + plugin.isVaultEconomyAvailable())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "预设 / UI / 会话: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : (service == null ? 0 : service.presetCount())
                        + " / "
                        + (service == null ? configuration.ui().inboxUiId() : service.inboxUiId())
                        + " / "
                        + (service == null ? 0 : service.composeSessionCount()))
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.MAIL));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "PAPI 占位符: " + ChatColor.WHITE + (plugin.isPlaceholderApiAvailable() ? (plugin.isPlaceholderExpansionRegistered() ? "已注册" : "注册失败") : "未安装"));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Debug: " + ChatColor.WHITE + (configuration != null && configuration.debug()));
    }

    private void sendQuestGpsStatus(CommandSender sender) {
        var configuration = plugin.getQuestGpsConfiguration();
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置文件: " + ChatColor.WHITE + plugin.getQuestGpsConfigFile().getName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "模块状态: " + ChatColor.WHITE + (plugin.isQuestGpsModuleReady() ? "运行中" : "未运行"));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Packet / Menu / HUD / 导航: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.client().packetId()
                        + " / "
                        + plugin.getQuestGpsRuntimeUiId()
                        + " / "
                        + plugin.getQuestGpsHudRuntimeUiId()
                        + " / "
                        + configuration.navigation().enabled())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "配置任务 / 追踪玩家: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.configuredQuestCount()
                        + " / "
                        + (plugin.getQuestGpsService() == null ? 0 : plugin.getQuestGpsService().trackingPlayerCount()))
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "路标样式 / 主线门禁: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.navigation().waypointStyleId()
                        + " / "
                        + configuration.gate().requiredMainlineQuestIds().size())
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.QUEST_GPS));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Debug: " + ChatColor.WHITE + (configuration != null && configuration.debug()));
    }

    private void sendMapStatus(CommandSender sender) {
        var configuration = plugin.getMapConfiguration();
        var service = plugin.getMapService();
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置文件: " + ChatColor.WHITE + plugin.getMapConfigFile().getName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "模块状态: " + ChatColor.WHITE + (plugin.isMapModuleReady() ? "运行中" : "未运行"));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "Packet / Menu / HUD: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.client().packetId()
                        + " / "
                        + plugin.getMapMenuRuntimeUiId()
                        + " / "
                        + plugin.getMapHudRuntimeUiId())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "世界 / 锚点 / 默认解锁: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.worlds().size()
                        + " / "
                        + configuration.anchors().size()
                        + " / "
                        + configuration.defaultUnlocks().size())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "路径点 / 导航 / 追踪玩家: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.waypoints().enabled()
                        + " / "
                        + (service == null ? false : service.waypointRuntimeReady())
                        + " / "
                        + (service == null ? 0 : service.trackingPlayerCount()))
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.MAP));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Debug: " + ChatColor.WHITE + (configuration != null && configuration.debug()));
    }

    private void sendChatStatus(CommandSender sender) {
        var configuration = plugin.getChatConfiguration();
        var service = plugin.getChatService();
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置文件: " + ChatColor.WHITE + plugin.getChatConfigFile().getName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "模块状态: " + ChatColor.WHITE + (plugin.isChatModuleReady() ? "运行中" : "未运行"));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "默认频道 / 频道数: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.defaultChannelId()
                        + " / "
                        + (service == null ? configuration.channels().size() : service.channelCount()))
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "缓存状态 / 禁言 / Profile: " + ChatColor.WHITE
                + (service == null
                    ? "未初始化"
                    : service.cachedStateCount()
                        + " / "
                        + service.cachedMuteCount()
                        + " / "
                        + service.cachedProfileCount())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "存储 / Redis / Proxy: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.storage().dialect().configKey()
                        + " / "
                        + configuration.redis().enabled()
                        + " / "
                        + configuration.proxy().enabled())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "其他聊天插件强制接管: " + ChatColor.WHITE
                + (configuration != null && configuration.forceChatTakeover())
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.CHAT));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Debug: " + ChatColor.WHITE + (configuration != null && configuration.debug()));
    }

    private void sendWarehouseStatus(CommandSender sender) {
        var configuration = plugin.getWarehouseConfiguration();
        var service = plugin.getWarehouseService();
        sender.sendMessage(PREFIX + ChatColor.GRAY + "配置文件: " + ChatColor.WHITE + plugin.getWarehouseConfigFile().getName());
        sender.sendMessage(PREFIX + ChatColor.GRAY + "模块状态: " + ChatColor.WHITE + (plugin.isWarehouseModuleReady() ? "运行中" : "未运行"));
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "仓库 / 分类 / 货币 / 定期产品: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.warehouses().size() + " / " + configuration.categories().size() + " / " + configuration.currencies().size() + " / " + configuration.depositProducts().size())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "存储 / 缓存 / 脏数据: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.storage().dialect().configKey()
                        + " / "
                        + (service == null ? 0 : service.cachedPlayerCount())
                        + " / "
                        + (service == null ? 0 : service.dirtyPlayerCount()))
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "UI 标识: " + ChatColor.WHITE
                + (configuration == null
                    ? "未初始化"
                    : configuration.ui().uiId() + " / " + configuration.ui().manageUiId())
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "MM / NI / 货币桥接: " + ChatColor.WHITE
                + (service == null
                    ? "未初始化"
                    : service.mythicBridgeAvailable() + " / " + service.neigeBridgeAvailable() + " / " + service.currencyIds().size())
        );
        sender.sendMessage(PREFIX + ChatColor.GRAY + "启动授权: " + ChatColor.WHITE + plugin.describeModulePasswordValidation(ModuleKey.WAREHOUSE));
        sender.sendMessage(PREFIX + ChatColor.GRAY + "Debug: " + ChatColor.WHITE + (configuration != null && configuration.debug()));
    }

    private void sendModuleReloadFeedback(CommandSender sender, ModuleKey moduleKey, boolean success) {
        if (success) {
            sender.sendMessage(PREFIX + ChatColor.GREEN + moduleKey.displayName() + " 模块重载完成。");
            return;
        }
        if (!plugin.isModuleEnabledInConfig(moduleKey)) {
            sender.sendMessage(PREFIX + ChatColor.YELLOW + moduleKey.displayName() + " 模块已在 config.yml 中关闭。");
            return;
        }

        ValidationResult validationResult = plugin.getModulePasswordValidation(moduleKey);
        if (validationResult == ValidationResult.MISSING) {
            sender.sendMessage(PREFIX + ChatColor.RED + moduleKey.displayName() + " 模块密码未填写，请先修正 config.yml 后再重载。");
            return;
        }
        if (validationResult == ValidationResult.INVALID) {
            sender.sendMessage(PREFIX + ChatColor.RED + moduleKey.displayName() + " 模块密码错误，请先修正 config.yml 后再重载。");
            return;
        }
        sender.sendMessage(PREFIX + ChatColor.RED + moduleKey.displayName() + " 模块重载失败，请检查控制台。");
    }

    private List<String> collectPasswordFailures(ModuleKey... moduleKeys) {
        List<String> failures = new ArrayList<>();
        for (ModuleKey moduleKey : moduleKeys) {
            if (!plugin.isModuleEnabledInConfig(moduleKey)) {
                continue;
            }
            ValidationResult validationResult = plugin.getModulePasswordValidation(moduleKey);
            if (validationResult == ValidationResult.VALID) {
                continue;
            }
            failures.add(moduleKey.displayName() + "=" + validationResult.displayText());
        }
        return failures;
    }

    private OfflinePlayer resolveOfflinePlayer(String playerName) {
        Player online = Bukkit.getPlayerExact(playerName);
        if (online != null) {
            return online;
        }
        for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            if (offline != null && offline.getName() != null && offline.getName().equalsIgnoreCase(playerName)) {
                return offline;
            }
        }
        return null;
    }

    private List<String> onlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }

    private List<String> configuredEventPacketSignals() {
        List<String> signals = new ArrayList<>();
        var configuration = plugin.getEventPacketConfiguration();
        if (configuration != null) {
            for (var rule : configuration.rules()) {
                if ("command-signal".equals(rule.trigger().configValue()) && !rule.signal().isBlank() && !signals.contains(rule.signal())) {
                    signals.add(rule.signal());
                }
            }
        }
        if (signals.isEmpty()) {
            signals.add("quest_signal");
        }
        return signals;
    }

    private boolean hasPermission(CommandSender sender, String[] args) {
        if (sender.hasPermission("arcartxsuite.admin")) {
            return true;
        }
        return args.length > 0
            && "mail".equalsIgnoreCase(args[0])
            && sender.hasPermission("arcartxsuite.mail.admin");
    }

    private List<String> filter(List<String> candidates, String input) {
        List<String> result = new ArrayList<>();
        String normalized = input.toLowerCase();
        for (String candidate : candidates) {
            if (candidate.toLowerCase().startsWith(normalized)) {
                result.add(candidate);
            }
        }
        return result;
    }

    private void sendSettlementLikePage(
        CommandSender sender,
        String title,
        String bossDisplayName,
        String mobId,
        String entityUuid,
        int participantCount,
        int trackedPlayerCount,
        double totalDamage,
        List<BossDamageSettlementEntry> entries,
        int page,
        boolean includeRewardStatus
    ) {
        int pageSize = 10;
        int totalPages = Math.max(1, (entries.size() + pageSize - 1) / pageSize);
        int safePage = Math.min(Math.max(1, page), totalPages);
        int fromIndex = (safePage - 1) * pageSize;
        int toIndex = Math.min(entries.size(), fromIndex + pageSize);
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + title + ": " + ChatColor.WHITE + bossDisplayName
                + ChatColor.GRAY + " | mobId=" + ChatColor.WHITE + mobId
                + ChatColor.GRAY + " | entity=" + ChatColor.WHITE + entityUuid
        );
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "排行玩家/跟踪玩家: " + ChatColor.WHITE + participantCount + "/" + trackedPlayerCount
                + ChatColor.GRAY + " | 总伤害: " + ChatColor.WHITE + formatDecimal(totalDamage)
                + ChatColor.GRAY + " | 页数: " + ChatColor.WHITE + safePage + "/" + totalPages
        );
        if (entries.isEmpty()) {
            sender.sendMessage(PREFIX + ChatColor.YELLOW + "暂无排行数据。");
            return;
        }
        for (BossDamageSettlementEntry entry : entries.subList(fromIndex, toIndex)) {
            String rewardText = includeRewardStatus
                ? (entry.rewarded() ? ChatColor.GREEN + "成功" : ChatColor.RED + (entry.rewardFailure().isBlank() ? "失败" : entry.rewardFailure()))
                : "";
            sender.sendMessage(
                PREFIX + ChatColor.WHITE + entry.rankText()
                    + ChatColor.GRAY + " | " + ChatColor.WHITE + entry.playerName()
                    + ChatColor.GRAY + " | 伤害 " + ChatColor.WHITE + formatDecimal(entry.damage())
                    + ChatColor.GRAY + " (" + ChatColor.WHITE + formatDecimal(entry.damagePercent()) + "%" + ChatColor.GRAY + ")"
                    + ChatColor.GRAY + " | 承伤 " + ChatColor.WHITE + formatDecimal(entry.takenDamage())
                    + (includeRewardStatus ? ChatColor.GRAY + " | 奖励 " + rewardText : "")
            );
        }
    }

    private static String formatDecimal(double value) {
        return DECIMAL_FORMAT.get().format(value);
    }

    private static String formatDateTime(long epochMillis) {
        return DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()));
    }

    private static String formatCdkExpiry(Instant expiresAt, Instant now) {
        if (expiresAt == null) {
            return "永久";
        }
        String formatted = DATE_TIME_FORMATTER.format(expiresAt.atZone(ZoneId.systemDefault()));
        return expiresAt.isAfter(now) ? formatted : formatted + " (已过期)";
    }

    private static String formatCdkState(MailCdkDefinition definition, Instant now) {
        if (!definition.enabled()) {
            return "禁用";
        }
        if (definition.expired(now)) {
            return "过期";
        }
        if (definition.claimedCount() >= definition.maxClaims()) {
            return "已领完";
        }
        return "可用";
    }

    private int parseInt(String rawValue, int defaultValue) {
        try {
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private Optional<Integer> parseOnlineRewardMinutes(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }
        String normalized = rawValue.trim().toLowerCase();
        if (normalized.matches("\\d+")) {
            try {
                return Optional.of(Math.max(0, Integer.parseInt(normalized)));
            } catch (NumberFormatException exception) {
                return Optional.empty();
            }
        }
        Optional<TitleDurationParser.TitleDurationSpec> durationSpec = TitleDurationParser.parse(normalized);
        if (durationSpec.isEmpty() || durationSpec.get().permanent()) {
            return Optional.empty();
        }
        Duration duration = durationSpec.get().duration();
        long minutes = duration.toMinutes();
        if (minutes < 0L || minutes > Integer.MAX_VALUE) {
            return Optional.empty();
        }
        return Optional.of((int) minutes);
    }

    private Instant parseTtl(String rawValue) {
        String normalized = rawValue == null ? "" : rawValue.trim().toLowerCase();
        if (normalized.isBlank()) {
            return Instant.MIN;
        }
        if ("permanent".equals(normalized) || "forever".equals(normalized) || "never".equals(normalized) || "0".equals(normalized)) {
            return null;
        }
        long multiplier = switch (normalized.charAt(normalized.length() - 1)) {
            case 's' -> 1L;
            case 'm' -> 60L;
            case 'h' -> 3600L;
            case 'd' -> 86400L;
            case 'w' -> 604800L;
            default -> -1L;
        };
        if (multiplier < 0L) {
            return Instant.MIN;
        }
        int amount = parseInt(normalized.substring(0, normalized.length() - 1), -1);
        if (amount <= 0) {
            return Instant.MIN;
        }
        return Instant.now().plusSeconds(amount * multiplier);
    }

    private String joinTail(String[] args, int fromIndex) {
        StringBuilder builder = new StringBuilder();
        for (int index = fromIndex; index < args.length; index++) {
            if (index > fromIndex) {
                builder.append(' ');
            }
            builder.append(args[index]);
        }
        return builder.toString();
    }

    private void sendUsage(CommandSender sender, String label) {
        sendHelpOverview(sender, label);
    }

    private void sendHelpOverview(CommandSender sender, String label) {
        sender.sendMessage(PREFIX + ChatColor.GOLD + "AXS 管理命令帮助");
        sender.sendMessage(PREFIX + ChatColor.GRAY + "/" + label + " status" + ChatColor.WHITE + " - 查看全部模块运行状态。");
        sender.sendMessage(PREFIX + ChatColor.GRAY + "/" + label + " reload all" + ChatColor.WHITE + " - 按模块顺序重载全部配置、UI 和服务。");
        sender.sendMessage(PREFIX + ChatColor.GRAY + "/" + label + " help <module>" + ChatColor.WHITE + " - 查看指定模块的命令用法和作用。");
        sender.sendMessage(PREFIX + ChatColor.YELLOW + "模块列表:");
        sendOverviewModule(sender, label, "entitytracker", "EntityTracker", "Boss 追踪、伤害排行、攻击目标 HUD 和结算记录。");
        sendOverviewModule(sender, label, "combateffect", "CombatEffect", "击杀特效、伤害飘字，战斗视觉反馈一站式解决。");
        sendOverviewModule(sender, label, "eventpacket", "EventPacket", "通用触发器和动作链，可手动 fire 调试信号。");
        sendOverviewModule(sender, label, "tab", "Tab", "玩家 Tab 列表显示、分组和占位符内容。");
        sendOverviewModule(sender, label, "title", "Title", "称号定义、发放、回收和玩家称号界面。");
        sendOverviewModule(sender, label, "conversation", "Conversation", "对话 UI 和 NPC/任务对话桥接。");
        sendOverviewModule(sender, label, "announcer", "Announcer", "HUD 公告 + 打字机字幕动画，服务器信息播报。");
        sendOverviewModule(sender, label, "pickup", "Pickup", "拾取提示 HUD。");
        sendOverviewModule(sender, label, "prop", "Prop", "自定义属性/道具脚本配置和玩家变量写入。");
        sendOverviewModule(sender, label, "rgb", "RGB", "动态渐变文本和颜色渲染配置。");
        sendOverviewModule(sender, label, "onlinerewards", "OnlineRewards", "在线奖励、签到、排行榜和客户端菜单。");
        sendOverviewModule(sender, label, "loginview", "LoginView", "登录视图、AuthMe 兼容和独立密码账户库。");
        sendOverviewModule(sender, label, "warehouse", "Warehouse", "AXUI 仓库、共享仓库、二级密码和银行管理。");
        sendOverviewModule(sender, label, "mail", "Mail", "玩家邮箱、写信附件、预设邮件、CDK 和管理界面。");
        sendOverviewModule(sender, label, "chat", "Chat", "频道聊天、私聊、跨服转发、禁言和监听。");
        sendOverviewModule(sender, label, "questgps", "QuestGPS", "任务导航菜单、追踪状态和 HUD。");
        sendOverviewModule(sender, label, "map", "Map", "大地图、小地图 HUD、锚点和世界地图配置。");
    }

    private void sendOverviewModule(CommandSender sender, String label, String module, String displayName, String description) {
        sender.sendMessage(
            PREFIX + ChatColor.AQUA + displayName
                + ChatColor.GRAY + " /" + label + " " + module + " help"
                + ChatColor.WHITE + " - " + description
        );
    }

    private void sendModuleHelp(CommandSender sender, String label, String module) {
        String normalized = module.toLowerCase();
        switch (normalized) {
            case "entitytracker" -> {
                sendHelpHeader(sender, "EntityTracker", "Boss 追踪、伤害排行、结算奖励 + 攻击目标 HUD。");
                sendCommandHelp(sender, label, "entitytracker status", "查看 EntityTracker 模块状态、会话数量和奖励状态。");
                sendCommandHelp(sender, label, "entitytracker reload", "重载 EntityTracker 配置、UI 和追踪服务。");
                sendCommandHelp(sender, label, "entitytracker sessions [mobId]", "列出正在追踪的 Boss 会话，可按 mobId 过滤。");
                sendCommandHelp(sender, label, "entitytracker rank <entityUuid> [page]", "查看指定 Boss 实体的伤害排行。");
                sendCommandHelp(sender, label, "entitytracker settlements [page]", "查看历史结算记录。");
                sendCommandHelp(sender, label, "entitytracker settlement <settlementId> [page]", "查看指定结算详情。");
                sendCommandHelp(sender, label, "entitytracker reissue <settlementId> <rank> [player]", "按结算名次补发奖励，可指定玩家。");
            }
            case "combateffect" -> {
                sendHelpHeader(sender, "CombatEffect", "击杀特效 + 伤害飘字，战斗视觉反馈一站式解决。");
                sendCommandHelp(sender, label, "combateffect status", "查看模块是否启用和配置加载状态。");
                sendCommandHelp(sender, label, "combateffect reload", "重载战斗特效配置。");
            }
            case "eventpacket" -> {
                sendHelpHeader(sender, "EventPacket", "通用触发器和动作链，可向 UI、聊天、称号、任务导航等模块发动作。");
                sendCommandHelp(sender, label, "eventpacket status", "查看规则数量和模块状态。");
                sendCommandHelp(sender, label, "eventpacket reload", "重载触发器配置。");
                sendCommandHelp(sender, label, "eventpacket fire <signal> <player> [key=value...]", "手动对玩家触发信号，并附带可选参数。");
            }
            case "tab" -> {
                sendHelpHeader(sender, "Tab", "玩家 Tab 列表显示、排序和占位符内容。");
                sendCommandHelp(sender, label, "tab status", "查看 Tab 模块状态。");
                sendCommandHelp(sender, label, "tab reload", "重载 Tab 配置并刷新在线玩家显示。");
            }
            case "title" -> {
                sendHelpHeader(sender, "Title", "称号定义、发放、回收、装备菜单和 Placeholder 输出。");
                sendCommandHelp(sender, label, "title status", "查看称号模块、数据库和缓存状态。");
                sendCommandHelp(sender, label, "title reload", "重载称号配置、UI 和玩家状态。");
                sendCommandHelp(sender, label, "title give <player> <titleId> <duration>", "向玩家发放称号，duration 示例 permanent、7d、12h。");
                sendCommandHelp(sender, label, "title revoke <player> <titleId>", "回收玩家指定称号。");
                sendCommandHelp(sender, label, "title open <player>", "为在线玩家打开称号界面。");
            }
            case "conversation" -> {
                sendHelpHeader(sender, "Conversation", "对话 UI、对话选择器和任务/NPC 对话桥接。");
                sendCommandHelp(sender, label, "conversation status", "查看 Conversation 模块状态。");
                sendCommandHelp(sender, label, "conversation reload", "重载对话配置和 UI。");
            }
            case "announcer" -> {
                sendHelpHeader(sender, "Announcer", "HUD 公告 + 打字机字幕动画，服务器信息播报一站式解决。");
                sendCommandHelp(sender, label, "announcer status", "查看播报模块状态和公告数量。");
                sendCommandHelp(sender, label, "announcer reload", "重载播报配置和 HUD。");
                sendCommandHelp(sender, label, "announcer subtitle list", "列出已加载的字幕组。");
                sendCommandHelp(sender, label, "announcer subtitle play <player> <group>", "向在线玩家播放指定字幕组。");
                sendCommandHelp(sender, label, "announcer subtitle stop <player>", "停止在线玩家当前字幕。");
            }
            case "pickup" -> {
                sendHelpHeader(sender, "Pickup", "拾取提示 HUD。");
                sendCommandHelp(sender, label, "pickup status", "查看拾取提示模块状态。");
                sendCommandHelp(sender, label, "pickup reload", "重载拾取提示配置和 HUD。");
            }
            case "prop" -> {
                sendHelpHeader(sender, "Prop", "自定义属性/道具脚本配置和玩家变量写入。");
                sendCommandHelp(sender, label, "prop status", "查看 Prop 模块状态和已加载道具。");
                sendCommandHelp(sender, label, "prop reload", "重载 Prop 配置。");
                sendCommandHelp(sender, label, "prop set <propId>", "把指定 propId 写入执行者的当前 Prop 状态。");
            }
            case "rgb" -> {
                sendHelpHeader(sender, "RGB", "动态渐变文本和颜色渲染配置。");
                sendCommandHelp(sender, label, "rgb status", "查看 RGB 模块状态。");
                sendCommandHelp(sender, label, "rgb reload", "重载动态渐变配置。");
            }
            case "onlinerewards" -> {
                sendHelpHeader(sender, "OnlineRewards", "在线奖励、签到、排行榜和客户端菜单。");
                sendCommandHelp(sender, label, "onlinerewards status", "查看在线奖励、签到和排行榜状态。");
                sendCommandHelp(sender, label, "onlinerewards reload", "重载在线奖励配置和 UI。");
                sendCommandHelp(sender, label, "onlinerewards add|remove|set <time> <player>", "修改在线玩家今日/本周/本月/总在线时长，time 示例 30m、2h、1d。");
                sendCommandHelp(sender, label, "onlinerewards card add|remove|set <amount> <player>", "修改在线玩家补签卡数量。");
            }
            case "loginview" -> {
                sendHelpHeader(sender, "LoginView", "登录视图、AuthMe 兼容和独立密码账户库。");
                sendCommandHelp(sender, label, "loginview status", "查看登录模块、模式、UI 和账户库状态。");
                sendCommandHelp(sender, label, "loginview reload", "重载登录视图配置、UI 和账户服务。");
                sendCommandHelp(sender, label, "loginview open <player>", "为在线玩家打开登录视图。");
                sendCommandHelp(sender, label, "loginview migrate-authme [dry-run]", "从 AuthMe 数据库复制密码 hash 到独立账户库，不破解明文。");
                sendCommandHelp(sender, label, "loginview migration-commands", "显示迁移后停用 AuthMe 的安全操作步骤。");
            }
            case "warehouse" -> {
                sendHelpHeader(sender, "Warehouse", "AXUI 仓库、共享仓库、二级密码和银行管理。");
                sendCommandHelp(sender, label, "warehouse status", "查看仓库模块、数据库和配置状态。");
                sendCommandHelp(sender, label, "warehouse reload", "重载仓库配置。");
                sendCommandHelp(sender, label, "warehouse open <player>", "为在线玩家打开仓库 AXUI。");
                sendCommandHelp(sender, label, "warehouse info <player>", "查看玩家仓库概览。");
                sendCommandHelp(sender, label, "warehouse password <player> clear", "清除玩家二级密码。");
                sendCommandHelp(sender, label, "warehouse bank <player> <currency> <set|add|take> <amount>", "管理玩家银行余额。");
            }
            case "mail" -> {
                sendHelpHeader(sender, "Mail", "玩家邮箱、写信附件、预设邮件、CDK 和管理界面。");
                sendCommandHelp(sender, label, "mail status", "查看邮箱模块、存储、预设和 CDK 状态。");
                sendCommandHelp(sender, label, "mail reload", "重载邮箱配置、预设和 UI。");
                sendCommandHelp(sender, label, "mail open <player>", "为在线玩家打开邮箱收件箱。");
                sendCommandHelp(sender, label, "mail preset send <presetId> <player|all-online|all-registered>", "按预设向单人、全在线或全注册玩家派发邮件。");
                sendCommandHelp(sender, label, "mail cdk create <presetId> <code|auto> <maxClaims> <ttl>", "基于邮件预设创建兑换码，ttl 示例 1d、12h、permanent。");
                sendCommandHelp(sender, label, "mail cdk info <code>", "查看 CDK 绑定预设、领取次数、过期时间和启用状态。");
                sendCommandHelp(sender, label, "mail cdk list [page]", "分页查看当前已有 CDK、绑定预设、领取次数和过期时间。");
                sendCommandHelp(sender, label, "mail cdk delete <code>", "禁用并删除指定 CDK。");
            }
            case "chat" -> {
                sendHelpHeader(sender, "Chat", "频道聊天、私聊、跨服转发、禁言和监听。");
                sendCommandHelp(sender, label, "chat status", "查看聊天模块、存储、Redis 和 Proxy 状态。");
                sendCommandHelp(sender, label, "chat reload", "重载聊天频道、过滤和跨服配置。");
                sendCommandHelp(sender, label, "chat mute <player> <duration> [reason]", "禁言玩家，duration 示例 30m、12h、7d、permanent。");
                sendCommandHelp(sender, label, "chat unmute <player>", "解除玩家禁言。");
                sendCommandHelp(sender, label, "chat spy <player> <on|off>", "开启或关闭玩家私聊监听状态。");
            }
            case "questgps" -> {
                sendHelpHeader(sender, "QuestGPS", "任务导航菜单、追踪状态和 HUD。");
                sendCommandHelp(sender, label, "questgps status", "查看任务导航模块状态。");
                sendCommandHelp(sender, label, "questgps reload", "重载任务导航配置和 UI。");
                sendCommandHelp(sender, label, "questgps open <player>", "为在线玩家打开任务导航界面。");
            }
            case "map" -> {
                sendHelpHeader(sender, "Map", "大地图、小地图 HUD、锚点和世界地图配置。");
                sendCommandHelp(sender, label, "map status", "查看地图模块、世界、锚点和路径点状态。");
                sendCommandHelp(sender, label, "map reload", "重载地图配置和 UI。");
                sendCommandHelp(sender, label, "map open <player> [world]", "为在线玩家打开地图界面，可指定世界。");
                sendCommandHelp(sender, label, "map list", "列出已配置的地图世界。");
                sendCommandHelp(sender, label, "map anchors [world]", "列出全部或指定世界的地图锚点。");
            }
            default -> {
                sender.sendMessage(PREFIX + ChatColor.RED + "未知模块: " + module);
                sender.sendMessage(PREFIX + ChatColor.YELLOW + "使用 /" + label + " help 查看可用模块。");
            }
        }
    }

    private void sendHelpHeader(CommandSender sender, String moduleName, String description) {
        sender.sendMessage(PREFIX + ChatColor.GOLD + moduleName + " 模块帮助");
        sender.sendMessage(PREFIX + ChatColor.WHITE + description);
    }

    private void sendCommandHelp(CommandSender sender, String label, String usage, String description) {
        sender.sendMessage(
            PREFIX + ChatColor.GRAY + "/" + label + " " + usage
                + ChatColor.WHITE + " - " + description
        );
    }
}
