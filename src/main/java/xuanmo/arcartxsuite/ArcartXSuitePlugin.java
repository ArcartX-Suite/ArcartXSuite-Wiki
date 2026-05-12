package xuanmo.arcartxsuite;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import xuanmo.arcartxsuite.bossbar.config.PluginConfiguration;
import xuanmo.arcartxsuite.bossbar.platform.ServerPlatform;
import xuanmo.arcartxsuite.bossbar.tracker.BossTrackerService;
import xuanmo.arcartxsuite.bossbar.ui.ArcartXHudTemplateWriter;
import xuanmo.arcartxsuite.bridge.ArcartXClientBridge;
import xuanmo.arcartxsuite.bridge.ArcartXItemStackBridge;
import xuanmo.arcartxsuite.bridge.ArcartXPacketBridge;
import xuanmo.arcartxsuite.bridge.ArcartXPropBridge;
import xuanmo.arcartxsuite.bridge.VaultEconomyBridge;
import xuanmo.arcartxsuite.command.ArcartXSuiteCommand;
import xuanmo.arcartxsuite.attacktarget.config.AttackTargetModuleConfiguration;
import xuanmo.arcartxsuite.attacktarget.service.AttackTargetService;
import xuanmo.arcartxsuite.announcer.config.AnnouncerModuleConfiguration;
import xuanmo.arcartxsuite.announcer.service.AnnouncerService;
import xuanmo.arcartxsuite.chat.command.ChatPlayerCommand;
import xuanmo.arcartxsuite.chat.config.ChatModuleConfiguration;
import xuanmo.arcartxsuite.chat.service.ChatService;
import xuanmo.arcartxsuite.chat.storage.JdbcChatRepository;
import xuanmo.arcartxsuite.config.DefaultConfigResourceRegistry;
import xuanmo.arcartxsuite.config.ProtectedResourceStore;
import xuanmo.arcartxsuite.config.SyncResult;
import xuanmo.arcartxsuite.config.YamlConfigSynchronizer;
import xuanmo.arcartxsuite.config.YamlConfigSynchronizer.BatchSyncResult;
import xuanmo.arcartxsuite.conversation.config.ConversationModuleConfiguration;
import xuanmo.arcartxsuite.conversation.service.ConversationService;
import xuanmo.arcartxsuite.combat.EntityCombatMetadata;
import xuanmo.arcartxsuite.combat.TaczCombatBridge;
import xuanmo.arcartxsuite.digisdisplay.config.DigisDisplayModuleConfiguration;
import xuanmo.arcartxsuite.digisdisplay.service.DigisDisplayService;
import xuanmo.arcartxsuite.eventpacket.config.EventPacketContext;
import xuanmo.arcartxsuite.eventpacket.config.EventPacketAction;
import xuanmo.arcartxsuite.eventpacket.config.EventPacketRecipient;
import xuanmo.arcartxsuite.eventpacket.config.EventPacketRule;
import xuanmo.arcartxsuite.eventpacket.config.EventPacketTrigger;
import xuanmo.arcartxsuite.eventpacket.listener.PlayerEventPacketListener;
import xuanmo.arcartxsuite.eventpacket.service.PapiWatcherService;
import xuanmo.arcartxsuite.killeffect.config.CombatPacketContext;
import xuanmo.arcartxsuite.killeffect.config.PacketDefinition;
import xuanmo.arcartxsuite.killeffect.config.PacketRecipient;
import xuanmo.arcartxsuite.killeffect.config.PacketTrigger;
import xuanmo.arcartxsuite.killeffect.listener.PlayerKillListener;
import xuanmo.arcartxsuite.loginview.config.LoginViewModuleConfiguration;
import xuanmo.arcartxsuite.loginview.service.LoginViewService;
import xuanmo.arcartxsuite.loginview.storage.JdbcLoginViewRepository;
import xuanmo.arcartxsuite.mail.command.MailPlayerCommand;
import xuanmo.arcartxsuite.mail.config.MailModuleConfiguration;
import xuanmo.arcartxsuite.mail.service.MailService;
import xuanmo.arcartxsuite.mail.storage.JdbcMailRepository;
import xuanmo.arcartxsuite.map.command.MapPlayerCommand;
import xuanmo.arcartxsuite.map.config.MapModuleConfiguration;
import xuanmo.arcartxsuite.map.service.MapService;
import xuanmo.arcartxsuite.map.storage.JdbcMapRepository;
import xuanmo.arcartxsuite.onlinerewards.command.OnlineRewardsPlayerCommand;
import xuanmo.arcartxsuite.onlinerewards.config.OnlineRewardsModuleConfiguration;
import xuanmo.arcartxsuite.onlinerewards.placeholder.OnlineRewardsPlaceholderExpansion;
import xuanmo.arcartxsuite.onlinerewards.service.OnlineRewardsService;
import xuanmo.arcartxsuite.onlinerewards.storage.JdbcOnlineRewardsRepository;
// PacketCommand merged into EventPacket module
import xuanmo.arcartxsuite.pickup.config.PickupModuleConfiguration;
import xuanmo.arcartxsuite.pickup.service.PickupService;
import xuanmo.arcartxsuite.pickup.ui.PickupHudTemplateWriter;
import xuanmo.arcartxsuite.prop.config.PropDefinition;
import xuanmo.arcartxsuite.prop.config.PropDefinitionLoader;
import xuanmo.arcartxsuite.prop.config.PropKeyMappingConfiguration;
import xuanmo.arcartxsuite.prop.config.PropLanguageConfiguration;
import xuanmo.arcartxsuite.prop.config.PropModuleConfiguration;
import xuanmo.arcartxsuite.prop.service.PropService;
import xuanmo.arcartxsuite.questgps.command.QuestGpsPlayerCommand;
import xuanmo.arcartxsuite.questgps.config.QuestGpsModuleConfiguration;
import xuanmo.arcartxsuite.questgps.service.QuestGpsService;
import xuanmo.arcartxsuite.rgb.config.ArcartRgbModuleConfiguration;
import xuanmo.arcartxsuite.rgb.shimmer.ArcartRgbShimmerBridge;
import xuanmo.arcartxsuite.rgb.service.ArcartRgbService;
import xuanmo.arcartxsuite.module.ModuleRegistry;
import xuanmo.arcartxsuite.security.ClientPacketGuard;
import xuanmo.arcartxsuite.security.ClientPacketGuardConfiguration;
import xuanmo.arcartxsuite.security.ModulePasswordAuthenticator;
import xuanmo.arcartxsuite.security.ModulePasswordAuthenticator.ModuleKey;
import xuanmo.arcartxsuite.security.ModulePasswordAuthenticator.ValidationResult;
import xuanmo.arcartxsuite.subtitle.config.SubtitleGroup;
import xuanmo.arcartxsuite.subtitle.config.SubtitleModuleConfiguration;
import xuanmo.arcartxsuite.subtitle.service.SubtitleService;
import xuanmo.arcartxsuite.tab.config.TabModuleConfiguration;
import xuanmo.arcartxsuite.tab.sync.TabSyncService;
import xuanmo.arcartxsuite.title.command.TitlePlayerCommand;
import xuanmo.arcartxsuite.title.TitleDurationParser;
import xuanmo.arcartxsuite.title.config.TitleModuleConfiguration;
import xuanmo.arcartxsuite.title.service.TitleService;
import xuanmo.arcartxsuite.title.storage.JdbcTitleRepository;
import xuanmo.arcartxsuite.warehouse.command.WarehousePlayerCommand;
import xuanmo.arcartxsuite.warehouse.config.WarehouseModuleConfiguration;
import xuanmo.arcartxsuite.warehouse.service.WarehouseService;
import xuanmo.arcartxsuite.warehouse.storage.JdbcWarehouseRepository;

public class ArcartXSuitePlugin extends JavaPlugin {

    public static final String KILL_EFFECT_CONFIG_FILE_NAME = "ArcartXKillEffect.yml";
    public static final String EVENT_PACKET_CONFIG_FILE_NAME = "ArcartXEventPacket.yml";
    public static final String BOSSBAR_CONFIG_FILE_NAME = "ArcartXBossBar.yml";
    public static final String TAB_CONFIG_FILE_NAME = "ArcartXTab.yml";
    public static final String TITLE_CONFIG_FILE_NAME = "ArcartXTitle.yml";
    public static final String SUBTITLE_CONFIG_FILE_NAME = "ArcartXSubtitle.yml";
    public static final String ANNOUNCER_CONFIG_FILE_NAME = "ArcartXAnnouncer.yml";
    public static final String ATTACK_TARGET_CONFIG_FILE_NAME = "ArcartXAttackTarget.yml";
    public static final String PICKUP_CONFIG_FILE_NAME = "ArcartXPickMessage.yml";
    public static final String DIGIS_DISPLAY_CONFIG_FILE_NAME = "ArcartXDigisDisplay.yml";
    public static final String ONLINE_REWARDS_CONFIG_FILE_NAME = "ArcartXOnlineRewards.yml";
    public static final String PACKET_COMMAND_CONFIG_FILE_NAME = "ArcartXPacketCommand.yml";
    public static final String LOGIN_VIEW_CONFIG_FILE_NAME = "ArcartXLoginView.yml";
    public static final String WAREHOUSE_CONFIG_FILE_NAME = "ArcartXWarehouse.yml";
    public static final String MAIL_CONFIG_FILE_NAME = "ArcartXMail.yml";
    public static final String CHAT_CONFIG_FILE_NAME = "ArcartXChat.yml";
    public static final String CONVERSATION_CONFIG_FILE_NAME = "ArcartXConversation.yml";
    public static final String RGB_CONFIG_FILE_NAME = "ArcartXRGB.yml";
    public static final String PROP_CONFIG_FILE_NAME = "ArcartXProp.yml";
    public static final String QUEST_GPS_CONFIG_FILE_NAME = "ArcartXQuestGPS.yml";
    public static final String MAP_CONFIG_FILE_NAME = "ArcartXMap.yml";

    private static final String BOSSBAR_UI_FILE_PATH = "ui/boss_tracker.yml";
    private static final String SUBTITLE_UI_FILE_PATH = "ui/subtitle_hud.yml";
    private static final String ANNOUNCER_UI_FILE_PATH = "ui/announcer_hud.yml";
    private static final String ATTACK_TARGET_UI_FILE_PATH = "ui/attack_target_hud.yml";
    private static final String PICKUP_UI_FILE_PATH = "ui/pickup_hud.yml";
    private static final String CONVERSATION_UI_FILE_PATH = "ui/conversation_menu.yml";
    private static final String CONVERSATION_SELECTOR_UI_FILE_PATH = "ui/conversation_selector_hud.yml";
    private static final String SUBTITLE_DEFAULT_GROUP_RESOURCE_PATH = "subtitle/groups/default.yml";
    private static final String ANNOUNCER_UI_SIGNATURE = "# AXS announcer_hud; version=2";
    private static final String PICKUP_UI_SIGNATURE_PREFIX = "# AXS pickup_hud;";
    private static final String AUTHOR_NAME = "墨墨墨";
    private static final String AUTHOR_CONTACT = "QQ1451759359";
    private static final String DISPLAY_NAME = "ArcartXSuite";
    private static final String CONSOLE_PREFIX =
        ChatColor.DARK_AQUA + "◆ " + ChatColor.GOLD + "ArcartXSuite " + ChatColor.GRAY + "| " + ChatColor.RESET;
    private static final String[] STARTUP_BANNER = {
        "   ___                        __  _  __  _____       _ __",
        "  /   |  ____ ___  ____ _____/ /_| |/ / / ___/__  __(_) /____",
        " / /| | / ___/ _ \\/ __ `/ __/ __/   /  \\__ \\/ / / / / __/ _ \\",
        "/ ___ |/ /  /  __/ /_/ / / / /_/   |  ___/ / /_/ / / /_/  __/",
        "/_/  |_/_/   \\___/\\__,_/_/  \\__/_/|_| /____/\\__,_/_/\\__/\\___/"
    };

    private final ModulePasswordAuthenticator modulePasswordAuthenticator = new ModulePasswordAuthenticator();
    private final EnumMap<ModuleKey, ValidationResult> modulePasswordValidationStates = createInitialPasswordValidationStates();
    private PluginConfiguration bossBarConfiguration;
    private ArcartXPacketBridge packetBridge;
    private ArcartXClientBridge clientBridge;
    private ArcartXItemStackBridge itemStackBridge;
    private ArcartXPropBridge propBridge;
    private BossTrackerService bossTrackerService;
    private TabModuleConfiguration tabConfiguration;
    private TabSyncService tabSyncService;
    private ClientPacketGuardConfiguration clientPacketGuardConfiguration;
    private ClientPacketGuard clientPacketGuard;
    private ChatModuleConfiguration chatConfiguration;
    private ChatService chatService;
    private TitleModuleConfiguration titleConfiguration;
    private TitleService titleService;
    private SubtitleModuleConfiguration subtitleConfiguration;
    private SubtitleService subtitleService;
    private AnnouncerModuleConfiguration announcerConfiguration;
    private AnnouncerService announcerService;
    private AttackTargetModuleConfiguration attackTargetConfiguration;
    private AttackTargetService attackTargetService;
    private PickupModuleConfiguration pickupConfiguration;
    private PickupService pickupService;
    private PropModuleConfiguration propConfiguration;
    private PropService propService;
    private DigisDisplayModuleConfiguration digisDisplayConfiguration;
    private DigisDisplayService digisDisplayService;
    private ArcartRgbModuleConfiguration rgbConfiguration;
    private ArcartRgbService rgbService;
    private ArcartRgbShimmerBridge rgbShimmerBridge;
    private OnlineRewardsModuleConfiguration onlineRewardsConfiguration;
    private OnlineRewardsService onlineRewardsService;
    // PacketCommand merged into EventPacket — fields removed
    private LoginViewModuleConfiguration loginViewConfiguration;
    private LoginViewService loginViewService;
    private WarehouseModuleConfiguration warehouseConfiguration;
    private WarehouseService warehouseService;
    private MailModuleConfiguration mailConfiguration;
    private MailService mailService;
    private MapModuleConfiguration mapConfiguration;
    private MapService mapService;
    private ConversationModuleConfiguration conversationConfiguration;
    private ConversationService conversationService;
    private QuestGpsModuleConfiguration questGpsConfiguration;
    private QuestGpsService questGpsService;
    private ServerPlatform serverPlatform;
    private boolean passwordGateLocked;
    private final List<Object> placeholderExpansions = new ArrayList<>();
    private final Set<String> registeredPlaceholderIds = new HashSet<>();
    private File killEffectConfigFile;
    private File eventPacketConfigFile;
    private File bossBarConfigFile;
    private File tabConfigFile;
    private File titleConfigFile;
    private File subtitleConfigFile;
    private File announcerConfigFile;
    private File attackTargetConfigFile;
    private File pickupConfigFile;
    private File propConfigFile;
    private File digisDisplayConfigFile;
    private File rgbConfigFile;
    private File onlineRewardsConfigFile;
    // packetCommandConfigFile removed — merged into EventPacket
    private File loginViewConfigFile;
    private File warehouseConfigFile;
    private File mailConfigFile;
    private File chatConfigFile;
    private File conversationConfigFile;
    private File questGpsConfigFile;
    private File mapConfigFile;
    private xuanmo.arcartxsuite.killeffect.config.PluginConfiguration killEffectConfiguration;
    private xuanmo.arcartxsuite.eventpacket.config.PluginConfiguration eventPacketConfiguration;
    private PapiWatcherService eventPacketWatcherService;
    private final Set<String> firedEventPacketRules = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final Map<String, Long> eventPacketCooldowns = new java.util.concurrent.ConcurrentHashMap<>();
    private VaultEconomyBridge vaultEconomyBridge;
    private String bossBarRuntimeUiId;
    private String bossBarRegisteredUiId;
    private String subtitleRuntimeUiId;
    private String subtitleRegisteredUiId;
    private String announcerRuntimeUiId;
    private String announcerRegisteredUiId;
    private String attackTargetRuntimeUiId;
    private String attackTargetRegisteredUiId;
    private String pickupRuntimeUiId;
    private String pickupRegisteredUiId;
    private String conversationRuntimeUiId;
    private String conversationRegisteredUiId;
    private String conversationSelectorRuntimeUiId;
    private String conversationSelectorRegisteredUiId;
    private String questGpsRuntimeUiId;
    private String questGpsRegisteredUiId;
    private String questGpsHudRuntimeUiId;
    private String questGpsHudRegisteredUiId;
    private String mapMenuRuntimeUiId;
    private String mapMenuRegisteredUiId;
    private String mapHudRuntimeUiId;
    private String mapHudRegisteredUiId;
    private String loginViewRuntimeUiId;
    private String loginViewRegisteredUiId;
    private TaczCombatBridge taczCombatBridge;
    private Listener clientCustomPacketListener;
    private Listener clientInitializedListener;
    private ModuleRegistry moduleRegistry;

    @Override
    public void onEnable() {
        ensureRootConfigExists();
        reloadRootConfiguration();
        serverPlatform = ServerPlatform.detect(getServer());
        printStartupBanner();
        consoleInfo("欢迎使用 " + DISPLAY_NAME);
        consoleInfo(ChatColor.RED + "作者保留一切权利，谢绝转载");
        consoleInfo("服务端环境: " + serverPlatform.displayName()
            + (serverPlatform.hybridServer() ? " | 已启用混合核心兼容启动模式" : ""));
        registerCommands();
        getServer().getPluginManager().registerEvents(new PlayerKillListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerEventPacketListener(this), this);
        if (serverPlatform.hybridServer()) {
            taczCombatBridge = TaczCombatBridge.tryInitialize(
                this,
                getConfig().getBoolean("tacz-compat.enabled", true),
                getConfig().getBoolean("tacz-compat.debug", false)
            );
        }
        packetBridge = new ArcartXPacketBridge(this);
        if (!packetBridge.initialize()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        clientBridge = new ArcartXClientBridge(this);
        clientBridge.initialize();
        itemStackBridge = new ArcartXItemStackBridge(this);
        itemStackBridge.initialize();
        propBridge = new ArcartXPropBridge(this);
        propBridge.initialize();
        vaultEconomyBridge = new VaultEconomyBridge(this);
        vaultEconomyBridge.initialize();
        registerClientCustomPacketListener();
        registerClientInitializedListener();
        registerPlaceholderExpansionIfAvailable();
        consoleInfo("ArcartX 桥接: " + describePacketBridgeMode());
        moduleRegistry = new ModuleRegistry(
            this,
            new File(getDataFolder(), "modules"),
            packetBridge,
            clientBridge,
            itemStackBridge,
            clientPacketGuard,
            modulePasswordAuthenticator
        );
        Set<String> externalModuleIds = moduleRegistry.scanAvailableModuleIds();
        if (!externalModuleIds.isEmpty()) {
            consoleInfo("检测到外部模块 Jar: " + externalModuleIds);
        }
        boolean killEffectLoaded = externalModuleIds.contains("killeffect") || reloadKillEffectState(true);
        boolean eventPacketLoaded = externalModuleIds.contains("eventpacket") || reloadEventPacketState(true);
        boolean tabLoaded = externalModuleIds.contains("tab") || reloadTabState(true);
        boolean titleLoaded = externalModuleIds.contains("title") || reloadTitleState(true);
        boolean conversationLoaded = externalModuleIds.contains("conversation") || reloadConversationState(true);
        boolean announcerLoaded = externalModuleIds.contains("announcer") || reloadAnnouncerState(true);
        boolean pickupLoaded = externalModuleIds.contains("pickup") || reloadPickupState(true);
        boolean propLoaded = externalModuleIds.contains("prop") || reloadPropState(true);
        boolean rgbLoaded = externalModuleIds.contains("rgb") || reloadRgbState(true);
        boolean loginViewLoaded = externalModuleIds.contains("loginview") || reloadLoginViewState(true);
        boolean onlineRewardsLoaded = externalModuleIds.contains("onlinerewards") || reloadOnlineRewardsState(true);
        boolean warehouseLoaded = externalModuleIds.contains("warehouse") || reloadWarehouseState(true);
        boolean mailLoaded = externalModuleIds.contains("mail") || reloadMailState(true);
        boolean chatLoaded = externalModuleIds.contains("chat") || reloadChatState(true);
        boolean questGpsLoaded = externalModuleIds.contains("questgps") || reloadQuestGpsState(true);
        boolean mapLoaded = externalModuleIds.contains("map") || reloadMapState(true);
        boolean bossBarLoaded = externalModuleIds.contains("bossbar") || reloadBossBarState(true);
        printModuleStatusSummary(
            killEffectLoaded, eventPacketLoaded, tabLoaded, titleLoaded, conversationLoaded,
            announcerLoaded, pickupLoaded, propLoaded,
            rgbLoaded, loginViewLoaded,
            onlineRewardsLoaded, warehouseLoaded, mailLoaded, chatLoaded,
            questGpsLoaded, mapLoaded, bossBarLoaded
        );
        moduleRegistry.loadAll();
        consoleInfo(ChatColor.GREEN + "加载完成");
    }

    @Override
    public void onDisable() {
        if (moduleRegistry != null) {
            moduleRegistry.unloadAll();
            moduleRegistry = null;
        }
        if (taczCombatBridge != null) {
            taczCombatBridge.shutdown();
            taczCombatBridge = null;
        }
        unregisterClientCustomPacketListener();
        unregisterClientInitializedListener();
        
        shutdownTabModule();
        shutdownTitleModule();
        shutdownConversationModule();
        shutdownOnlineRewardsModule();
        shutdownLoginViewModule();
        shutdownDigisDisplayModule();
        shutdownRgbModule();
        shutdownWarehouseModule();
        shutdownMailModule();
        shutdownChatModule();
        shutdownQuestGpsModule();
        shutdownMapModule();
        shutdownPickupModule();
        shutdownPropModule();
        shutdownAnnouncerModule();
        shutdownAttackTargetModule();
        shutdownSubtitleModule();
        shutdownEventPacketModule();
        shutdownBossBarModule();
        if (packetBridge != null) {
            packetBridge.shutdown();
        }
        if (clientBridge != null) {
            clientBridge.shutdown();
        }
        if (itemStackBridge != null) {
            itemStackBridge.shutdown();
        }
        if (propBridge != null) {
            propBridge.shutdown();
            propBridge = null;
        }
        if (vaultEconomyBridge != null) {
            vaultEconomyBridge.shutdown();
            vaultEconomyBridge = null;
        }
        shutdownClientPacketGuard();
        unregisterPlaceholderExpansion();
    }

    private void reloadRootConfiguration() {
        reloadConfig();
        reloadClientPacketGuard();
    }

    private void reloadClientPacketGuard() {
        clientPacketGuardConfiguration = ClientPacketGuardConfiguration.load(getConfig(), getLogger());
        if (clientPacketGuard != null) {
            clientPacketGuard.shutdown();
        }
        clientPacketGuard = new ClientPacketGuard(this, clientPacketGuardConfiguration);
        clientPacketGuard.start();
    }

    private void shutdownClientPacketGuard() {
        if (clientPacketGuard != null) {
            clientPacketGuard.shutdown();
            clientPacketGuard = null;
        }
        clientPacketGuardConfiguration = null;
    }

    public boolean reloadBossBarState(boolean logSummary) {
        reloadRootConfiguration();
        registerPlaceholderExpansionIfAvailable();
        passwordGateLocked = false;
        shutdownBossBarModule();
        ValidationResult passwordValidation = refreshModulePasswordValidation(ModuleKey.ENTITY_TRACKER);

        if (!isModuleEnabled("entitytracker", true)) {
            consoleInfo("EntityTracker 模块已在 config.yml 中关闭");
            return false;
        }

        if (passwordValidation != ValidationResult.VALID) {
            passwordGateLocked = true;
            logModulePasswordFailure(ModuleKey.ENTITY_TRACKER, passwordValidation);
            return false;
        }

        ensureBossBarConfigExists();
        bossBarConfiguration = loadBossBarConfiguration();
        bossBarRuntimeUiId = bossBarConfiguration == null ? "" : bossBarConfiguration.uiId();

        if (!hasAnyPlugin("MythicMobs", "MythicBukkit")) {
            consoleError("EntityTracker 模块需要 MythicMobs 或 MythicBukkit");
            sendConsoleBanner(ChatColor.RED, "EntityTracker 初始化失败", "缺少 MythicMobs 或 MythicBukkit");
            return false;
        }

        int supportedSlotCount;
        File uiFile;
        try {
            supportedSlotCount = ArcartXHudTemplateWriter.supportedSlotCount();
            if (bossBarConfiguration.maxVisibleBars() > supportedSlotCount) {
                consoleError(
                    "当前受保护的 boss_tracker 模板最多只支持 "
                        + supportedSlotCount
                        + " 条血条，但 ArcartXBossBar.yml 的 settings.max-visible-bars 为 "
                        + bossBarConfiguration.maxVisibleBars()
                );
                consoleError("请先降低 max-visible-bars，或重新封装新的 boss_tracker 模板后再启动插件。");
                return false;
            }
            uiFile = exportBundledUiFile(bossBarConfiguration.overwriteUiFile());
        } catch (IOException exception) {
            consoleError("导出内置 ArcartX HUD 失败: " + exception.getMessage());
            sendConsoleBanner(ChatColor.RED, "EntityTracker 初始化失败", "导出 boss_tracker 失败", exception.getMessage());
            return false;
        }

        bossBarRuntimeUiId = ArcartXPacketBridge.normalizeUiId(bossBarConfiguration.uiId(), uiFile);
        bossBarRegisteredUiId = null;
        if (bossBarConfiguration.registerUiOnEnable()) {
            ArcartXPacketBridge.UiRegistrationResult registration = packetBridge.registerOrReloadUi(bossBarConfiguration.uiId(), uiFile);
            if (!registration.success()) {
                consoleError("初始化 ArcartX EntityTracker UI 失败: " + registration.message());
                sendConsoleBanner(
                    ChatColor.RED,
                    "EntityTracker 初始化失败",
                    "ArcartX UI 注册失败",
                    "请检查 ArcartX 版本和控制台详细报错"
                );
                return false;
            }
            bossBarRuntimeUiId = registration.runtimeUiId();
            bossBarRegisteredUiId = registration.registeredUiId();
        } else {
            consoleInfo("ArcartX UI 自动注册已关闭，将直接使用 UI 标识: " + bossBarRuntimeUiId);
        }

        bossTrackerService = new BossTrackerService(this, bossBarConfiguration, packetBridge);
        bossTrackerService.start();

        if (logSummary) {
            consoleInfo(
                "EntityTracker 模块已载入，跟踪 "
                    + bossBarConfiguration.getTrackedBossCount()
                    + " 个 MythicMobs 配置"
                    + " | Boss 预览: " + bossBarConfiguration.summarizeTrackedBossIds(8)
                    + " | UI: " + bossBarRuntimeUiId
            );
        }
        reloadAttackTargetStateInternal(logSummary);
        return true;
    }

    public PluginConfiguration getBossBarConfiguration() {
        return bossBarConfiguration;
    }

    public xuanmo.arcartxsuite.killeffect.config.PluginConfiguration getKillEffectConfiguration() {
        return killEffectConfiguration;
    }

    public xuanmo.arcartxsuite.eventpacket.config.PluginConfiguration getEventPacketConfiguration() {
        return eventPacketConfiguration;
    }

    public TabModuleConfiguration getTabConfiguration() {
        return tabConfiguration;
    }

    public TabSyncService getTabSyncService() {
        return tabSyncService;
    }

    public ClientPacketGuardConfiguration getClientPacketGuardConfiguration() {
        return clientPacketGuardConfiguration;
    }

    public ClientPacketGuard getClientPacketGuard() {
        return clientPacketGuard;
    }

    public ChatModuleConfiguration getChatConfiguration() {
        return chatConfiguration;
    }

    public ChatService getChatService() {
        return chatService;
    }

    public TitleModuleConfiguration getTitleConfiguration() {
        return titleConfiguration;
    }

    public TitleService getTitleService() {
        return titleService;
    }

    public SubtitleModuleConfiguration getSubtitleConfiguration() {
        return subtitleConfiguration;
    }

    public ConversationModuleConfiguration getConversationConfiguration() {
        return conversationConfiguration;
    }

    public ConversationService getConversationService() {
        return conversationService;
    }

    public AnnouncerModuleConfiguration getAnnouncerConfiguration() {
        return announcerConfiguration;
    }

    public AttackTargetModuleConfiguration getAttackTargetConfiguration() {
        return attackTargetConfiguration;
    }

    public PickupModuleConfiguration getPickupConfiguration() {
        return pickupConfiguration;
    }

    public PropModuleConfiguration getPropConfiguration() {
        return propConfiguration;
    }

    public PropService getPropService() {
        return propService;
    }

    public String describePropWriterBackend() {
        return propBridge == null ? "pdc" : propBridge.propIdWriterBackendKey();
    }

    public DigisDisplayModuleConfiguration getDigisDisplayConfiguration() {
        return digisDisplayConfiguration;
    }

    public ArcartRgbModuleConfiguration getRgbConfiguration() {
        return rgbConfiguration;
    }

    public ArcartRgbService getRgbService() {
        return rgbService;
    }

    public OnlineRewardsModuleConfiguration getOnlineRewardsConfiguration() {
        return onlineRewardsConfiguration;
    }

    public OnlineRewardsService getOnlineRewardsService() {
        return onlineRewardsService;
    }

    public LoginViewModuleConfiguration getLoginViewConfiguration() {
        return loginViewConfiguration;
    }

    public LoginViewService getLoginViewService() {
        return loginViewService;
    }

    public WarehouseModuleConfiguration getWarehouseConfiguration() {
        return warehouseConfiguration;
    }

    public WarehouseService getWarehouseService() {
        return warehouseService;
    }

    public MailModuleConfiguration getMailConfiguration() {
        return mailConfiguration;
    }

    public MailService getMailService() {
        return mailService;
    }

    public MapModuleConfiguration getMapConfiguration() {
        return mapConfiguration;
    }

    public MapService getMapService() {
        return mapService;
    }

    public QuestGpsModuleConfiguration getQuestGpsConfiguration() {
        return questGpsConfiguration;
    }

    public QuestGpsService getQuestGpsService() {
        return questGpsService;
    }

    public ModuleRegistry getModuleRegistry() {
        return moduleRegistry;
    }

    public ArcartXPacketBridge getPacketBridge() {
        return packetBridge;
    }

    public ArcartXClientBridge getClientBridge() {
        return clientBridge;
    }

    public ArcartXItemStackBridge getItemStackBridge() {
        return itemStackBridge;
    }

    public BossTrackerService getBossTrackerService() {
        return bossTrackerService;
    }

    public boolean isBossBarAttributePlusHooked() {
        return bossTrackerService != null && bossTrackerService.attributePlusHooked();
    }

    public ServerPlatform getServerPlatform() {
        return serverPlatform;
    }

    public File getKillEffectConfigFile() {
        return killEffectConfigFile;
    }

    public File getEventPacketConfigFile() {
        return eventPacketConfigFile;
    }

    public File getBossBarConfigFile() {
        return bossBarConfigFile;
    }

    public File getTabConfigFile() {
        return tabConfigFile;
    }

    public File getTitleConfigFile() {
        return titleConfigFile;
    }

    public File getSubtitleConfigFile() {
        return subtitleConfigFile;
    }

    public File getAnnouncerConfigFile() {
        return announcerConfigFile;
    }

    public File getAttackTargetConfigFile() {
        return attackTargetConfigFile;
    }

    public File getPickupConfigFile() {
        return pickupConfigFile;
    }

    public File getPropConfigFile() {
        return propConfigFile;
    }

    public File getDigisDisplayConfigFile() {
        return digisDisplayConfigFile;
    }

    public File getRgbConfigFile() {
        return rgbConfigFile;
    }

    public File getOnlineRewardsConfigFile() {
        return onlineRewardsConfigFile;
    }

    public File getLoginViewConfigFile() {
        return loginViewConfigFile;
    }

    public File getWarehouseConfigFile() {
        return warehouseConfigFile;
    }

    public File getMailConfigFile() {
        return mailConfigFile;
    }

    public File getChatConfigFile() {
        return chatConfigFile;
    }

    public File getConversationConfigFile() {
        return conversationConfigFile;
    }

    public File getQuestGpsConfigFile() {
        return questGpsConfigFile;
    }

    public File getMapConfigFile() {
        return mapConfigFile;
    }

    public String getBossBarRuntimeUiId() {
        if (bossBarRuntimeUiId != null && !bossBarRuntimeUiId.isBlank()) {
            return bossBarRuntimeUiId;
        }
        return bossBarConfiguration == null ? "" : bossBarConfiguration.uiId();
    }

    public String getAttackTargetRuntimeUiId() {
        if (attackTargetRuntimeUiId != null && !attackTargetRuntimeUiId.isBlank()) {
            return attackTargetRuntimeUiId;
        }
        return attackTargetConfiguration == null ? "" : attackTargetConfiguration.uiId();
    }

    public String getConversationRuntimeUiId() {
        if (conversationRuntimeUiId != null && !conversationRuntimeUiId.isBlank()) {
            return conversationRuntimeUiId;
        }
        return conversationConfiguration == null ? "" : conversationConfiguration.uiId();
    }

    public String getConversationSelectorRuntimeUiId() {
        if (conversationSelectorRuntimeUiId != null && !conversationSelectorRuntimeUiId.isBlank()) {
            return conversationSelectorRuntimeUiId;
        }
        return conversationConfiguration == null ? "" : conversationConfiguration.selectorUiId();
    }

    public String getQuestGpsRuntimeUiId() {
        if (questGpsRuntimeUiId != null && !questGpsRuntimeUiId.isBlank()) {
            return questGpsRuntimeUiId;
        }
        return questGpsConfiguration == null ? "" : questGpsConfiguration.client().menuUiId();
    }

    public String getQuestGpsHudRuntimeUiId() {
        if (questGpsHudRuntimeUiId != null && !questGpsHudRuntimeUiId.isBlank()) {
            return questGpsHudRuntimeUiId;
        }
        return questGpsConfiguration == null ? "" : questGpsConfiguration.client().hudUiId();
    }

    public String getMapMenuRuntimeUiId() {
        if (mapMenuRuntimeUiId != null && !mapMenuRuntimeUiId.isBlank()) {
            return mapMenuRuntimeUiId;
        }
        return mapConfiguration == null ? "" : mapConfiguration.client().menuUiId();
    }

    public String getMapHudRuntimeUiId() {
        if (mapHudRuntimeUiId != null && !mapHudRuntimeUiId.isBlank()) {
            return mapHudRuntimeUiId;
        }
        return mapConfiguration == null ? "" : mapConfiguration.client().hudUiId();
    }

    public String getLoginViewRuntimeUiId() {
        if (loginViewRuntimeUiId != null && !loginViewRuntimeUiId.isBlank()) {
            return loginViewRuntimeUiId;
        }
        return loginViewConfiguration == null ? "" : loginViewConfiguration.ui().uiId();
    }

    public boolean isHybridBootstrapPending() {
        return false;
    }

    public boolean isPasswordGateLocked() {
        return passwordGateLocked;
    }

    public boolean isModuleEnabledInConfig(ModuleKey moduleKey) {
        return isModuleEnabled(moduleKey.configKey(), moduleKey.enabledByDefault());
    }

    public ValidationResult getModulePasswordValidation(ModuleKey moduleKey) {
        return modulePasswordValidationStates.getOrDefault(moduleKey, ValidationResult.MISSING);
    }

    public String describeModulePasswordValidation(ModuleKey moduleKey) {
        return getModulePasswordValidation(moduleKey).displayText();
    }

    public String getFirstBossReloadStatus() {
        return "UI 自动导入";
    }

    public boolean isPlaceholderExpansionRegistered() {
        return !registeredPlaceholderIds.isEmpty();
    }

    public boolean isPlaceholderApiAvailable() {
        return hasPlugin("PlaceholderAPI");
    }

    public boolean isBossBarModuleReady() {
        return bossTrackerService != null;
    }

    public boolean isKillEffectModuleReady() {
        return killEffectConfiguration != null && packetBridge != null && packetBridge.isAvailable();
    }

    public boolean isEventPacketModuleReady() {
        return eventPacketConfiguration != null && packetBridge != null && packetBridge.isAvailable();
    }

    public boolean isEventPacketWatcherRunning() {
        return eventPacketWatcherService != null && eventPacketWatcherService.isRunning();
    }

    public boolean isTabModuleReady() {
        return tabSyncService != null;
    }

    public boolean isChatModuleReady() {
        return chatService != null;
    }

    public boolean isTitleModuleReady() {
        return titleService != null;
    }

    public boolean isConversationModuleReady() {
        return conversationService != null;
    }

    public boolean isSubtitleModuleReady() {
        return subtitleService != null;
    }

    public boolean isAnnouncerModuleReady() {
        return announcerService != null;
    }

    public boolean isAttackTargetModuleReady() {
        return attackTargetService != null;
    }

    public boolean isPickupModuleReady() {
        return pickupService != null;
    }

    public boolean isPropModuleReady() {
        return propService != null;
    }

    public boolean isDigisDisplayModuleReady() {
        return digisDisplayService != null;
    }

    public boolean isRgbModuleReady() {
        return rgbService != null;
    }

    public boolean isOnlineRewardsModuleReady() {
        return onlineRewardsService != null;
    }

    public boolean isPacketCommandIntegrated() {
        return eventPacketConfiguration != null && !eventPacketConfiguration.packetCommandPacketId().isBlank();
    }

    public int getPacketCommandPresetCount() {
        return eventPacketConfiguration == null ? 0 : eventPacketConfiguration.packetCommandPresetCount();
    }

    public String getPacketCommandPacketId() {
        return eventPacketConfiguration == null ? "" : eventPacketConfiguration.packetCommandPacketId();
    }

    public boolean isLoginViewModuleReady() {
        return loginViewService != null;
    }

    public boolean isWarehouseModuleReady() {
        return warehouseService != null;
    }

    public boolean isMailModuleReady() {
        return mailService != null;
    }

    public boolean isQuestGpsModuleReady() {
        return questGpsService != null;
    }

    public boolean isMapModuleReady() {
        return mapService != null;
    }

    public boolean isVaultEconomyAvailable() {
        return vaultEconomyBridge != null && vaultEconomyBridge.isAvailable();
    }

    public List<String> getSubtitleGroupIds() {
        return subtitleService == null ? List.of() : subtitleService.groupIds();
    }

    public int getSubtitleActiveSessionCount() {
        return subtitleService == null ? 0 : subtitleService.activeSessionCount();
    }

    public int getConversationActiveSessionCount() {
        return conversationService == null ? 0 : conversationService.activeConversationCount();
    }

    public int getConversationOpenedPlayerCount() {
        return conversationService == null ? 0 : conversationService.openedPlayerCount();
    }

    public int getConversationCandidatePlayerCount() {
        return conversationService == null ? 0 : conversationService.candidatePlayerCount();
    }

    public int getConversationSelectorOpenedPlayerCount() {
        return conversationService == null ? 0 : conversationService.selectorOpenedPlayerCount();
    }

    public boolean isConversationInteractionReady() {
        return conversationService != null && conversationService.interactionReady();
    }

    public boolean isConversationKeybindReady() {
        return conversationService != null && conversationService.keybindReady();
    }

    public boolean playSubtitleGroup(Player player, String groupId) {
        return subtitleService != null && subtitleService.play(player, groupId);
    }

    public void stopSubtitle(Player player) {
        if (subtitleService != null) {
            subtitleService.stop(player);
        }
    }

    public int getAnnouncerActiveEntryCount() {
        return announcerService == null ? 0 : announcerService.activeEntryCount();
    }

    public int getAnnouncerInitializedPlayerCount() {
        return announcerService == null ? 0 : announcerService.initializedPlayerCount();
    }

    public int getAttackTargetActiveTargetCount() {
        return attackTargetService == null ? 0 : attackTargetService.activeTargetCount();
    }

    public int getAttackTargetActiveViewerCount() {
        return attackTargetService == null ? 0 : attackTargetService.activeViewerCount();
    }

    public String describePacketBridgeMode() {
        return packetBridge == null ? "unavailable" : packetBridge.describePacketMode();
    }

    public int getOnlineRewardsCachedPlayerCount() {
        return onlineRewardsService == null ? 0 : onlineRewardsService.cachedPlayerCount();
    }

    public boolean isDigisDisplayAttributePlusHooked() {
        return digisDisplayService != null && digisDisplayService.attributePlusHooked();
    }

    public boolean isDigisDisplayMythicLibDamageHooked() {
        return digisDisplayService != null && digisDisplayService.mythicLibDamageHooked();
    }

    public boolean isDigisDisplayCraneAttributeHooked() {
        return digisDisplayService != null && digisDisplayService.craneAttributeHooked();
    }

    public String getDigisDisplayActiveDamageSource() {
        return digisDisplayService == null ? "NONE" : digisDisplayService.activeDamageSource().name();
    }

    public boolean isDigisDisplayMythicHealHooked() {
        return digisDisplayService != null && digisDisplayService.mythicHealHooked();
    }

    public int getRgbActiveEntryCount() {
        return rgbService == null ? 0 : rgbService.activeEntryCount();
    }

    public boolean reloadKillEffectState(boolean logSummary) {
        reloadRootConfiguration();
        ValidationResult passwordValidation = refreshModulePasswordValidation(ModuleKey.COMBAT_EFFECT);
        if (!isModuleEnabled("combateffect", true)) {
            killEffectConfiguration = null;
            consoleInfo("CombatEffect 模块已在 config.yml 中关闭");
            return false;
        }

        if (passwordValidation != ValidationResult.VALID) {
            killEffectConfiguration = null;
            logModulePasswordFailure(ModuleKey.COMBAT_EFFECT, passwordValidation);
            return false;
        }

        ensureKillEffectConfigExists();
        killEffectConfiguration = loadKillEffectConfiguration();
        if (logSummary) {
            consoleInfo(
                "CombatEffect 模块已载入，已启用配置: "
                    + killEffectConfiguration.enabledPacketCount()
                    + "/"
                    + killEffectConfiguration.packetDefinitions().size()
                    + " | 发包模式: "
                    + describePacketBridgeMode()
            );
        }
        reloadDigisDisplayStateInternal(logSummary);
        return true;
    }

    public boolean reloadEventPacketState(boolean logSummary) {
        reloadRootConfiguration();
        shutdownEventPacketModule();
        ValidationResult passwordValidation = refreshModulePasswordValidation(ModuleKey.EVENT_PACKET);

        if (!isModuleEnabled("eventpacket", false)) {
            eventPacketConfiguration = null;
            consoleInfo("EventPacket 模块已在 config.yml 中关闭");
            return false;
        }

        if (passwordValidation != ValidationResult.VALID) {
            eventPacketConfiguration = null;
            logModulePasswordFailure(ModuleKey.EVENT_PACKET, passwordValidation);
            return false;
        }

        ensureEventPacketConfigExists();
        ensurePacketCommandPresetDefaultsExist();
        eventPacketConfiguration = loadEventPacketConfiguration();
        int papiPacketCount = eventPacketConfiguration.papiPacketCount();
        if (eventPacketConfiguration.packetCommandPresetCount() > 0 && logSummary) {
            consoleInfo(
                "PacketCommand 已集成到 EventPacket: packet-id="
                    + eventPacketConfiguration.packetCommandPacketId()
                    + " | presets="
                    + eventPacketConfiguration.packetCommandPresetCount()
            );
        }
        boolean placeholderApiAvailable = hasPlugin("PlaceholderAPI");
        boolean hasMobKillRules = eventPacketConfiguration.rules().stream()
            .anyMatch(rule -> rule.enabled() && rule.trigger() == EventPacketTrigger.MOB_KILL_COUNT);
        if (papiPacketCount > 0 && !placeholderApiAvailable) {
            consoleWarn(
                "EventPacket 模块检测到 "
                    + papiPacketCount
                    + " 个 PAPI 触发配置，但当前未安装 PlaceholderAPI，这部分触发器不会生效。"
            );
        }
        if ((papiPacketCount > 0 && placeholderApiAvailable) || hasMobKillRules) {
            eventPacketWatcherService = new PapiWatcherService(this, eventPacketConfiguration, packetBridge);
            eventPacketWatcherService.start();
        }

        if (logSummary) {
            consoleInfo(
                "EventPacket 模块已载入，rules="
                    + eventPacketConfiguration.enabledRuleCount()
                    + "/"
                    + eventPacketConfiguration.rules().size()
                    + " | PAPI 监听: "
                    + papiPacketCount
                    + " | 轮询间隔: "
                    + eventPacketConfiguration.refreshIntervalTicks()
                    + " ticks"
            );
        }
        return true;
    }

    public boolean reloadTabState(boolean logSummary) {
        reloadRootConfiguration();
        shutdownTabModule();
        ValidationResult passwordValidation = refreshModulePasswordValidation(ModuleKey.TAB);

        if (!isModuleEnabled("tab", false)) {
            tabConfiguration = null;
            consoleInfo("Tab 模块已在 config.yml 中关闭");
            return false;
        }

        if (passwordValidation != ValidationResult.VALID) {
            tabConfiguration = null;
            logModulePasswordFailure(ModuleKey.TAB, passwordValidation);
            return false;
        }

        if (!hasPlugin("PlaceholderAPI")) {
            tabConfiguration = null;
            consoleWarn("Tab 模块需要 PlaceholderAPI，当前已跳过加载");
            return false;
        }

        ensureTabConfigExists();
        tabConfiguration = loadTabConfiguration();
        tabSyncService = new TabSyncService(this, tabConfiguration, packetBridge);
        tabSyncService.start();
        if (logSummary) {
            consoleInfo(
                "Tab 模块已载入，定义数量: "
                    + tabConfiguration.definitions().size()
                    + " | 轮询间隔: "
                    + tabConfiguration.refreshIntervalTicks()
                    + " ticks"
            );
        }
        return true;
    }

    public boolean reloadTitleState(boolean logSummary) {
        reloadRootConfiguration();
        shutdownTitleModule();
        registerPlaceholderExpansionIfAvailable();
        ValidationResult passwordValidation = refreshModulePasswordValidation(ModuleKey.TITLE);

        if (!isModuleEnabled("title", false)) {
            titleConfiguration = null;
            consoleInfo("Title 模块已在 config.yml 中关闭");
            return false;
        }

        if (passwordValidation != ValidationResult.VALID) {
            titleConfiguration = null;
            logModulePasswordFailure(ModuleKey.TITLE, passwordValidation);
            return false;
        }

        ensureTitleConfigExists();
        titleConfiguration = loadTitleConfiguration();
        try {
            titleService = new TitleService(
                this,
                titleConfiguration,
                new JdbcTitleRepository(getDataFolder(), titleConfiguration.storage(), getLogger()),
                packetBridge
            );
            titleService.start();
            if (logSummary) {
                consoleInfo(
                    "Title 模块已载入，称号数量: "
                        + titleConfiguration.enabledTitleCount()
                        + " | 存储模式: "
                        + titleConfiguration.storage().dialect().configKey()
                        + " | UI: "
                        + titleService.runtimeUiId()
                        + " | mythiclib="
                        + titleConfiguration.mythicLib().enabled()
                        + "/"
                        + titleService.mythicLibHooked()
                );
            }
            return true;
        } catch (Exception exception) {
            consoleWarn("Title 模块启动失败: " + exception.getMessage());
            shutdownTitleModule();
            return false;
        }
    }

    public boolean reloadSubtitleState(boolean logSummary) {
        reloadSubtitleStateInternal(logSummary);
        return subtitleService != null;
    }

    private void reloadSubtitleStateInternal(boolean logSummary) {
        shutdownSubtitleModule();

        ensureSubtitleConfigExists();
        subtitleConfiguration = loadSubtitleConfiguration();
        try {
            ensureSubtitleDefaultGroupExists(subtitleConfiguration.groupsDirectory());
            Map<String, SubtitleGroup> groups = loadSubtitleGroups(subtitleConfiguration);
            File uiFile = exportBundledUiResource("arcartx/ui/subtitle_hud.yml", SUBTITLE_UI_FILE_PATH, subtitleConfiguration.overwriteUiFile());
            UiBinding uiBinding = prepareUiBinding("Subtitle", subtitleConfiguration.uiId(), subtitleConfiguration.registerUiOnEnable(), uiFile);
            if (uiBinding == null) {
                return;
            }
            subtitleRuntimeUiId = uiBinding.runtimeUiId();
            subtitleRegisteredUiId = uiBinding.registeredUiId();
            subtitleService = new SubtitleService(this, subtitleConfiguration, packetBridge, subtitleRuntimeUiId, groups);
            subtitleService.start();
            if (logSummary) {
                consoleInfo(
                    "Subtitle(→Announcer) 已载入，字幕组数量: "
                        + groups.size()
                        + " | UI: "
                        + subtitleRuntimeUiId
                );
            }
        } catch (IOException exception) {
            consoleWarn("Subtitle 模块启动失败: " + exception.getMessage());
            shutdownSubtitleModule();
        }
    }

    public boolean reloadConversationState(boolean logSummary) {
        reloadRootConfiguration();
        shutdownConversationModule();
        ValidationResult passwordValidation = refreshModulePasswordValidation(ModuleKey.CONVERSATION);

        if (!isModuleEnabled("conversation", false)) {
            conversationConfiguration = null;
            conversationRuntimeUiId = null;
            conversationSelectorRuntimeUiId = null;
            consoleInfo("Conversation 模块已在 config.yml 中关闭");
            return false;
        }

        if (passwordValidation != ValidationResult.VALID) {
            conversationConfiguration = null;
            conversationRuntimeUiId = null;
            conversationSelectorRuntimeUiId = null;
            logModulePasswordFailure(ModuleKey.CONVERSATION, passwordValidation);
            return false;
        }

        if (!hasPlugin("Chemdah")) {
            conversationConfiguration = null;
            conversationRuntimeUiId = null;
            conversationSelectorRuntimeUiId = null;
            consoleWarn("Conversation 模块需要 Chemdah，当前已跳过加载");
            return false;
        }

        ensureConversationConfigExists();
        conversationConfiguration = loadConversationConfiguration();
        boolean adyeshachAvailable = hasPlugin("Adyeshach");
        if (conversationConfiguration.interactionEnabled() && !adyeshachAvailable) {
            consoleWarn("Conversation 按键增强需要 Adyeshach，当前将仅启用 Chemdah 对话渲染");
        }
        try {
            File uiFile = exportBundledUiResource(
                ConversationService.UI_RESOURCE_PATH,
                CONVERSATION_UI_FILE_PATH,
                conversationConfiguration.overwriteUiFile()
            );
            UiBinding uiBinding = prepareUiBinding(
                "Conversation",
                conversationConfiguration.uiId(),
                conversationConfiguration.registerUiOnEnable(),
                uiFile
            );
            if (uiBinding == null) {
                return false;
            }
            conversationRuntimeUiId = uiBinding.runtimeUiId();
            conversationRegisteredUiId = uiBinding.registeredUiId();
            conversationSelectorRuntimeUiId = null;
            conversationSelectorRegisteredUiId = null;
            if (conversationConfiguration.interactionEnabled() && adyeshachAvailable) {
                try {
                    File selectorUiFile = exportBundledUiResource(
                        ConversationService.SELECTOR_UI_RESOURCE_PATH,
                        CONVERSATION_SELECTOR_UI_FILE_PATH,
                        conversationConfiguration.selectorOverwriteUiFile()
                    );
                    UiBinding selectorUiBinding = prepareOptionalUiBinding(
                        "Conversation Selector",
                        conversationConfiguration.selectorUiId(),
                        conversationConfiguration.selectorRegisterUiOnEnable(),
                        selectorUiFile
                    );
                    if (selectorUiBinding != null) {
                        conversationSelectorRuntimeUiId = selectorUiBinding.runtimeUiId();
                        conversationSelectorRegisteredUiId = selectorUiBinding.registeredUiId();
                    }
                } catch (IOException exception) {
                    consoleWarn("导出 Conversation selector HUD 失败，已降级关闭按键增强: " + exception.getMessage());
                }
            }
            conversationService = new ConversationService(
                this,
                conversationConfiguration,
                packetBridge,
                conversationRuntimeUiId,
                conversationSelectorRuntimeUiId
            );
            conversationService.start();
            if (logSummary) {
                consoleInfo(
                    "Conversation 模块已载入，theme="
                        + conversationConfiguration.themeName()
                        + " | dialogUI="
                        + conversationRuntimeUiId
                        + " | selectorUI="
                        + (conversationSelectorRuntimeUiId == null ? "disabled" : conversationSelectorRuntimeUiId)
                        + " | packetId="
                        + conversationConfiguration.clientPacketId()
                );
            }
            return true;
        } catch (IOException exception) {
            consoleWarn("Conversation 模块启动失败: " + exception.getMessage());
            shutdownConversationModule();
            return false;
        }
    }

    public boolean reloadAnnouncerState(boolean logSummary) {
        reloadRootConfiguration();
        shutdownAnnouncerModule();
        ValidationResult passwordValidation = refreshModulePasswordValidation(ModuleKey.ANNOUNCER);

        if (!isModuleEnabled("announcer", false)) {
            announcerConfiguration = null;
            consoleInfo("Announcer 模块已在 config.yml 中关闭");
            return false;
        }

        if (passwordValidation != ValidationResult.VALID) {
            announcerConfiguration = null;
            logModulePasswordFailure(ModuleKey.ANNOUNCER, passwordValidation);
            return false;
        }

        ensureAnnouncerConfigExists();
        announcerConfiguration = loadAnnouncerConfiguration();
        try {
            File uiFile = exportAnnouncerUiFile(announcerConfiguration.overwriteUiFile());
            UiBinding uiBinding = prepareUiBinding("Announcer", announcerConfiguration.uiId(), announcerConfiguration.registerUiOnEnable(), uiFile);
            if (uiBinding == null) {
                return false;
            }
            announcerRuntimeUiId = uiBinding.runtimeUiId();
            announcerRegisteredUiId = uiBinding.registeredUiId();
            announcerService = new AnnouncerService(this, announcerConfiguration, packetBridge, announcerRuntimeUiId);
            announcerService.start();
            announcerService.syncAllOnlinePlayers(true);
            if (logSummary) {
                consoleInfo(
                    "Announcer 模块已载入，启用公告数: "
                        + announcerService.activeEntryCount()
                        + "/"
                        + announcerConfiguration.entries().size()
                        + " | UI: "
                        + announcerRuntimeUiId
                );
            }
            reloadSubtitleStateInternal(logSummary);
            return true;
        } catch (IOException exception) {
            consoleWarn("Announcer 模块启动失败: " + exception.getMessage());
            shutdownAnnouncerModule();
            return false;
        }
    }

    public boolean reloadAttackTargetState(boolean logSummary) {
        reloadAttackTargetStateInternal(logSummary);
        return attackTargetService != null;
    }

    private void reloadAttackTargetStateInternal(boolean logSummary) {
        shutdownAttackTargetModule();

        ensureAttackTargetConfigExists();
        attackTargetConfiguration = loadAttackTargetConfiguration();
        try {
            File uiFile = exportBundledUiResource(
                "arcartx/ui/attack_target_hud.yml",
                ATTACK_TARGET_UI_FILE_PATH,
                attackTargetConfiguration.overwriteUiFile()
            );
            UiBinding uiBinding = prepareUiBinding(
                "EntityTracker",
                attackTargetConfiguration.uiId(),
                attackTargetConfiguration.registerUiOnEnable(),
                uiFile
            );
            if (uiBinding == null) {
                return;
            }
            attackTargetRuntimeUiId = uiBinding.runtimeUiId();
            attackTargetRegisteredUiId = uiBinding.registeredUiId();
            attackTargetService = new AttackTargetService(this, attackTargetConfiguration, packetBridge, attackTargetRuntimeUiId);
            attackTargetService.start();
            if (logSummary) {
                consoleInfo(
                    "AttackTarget(→BossBar) 已载入，refresh="
                        + attackTargetConfiguration.refreshIntervalTicks()
                        + " ticks | timeout="
                        + attackTargetConfiguration.targetTimeoutMs()
                        + "ms | range="
                        + attackTargetConfiguration.maxViewDistance()
                        + " | UI="
                        + attackTargetRuntimeUiId
                );
            }
        } catch (IOException exception) {
            consoleWarn("AttackTarget 模块启动失败: " + exception.getMessage());
            shutdownAttackTargetModule();
        }
    }

    public boolean reloadPickupState(boolean logSummary) {
        reloadRootConfiguration();
        shutdownPickupModule();
        ValidationResult passwordValidation = refreshModulePasswordValidation(ModuleKey.PICKUP);

        if (!isModuleEnabled("pickup", false)) {
            pickupConfiguration = null;
            consoleInfo("Pickup 模块已在 config.yml 中关闭");
            return false;
        }

        if (passwordValidation != ValidationResult.VALID) {
            pickupConfiguration = null;
            logModulePasswordFailure(ModuleKey.PICKUP, passwordValidation);
            return false;
        }

        ensurePickupConfigExists();
        pickupConfiguration = loadPickupConfiguration();
        try {
            File uiFile = exportPickupUiFile(pickupConfiguration);
            UiBinding uiBinding = prepareUiBinding("Pickup", pickupConfiguration.uiId(), pickupConfiguration.registerUiOnEnable(), uiFile);
            if (uiBinding == null) {
                return false;
            }
            pickupRuntimeUiId = uiBinding.runtimeUiId();
            pickupRegisteredUiId = uiBinding.registeredUiId();
            pickupService = new PickupService(this, pickupConfiguration, packetBridge, itemStackBridge, pickupRuntimeUiId);
            pickupService.start();
            if (logSummary) {
                consoleInfo(
                    "Pickup 模块已载入，max-visible="
                        + pickupConfiguration.maxVisible()
                        + " | ttl="
                        + pickupConfiguration.entryTtlMs()
                        + "ms | UI="
                        + pickupRuntimeUiId
                );
            }
            return true;
        } catch (IOException exception) {
            consoleWarn("Pickup 模块启动失败: " + exception.getMessage());
            shutdownPickupModule();
            return false;
        }
    }

    public boolean reloadPropState(boolean logSummary) {
        reloadRootConfiguration();
        shutdownPropModule();
        ValidationResult passwordValidation = refreshModulePasswordValidation(ModuleKey.PROP);

        if (!isModuleEnabled("prop", false)) {
            propConfiguration = null;
            consoleInfo("Prop 模块已在 config.yml 中关闭");
            return false;
        }

        if (passwordValidation != ValidationResult.VALID) {
            propConfiguration = null;
            logModulePasswordFailure(ModuleKey.PROP, passwordValidation);
            return false;
        }

        if (propBridge == null || !propBridge.isAvailable()) {
            propConfiguration = null;
            consoleWarn("Prop 模块需要 ArcartX API 桥接，当前已跳过加载");
            return false;
        }

        ensurePropConfigExists();
        propConfiguration = loadPropConfiguration();
        try {
            ensurePropDataDefaultsExist();
            PropKeyMappingConfiguration keyMappingConfiguration = loadPropKeyMappingConfiguration();
            PropLanguageConfiguration languageConfiguration = loadPropLanguageConfiguration();
            Map<String, PropDefinition> definitionsById = loadPropDefinitions();
            propService = new PropService(
                this,
                propConfiguration,
                propBridge,
                keyMappingConfiguration,
                languageConfiguration,
                definitionsById
            );
            propService.start();
            if (logSummary) {
                consoleInfo(
                    "Prop 模块已载入，props="
                        + propService.propCount()
                        + " | keys="
                        + propService.registeredKeyCount()
                        + " | category="
                        + propService.keyCategory()
                        + " | mythiclib="
                        + propConfiguration.mythicLib().enabled()
                        + "/"
                        + propService.mythicLibHooked()
                        + " | debug="
                        + propConfiguration.debug()
                );
            }
            return true;
        } catch (IOException exception) {
            consoleWarn("Prop 模块启动失败: " + exception.getMessage());
            shutdownPropModule();
            return false;
        }
    }

    public boolean reloadDigisDisplayState(boolean logSummary) {
        reloadDigisDisplayStateInternal(logSummary);
        return digisDisplayService != null;
    }

    private void reloadDigisDisplayStateInternal(boolean logSummary) {
        shutdownDigisDisplayModule();

        if (clientBridge == null || !clientBridge.isAvailable()) {
            digisDisplayConfiguration = null;
            if (logSummary) {
                consoleWarn("DigisDisplay(→KillEffect) 需要客户端桥接，当前已跳过");
            }
            return;
        }

        ensureDigisDisplayConfigExists();
        digisDisplayConfiguration = loadDigisDisplayConfiguration();
        digisDisplayService = new DigisDisplayService(this, digisDisplayConfiguration, clientBridge);
        digisDisplayService.start();
        if (logSummary) {
            consoleInfo(
                "DigisDisplay 模块已载入，damage="
                    + digisDisplayConfiguration.damageEnabled()
                    + " | player="
                    + digisDisplayConfiguration.playerDamageEnabled()
                    + " | source-mode="
                    + digisDisplayConfiguration.damageSourceMode()
                    + " | active-source="
                    + digisDisplayService.activeDamageSource()
                    + " | heal="
                    + digisDisplayConfiguration.healEnabled()
                    + " | mythic="
                    + digisDisplayConfiguration.mythicHealEnabled()
                    + " | ml-hook="
                    + digisDisplayService.mythicLibDamageHooked()
                    + " | crane-hook="
                    + digisDisplayService.craneAttributeHooked()
                    + " | ap-hook="
                    + digisDisplayService.attributePlusHooked()
            );
        }
    }

    public boolean reloadRgbState(boolean logSummary) {
        reloadRootConfiguration();
        shutdownRgbModule();
        registerPlaceholderExpansionIfAvailable();
        ValidationResult passwordValidation = refreshModulePasswordValidation(ModuleKey.RGB);

        if (!isModuleEnabled("rgb", false)) {
            rgbConfiguration = null;
            consoleInfo("ArcartRGB 模块已在 config.yml 中关闭");
            return false;
        }

        if (passwordValidation != ValidationResult.VALID) {
            rgbConfiguration = null;
            logModulePasswordFailure(ModuleKey.RGB, passwordValidation);
            return false;
        }

        if (!hasPlugin("PlaceholderAPI")) {
            rgbConfiguration = null;
            consoleWarn("ArcartRGB 模块需要 PlaceholderAPI，当前已跳过加载");
            return false;
        }

        ensureRgbConfigExists();
        rgbConfiguration = loadRgbConfiguration();
        rgbService = new ArcartRgbService(rgbConfiguration, getLogger());
        if (rgbShimmerBridge == null) {
            rgbShimmerBridge = new ArcartRgbShimmerBridge(this);
        }
        boolean shimmerRegistered = rgbShimmerBridge.register(rgbConfiguration);
        if (logSummary) {
            consoleInfo(
                "ArcartRGB 模块已载入，条目数: "
                    + rgbService.activeEntryCount()
                    + "/"
                    + rgbService.entryCount()
                    + " | PAPI: "
                    + (registeredPlaceholderIds.contains("%arcartrgb_%") ? "已注册" : "待注册")
                    + " | Shimmer: "
                    + (shimmerRegistered ? "已注册" : "未注册")
            );
        }
        return true;
    }

    public String renderArcartRgbShimmer(Object invocationData) {
        if (rgbShimmerBridge == null) {
            return ArcartRgbShimmerBridge.fallbackText(invocationData);
        }
        return rgbShimmerBridge.render(invocationData);
    }

    // reloadPacketCommandState removed — PacketCommand is now integrated into EventPacket

    public boolean reloadLoginViewState(boolean logSummary) {
        reloadRootConfiguration();
        shutdownLoginViewModule();
        ValidationResult passwordValidation = refreshModulePasswordValidation(ModuleKey.LOGIN_VIEW);

        if (!isModuleEnabled("loginview", false)) {
            loginViewConfiguration = null;
            loginViewRuntimeUiId = null;
            consoleInfo("LoginView 模块已在 config.yml 中关闭");
            return false;
        }

        if (passwordValidation != ValidationResult.VALID) {
            loginViewConfiguration = null;
            loginViewRuntimeUiId = null;
            logModulePasswordFailure(ModuleKey.LOGIN_VIEW, passwordValidation);
            return false;
        }

        ensureLoginViewConfigExists();
        loginViewConfiguration = loadLoginViewConfiguration();
        try {
            File uiFile = exportBundledUiResource(
                LoginViewService.UI_RESOURCE_PATH,
                LoginViewService.UI_FILE_PATH,
                loginViewConfiguration.ui().overwriteUiFiles()
            );
            UiBinding uiBinding = prepareUiBinding(
                "LoginView",
                loginViewConfiguration.ui().uiId(),
                loginViewConfiguration.ui().registerUiOnEnable(),
                uiFile
            );
            if (uiBinding == null) {
                return false;
            }
            loginViewRuntimeUiId = uiBinding.runtimeUiId();
            loginViewRegisteredUiId = uiBinding.registeredUiId();
            loginViewService = new LoginViewService(
                this,
                loginViewConfiguration,
                new JdbcLoginViewRepository(getDataFolder(), loginViewConfiguration.storage(), getLogger()),
                packetBridge,
                loginViewRuntimeUiId
            );
            loginViewService.start();
            if (logSummary) {
                consoleInfo(
                    "LoginView 模块已载入，mode="
                        + loginViewConfiguration.authMode().configKey()
                        + " | ui="
                        + loginViewRuntimeUiId
                        + " | packet-id="
                        + loginViewConfiguration.ui().packetId()
                        + " | accounts="
                        + loginViewService.accountCount()
                );
            }
            return true;
        } catch (Exception exception) {
            consoleWarn("LoginView 模块启动失败: " + exception.getMessage());
            shutdownLoginViewModule();
            return false;
        }
    }

    public boolean reloadOnlineRewardsState(boolean logSummary) {
        reloadRootConfiguration();
        shutdownOnlineRewardsModule();
        ValidationResult passwordValidation = refreshModulePasswordValidation(ModuleKey.ONLINE_REWARDS);

        if (!isModuleEnabled("onlinerewards", false)) {
            onlineRewardsConfiguration = null;
            consoleInfo("OnlineRewards 模块已在 config.yml 中关闭");
            return false;
        }

        if (passwordValidation != ValidationResult.VALID) {
            onlineRewardsConfiguration = null;
            logModulePasswordFailure(ModuleKey.ONLINE_REWARDS, passwordValidation);
            return false;
        }

        if (clientBridge == null || !clientBridge.isAvailable()) {
            onlineRewardsConfiguration = null;
            consoleWarn("OnlineRewards 模块需要 ArcartX 客户端桥接，当前已跳过加载");
            return false;
        }

        ensureOnlineRewardsConfigExists();
        onlineRewardsConfiguration = loadOnlineRewardsConfiguration();
        try {
            onlineRewardsService = new OnlineRewardsService(
                this,
                onlineRewardsConfiguration,
                new JdbcOnlineRewardsRepository(getDataFolder(), onlineRewardsConfiguration.storage(), getLogger()),
                clientBridge,
                packetBridge
            );
            onlineRewardsService.start();
            if (logSummary) {
                consoleInfo(
                    "OnlineRewards 模块已载入，rewards="
                        + onlineRewardsConfiguration.rewards().size()
                        + " | storage="
                        + onlineRewardsConfiguration.storage().dialect().configKey()
                        + " | menu-ui="
                        + onlineRewardsService.runtimeMenuUiId()
                        + " | variables="
                        + onlineRewardsConfiguration.progressVariableName()
                        + ", "
                        + onlineRewardsConfiguration.titleVariableName()
                );
            }
            return true;
        } catch (Exception exception) {
            consoleWarn("OnlineRewards 模块启动失败: " + exception.getMessage());
            shutdownOnlineRewardsModule();
            return false;
        }
    }

    public boolean reloadWarehouseState(boolean logSummary) {
        reloadRootConfiguration();
        shutdownWarehouseModule();
        ValidationResult passwordValidation = refreshModulePasswordValidation(ModuleKey.WAREHOUSE);

        if (!isModuleEnabled("warehouse", false)) {
            warehouseConfiguration = null;
            consoleInfo("Warehouse 模块已在 config.yml 中关闭");
            return false;
        }

        if (passwordValidation != ValidationResult.VALID) {
            warehouseConfiguration = null;
            logModulePasswordFailure(ModuleKey.WAREHOUSE, passwordValidation);
            return false;
        }

        ensureWarehouseConfigExists();
        warehouseConfiguration = loadWarehouseConfiguration();
        try {
            warehouseService = new WarehouseService(
                this,
                warehouseConfiguration,
                new JdbcWarehouseRepository(getDataFolder(), warehouseConfiguration.storage(), getLogger())
            );
            warehouseService.start();
            if (logSummary) {
                consoleInfo(
                    "Warehouse 模块已载入，仓库定义="
                        + warehouseConfiguration.warehouses().size()
                        + " | 分类="
                        + warehouseConfiguration.categories().size()
                        + " | 银行货币="
                        + warehouseConfiguration.currencies().size()
                        + " | 存储="
                        + warehouseConfiguration.storage().dialect().configKey()
                );
            }
            return true;
        } catch (Exception exception) {
            consoleWarn("Warehouse 模块启动失败: " + exception.getMessage());
            shutdownWarehouseModule();
            return false;
        }
    }

    public boolean reloadMailState(boolean logSummary) {
        reloadRootConfiguration();
        shutdownMailModule();
        registerPlaceholderExpansionIfAvailable();
        ValidationResult passwordValidation = refreshModulePasswordValidation(ModuleKey.MAIL);

        if (!isModuleEnabled("mail", false)) {
            mailConfiguration = null;
            consoleInfo("Mail 模块已在 config.yml 中关闭");
            return false;
        }

        if (passwordValidation != ValidationResult.VALID) {
            mailConfiguration = null;
            logModulePasswordFailure(ModuleKey.MAIL, passwordValidation);
            return false;
        }

        ensureMailConfigExists();
        mailConfiguration = loadMailConfiguration();
        if (vaultEconomyBridge == null) {
            vaultEconomyBridge = new VaultEconomyBridge(this);
            vaultEconomyBridge.initialize();
        }

        try {
            mailService = new MailService(
                this,
                mailConfiguration,
                new JdbcMailRepository(getDataFolder(), mailConfiguration.storage(), getLogger()),
                packetBridge,
                vaultEconomyBridge
            );
            mailService.start();
            if (logSummary) {
                consoleInfo(
                    "Mail 模块已载入，预设="
                        + mailService.presetCount()
                        + " | 存储="
                        + mailConfiguration.storage().dialect().configKey()
                        + " | InboxUI="
                        + mailService.inboxUiId()
                        + " | Redis="
                        + mailService.redisActive()
                        + " | Vault="
                        + isVaultEconomyAvailable()
                );
            }
            return true;
        } catch (Exception exception) {
            consoleWarn("Mail 模块启动失败: " + exception.getMessage());
            shutdownMailModule();
            return false;
        }
    }

    public boolean reloadChatState(boolean logSummary) {
        reloadRootConfiguration();
        shutdownChatModule();
        registerPlaceholderExpansionIfAvailable();
        ValidationResult passwordValidation = refreshModulePasswordValidation(ModuleKey.CHAT);

        if (!isModuleEnabled("chat", false)) {
            chatConfiguration = null;
            consoleInfo("Chat 模块已在 config.yml 中关闭");
            return false;
        }

        if (passwordValidation != ValidationResult.VALID) {
            chatConfiguration = null;
            logModulePasswordFailure(ModuleKey.CHAT, passwordValidation);
            return false;
        }

        ensureChatConfigExists();
        chatConfiguration = loadChatConfiguration();
        try {
            chatService = new ChatService(
                this,
                chatConfiguration,
                new JdbcChatRepository(getDataFolder(), chatConfiguration.storage(), getLogger()),
                packetBridge,
                itemStackBridge
            );
            chatService.start();
            if (logSummary) {
                consoleInfo(
                    "Chat 模块已载入，频道="
                        + chatService.channelCount()
                        + " | 存储="
                        + chatConfiguration.storage().dialect().configKey()
                        + " | Transport="
                        + chatService.transportName()
                );
            }
            return true;
        } catch (Exception exception) {
            consoleWarn("Chat 模块启动失败: " + exception.getMessage());
            shutdownChatModule();
            return false;
        }
    }

    public boolean reloadQuestGpsState(boolean logSummary) {
        reloadRootConfiguration();
        shutdownQuestGpsModule();
        ValidationResult passwordValidation = refreshModulePasswordValidation(ModuleKey.QUEST_GPS);

        if (!isModuleEnabled("questgps", false)) {
            questGpsConfiguration = null;
            questGpsRuntimeUiId = null;
            questGpsHudRuntimeUiId = null;
            consoleInfo("QuestGPS 模块已在 config.yml 中关闭");
            return false;
        }

        if (passwordValidation != ValidationResult.VALID) {
            questGpsConfiguration = null;
            questGpsRuntimeUiId = null;
            questGpsHudRuntimeUiId = null;
            logModulePasswordFailure(ModuleKey.QUEST_GPS, passwordValidation);
            return false;
        }

        if (!hasPlugin("Chemdah")) {
            questGpsConfiguration = null;
            questGpsRuntimeUiId = null;
            questGpsHudRuntimeUiId = null;
            consoleWarn("QuestGPS 模块需要 Chemdah，当前已跳过加载");
            return false;
        }

        ensureQuestGpsConfigExists();
        questGpsConfiguration = loadQuestGpsConfiguration();
        try {
            File menuUiFile = exportBundledUiResource(
                QuestGpsService.MENU_UI_RESOURCE_PATH,
                QuestGpsService.MENU_UI_FILE_PATH,
                questGpsConfiguration.client().overwriteUiFiles()
            );
            File hudUiFile = exportBundledUiResource(
                QuestGpsService.HUD_UI_RESOURCE_PATH,
                QuestGpsService.HUD_UI_FILE_PATH,
                questGpsConfiguration.client().overwriteUiFiles()
            );
            UiBinding menuUiBinding = prepareUiBinding(
                "QuestGPS Menu",
                questGpsConfiguration.client().menuUiId(),
                questGpsConfiguration.client().registerUiOnEnable(),
                menuUiFile
            );
            UiBinding hudUiBinding = prepareUiBinding(
                "QuestGPS HUD",
                questGpsConfiguration.client().hudUiId(),
                questGpsConfiguration.client().registerUiOnEnable(),
                hudUiFile
            );
            if (menuUiBinding == null || hudUiBinding == null) {
                return false;
            }
            questGpsRuntimeUiId = menuUiBinding.runtimeUiId();
            questGpsRegisteredUiId = menuUiBinding.registeredUiId();
            questGpsHudRuntimeUiId = hudUiBinding.runtimeUiId();
            questGpsHudRegisteredUiId = hudUiBinding.registeredUiId();
            questGpsService = new QuestGpsService(
                this,
                questGpsConfiguration,
                packetBridge,
                itemStackBridge,
                questGpsRuntimeUiId,
                questGpsHudRuntimeUiId
            );
            questGpsService.start();
            if (logSummary) {
                consoleInfo(
                    "QuestGPS 模块已载入，packet-id="
                        + questGpsConfiguration.client().packetId()
                        + " | menu-ui="
                        + questGpsRuntimeUiId
                        + " | hud-ui="
                        + questGpsHudRuntimeUiId
                        + " | navigation="
                        + questGpsConfiguration.navigation().enabled()
                        + " | quests="
                        + questGpsConfiguration.configuredQuestCount()
                );
            }
            return true;
        } catch (IOException exception) {
            consoleWarn("QuestGPS 模块启动失败: " + exception.getMessage());
            shutdownQuestGpsModule();
            return false;
        }
    }

    public boolean reloadMapState(boolean logSummary) {
        reloadRootConfiguration();
        shutdownMapModule();
        ValidationResult passwordValidation = refreshModulePasswordValidation(ModuleKey.MAP);

        if (!isModuleEnabled("map", false)) {
            mapConfiguration = null;
            mapMenuRuntimeUiId = null;
            mapHudRuntimeUiId = null;
            consoleInfo("Map 模块已在 config.yml 中关闭");
            return false;
        }

        if (passwordValidation != ValidationResult.VALID) {
            mapConfiguration = null;
            mapMenuRuntimeUiId = null;
            mapHudRuntimeUiId = null;
            logModulePasswordFailure(ModuleKey.MAP, passwordValidation);
            return false;
        }

        ensureMapConfigExists();
        mapConfiguration = loadMapConfiguration();
        try {
            File menuUiFile = exportBundledUiResource(
                MapService.MENU_UI_RESOURCE_PATH,
                MapService.MENU_UI_FILE_PATH,
                mapConfiguration.client().overwriteUiFiles()
            );
            File hudUiFile = exportBundledUiResource(
                MapService.HUD_UI_RESOURCE_PATH,
                MapService.HUD_UI_FILE_PATH,
                mapConfiguration.client().overwriteUiFiles()
            );
            UiBinding menuUiBinding = prepareUiBinding(
                "Map Menu",
                mapConfiguration.client().menuUiId(),
                mapConfiguration.client().registerUiOnEnable(),
                menuUiFile
            );
            UiBinding hudUiBinding = prepareUiBinding(
                "Map HUD",
                mapConfiguration.client().hudUiId(),
                mapConfiguration.client().registerUiOnEnable(),
                hudUiFile
            );
            if (menuUiBinding == null || hudUiBinding == null) {
                shutdownMapModule();
                return false;
            }
            mapMenuRuntimeUiId = menuUiBinding.runtimeUiId();
            mapMenuRegisteredUiId = menuUiBinding.registeredUiId();
            mapHudRuntimeUiId = hudUiBinding.runtimeUiId();
            mapHudRegisteredUiId = hudUiBinding.registeredUiId();
            mapService = new MapService(
                this,
                mapConfiguration,
                new JdbcMapRepository(getDataFolder(), mapConfiguration.storage(), getLogger()),
                packetBridge,
                mapMenuRuntimeUiId,
                mapHudRuntimeUiId
            );
            mapService.start();
            if (logSummary) {
                consoleInfo(
                    "Map 模块已载入，packet-id="
                        + mapConfiguration.client().packetId()
                        + " | menu-ui="
                        + mapMenuRuntimeUiId
                        + " | hud-ui="
                        + mapHudRuntimeUiId
                        + " | worlds="
                        + mapConfiguration.worlds().size()
                        + " | anchors="
                        + mapConfiguration.anchors().size()
                );
            }
            return true;
        } catch (Exception exception) {
            consoleWarn("Map 模块启动失败: " + exception.getMessage());
            shutdownMapModule();
            return false;
        }
    }

    public void dispatchKillPackets(Player killer, LivingEntity victim, String deathMessage) {
        dispatchKillEffectPackets(PacketTrigger.KILL, CombatPacketContext.fromKill(killer, victim, deathMessage));
    }

    public void dispatchEventPackets(EventPacketTrigger trigger, Player subject) {
        dispatchEventPackets(trigger, subject, EventPacketContext.fromSubjectTrigger(trigger, subject));
    }

    public void dispatchEventPacketSignal(String signal, Player subject, Map<String, String> variables) {
        if (signal == null || signal.isBlank()) {
            return;
        }
        dispatchEventPackets(
            EventPacketTrigger.COMMAND_SIGNAL,
            subject,
            EventPacketContext.fromSignal(subject, signal, variables == null ? Map.of() : variables)
        );
    }

    public void clearEventPacketPlayerState(Player player) {
        if (player == null || eventPacketWatcherService == null) {
            return;
        }
        eventPacketWatcherService.clearPlayer(player.getUniqueId());
    }

    public void recordEventPacketMobKill(Player player, String worldName, String entityType, String mythicMobId) {
        if (eventPacketWatcherService == null) {
            return;
        }
        eventPacketWatcherService.recordMobKill(player, worldName, entityType, mythicMobId);
    }

    public void dispatchEventPacketRule(
        EventPacketRule rule,
        EventPacketTrigger trigger,
        Player subject,
        EventPacketContext context
    ) {
        if (eventPacketConfiguration == null || rule == null || context == null || !rule.enabled() || rule.trigger() != trigger) {
            return;
        }
        if (trigger == EventPacketTrigger.COMMAND_SIGNAL && !rule.signal().isBlank()) {
            Object renderedSignal = context.renderPayload("{signal}", EventPacketRecipient.SELF, subject);
            if (!rule.signal().equalsIgnoreCase(String.valueOf(renderedSignal))) {
                return;
            }
        }
        if (questGpsService != null && questGpsService.eventRuleLocked(subject, rule.id())) {
            return;
        }
        String playerId = subject == null ? "console" : subject.getUniqueId().toString();
        String stateKey = rule.id() + "|" + playerId;
        long now = System.currentTimeMillis();
        if (!rule.repeatable() && firedEventPacketRules.contains(stateKey)) {
            return;
        }
        Long nextAllowedAt = eventPacketCooldowns.get(stateKey);
        if (nextAllowedAt != null && nextAllowedAt > now) {
            return;
        }

        boolean anySuccess = false;
        for (EventPacketAction action : rule.actions()) {
            anySuccess |= executeEventPacketAction(rule, action, subject, context);
        }
        if (anySuccess || !rule.actions().isEmpty()) {
            firedEventPacketRules.add(stateKey);
            if (rule.cooldownMillis() > 0L) {
                eventPacketCooldowns.put(stateKey, now + rule.cooldownMillis());
            }
        }
    }

    private boolean executeEventPacketAction(
        EventPacketRule rule,
        EventPacketAction action,
        Player subject,
        EventPacketContext context
    ) {
        if (action == null || action.type() == null || action.type().isBlank()) {
            return false;
        }
        try {
            String actionType = action.type().trim().toLowerCase(Locale.ROOT);
            boolean success = switch (actionType) {
                case "questgps.offer" -> executeQuestGpsOfferAction(action, subject, context);
                case "questgps.accept" -> executeQuestGpsAcceptAction(action, subject, context);
                case "questgps.open" -> executeQuestGpsOpenAction(subject);
                case "questgps.track" -> executeQuestGpsTrackAction(action, subject, context);
                case "subtitle.play" -> executeSubtitlePlayAction(action, subject, context);
                case "chat.card" -> executeChatCardAction(action, subject, context);
                case "title.give" -> executeTitleGiveAction(action, subject, context);
                case "command.dispatch" -> executeCommandDispatchAction(action, subject, context);
                case "ui-packet" -> executeUiPacketAction(rule, action, subject, context);
                case "mail.send" -> executeMailSendAction(action, subject, context);
                case "announcer.play" -> executeAnnouncerPlayAction(action, subject, context);
                case "combateffect.play" -> executeCombatEffectPlayAction(action, subject, context);
                default -> {
                    getLogger().warning("EventPacket 规则动作类型未知，已跳过: " + rule.id() + " -> " + action.type());
                    yield false;
                }
            };
            if (eventPacketConfiguration != null && eventPacketConfiguration.debug()) {
                getLogger().info(
                    "EventPacket 动作执行 -> rule="
                        + rule.id()
                        + " | type="
                        + action.type()
                        + " | player="
                        + (subject == null ? "console" : subject.getName())
                        + " | success="
                        + success
                );
            }
            return success;
        } catch (RuntimeException exception) {
            getLogger().warning(
                "EventPacket 规则动作执行失败: "
                    + rule.id()
                    + " -> "
                    + action.type()
                    + " | "
                    + exception.getMessage()
            );
            return false;
        }
    }

    private boolean executeQuestGpsOfferAction(EventPacketAction action, Player subject, EventPacketContext context) {
        if (questGpsService == null || subject == null || !subject.isOnline()) {
            return false;
        }
        String questId = renderEventPacketString(action.object("quest-id"), context, subject);
        if (questId.isBlank()) {
            questId = renderEventPacketString(action.object("questId"), context, subject);
        }
        if (questId.isBlank()) {
            return false;
        }
        questGpsService.offerQuest(subject, questId, action.bool("open-menu", true));
        return true;
    }

    private boolean executeQuestGpsAcceptAction(EventPacketAction action, Player subject, EventPacketContext context) {
        if (questGpsService == null || subject == null || !subject.isOnline()) {
            return false;
        }
        String questId = renderEventPacketString(action.object("quest-id"), context, subject);
        if (questId.isBlank()) {
            questId = renderEventPacketString(action.object("questId"), context, subject);
        }
        if (questId.isBlank()) {
            return false;
        }
        questGpsService.acceptQuest(subject, questId);
        return true;
    }

    private boolean executeQuestGpsOpenAction(Player subject) {
        if (questGpsService == null || subject == null || !subject.isOnline()) {
            return false;
        }
        questGpsService.openMenu(subject);
        return true;
    }

    private boolean executeQuestGpsTrackAction(EventPacketAction action, Player subject, EventPacketContext context) {
        if (questGpsService == null || subject == null || !subject.isOnline()) {
            return false;
        }
        String questId = renderEventPacketString(action.object("quest-id"), context, subject);
        if (questId.isBlank()) {
            questId = renderEventPacketString(action.object("questId"), context, subject);
        }
        if (questId.isBlank()) {
            return false;
        }
        String taskId = renderEventPacketString(action.object("task-id"), context, subject);
        if (taskId.isBlank()) {
            taskId = renderEventPacketString(action.object("taskId"), context, subject);
        }
        if (taskId.isBlank()) {
            questGpsService.trackQuest(subject, questId);
        } else {
            questGpsService.trackTask(subject, questId, taskId);
        }
        return true;
    }

    private boolean executeSubtitlePlayAction(EventPacketAction action, Player subject, EventPacketContext context) {
        if (subject == null || !subject.isOnline()) {
            return false;
        }
        String groupId = renderEventPacketString(action.object("group-id"), context, subject);
        if (groupId.isBlank()) {
            groupId = renderEventPacketString(action.object("subtitle-id"), context, subject);
        }
        if (groupId.isBlank()) {
            groupId = renderEventPacketString(action.object("id"), context, subject);
        }
        return !groupId.isBlank() && playSubtitleGroup(subject, groupId);
    }

    private boolean executeChatCardAction(EventPacketAction action, Player subject, EventPacketContext context) {
        if (packetBridge == null || subject == null || !subject.isOnline()) {
            return false;
        }
        String cardId = renderEventPacketString(action.object("card-id"), context, subject);
        if (cardId.isBlank()) {
            cardId = renderEventPacketString(action.object("id"), context, subject);
        }
        if (cardId.isBlank()) {
            return false;
        }
        return packetBridge.sendChatCard(subject, cardId, renderEventPacketStringMap(action.object("data"), context, subject));
    }

    private boolean executeTitleGiveAction(EventPacketAction action, Player subject, EventPacketContext context) {
        if (titleService == null || subject == null || !subject.isOnline()) {
            return false;
        }
        String titleId = renderEventPacketString(action.object("title-id"), context, subject);
        if (titleId.isBlank()) {
            titleId = renderEventPacketString(action.object("id"), context, subject);
        }
        if (titleId.isBlank()) {
            return false;
        }
        String duration = renderEventPacketString(action.object("duration"), context, subject);
        TitleDurationParser.TitleDurationSpec durationSpec = TitleDurationParser.parse(duration.isBlank() ? "permanent" : duration)
            .orElse(TitleDurationParser.TitleDurationSpec.ofPermanent());
        return titleService.giveTitle(subject.getUniqueId(), titleId, durationSpec, "EventPacket").success();
    }

    private boolean executeCommandDispatchAction(EventPacketAction action, Player subject, EventPacketContext context) {
        String command = renderEventPacketString(action.object("command"), context, subject);
        if (command.isBlank()) {
            return false;
        }
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        String executor = renderEventPacketString(action.object("executor"), context, subject);
        if ("op".equalsIgnoreCase(executor) && subject != null) {
            boolean wasOp = subject.isOp();
            try {
                if (!wasOp) {
                    subject.setOp(true);
                }
                return Bukkit.dispatchCommand(subject, command);
            } finally {
                if (!wasOp) {
                    subject.setOp(false);
                }
            }
        }
        CommandSender sender = "player".equalsIgnoreCase(executor) && subject != null
            ? subject
            : Bukkit.getConsoleSender();
        return Bukkit.dispatchCommand(sender, command);
    }

    private boolean executeUiPacketAction(
        EventPacketRule rule,
        EventPacketAction action,
        Player subject,
        EventPacketContext context
    ) {
        if (packetBridge == null || !packetBridge.isAvailable()) {
            return false;
        }
        String uiId = renderEventPacketString(action.object("ui-id"), context, subject);
        String handler = renderEventPacketString(action.object("packet-handler"), context, subject);
        if (handler.isBlank()) {
            handler = renderEventPacketString(action.object("handler"), context, subject);
        }
        if (uiId.isBlank() || handler.isBlank()) {
            getLogger().warning("EventPacket ui-packet 动作缺少 ui-id 或 packet-handler: " + rule.id());
            return false;
        }

        boolean anySuccess = false;
        for (EventPacketRecipient recipientType : resolveEventPacketRecipients(action.object("recipients"))) {
            for (Player recipient : recipientType.resolve(subject, Bukkit.getOnlinePlayers())) {
                if (!canDispatchEventPacketTo(recipient, rule.trigger(), subject)) {
                    continue;
                }
                Object payload = context.renderPayload(action.object("pack"), recipientType, recipient);
                anySuccess |= packetBridge.sendPacket(recipient, uiId, handler, payload);
            }
        }
        return anySuccess;
    }

    private List<EventPacketRecipient> resolveEventPacketRecipients(Object rawRecipients) {
        List<EventPacketRecipient> recipients = new ArrayList<>();
        if (rawRecipients instanceof Iterable<?> iterable) {
            for (Object rawRecipient : iterable) {
                EventPacketRecipient recipient = EventPacketRecipient.parse(String.valueOf(rawRecipient));
                if (recipient != null && !recipients.contains(recipient)) {
                    recipients.add(recipient);
                }
            }
        } else if (rawRecipients != null) {
            String[] parts = String.valueOf(rawRecipients).split(",");
            for (String part : parts) {
                EventPacketRecipient recipient = EventPacketRecipient.parse(part);
                if (recipient != null && !recipients.contains(recipient)) {
                    recipients.add(recipient);
                }
            }
        }
        if (recipients.isEmpty()) {
            recipients.add(EventPacketRecipient.SELF);
        }
        return List.copyOf(recipients);
    }

    private String renderEventPacketString(Object rawValue, EventPacketContext context, Player subject) {
        if (rawValue == null || context == null || subject == null) {
            return "";
        }
        Object rendered = context.renderPayload(rawValue, EventPacketRecipient.SELF, subject);
        return rendered == null ? "" : String.valueOf(rendered).trim();
    }

    private Map<String, String> renderEventPacketStringMap(Object rawValue, EventPacketContext context, Player subject) {
        if (rawValue == null || context == null || subject == null) {
            return Map.of();
        }
        Object rendered = context.renderPayload(rawValue, EventPacketRecipient.SELF, subject);
        if (!(rendered instanceof Map<?, ?> renderedMap)) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : renderedMap.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            result.put(String.valueOf(entry.getKey()), entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
        }
        return result;
    }

    private boolean executeMailSendAction(EventPacketAction action, Player subject, EventPacketContext context) {
        if (mailService == null || subject == null || !subject.isOnline()) {
            return false;
        }
        String presetId = renderEventPacketString(action.object("preset-id"), context, subject);
        if (presetId.isBlank()) {
            presetId = renderEventPacketString(action.object("presetId"), context, subject);
        }
        if (presetId.isBlank()) {
            return false;
        }
        return mailService.dispatchPreset(presetId, subject.getName(), "EventPacket").success();
    }

    private boolean executeAnnouncerPlayAction(EventPacketAction action, Player subject, EventPacketContext context) {
        if (subject == null || !subject.isOnline()) {
            return false;
        }
        String groupId = renderEventPacketString(action.object("group-id"), context, subject);
        if (groupId.isBlank()) {
            groupId = renderEventPacketString(action.object("id"), context, subject);
        }
        if (groupId.isBlank()) {
            return false;
        }
        return playSubtitleGroup(subject, groupId);
    }

    private boolean executeCombatEffectPlayAction(EventPacketAction action, Player subject, EventPacketContext context) {
        if (packetBridge == null || !packetBridge.isAvailable() || subject == null || !subject.isOnline()) {
            return false;
        }
        String uiId = renderEventPacketString(action.object("ui-id"), context, subject);
        if (uiId.isBlank()) {
            uiId = getCombatEffectUiId();
        }
        if (uiId.isBlank()) {
            return false;
        }
        Object pack = context.renderPayload(action.object("pack"), EventPacketRecipient.SELF, subject);
        String handler = renderEventPacketString(action.object("packet-handler"), context, subject);
        if (handler.isBlank()) {
            handler = "play";
        }
        return packetBridge.sendPacket(subject, uiId, handler, pack);
    }

    private String getCombatEffectUiId() {
        if (killEffectConfiguration == null || killEffectConfiguration.packetDefinitions().isEmpty()) {
            return "";
        }
        return killEffectConfiguration.packetDefinitions().get(0).uiId();
    }

    public boolean handleTabClientRefreshPacket(Player player, String packetId, List<String> data) {
        return tabSyncService != null && tabSyncService.handleClientRefreshPacket(player, packetId, data);
    }

    public boolean handleAnnouncerClientPacket(Player player, String packetId, List<String> data) {
        return announcerService != null && announcerService.handleClientPacket(player, packetId, data);
    }

    public void dispatchAttackPackets(Player attacker, LivingEntity target, org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        dispatchKillEffectPackets(PacketTrigger.ATTACK, CombatPacketContext.fromAttack(attacker, target, event));
    }

    private void dispatchKillEffectPackets(PacketTrigger trigger, CombatPacketContext context) {
        if (killEffectConfiguration == null || packetBridge == null || !packetBridge.isAvailable()) {
            return;
        }
        if (!killEffectConfiguration.shouldProcessTarget(
            context.target() instanceof Player,
            context.targetMythicMobId(),
            context.targetEntityType()
        )) {
            if (killEffectConfiguration.debug()) {
                getLogger().info(
                    "CombatEffect 已跳过目标 -> trigger="
                        + trigger.configValue()
                        + " | target="
                        + EntityCombatMetadata.resolveDisplayName(context.target(), context.targetMythicMobId())
                        + " | mythicMobId="
                        + context.targetMythicMobId()
                        + " | entityType="
                        + context.targetEntityType()
                );
            }
            return;
        }

        for (PacketDefinition definition : killEffectConfiguration.packetDefinitions()) {
            if (!definition.enabled() || !definition.triggers().contains(trigger)) {
                continue;
            }

            for (PacketRecipient recipientType : definition.recipients()) {
                Player recipient = recipientType.resolve(context.attacker(), context.target());
                if (recipient == null || !recipient.isOnline()) {
                    continue;
                }

                Object payload = context.renderPayload(definition.packTemplate(), recipientType, recipient);
                boolean success = packetBridge.sendPacket(
                    recipient,
                    definition.uiId(),
                    definition.packetHandler(),
                    payload
                );

                if (killEffectConfiguration.debug()) {
                    getLogger().info(
                        "发包[" + definition.id() + "] -> "
                            + recipient.getName()
                            + " | ui="
                            + definition.uiId()
                            + " | handler="
                            + definition.packetHandler()
                            + " | trigger="
                            + trigger.configValue()
                            + " | success="
                            + success
                            + " | payload="
                            + payload
                    );
                }
            }
        }
    }

    private void dispatchEventPackets(EventPacketTrigger trigger, Player subject, EventPacketContext context) {
        if (eventPacketConfiguration == null || context == null) {
            return;
        }

        for (EventPacketRule rule : eventPacketConfiguration.rules()) {
            dispatchEventPacketRule(rule, trigger, subject, context);
        }
    }

    private boolean canDispatchEventPacketTo(Player recipient, EventPacketTrigger trigger, Player subject) {
        if (recipient == null) {
            return false;
        }
        if (recipient.isOnline()) {
            return true;
        }
        return trigger == EventPacketTrigger.QUIT
            && subject != null
            && recipient.getUniqueId().equals(subject.getUniqueId());
    }

    private void registerCommands() {
        PluginCommand packetCommand = Objects.requireNonNull(
            getCommand("arcartxsuite"),
            "arcartxsuite command is missing"
        );
        ArcartXSuiteCommand packetExecutor = new ArcartXSuiteCommand(this);
        packetCommand.setExecutor(packetExecutor);
        packetCommand.setTabCompleter(packetExecutor);

        PluginCommand chatCommand = Objects.requireNonNull(
            getCommand("chat"),
            "chat command is missing"
        );
        ChatPlayerCommand chatExecutor = new ChatPlayerCommand(this, ChatPlayerCommand.CommandMode.CHAT);
        chatCommand.setExecutor(chatExecutor);
        chatCommand.setTabCompleter(chatExecutor);

        PluginCommand messageCommand = Objects.requireNonNull(
            getCommand("msg"),
            "msg command is missing"
        );
        ChatPlayerCommand messageExecutor = new ChatPlayerCommand(this, ChatPlayerCommand.CommandMode.MESSAGE);
        messageCommand.setExecutor(messageExecutor);
        messageCommand.setTabCompleter(messageExecutor);

        PluginCommand replyCommand = Objects.requireNonNull(
            getCommand("reply"),
            "reply command is missing"
        );
        ChatPlayerCommand replyExecutor = new ChatPlayerCommand(this, ChatPlayerCommand.CommandMode.REPLY);
        replyCommand.setExecutor(replyExecutor);
        replyCommand.setTabCompleter(replyExecutor);

        PluginCommand titleCommand = Objects.requireNonNull(
            getCommand("title"),
            "title command is missing"
        );
        TitlePlayerCommand titleExecutor = new TitlePlayerCommand(this);
        titleCommand.setExecutor(titleExecutor);
        titleCommand.setTabCompleter(titleExecutor);

        PluginCommand warehouseCommand = Objects.requireNonNull(
            getCommand("warehouse"),
            "warehouse command is missing"
        );
        WarehousePlayerCommand warehouseExecutor = new WarehousePlayerCommand(this);
        warehouseCommand.setExecutor(warehouseExecutor);
        warehouseCommand.setTabCompleter(warehouseExecutor);

        PluginCommand mailCommand = Objects.requireNonNull(
            getCommand("mail"),
            "mail command is missing"
        );
        MailPlayerCommand mailExecutor = new MailPlayerCommand(this);
        mailCommand.setExecutor(mailExecutor);
        mailCommand.setTabCompleter(mailExecutor);

        PluginCommand onlineRewardsCommand = Objects.requireNonNull(
            getCommand("onlinerewards"),
            "onlinerewards command is missing"
        );
        OnlineRewardsPlayerCommand onlineRewardsExecutor = new OnlineRewardsPlayerCommand(this);
        onlineRewardsCommand.setExecutor(onlineRewardsExecutor);
        onlineRewardsCommand.setTabCompleter(onlineRewardsExecutor);

        PluginCommand questGpsCommand = Objects.requireNonNull(
            getCommand("questgps"),
            "questgps command is missing"
        );
        QuestGpsPlayerCommand questGpsExecutor = new QuestGpsPlayerCommand(this);
        questGpsCommand.setExecutor(questGpsExecutor);
        questGpsCommand.setTabCompleter(questGpsExecutor);

        PluginCommand mapCommand = Objects.requireNonNull(
            getCommand("map"),
            "map command is missing"
        );
        MapPlayerCommand mapExecutor = new MapPlayerCommand(this);
        mapCommand.setExecutor(mapExecutor);
        mapCommand.setTabCompleter(mapExecutor);
    }

    @SuppressWarnings("unchecked")
    private void registerClientCustomPacketListener() {
        unregisterClientCustomPacketListener();

        Plugin arcartX = Bukkit.getPluginManager().getPlugin("ArcartX");
        if (arcartX == null) {
            return;
        }

        try {
            ClassLoader classLoader = arcartX.getClass().getClassLoader();
            Class<?> rawEventClass = Class.forName(
                "priv.seventeen.artist.arcartx.event.client.ClientCustomPacketEvent",
                true,
                classLoader
            );
            if (!Event.class.isAssignableFrom(rawEventClass)) {
                getLogger().warning("ArcartX ClientCustomPacketEvent 不是 Bukkit Event，已跳过 Tab 回包监听。");
                return;
            }

            Class<? extends Event> eventClass = (Class<? extends Event>) rawEventClass;
            var getPlayerMethod = rawEventClass.getMethod("getPlayer");
            var getIdMethod = rawEventClass.getMethod("getId");
            var getDataMethod = rawEventClass.getMethod("getData");

            clientCustomPacketListener = new Listener() {
            };
            getServer().getPluginManager().registerEvent(
                eventClass,
                clientCustomPacketListener,
                EventPriority.MONITOR,
                (listener, event) -> handleClientCustomPacketEvent(
                    event,
                    rawEventClass,
                    getPlayerMethod,
                    getIdMethod,
                    getDataMethod
                ),
                this,
                true
            );
        } catch (ReflectiveOperationException exception) {
            getLogger().warning("注册 ArcartX 客户端自定义包监听失败: " + exception.getMessage());
        }
    }

    private void unregisterClientCustomPacketListener() {
        if (clientCustomPacketListener == null) {
            return;
        }
        HandlerList.unregisterAll(clientCustomPacketListener);
        clientCustomPacketListener = null;
    }

    @SuppressWarnings("unchecked")
    private void registerClientInitializedListener() {
        unregisterClientInitializedListener();

        Plugin arcartX = Bukkit.getPluginManager().getPlugin("ArcartX");
        if (arcartX == null) {
            return;
        }

        try {
            ClassLoader classLoader = arcartX.getClass().getClassLoader();
            Class<?> rawEventClass = Class.forName(
                "priv.seventeen.artist.arcartx.event.client.ClientInitializedEvent$End",
                true,
                classLoader
            );
            if (!Event.class.isAssignableFrom(rawEventClass)) {
                getLogger().warning("ArcartX ClientInitializedEvent$End 不是 Bukkit Event，已跳过公告初始化监听。");
                return;
            }

            Class<? extends Event> eventClass = (Class<? extends Event>) rawEventClass;
            var getPlayerMethod = rawEventClass.getMethod("getPlayer");
            clientInitializedListener = new Listener() {
            };
            getServer().getPluginManager().registerEvent(
                eventClass,
                clientInitializedListener,
                EventPriority.MONITOR,
                (listener, event) -> handleClientInitializedEvent(event, rawEventClass, getPlayerMethod),
                this,
                true
            );
        } catch (ReflectiveOperationException exception) {
            getLogger().warning("注册 ArcartX 客户端初始化监听失败: " + exception.getMessage());
        }
    }

    private void unregisterClientInitializedListener() {
        if (clientInitializedListener == null) {
            return;
        }
        HandlerList.unregisterAll(clientInitializedListener);
        clientInitializedListener = null;
    }

    @SuppressWarnings("unchecked")
    private void handleClientCustomPacketEvent(
        Event event,
        Class<?> clientCustomPacketEventClass,
        java.lang.reflect.Method getPlayerMethod,
        java.lang.reflect.Method getIdMethod,
        java.lang.reflect.Method getDataMethod
    ) {
        if (!clientCustomPacketEventClass.isInstance(event)) {
            return;
        }

        try {
            Object rawPlayer = getPlayerMethod.invoke(event);
            Object rawId = getIdMethod.invoke(event);
            Object rawData = getDataMethod.invoke(event);
            if (!(rawPlayer instanceof Player player) || !(rawId instanceof String packetId)) {
                return;
            }

            List<String> data = rawData instanceof List<?> rawList
                ? rawList.stream().map(String::valueOf).toList()
                : List.of();
            boolean handled = false;
            boolean tabHandled = false;
            if (titleService != null) {
                handled = titleService.handleClientPacket(player, packetId, data);
            }
            if (!handled && conversationService != null) {
                handled = conversationService.handleClientPacket(player, packetId, data);
            }
            if (!handled && mailService != null) {
                handled = mailService.handleClientPacket(player, packetId, data);
                if (handled && mailConfiguration != null && mailConfiguration.debug()) {
                    getLogger().info(
                        "ArcartXMail 收到客户端回包 -> player="
                            + player.getName()
                            + " | packetId="
                            + packetId
                            + " | data="
                            + data
                        );
                }
            }
            if (!handled && warehouseService != null) {
                handled = warehouseService.handleClientPacket(player, packetId, data);
                if (handled && warehouseConfiguration != null && warehouseConfiguration.debug()) {
                    getLogger().info(
                        "ArcartXWarehouse 收到客户端回包 -> player="
                            + player.getName()
                            + " | packetId="
                            + packetId
                            + " | data="
                            + data
                    );
                }
            }
            if (!handled && loginViewService != null) {
                handled = loginViewService.handleClientPacket(player, packetId, data);
                if (handled && loginViewConfiguration != null && loginViewConfiguration.debug()) {
                    getLogger().info(
                        "ArcartXLoginView 收到客户端回包 -> player="
                            + player.getName()
                            + " | packetId="
                            + packetId
                            + " | data="
                            + data
                    );
                }
            }
            if (!handled && onlineRewardsService != null) {
                handled = onlineRewardsService.handleClientPacket(player, packetId, data);
                if (handled && onlineRewardsConfiguration != null && onlineRewardsConfiguration.debug()) {
                    getLogger().info(
                        "ArcartXOnlineRewards 收到客户端回包 -> player="
                            + player.getName()
                            + " | packetId="
                            + packetId
                            + " | data="
                            + data
                    );
                }
            }
            if (!handled && questGpsService != null) {
                handled = questGpsService.handleClientPacket(player, packetId, data);
                if (handled && questGpsConfiguration != null && questGpsConfiguration.debug()) {
                    getLogger().info(
                        "ArcartXQuestGPS 收到客户端回包 -> player="
                            + player.getName()
                            + " | packetId="
                            + packetId
                            + " | data="
                            + data
                    );
                }
            }
            if (!handled && mapService != null) {
                handled = mapService.handleClientPacket(player, packetId, data);
                if (handled && mapConfiguration != null && mapConfiguration.debug()) {
                    getLogger().info(
                        "ArcartXMap 收到客户端回包 -> player="
                            + player.getName()
                            + " | packetId="
                            + packetId
                            + " | data="
                            + data
                    );
                }
            }
            if (!handled) {
                handled = handleAnnouncerClientPacket(player, packetId, data);
                if (handled && announcerConfiguration != null && announcerConfiguration.debug()) {
                    getLogger().info(
                        "ArcartXAnnouncer 收到客户端回包 -> player="
                            + player.getName()
                            + " | packetId="
                            + packetId
                            + " | data="
                            + data
                    );
                }
            }
            if (!handled && eventPacketConfiguration != null && data != null && data.size() == 1) {
                handled = handleClientPacketViaEventPacket(player, packetId, data.get(0));
            }
            if (!handled) {
                tabHandled = handleTabClientRefreshPacket(player, packetId, data);
                handled = tabHandled;
            }
            if (tabHandled && tabConfiguration != null && tabConfiguration.debug()) {
                getLogger().info(
                    "ArcartXTab 收到客户端刷新请求 -> player="
                        + player.getName()
                        + " | packetId="
                        + packetId
                        + " | data="
                        + data
                );
            }
        } catch (ReflectiveOperationException exception) {
            getLogger().warning("处理 ArcartX 客户端自定义包失败: " + exception.getMessage());
        }
    }

    private boolean handleClientPacketViaEventPacket(Player player, String packetId, String presetId) {
        if (eventPacketConfiguration == null || packetId == null || presetId == null) {
            return false;
        }
        EventPacketRule rule = eventPacketConfiguration.findClientPacketRule(packetId, presetId);
        if (rule == null) {
            return false;
        }
        ClientPacketGuard packetGuard = getClientPacketGuard();
        if (packetGuard != null && !packetGuard.allow(player, "packetcommand", "execute", eventPacketConfiguration.debug())) {
            return true;
        }
        EventPacketContext context = EventPacketContext.fromSubjectTrigger(EventPacketTrigger.CLIENT_PACKET, player);
        dispatchEventPacketRule(rule, EventPacketTrigger.CLIENT_PACKET, player, context);
        if (eventPacketConfiguration.debug()) {
            getLogger().info(
                "EventPacket(PacketCommand) 处理客户端回包 -> player="
                    + player.getName()
                    + " | packetId="
                    + packetId
                    + " | preset="
                    + presetId
            );
        }
        return true;
    }

    private void handleClientInitializedEvent(
        Event event,
        Class<?> clientInitializedEventClass,
        java.lang.reflect.Method getPlayerMethod
    ) {
        if (!clientInitializedEventClass.isInstance(event)) {
            return;
        }
        try {
            Object rawPlayer = getPlayerMethod.invoke(event);
            if (rawPlayer instanceof Player player) {
                if (propService != null) {
                    propService.handleClientInitialized(player);
                }
                if (announcerService != null) {
                    announcerService.markClientInitialized(player);
                }
                if (onlineRewardsService != null) {
                    onlineRewardsService.handleClientInitialized(player);
                }
                if (mapService != null) {
                    mapService.handleClientInitialized(player);
                }
                if (loginViewService != null) {
                    loginViewService.handleClientInitialized(player);
                }
                if (propService != null && propConfiguration != null && propConfiguration.debug()) {
                    getLogger().info("ArcartXProp 客户端初始化完成 -> player=" + player.getName());
                }
                if (announcerService != null && announcerConfiguration != null && announcerConfiguration.debug()) {
                    getLogger().info("ArcartXAnnouncer 客户端初始化完成 -> player=" + player.getName());
                }
                if (onlineRewardsService != null && onlineRewardsConfiguration != null && onlineRewardsConfiguration.debug()) {
                    getLogger().info("ArcartXOnlineRewards 客户端初始化完成 -> player=" + player.getName());
                }
                if (mapService != null && mapConfiguration != null && mapConfiguration.debug()) {
                    getLogger().info("ArcartXMap 客户端初始化完成 -> player=" + player.getName());
                }
                if (loginViewService != null && loginViewConfiguration != null && loginViewConfiguration.debug()) {
                    getLogger().info("ArcartXLoginView 客户端初始化完成 -> player=" + player.getName());
                }
            }
        } catch (ReflectiveOperationException exception) {
            getLogger().warning("处理 ArcartX 客户端初始化事件失败: " + exception.getMessage());
        }
    }

    private boolean hasPlugin(String pluginName) {
        return Bukkit.getPluginManager().getPlugin(pluginName) != null;
    }

    private boolean hasAnyPlugin(String... pluginNames) {
        for (String pluginName : pluginNames) {
            if (hasPlugin(pluginName)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyPluginEnabled(String... pluginNames) {
        return hasAnyPlugin(pluginNames);
    }

    private boolean isModuleEnabled(String moduleKey, boolean defaultValue) {
        return getConfig().getBoolean("modules." + moduleKey + ".enabled", defaultValue);
    }

    private String getModulePassword(ModuleKey moduleKey) {
        return getModulePassword(moduleKey.configKey());
    }

    private String getModulePassword(String moduleKey) {
        Object raw = getConfig().get("modules." + moduleKey + ".password");
        return raw == null ? "" : String.valueOf(raw).trim();
    }

    private ValidationResult refreshModulePasswordValidation(ModuleKey moduleKey) {
        ValidationResult result = modulePasswordAuthenticator.validate(moduleKey, getModulePassword(moduleKey));
        modulePasswordValidationStates.put(moduleKey, result);
        return result;
    }

    private void logModulePasswordFailure(ModuleKey moduleKey, ValidationResult result) {
        if (result == ValidationResult.VALID) {
            return;
        }

        String configPath = passwordConfigPath(moduleKey);
        if (result == ValidationResult.MISSING) {
            consoleWarn(moduleKey.displayName() + " 模块未填写密码，已跳过加载。");
            consoleWarn("请在 config.yml 的 " + configPath + " 中填写正确私有密码后再重载。");
            return;
        }

        consoleWarn(moduleKey.displayName() + " 模块密码错误，已跳过加载。");
        consoleWarn("请确认 config.yml 的 " + configPath + " 为正确私有密码后再重载。");
    }

    private String passwordConfigPath(ModuleKey moduleKey) {
        return "modules." + moduleKey.configKey() + ".password";
    }

    private EnumMap<ModuleKey, ValidationResult> createInitialPasswordValidationStates() {
        EnumMap<ModuleKey, ValidationResult> initialStates = new EnumMap<>(ModuleKey.class);
        for (ModuleKey moduleKey : ModuleKey.values()) {
            initialStates.put(moduleKey, ValidationResult.MISSING);
        }
        return initialStates;
    }

    private void registerPlaceholderExpansionIfAvailable() {
        if (!hasPlugin("PlaceholderAPI")) {
            return;
        }
        if (isModuleEnabled("entitytracker", true)) {
            registerPlaceholderExpansion(
                "xuanmo.arcartxsuite.bossbar.placeholder.ArcartXBossBarPlaceholderExpansion",
                "%AXSentitytracker_%"
            );
        }
        if (isModuleEnabled("title", false)) {
            registerPlaceholderExpansion(
                "xuanmo.arcartxsuite.title.placeholder.TitlePlaceholderExpansion",
                "%AXStitle_%"
            );
        }
        if (isModuleEnabled("chat", false)) {
            registerPlaceholderExpansion(
                "xuanmo.arcartxsuite.chat.placeholder.ChatPlaceholderExpansion",
                "%AXSchat_%"
            );
        }
        if (isModuleEnabled("rgb", false)) {
            registerPlaceholderExpansion(
                "xuanmo.arcartxsuite.rgb.placeholder.ArcartRgbPlaceholderExpansion",
                "%arcartrgb_%"
            );
        }
        if (isModuleEnabled("mail", false)) {
            registerPlaceholderExpansion(
                "xuanmo.arcartxsuite.mail.placeholder.MailPlaceholderExpansion",
                "%AXSmail_%"
            );
        }
        if (isModuleEnabled("warehouse", false)) {
            registerPlaceholderExpansion(
                "xuanmo.arcartxsuite.warehouse.placeholder.WarehousePlaceholderExpansion",
                "%AXSwarehouse_%"
            );
        }
        if (isModuleEnabled("onlinerewards", false)) {
            registerPlaceholderExpansion(
                OnlineRewardsPlaceholderExpansion.class.getName(),
                "%AXSonlinerewards_%"
            );
        }
    }

    private void unregisterPlaceholderExpansion() {
        if (placeholderExpansions.isEmpty()) {
            registeredPlaceholderIds.clear();
            return;
        }
        for (Object expansion : List.copyOf(placeholderExpansions)) {
            try {
                expansion.getClass().getMethod("unregister").invoke(expansion);
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // PlaceholderAPI 可能已经先于本插件卸载，这里只做尽力注销。
            }
        }
        placeholderExpansions.clear();
        registeredPlaceholderIds.clear();
    }

    private void registerPlaceholderExpansion(String className, String placeholderId) {
        if (registeredPlaceholderIds.contains(placeholderId)) {
            return;
        }
        try {
            Class<?> expansionClass = Class.forName(className, true, getClassLoader());
            Object expansion = expansionClass.getConstructor(ArcartXSuitePlugin.class).newInstance(this);
            Object result = expansionClass.getMethod("register").invoke(expansion);
            if (result instanceof Boolean registered && registered.booleanValue()) {
                placeholderExpansions.add(expansion);
                registeredPlaceholderIds.add(placeholderId);
                getLogger().fine("已注册 PlaceholderAPI 占位符: " + placeholderId);
                return;
            }
            getLogger().warning("PlaceholderAPI 占位符注册失败(" + placeholderId + ")，register() 返回 false。");
        } catch (ReflectiveOperationException | LinkageError exception) {
            getLogger().warning("PlaceholderAPI 占位符注册失败(" + placeholderId + "): " + exception.getMessage());
        }
    }

    private void ensureRootConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        File rootConfigFile = new File(getDataFolder(), "config.yml");
        if (!rootConfigFile.exists()) {
            writeBundledResourceUnchecked("config.yml", rootConfigFile);
        }
    }

    private void ensureKillEffectConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        killEffectConfigFile = new File(getDataFolder(), KILL_EFFECT_CONFIG_FILE_NAME);
        if (!killEffectConfigFile.exists()) {
            writeBundledResourceUnchecked(KILL_EFFECT_CONFIG_FILE_NAME, killEffectConfigFile);
        }
    }

    private void ensureEventPacketConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        eventPacketConfigFile = new File(getDataFolder(), EVENT_PACKET_CONFIG_FILE_NAME);
        if (!eventPacketConfigFile.exists()) {
            writeBundledResourceUnchecked(EVENT_PACKET_CONFIG_FILE_NAME, eventPacketConfigFile);
        }
    }

    private void ensureBossBarConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        bossBarConfigFile = new File(getDataFolder(), BOSSBAR_CONFIG_FILE_NAME);
        if (!bossBarConfigFile.exists()) {
            writeBundledResourceUnchecked(BOSSBAR_CONFIG_FILE_NAME, bossBarConfigFile);
        }
    }

    private void ensureTabConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        tabConfigFile = new File(getDataFolder(), TAB_CONFIG_FILE_NAME);
        if (!tabConfigFile.exists()) {
            writeBundledResourceUnchecked(TAB_CONFIG_FILE_NAME, tabConfigFile);
        }
    }

    private void ensureTitleConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        titleConfigFile = new File(getDataFolder(), TITLE_CONFIG_FILE_NAME);
        if (!titleConfigFile.exists()) {
            writeBundledResourceUnchecked(TITLE_CONFIG_FILE_NAME, titleConfigFile);
        }
    }

    private void ensureSubtitleConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        subtitleConfigFile = new File(getDataFolder(), SUBTITLE_CONFIG_FILE_NAME);
        if (!subtitleConfigFile.exists()) {
            writeBundledResourceUnchecked(SUBTITLE_CONFIG_FILE_NAME, subtitleConfigFile);
        }
    }

    private void ensureAnnouncerConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        announcerConfigFile = new File(getDataFolder(), ANNOUNCER_CONFIG_FILE_NAME);
        if (!announcerConfigFile.exists()) {
            writeBundledResourceUnchecked(ANNOUNCER_CONFIG_FILE_NAME, announcerConfigFile);
        }
    }

    private void ensureAttackTargetConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        attackTargetConfigFile = new File(getDataFolder(), ATTACK_TARGET_CONFIG_FILE_NAME);
        if (!attackTargetConfigFile.exists()) {
            writeBundledResourceUnchecked(ATTACK_TARGET_CONFIG_FILE_NAME, attackTargetConfigFile);
        }
    }

    private void ensurePickupConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        pickupConfigFile = new File(getDataFolder(), PICKUP_CONFIG_FILE_NAME);
        if (!pickupConfigFile.exists()) {
            writeBundledResourceUnchecked(PICKUP_CONFIG_FILE_NAME, pickupConfigFile);
        }
    }

    private void ensurePropConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        propConfigFile = new File(getDataFolder(), PROP_CONFIG_FILE_NAME);
        if (!propConfigFile.exists()) {
            writeBundledResourceUnchecked(PROP_CONFIG_FILE_NAME, propConfigFile);
        }
    }

    private void ensurePropDataDefaultsExist() {
        File propDirectory = resolvePropDataDirectory();
        if (!propDirectory.exists() && !propDirectory.mkdirs()) {
            throw new IllegalStateException("无法创建 Prop 数据目录: " + propDirectory.getAbsolutePath());
        }

        ensureBundledResourceExists("prop/key.yml");
        ensureBundledResourceExists("prop/language.yml");
        ensureBundledResourceExists("prop/props/道具示例.yml");
    }

    private void ensureDigisDisplayConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        digisDisplayConfigFile = new File(getDataFolder(), DIGIS_DISPLAY_CONFIG_FILE_NAME);
        if (!digisDisplayConfigFile.exists()) {
            writeBundledResourceUnchecked(DIGIS_DISPLAY_CONFIG_FILE_NAME, digisDisplayConfigFile);
        }
    }

    private void ensureRgbConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        rgbConfigFile = new File(getDataFolder(), RGB_CONFIG_FILE_NAME);
        if (!rgbConfigFile.exists()) {
            writeBundledResourceUnchecked(RGB_CONFIG_FILE_NAME, rgbConfigFile);
        }
    }

    private void ensureOnlineRewardsConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        onlineRewardsConfigFile = new File(getDataFolder(), ONLINE_REWARDS_CONFIG_FILE_NAME);
        if (!onlineRewardsConfigFile.exists()) {
            writeBundledResourceUnchecked(ONLINE_REWARDS_CONFIG_FILE_NAME, onlineRewardsConfigFile);
        }
    }

    // ensurePacketCommandConfigExists removed — config file still generated for backward compat,
    // but loaded by EventPacket via loadEventPacketConfiguration

    private void ensureLoginViewConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        loginViewConfigFile = new File(getDataFolder(), LOGIN_VIEW_CONFIG_FILE_NAME);
        if (!loginViewConfigFile.exists()) {
            writeBundledResourceUnchecked(LOGIN_VIEW_CONFIG_FILE_NAME, loginViewConfigFile);
        }
    }

    private void ensureWarehouseConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        warehouseConfigFile = new File(getDataFolder(), WAREHOUSE_CONFIG_FILE_NAME);
        if (!warehouseConfigFile.exists()) {
            writeBundledResourceUnchecked(WAREHOUSE_CONFIG_FILE_NAME, warehouseConfigFile);
        }
    }

    private void ensureMailConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        mailConfigFile = new File(getDataFolder(), MAIL_CONFIG_FILE_NAME);
        if (!mailConfigFile.exists()) {
            writeBundledResourceUnchecked(MAIL_CONFIG_FILE_NAME, mailConfigFile);
        }
    }

    private void ensureChatConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        chatConfigFile = new File(getDataFolder(), CHAT_CONFIG_FILE_NAME);
        if (!chatConfigFile.exists()) {
            writeBundledResourceUnchecked(CHAT_CONFIG_FILE_NAME, chatConfigFile);
        }

        ensureBundledResourceExists("chat/channels/Normal.yml");
        ensureBundledResourceExists("chat/channels/Global.yml");
        ensureBundledResourceExists("chat/channels/Private.yml");
        ensureBundledResourceExists("chat/channels/Staff.yml");
    }

    private void ensureConversationConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        conversationConfigFile = new File(getDataFolder(), CONVERSATION_CONFIG_FILE_NAME);
        if (!conversationConfigFile.exists()) {
            writeBundledResourceUnchecked(CONVERSATION_CONFIG_FILE_NAME, conversationConfigFile);
        }
    }

    private void ensureQuestGpsConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        questGpsConfigFile = new File(getDataFolder(), QUEST_GPS_CONFIG_FILE_NAME);
        if (!questGpsConfigFile.exists()) {
            writeBundledResourceUnchecked(QUEST_GPS_CONFIG_FILE_NAME, questGpsConfigFile);
        }
    }

    private void ensureMapConfigExists() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IllegalStateException("无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
        }

        mapConfigFile = new File(getDataFolder(), MAP_CONFIG_FILE_NAME);
        if (!mapConfigFile.exists()) {
            writeBundledResourceUnchecked(MAP_CONFIG_FILE_NAME, mapConfigFile);
        }
    }

    private void synchronizeBundledNonUiConfigurations() {
        String backupTimestamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").format(LocalDateTime.now());
        File backupRoot = new File(getDataFolder(), "config-backups/" + backupTimestamp);
        BatchSyncResult result = YamlConfigSynchronizer.synchronizeAll(
            getDataFolder(),
            DefaultConfigResourceRegistry.bundledNonUiResources(),
            this::openBundledResource,
            backupRoot
        );

        for (SyncResult syncResult : result.results()) {
            if (!syncResult.skipped()) {
                continue;
            }
            getLogger().warning("已跳过非 UI 配置同步: " + syncResult.resourcePath() + " | " + syncResult.message());
        }

        if (!result.changed()) {
            return;
        }

        String backupText = backupRoot.exists()
            ? " | 备份目录=" + backupRoot.getAbsolutePath()
            : "";
        getLogger().info(
            "非 UI 配置自动同步完成: 新建="
                + result.createdCount()
                + " | 更新="
                + result.changedExistingCount()
                + " | 补齐项="
                + result.addedPathCount()
                + " | 删除项="
                + result.removedPathCount()
                + " | 跳过="
                + result.skippedCount()
                + backupText
                + summarizeSyncedResources(result)
        );
    }

    private String summarizeSyncedResources(BatchSyncResult result) {
        List<String> changedResources = result.results().stream()
            .filter(syncResult -> syncResult.changed() && !syncResult.skipped())
            .map(SyncResult::resourcePath)
            .limit(8)
            .toList();
        if (changedResources.isEmpty()) {
            return "";
        }
        int changedCount = result.createdCount() + result.changedExistingCount();
        String suffix = changedCount > changedResources.size() ? " ..." : "";
        return " | 文件=" + String.join(", ", changedResources) + suffix;
    }

    private void ensureBundledResourceExists(String relativePath) {
        File target = new File(getDataFolder(), relativePath);
        File parent = target.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("无法创建目录: " + parent.getAbsolutePath());
        }
        if (!target.exists()) {
            writeBundledResourceUnchecked(relativePath, target);
        }
    }

    private void ensureSubtitleDefaultGroupExists(String groupsDirectory) throws IOException {
        File directory = new File(getDataFolder(), groupsDirectory);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("无法创建字幕组目录: " + directory.getAbsolutePath());
        }
        File defaultGroupFile = new File(directory, "default.yml");
        if (!defaultGroupFile.exists()) {
            writeBundledResource(SUBTITLE_DEFAULT_GROUP_RESOURCE_PATH, defaultGroupFile);
        }
    }

    private void ensurePacketCommandPresetDefaultsExist() {
        File presetsDirectory = resolvePacketCommandPresetsDirectory();
        if (!presetsDirectory.exists() && !presetsDirectory.mkdirs()) {
            throw new IllegalStateException("无法创建 PacketCommand 预设目录: " + presetsDirectory.getAbsolutePath());
        }
        File sampleFile = new File(presetsDirectory, "example.yml");
        if (!sampleFile.exists()) {
            writeBundledResourceUnchecked("packetcommand/presets/example.yml", sampleFile);
        }
    }

    private File resolvePacketCommandPresetsDirectory() {
        return new File(getDataFolder(), "packetcommand/presets");
    }

    private PluginConfiguration loadBossBarConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(bossBarConfigFile);
        return PluginConfiguration.from(configuration);
    }

    private xuanmo.arcartxsuite.killeffect.config.PluginConfiguration loadKillEffectConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(killEffectConfigFile);
        return xuanmo.arcartxsuite.killeffect.config.PluginConfiguration.load(configuration, getLogger());
    }

    private xuanmo.arcartxsuite.eventpacket.config.PluginConfiguration loadEventPacketConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(eventPacketConfigFile);
        File packetCommandConfigFile = new File(getDataFolder(), PACKET_COMMAND_CONFIG_FILE_NAME);
        if (!packetCommandConfigFile.exists()) {
            writeBundledResourceUnchecked(PACKET_COMMAND_CONFIG_FILE_NAME, packetCommandConfigFile);
        }
        FileConfiguration pcConfig = YamlConfiguration.loadConfiguration(packetCommandConfigFile);
        File presetsDir = resolvePacketCommandPresetsDirectory();
        return xuanmo.arcartxsuite.eventpacket.config.PluginConfiguration.load(
            configuration, getLogger(), packetCommandConfigFile, presetsDir, pcConfig
        );
    }

    private TabModuleConfiguration loadTabConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(tabConfigFile);
        return TabModuleConfiguration.load(configuration, getLogger());
    }

    private TitleModuleConfiguration loadTitleConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(titleConfigFile);
        return TitleModuleConfiguration.load(configuration, getLogger());
    }

    private SubtitleModuleConfiguration loadSubtitleConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(subtitleConfigFile);
        return SubtitleModuleConfiguration.load(configuration, getLogger());
    }

    private AnnouncerModuleConfiguration loadAnnouncerConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(announcerConfigFile);
        return AnnouncerModuleConfiguration.load(configuration, getLogger());
    }

    private AttackTargetModuleConfiguration loadAttackTargetConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(attackTargetConfigFile);
        return AttackTargetModuleConfiguration.load(configuration);
    }

    private PickupModuleConfiguration loadPickupConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(pickupConfigFile);
        return PickupModuleConfiguration.load(configuration);
    }

    private PropModuleConfiguration loadPropConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(propConfigFile);
        return PropModuleConfiguration.load(configuration);
    }

    private PropKeyMappingConfiguration loadPropKeyMappingConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(new File(resolvePropDataDirectory(), "key.yml"));
        return PropKeyMappingConfiguration.load(configuration);
    }

    private PropLanguageConfiguration loadPropLanguageConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(new File(resolvePropDataDirectory(), "language.yml"));
        return PropLanguageConfiguration.load(configuration);
    }

    private Map<String, PropDefinition> loadPropDefinitions() throws IOException {
        return PropDefinitionLoader.load(new File(resolvePropDataDirectory(), "props"), getLogger());
    }

    private DigisDisplayModuleConfiguration loadDigisDisplayConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(digisDisplayConfigFile);
        return DigisDisplayModuleConfiguration.load(configuration);
    }

    private ArcartRgbModuleConfiguration loadRgbConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(rgbConfigFile);
        return ArcartRgbModuleConfiguration.load(configuration, getLogger());
    }

    private OnlineRewardsModuleConfiguration loadOnlineRewardsConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(onlineRewardsConfigFile);
        return OnlineRewardsModuleConfiguration.load(configuration);
    }

    // loadPacketCommandConfiguration removed — merged into loadEventPacketConfiguration

    private LoginViewModuleConfiguration loadLoginViewConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(loginViewConfigFile);
        return LoginViewModuleConfiguration.load(configuration, getLogger());
    }

    private WarehouseModuleConfiguration loadWarehouseConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(warehouseConfigFile);
        return WarehouseModuleConfiguration.load(configuration, getLogger());
    }

    private MailModuleConfiguration loadMailConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(mailConfigFile);
        return MailModuleConfiguration.load(configuration, getLogger());
    }

    private ChatModuleConfiguration loadChatConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(chatConfigFile);
        File channelsDirectory = new File(getDataFolder(), configuration.getString("channels-directory", "chat/channels"));
        return ChatModuleConfiguration.load(configuration, channelsDirectory, getLogger());
    }

    private ConversationModuleConfiguration loadConversationConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(conversationConfigFile);
        return ConversationModuleConfiguration.load(configuration);
    }

    private QuestGpsModuleConfiguration loadQuestGpsConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(questGpsConfigFile);
        return QuestGpsModuleConfiguration.load(configuration, getLogger());
    }

    private MapModuleConfiguration loadMapConfiguration() {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(mapConfigFile);
        return MapModuleConfiguration.load(configuration, getLogger());
    }

    private File resolvePropDataDirectory() {
        return new File(getDataFolder(), "prop");
    }


    private Map<String, SubtitleGroup> loadSubtitleGroups(SubtitleModuleConfiguration configuration) throws IOException {
        File directory = new File(getDataFolder(), configuration.groupsDirectory());
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("无法创建字幕组目录: " + directory.getAbsolutePath());
        }

        File[] groupFiles = directory.listFiles(file -> file.isFile() && file.getName().toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (groupFiles == null || groupFiles.length == 0) {
            return Map.of();
        }

        Arrays.sort(groupFiles, (left, right) -> left.getName().compareToIgnoreCase(right.getName()));
        Map<String, SubtitleGroup> groups = new LinkedHashMap<>();
        for (File groupFile : groupFiles) {
            String fileName = groupFile.getName();
            String groupId = fileName.substring(0, fileName.length() - 4);
            SubtitleGroup group = SubtitleGroup.load(groupId, YamlConfiguration.loadConfiguration(groupFile), getLogger());
            groups.put(groupId.toLowerCase(Locale.ROOT), group);
        }
        return Map.copyOf(groups);
    }

    public void shutdownBossBarModule() {
        if (bossTrackerService != null) {
            bossTrackerService.shutdown();
            bossTrackerService = null;
        }
        unregisterBossBarUi();
        bossBarRuntimeUiId = bossBarConfiguration == null ? null : bossBarConfiguration.uiId();
    }

    public void shutdownTabModule() {
        if (tabSyncService == null) {
            return;
        }
        tabSyncService.shutdown();
        tabSyncService = null;
    }

    public void shutdownTitleModule() {
        if (titleService != null) {
            titleService.shutdown();
            titleService = null;
        }
    }

    public void shutdownConversationModule() {
        if (conversationService != null) {
            conversationService.shutdown();
            conversationService = null;
        }
        unregisterConversationUi();
        unregisterConversationSelectorUi();
        conversationRuntimeUiId = conversationConfiguration == null ? null : conversationConfiguration.uiId();
        conversationSelectorRuntimeUiId = conversationConfiguration == null ? null : conversationConfiguration.selectorUiId();
    }

    public void shutdownSubtitleModule() {
        if (subtitleService != null) {
            subtitleService.shutdown();
            subtitleService = null;
        }
        unregisterSubtitleUi();
    }

    public void shutdownAnnouncerModule() {
        if (announcerService != null) {
            announcerService.shutdown();
            announcerService = null;
        }
        unregisterAnnouncerUi();
    }

    public void shutdownAttackTargetModule() {
        if (attackTargetService != null) {
            attackTargetService.shutdown();
            attackTargetService = null;
        }
        unregisterAttackTargetUi();
        attackTargetRuntimeUiId = attackTargetConfiguration == null ? null : attackTargetConfiguration.uiId();
    }

    public void shutdownPickupModule() {
        if (pickupService != null) {
            pickupService.shutdown();
            pickupService = null;
        }
        closePickupUiForOnlinePlayers();
        unregisterPickupUi();
        pickupRuntimeUiId = null;
    }

    public void shutdownPropModule() {
        if (propService != null) {
            propService.shutdown();
            propService = null;
        }
    }

    public void shutdownDigisDisplayModule() {
        if (digisDisplayService != null) {
            digisDisplayService.shutdown();
            digisDisplayService = null;
        }
    }

    public void shutdownRgbModule() {
        if (rgbService != null) {
            rgbService.shutdown();
            rgbService = null;
        }
        if (rgbShimmerBridge != null) {
            rgbShimmerBridge.shutdown();
        }
    }

    // shutdownPacketCommandModule removed — merged into EventPacket

    public void shutdownLoginViewModule() {
        if (loginViewService != null) {
            loginViewService.shutdown();
            loginViewService = null;
        }
        unregisterLoginViewUi();
        loginViewRuntimeUiId = loginViewConfiguration == null ? null : loginViewConfiguration.ui().uiId();
    }

    public void shutdownOnlineRewardsModule() {
        if (onlineRewardsService != null) {
            onlineRewardsService.shutdown();
            onlineRewardsService = null;
        }
    }

    public void shutdownWarehouseModule() {
        if (warehouseService != null) {
            warehouseService.shutdown();
            warehouseService = null;
        }
    }

    public void shutdownMailModule() {
        if (mailService != null) {
            mailService.shutdown();
            mailService = null;
        }
    }

    public void shutdownChatModule() {
        if (chatService != null) {
            chatService.shutdown();
            chatService = null;
        }
    }

    public void shutdownQuestGpsModule() {
        if (questGpsService != null) {
            questGpsService.shutdown();
            questGpsService = null;
        }
        unregisterQuestGpsUi();
        unregisterQuestGpsHudUi();
        questGpsRuntimeUiId = questGpsConfiguration == null ? null : questGpsConfiguration.client().menuUiId();
        questGpsHudRuntimeUiId = questGpsConfiguration == null ? null : questGpsConfiguration.client().hudUiId();
    }

    public void shutdownMapModule() {
        if (mapService != null) {
            mapService.shutdown();
            mapService = null;
        }
        unregisterMapMenuUi();
        unregisterMapHudUi();
        mapMenuRuntimeUiId = mapConfiguration == null ? null : mapConfiguration.client().menuUiId();
        mapHudRuntimeUiId = mapConfiguration == null ? null : mapConfiguration.client().hudUiId();
    }

    public void shutdownEventPacketModule() {
        if (eventPacketWatcherService != null) {
            eventPacketWatcherService.shutdown();
            eventPacketWatcherService = null;
        }
    }

    private void unregisterBossBarUi() {
        if (packetBridge == null || bossBarRegisteredUiId == null) {
            bossBarRegisteredUiId = null;
            return;
        }
        packetBridge.unregisterUi(bossBarRegisteredUiId);
        bossBarRegisteredUiId = null;
    }

    private void unregisterSubtitleUi() {
        if (packetBridge == null || subtitleRegisteredUiId == null) {
            subtitleRegisteredUiId = null;
            return;
        }
        packetBridge.unregisterUi(subtitleRegisteredUiId);
        subtitleRegisteredUiId = null;
    }

    private void unregisterAnnouncerUi() {
        if (packetBridge == null || announcerRegisteredUiId == null) {
            announcerRegisteredUiId = null;
            return;
        }
        packetBridge.unregisterUi(announcerRegisteredUiId);
        announcerRegisteredUiId = null;
    }

    private void unregisterAttackTargetUi() {
        if (packetBridge == null || attackTargetRegisteredUiId == null) {
            attackTargetRegisteredUiId = null;
            return;
        }
        packetBridge.unregisterUi(attackTargetRegisteredUiId);
        attackTargetRegisteredUiId = null;
    }

    private void unregisterConversationUi() {
        if (packetBridge == null || conversationRegisteredUiId == null) {
            conversationRegisteredUiId = null;
            return;
        }
        packetBridge.unregisterUi(conversationRegisteredUiId);
        conversationRegisteredUiId = null;
    }

    private void unregisterConversationSelectorUi() {
        if (packetBridge == null || conversationSelectorRegisteredUiId == null) {
            conversationSelectorRegisteredUiId = null;
            return;
        }
        packetBridge.unregisterUi(conversationSelectorRegisteredUiId);
        conversationSelectorRegisteredUiId = null;
    }

    private void unregisterPickupUi() {
        if (packetBridge == null || pickupRegisteredUiId == null) {
            pickupRegisteredUiId = null;
            return;
        }
        packetBridge.unregisterUi(pickupRegisteredUiId);
        pickupRegisteredUiId = null;
    }

    private void closePickupUiForOnlinePlayers() {
        if (packetBridge == null) {
            return;
        }
        if (pickupRuntimeUiId != null && !pickupRuntimeUiId.isBlank()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                packetBridge.closeUi(player, pickupRuntimeUiId);
            }
        }
        if (pickupRegisteredUiId != null
            && !pickupRegisteredUiId.isBlank()
            && !Objects.equals(pickupRegisteredUiId, pickupRuntimeUiId)) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                packetBridge.closeUi(player, pickupRegisteredUiId);
            }
        }
    }

    private void unregisterQuestGpsUi() {
        if (packetBridge == null || questGpsRegisteredUiId == null) {
            questGpsRegisteredUiId = null;
            return;
        }
        packetBridge.unregisterUi(questGpsRegisteredUiId);
        questGpsRegisteredUiId = null;
    }

    private void unregisterQuestGpsHudUi() {
        if (packetBridge == null || questGpsHudRegisteredUiId == null) {
            questGpsHudRegisteredUiId = null;
            return;
        }
        packetBridge.unregisterUi(questGpsHudRegisteredUiId);
        questGpsHudRegisteredUiId = null;
    }

    private void unregisterMapMenuUi() {
        if (packetBridge == null || mapMenuRegisteredUiId == null) {
            mapMenuRegisteredUiId = null;
            return;
        }
        packetBridge.unregisterUi(mapMenuRegisteredUiId);
        mapMenuRegisteredUiId = null;
    }

    private void unregisterMapHudUi() {
        if (packetBridge == null || mapHudRegisteredUiId == null) {
            mapHudRegisteredUiId = null;
            return;
        }
        packetBridge.unregisterUi(mapHudRegisteredUiId);
        mapHudRegisteredUiId = null;
    }

    private void unregisterLoginViewUi() {
        if (packetBridge == null || loginViewRegisteredUiId == null) {
            loginViewRegisteredUiId = null;
            return;
        }
        packetBridge.unregisterUi(loginViewRegisteredUiId);
        loginViewRegisteredUiId = null;
    }

    private File exportBundledUiFile(boolean overwrite) throws IOException {
        File target = resolveAXSUiFile(BOSSBAR_UI_FILE_PATH);
        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) {
            Files.createDirectories(parent.toPath());
        }

        if (target.exists() && !shouldRewriteUiFile(target, overwrite)) {
            return target;
        }

        ArcartXHudTemplateWriter.write(target);
        getLogger().info("已导出 boss_tracker UI 到 AXS 目录: " + target.getAbsolutePath());
        return target;
    }

    private File exportBundledUiResource(String resourcePath, String relativeUiPath, boolean overwrite) throws IOException {
        return exportUiResourceToDataFolder(resourcePath, relativeUiPath, overwrite);
    }

    public File exportUiResourceToDataFolder(String resourcePath, String relativeUiPath, boolean overwrite) throws IOException {
        File target = resolveAXSUiFile(relativeUiPath);
        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) {
            Files.createDirectories(parent.toPath());
        }
        if (target.exists() && !overwrite) {
            return target;
        }
        writeBundledResource(resourcePath, target);
        return target;
    }

    public File exportBundledResourceToDataFolder(String resourcePath, String relativePath, boolean overwrite) throws IOException {
        File target = new File(getDataFolder(), relativePath);
        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) {
            Files.createDirectories(parent.toPath());
        }
        if (target.exists() && !overwrite) {
            return target;
        }
        writeBundledResource(resourcePath, target);
        return target;
    }

    private File exportAnnouncerUiFile(boolean overwrite) throws IOException {
        File target = resolveAXSUiFile(ANNOUNCER_UI_FILE_PATH);
        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) {
            Files.createDirectories(parent.toPath());
        }
        if (target.exists() && !shouldRewriteAnnouncerUi(target, overwrite)) {
            return target;
        }
        writeBundledResource("arcartx/ui/announcer_hud.yml", target);
        return target;
    }

    private File exportPickupUiFile(PickupModuleConfiguration configuration) throws IOException {
        File target = resolveAXSUiFile(PICKUP_UI_FILE_PATH);
        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) {
            Files.createDirectories(parent.toPath());
        }
        String expectedSignature = PickupHudTemplateWriter.signature(
            configuration.maxVisible(),
            configuration.entryTtlMs()
        );
        if (target.exists() && !shouldRewritePickupUi(target, configuration.overwriteUiFile(), expectedSignature)) {
            return target;
        }

        PickupHudTemplateWriter.write(target, configuration.maxVisible(), configuration.entryTtlMs());
        return target;
    }

    private File resolveAXSUiFile(String relativePath) {
        return new File(getDataFolder(), relativePath);
    }

    private UiBinding prepareUiBinding(String moduleName, String configuredUiId, boolean registerUiOnEnable, File uiFile) {
        String runtimeUiId = ArcartXPacketBridge.normalizeUiId(configuredUiId, uiFile);
        String registeredUiId = null;
        if (registerUiOnEnable) {
            ArcartXPacketBridge.UiRegistrationResult registration = packetBridge.registerOrReloadUi(configuredUiId, uiFile);
            if (!registration.success()) {
                getLogger().severe("初始化 ArcartX " + moduleName + " UI 失败: " + registration.message());
                return null;
            }
            runtimeUiId = registration.runtimeUiId();
            registeredUiId = registration.registeredUiId();
        } else {
            getLogger().info("ArcartX " + moduleName + " UI 自动注册已关闭，将直接使用 UI 标识: " + runtimeUiId);
        }
        getLogger().info("ArcartX " + moduleName + " UI 目标: " + runtimeUiId + " | 文件: " + uiFile.getAbsolutePath());
        return new UiBinding(runtimeUiId, registeredUiId);
    }

    private UiBinding prepareOptionalUiBinding(String moduleName, String configuredUiId, boolean registerUiOnEnable, File uiFile) {
        String runtimeUiId = ArcartXPacketBridge.normalizeUiId(configuredUiId, uiFile);
        String registeredUiId = null;
        if (registerUiOnEnable) {
            ArcartXPacketBridge.UiRegistrationResult registration = packetBridge.registerOrReloadUi(configuredUiId, uiFile);
            if (!registration.success()) {
                getLogger().warning("初始化 ArcartX " + moduleName + " UI 失败，已跳过该增强能力: " + registration.message());
                return null;
            }
            runtimeUiId = registration.runtimeUiId();
            registeredUiId = registration.registeredUiId();
        } else {
            getLogger().info("ArcartX " + moduleName + " UI 自动注册已关闭，将直接使用 UI 标识: " + runtimeUiId);
        }
        getLogger().info("ArcartX " + moduleName + " UI 目标: " + runtimeUiId + " | 文件: " + uiFile.getAbsolutePath());
        return new UiBinding(runtimeUiId, registeredUiId);
    }

    public void writeBundledResourceToFile(String resourcePath, File target) throws IOException {
        writeBundledResource(resourcePath, target);
    }

    private void writeBundledResourceUnchecked(String resourcePath, File target) {
        try {
            writeBundledResource(resourcePath, target);
        } catch (IOException exception) {
            throw new IllegalStateException("写出内置资源失败: " + resourcePath + " -> " + target.getAbsolutePath(), exception);
        }
    }

    private InputStream openBundledResource(String resourcePath) throws IOException {
        if (ProtectedResourceStore.exists(resourcePath)) {
            return ProtectedResourceStore.open(resourcePath);
        }
        return getResource(resourcePath);
    }

    private void writeBundledResource(String resourcePath, File target) throws IOException {
        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) {
            Files.createDirectories(parent.toPath());
        }
        try (InputStream input = openBundledResource(resourcePath)) {
            if (input == null) {
                throw new IOException("未找到资源: " + resourcePath);
            }
            Files.copy(input, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void printStartupBanner() {
        CommandSender console = Bukkit.getConsoleSender();
        for (String line : STARTUP_BANNER) {
            console.sendMessage(line);
        }
        console.sendMessage(ChatColor.GRAY + "                                    By." + AUTHOR_NAME + " - " + AUTHOR_CONTACT);
    }

    private void consoleInfo(String message) {
        Bukkit.getConsoleSender().sendMessage(CONSOLE_PREFIX + "INFO: " + message);
    }

    private void consoleWarn(String message) {
        Bukkit.getConsoleSender().sendMessage(CONSOLE_PREFIX + ChatColor.YELLOW + "WARN: " + message);
    }

    private void consoleError(String message) {
        Bukkit.getConsoleSender().sendMessage(CONSOLE_PREFIX + ChatColor.RED + "ERROR: " + message);
    }

    private void printModuleStatusSummary(
        boolean killEffect, boolean eventPacket, boolean tab, boolean title, boolean conversation,
        boolean announcer, boolean pickup, boolean prop,
        boolean rgb, boolean loginView,
        boolean onlineRewards, boolean warehouse, boolean mail, boolean chat,
        boolean questGps, boolean map, boolean bossBar
    ) {
        consoleInfo("模块状态:");
        printModuleFlag("EntityTracker(+AttackTarget)", bossBar);
        printModuleFlag("CombatEffect(+DigisDisplay)", killEffect);
        printModuleFlag("EventPacket", eventPacket);
        printModuleFlag("Tab", tab);
        printModuleFlag("Title", title);
        printModuleFlag("Conversation", conversation);
        printModuleFlag("Announcer(+Subtitle)", announcer);
        printModuleFlag("Pickup", pickup);
        printModuleFlag("Prop", prop);
        printModuleFlag("RGB", rgb);
        printModuleFlag("LoginView", loginView);
        printModuleFlag("OnlineRewards", onlineRewards);
        printModuleFlag("Warehouse", warehouse);
        printModuleFlag("Mail", mail);
        printModuleFlag("Chat", chat);
        printModuleFlag("QuestGPS", questGps);
        printModuleFlag("Map", map);
        if (taczCombatBridge != null && taczCombatBridge.isActive()) {
            printModuleFlag("TACZ", true);
        }
    }

    private void printModuleFlag(String name, boolean loaded) {
        consoleInfo(ChatColor.GRAY + " - " + ChatColor.WHITE + name + ": "
            + (loaded ? ChatColor.GREEN + "已启用" : ChatColor.GRAY + "未启用"));
    }

    private void sendConsoleBanner(ChatColor accent, String title, String... lines) {
        if (accent == ChatColor.RED) {
            consoleError(title);
        } else if (accent == ChatColor.YELLOW) {
            consoleWarn(title);
        } else {
            consoleInfo(title);
        }
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            consoleInfo(ChatColor.GRAY + " - " + ChatColor.WHITE + line);
        }
    }

    private boolean shouldRewriteUiFile(File target, boolean overwrite) throws IOException {
        if (!target.exists()) {
            return true;
        }
        if (overwrite) {
            return true;
        }
        String signature = ArcartXHudTemplateWriter.signature();
        return !Files.readString(target.toPath()).contains(signature);
    }

    private boolean shouldRewriteAnnouncerUi(File target, boolean overwrite) throws IOException {
        if (!target.exists()) {
            return true;
        }
        return overwrite;
    }

    private boolean shouldRewritePickupUi(File target, boolean overwrite, String expectedSignature) throws IOException {
        if (!target.exists()) {
            return true;
        }
        if (overwrite) {
            return true;
        }
        String content = Files.readString(target.toPath());
        if (content.contains(expectedSignature)) {
            return false;
        }
        return content.contains(PICKUP_UI_SIGNATURE_PREFIX);
    }

    private record UiBinding(String runtimeUiId, String registeredUiId) {
    }
}
