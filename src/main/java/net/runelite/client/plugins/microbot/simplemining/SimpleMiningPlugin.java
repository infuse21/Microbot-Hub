package net.runelite.client.plugins.microbot.simplemining;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.WorldType;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.PluginConstants;
import net.runelite.client.plugins.microbot.simplemining.enums.MiningStage;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

@PluginDescriptor(
        name = PluginConstants.DEFAULT_PREFIX + "Simple Mining",
        description = "Fast Mining progression plus a curated catalogue of worthwhile money rocks",
        tags = {"mining", "skilling", "progression", "profit"},
        authors = {"Infuse"},
        version = SimpleMiningPlugin.version,
        minClientVersion = "2.6.14",
        enabledByDefault = PluginConstants.DEFAULT_ENABLED,
        isExternal = PluginConstants.IS_EXTERNAL
)
@Slf4j
public class SimpleMiningPlugin extends Plugin {
    public static final String version = "1.0.6";

    @Inject
    @Getter
    private SimpleMiningConfig config;
    @Inject
    @Getter
    private SimpleMiningScript script;
    @Inject
    private ClientToolbar clientToolbar;

    private SimpleMiningPanel panel;
    private NavigationButton navButton;
    private ExecutorService uiExecutor;
    private int startXp;
    private long startTime;

    @Provides
    SimpleMiningConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SimpleMiningConfig.class);
    }

    @Override
    protected void startUp() {
        uiExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "SimpleMining-ui");
            thread.setDaemon(true);
            return thread;
        });
        panel = injector.getInstance(SimpleMiningPanel.class);
        panel.bind(this);
        navButton = NavigationButton.builder()
                .tooltip("Simple Mining")
                .icon(buildIcon())
                .priority(7)
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navButton);
        refreshPanel();
    }

    @Override
    protected void shutDown() {
        script.shutdown();
        if (panel != null) {
            panel.unbind();
            panel = null;
        }
        if (navButton != null) {
            clientToolbar.removeNavigation(navButton);
            navButton = null;
        }
        if (uiExecutor != null) {
            uiExecutor.shutdownNow();
            uiExecutor = null;
        }
    }

    public void startScript() {
        submitAction(() -> {
            if (script.isRunning()) {
                return;
            }
            startXp = currentXp();
            startTime = System.currentTimeMillis();
            script.run(config);
            refreshPanel();
            log.info("Simple Mining started");
        });
    }

    public void stopScript() {
        submitAction(() -> {
            if (script.isRunning()) {
                script.shutdown();
                log.info("Simple Mining stopped");
            }
        });
    }

    public void togglePause() {
        if (script.isRunning()) {
            script.togglePause();
        }
    }

    public boolean isScriptRunning() {
        return script.isRunning();
    }

    public boolean isScriptPaused() {
        return script.isRunning() && script.isPaused();
    }

    private void submitAction(Runnable action) {
        ExecutorService executor = uiExecutor;
        if (executor == null || executor.isShutdown()) {
            return;
        }
        executor.submit(() -> {
            try {
                action.run();
            } catch (Exception ex) {
                log.error("Simple Mining control action failed", ex);
            }
        });
    }

    public static final BooleanSupplier MEMBER_WORLD = () -> {
        Client client = Microbot.getClient();
        if (client == null) {
            return false;
        }
        EnumSet<WorldType> types = client.getWorldType();
        return types != null && types.contains(WorldType.MEMBERS);
    };

    public static boolean isMembersWorld(SimpleMiningConfig config) {
        return config.worldMode().isMembersWorld(MEMBER_WORLD);
    }

    public static final Function<Quest, QuestState> QUEST_STATES = quest -> {
        try {
            return Rs2Player.getQuestState(quest);
        } catch (Exception ignored) {
            return null;
        }
    };

    public MiningStage resolveDisplayStage(int miningLevel, int sailingLevel) {
        return config.autoProgress()
                ? MiningStage.bestFor(miningLevel, sailingLevel, isMembersWorld(config), QUEST_STATES)
                : config.manualStage();
    }

    private Map<MiningStage, String> lockReasons(int miningLevel, int sailingLevel) {
        Map<MiningStage, String> reasons = new EnumMap<>(MiningStage.class);
        for (MiningStage stage : MiningStage.values()) {
            String reason = stage.lockReason(miningLevel, sailingLevel,
                    isMembersWorld(config), QUEST_STATES);
            if (reason != null) {
                reasons.put(stage, reason);
            }
        }
        return reasons;
    }

    @Subscribe
    public void onStatChanged(StatChanged event) {
        if (event.getSkill() == Skill.MINING || event.getSkill() == Skill.SAILING) {
            refreshPanel();
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (SimpleMiningPanel.CONFIG_GROUP.equals(event.getGroup())) {
            refreshPanel();
        }
    }

    private void refreshPanel() {
        if (panel == null) {
            return;
        }
        int level = Rs2Player.getRealSkillLevel(Skill.MINING);
        int sailingLevel = Rs2Player.getRealSkillLevel(Skill.SAILING);
        panel.update(level, sailingLevel, resolveDisplayStage(level, sailingLevel),
                isMembersWorld(config), config.autoProgress(),
                lockReasons(level, sailingLevel), config.manualLocation());
    }

    private int currentXp() {
        Client client = Microbot.getClient();
        return client == null ? 0 : client.getSkillExperience(Skill.MINING);
    }

    public int getXpGained() {
        return startTime == 0 ? 0 : Math.max(0, currentXp() - startXp);
    }

    public long getRuntimeMillis() {
        return startTime == 0 ? 0 : System.currentTimeMillis() - startTime;
    }

    public String getFormattedRuntime() {
        long millis = getRuntimeMillis();
        return String.format("%02d:%02d:%02d", millis / 3600000,
                (millis % 3600000) / 60000, (millis % 60000) / 1000);
    }

    public int getXpPerHour() {
        long runtime = getRuntimeMillis();
        return runtime <= 0 ? 0 : (int) (getXpGained() * 3600000.0 / runtime);
    }

    private static BufferedImage buildIcon() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(143, 96, 57));
            g.rotate(-Math.PI / 4, 8, 8);
            g.fillRoundRect(7, 3, 2, 12, 2, 2);
            g.setColor(new Color(190, 198, 207));
            g.fillRoundRect(3, 2, 10, 4, 2, 2);
            g.setColor(new Color(236, 164, 50));
            g.drawLine(4, 6, 12, 6);
        } finally {
            g.dispose();
        }
        return image;
    }
}
