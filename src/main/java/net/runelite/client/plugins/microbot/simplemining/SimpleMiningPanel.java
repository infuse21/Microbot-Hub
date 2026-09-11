package net.runelite.client.plugins.microbot.simplemining;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.microbot.simplemining.enums.MiningLocation;
import net.runelite.client.plugins.microbot.simplemining.enums.MiningStage;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Map;

/** Sidebar patterned after SimpleFishing's separate Ladders and Manual pages. */
public class SimpleMiningPanel extends PluginPanel {
    static final String CONFIG_GROUP = "SimpleMining";

    private static final Color ACTIVE = ColorScheme.PROGRESS_COMPLETE_COLOR;
    private static final Color UNLOCKED = ColorScheme.BRAND_ORANGE;
    private static final Color LOCKED = ColorScheme.MEDIUM_GRAY_COLOR;
    private static final Color MUTED = ColorScheme.LIGHT_GRAY_COLOR;
    private static final Color CARD_BG = ColorScheme.DARKER_GRAY_COLOR;
    private static final Color CARD_HOVER_BG = ColorScheme.DARKER_GRAY_HOVER_COLOR;
    private static final Color ERROR = ColorScheme.PROGRESS_ERROR_COLOR;
    private static final int CARD_TEXT_WIDTH = PluginPanel.PANEL_WIDTH - 48;

    private final ConfigManager configManager;
    private SimpleMiningPlugin plugin;

    private final JPanel modeRow = new JPanel(new DynamicGridLayout(1, 2, 4, 0));
    private final JLabel laddersTab = new JLabel("Ladders", SwingConstants.CENTER);
    private final JLabel manualTab = new JLabel("Manual", SwingConstants.CENTER);
    private final JPanel pageHolder = new JPanel(new DynamicGridLayout(0, 1, 0, 3));
    private final JPanel laddersBody = new JPanel(new DynamicGridLayout(0, 1, 0, 3));
    private final JPanel manualBody = new JPanel(new DynamicGridLayout(0, 1, 0, 3));
    private final JPanel ladderColumns = new JPanel(new GridLayout(1, 2, 6, 0));
    private final JPanel stageList = new JPanel(new DynamicGridLayout(0, 1, 0, 6));

    private final JLabel levelValue = new JLabel("--");
    private final JLabel activeValue = new JLabel("-");
    private final JLabel nextValue = new JLabel("-");
    private final JLabel runtimeValue = new JLabel("00:00:00");
    private final JLabel xpRateValue = new JLabel("0 xp/h");
    private final JLabel runState = new JLabel("READY", SwingConstants.CENTER);
    private final JButton startButton = new JButton("Start");
    private final JButton pauseButton = new JButton("Pause");
    private final JButton stopButton = new JButton("Stop");
    private final ProgressBar progress = new ProgressBar();
    private final Timer refreshTimer;

    private int miningLevel = 1;
    private int sailingLevel = 1;
    private MiningStage activeStage = MiningStage.COPPER_TIN;
    private boolean membersWorld;
    private boolean autoProgress = true;
    private Map<MiningStage, String> lockReasons = Collections.emptyMap();
    private String pinnedLocation = "";

    @Inject
    public SimpleMiningPanel(ConfigManager configManager) {
        this.configManager = configManager;
        setBorder(new EmptyBorder(10, 8, 10, 8));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        add(buildHeader());
        add(buildModeToggle());
        add(buildControls());

        laddersBody.setBackground(ColorScheme.DARK_GRAY_COLOR);
        laddersBody.add(buildStatusCard());
        laddersBody.add(sectionLabel("FAST XP PROGRESSION"));
        ladderColumns.setBackground(ColorScheme.DARK_GRAY_COLOR);
        laddersBody.add(ladderColumns);

        manualBody.setBackground(ColorScheme.DARK_GRAY_COLOR);
        manualBody.add(sectionLabel("CURATED ROCKS"));
        stageList.setBackground(ColorScheme.DARK_GRAY_COLOR);
        manualBody.add(stageList);

        pageHolder.setBackground(ColorScheme.DARK_GRAY_COLOR);
        add(pageHolder);

        refreshTimer = new Timer(1000, event -> refreshRuntime());
        refreshTimer.setRepeats(true);
        refreshTimer.start();
        rebuild();
    }

    void bind(SimpleMiningPlugin plugin) {
        this.plugin = plugin;
        refreshRuntime();
    }

    void unbind() {
        refreshTimer.stop();
        plugin = null;
    }

    public void update(int miningLevel, int sailingLevel, MiningStage activeStage, boolean membersWorld,
                       boolean autoProgress, Map<MiningStage, String> lockReasons,
                       String pinnedLocation) {
        this.miningLevel = miningLevel;
        this.sailingLevel = sailingLevel;
        this.activeStage = activeStage;
        this.membersWorld = membersWorld;
        this.autoProgress = autoProgress;
        this.lockReasons = lockReasons == null ? Collections.emptyMap() : lockReasons;
        this.pinnedLocation = pinnedLocation == null ? "" : pinnedLocation;
        SwingUtilities.invokeLater(this::rebuild);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ColorScheme.DARK_GRAY_COLOR);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        JLabel title = new JLabel("SIMPLE MINING");
        title.setFont(FontManager.getRunescapeBoldFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(UNLOCKED);
        JLabel subtitle = fittedLabel("Fast XP ladders · useful F2P and P2P rocks",
                FontManager.getRunescapeSmallFont(), 18);
        subtitle.setForeground(MUTED);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);
        return header;
    }

    private JPanel buildModeToggle() {
        modeRow.setBackground(ColorScheme.DARK_GRAY_COLOR);
        modeRow.setBorder(new EmptyBorder(0, 0, 6, 0));
        styleTab(laddersTab, true);
        styleTab(manualTab, false);
        modeRow.add(laddersTab);
        modeRow.add(manualTab);
        return modeRow;
    }

    private void styleTab(JLabel tab, boolean auto) {
        tab.setFont(FontManager.getRunescapeSmallFont());
        tab.setOpaque(true);
        tab.setBorder(new EmptyBorder(6, 0, 6, 0));
        tab.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tab.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                setAutoProgress(auto);
            }
        });
    }

    private JPanel buildControls() {
        JPanel outer = new JPanel(new DynamicGridLayout(0, 1, 0, 4));
        outer.setBackground(CARD_BG);
        outer.setBorder(new EmptyBorder(7, 7, 7, 7));
        runState.setFont(FontManager.getRunescapeSmallFont());
        runState.setForeground(ACTIVE);
        outer.add(runState);

        JPanel buttons = new JPanel(new GridLayout(1, 3, 4, 0));
        buttons.setBackground(CARD_BG);
        styleButton(startButton, UNLOCKED);
        styleButton(pauseButton, MUTED);
        styleButton(stopButton, ColorScheme.PROGRESS_ERROR_COLOR);
        startButton.addActionListener(event -> { if (plugin != null) plugin.startScript(); });
        pauseButton.addActionListener(event -> { if (plugin != null) plugin.togglePause(); });
        stopButton.addActionListener(event -> { if (plugin != null) plugin.stopScript(); });
        buttons.add(startButton);
        buttons.add(pauseButton);
        buttons.add(stopButton);
        outer.add(buttons);
        return outer;
    }

    private void styleButton(JButton button, Color foreground) {
        button.setFont(FontManager.getRunescapeSmallFont());
        button.setForeground(foreground);
        button.setBackground(ColorScheme.DARK_GRAY_COLOR);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JPanel buildStatusCard() {
        JPanel card = new JPanel(new DynamicGridLayout(0, 1, 0, 4));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, ACTIVE),
                new EmptyBorder(8, 8, 8, 8)));
        JPanel levelRow = new JPanel(new BorderLayout());
        levelRow.setBackground(CARD_BG);
        JLabel caption = new JLabel("MINING LEVEL");
        caption.setFont(FontManager.getRunescapeSmallFont());
        caption.setForeground(MUTED);
        levelValue.setFont(FontManager.getRunescapeBoldFont().deriveFont(Font.BOLD, 20f));
        levelValue.setForeground(Color.WHITE);
        levelRow.add(caption, BorderLayout.WEST);
        levelRow.add(levelValue, BorderLayout.EAST);
        card.add(levelRow);
        card.add(progress);
        card.add(kvRow("Mining", activeValue, ACTIVE));
        card.add(kvRow("Next", nextValue, MUTED));
        card.add(kvRow("Runtime", runtimeValue, MUTED));
        card.add(kvRow("Rate", xpRateValue, UNLOCKED));
        return card;
    }

    private JPanel kvRow(String key, JLabel value, Color color) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(CARD_BG);
        JLabel label = new JLabel(key);
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setForeground(MUTED);
        value.setFont(FontManager.getRunescapeSmallFont());
        value.setForeground(color);
        value.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(label, BorderLayout.WEST);
        // CENTER is allowed to shrink. EAST would retain the value's full preferred width
        // and paint it over the caption on a narrow sidebar.
        row.add(value, BorderLayout.CENTER);
        return row;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setForeground(MUTED);
        label.setBorder(new EmptyBorder(10, 2, 2, 0));
        return label;
    }

    private void setAutoProgress(boolean auto) {
        autoProgress = auto;
        configManager.setConfiguration(CONFIG_GROUP, "autoProgress", auto);
        rebuild();
    }

    private void selectStage(MiningStage stage) {
        configManager.setConfiguration(CONFIG_GROUP, "manualStage", stage);
        configManager.setConfiguration(CONFIG_GROUP, "manualLocation", "");
        pinnedLocation = "";
        activeStage = stage;
        rebuild();
    }

    private void selectLocation(String location) {
        configManager.setConfiguration(CONFIG_GROUP, "manualLocation", location);
        pinnedLocation = location;
        rebuild();
    }

    private void rebuild() {
        paintTab(laddersTab, autoProgress);
        paintTab(manualTab, !autoProgress);
        JPanel wanted = autoProgress ? laddersBody : manualBody;
        if (pageHolder.getComponentCount() != 1 || pageHolder.getComponent(0) != wanted) {
            pageHolder.removeAll();
            pageHolder.add(wanted);
        }

        levelValue.setText(String.valueOf(miningLevel));
        activeValue.setText(activeStage.getDisplayName());
        activeValue.setToolTipText(activeStage.getDisplayName());
        MiningStage next = autoProgress ? activeStage.next(membersWorld) : null;
        if (!autoProgress) {
            nextValue.setText("manual");
            nextValue.setToolTipText(null);
            progress.setFraction(1f);
        } else if (next == null) {
            nextValue.setText("max stage");
            nextValue.setToolTipText(null);
            progress.setFraction(1f);
        } else {
            nextValue.setText(next.getDisplayName() + " @ " + next.getMinLevel());
            nextValue.setToolTipText(nextValue.getText());
            int span = next.getMinLevel() - activeStage.getMinLevel();
            int done = miningLevel - activeStage.getMinLevel();
            progress.setFraction(span <= 0 ? 1f : Math.max(0f, Math.min(1f, (float) done / span)));
        }

        if (autoProgress) {
            buildLadderColumns();
        } else {
            buildManualCatalogue();
        }
        refreshRuntime();
        revalidate();
        repaint();
    }

    private void buildLadderColumns() {
        ladderColumns.removeAll();
        ladderColumns.add(buildLadderColumn("MEMBERS", true));
        ladderColumns.add(buildLadderColumn("FREE", false));
    }

    private JPanel buildLadderColumn(String title, boolean members) {
        boolean yours = members == membersWorld;
        JPanel column = new JPanel(new DynamicGridLayout(0, 1, 0, 2));
        column.setBackground(yours ? CARD_BG : ColorScheme.DARK_GRAY_COLOR);
        column.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(yours ? UNLOCKED : LOCKED),
                new EmptyBorder(4, 5, 5, 4)));
        JLabel heading = new JLabel(title + (yours ? " •" : ""));
        heading.setFont(FontManager.getRunescapeSmallFont());
        heading.setForeground(yours ? UNLOCKED : MUTED);
        column.add(heading);
        for (MiningStage stage : MiningStage.values()) {
            if (!stage.isAutoStage(members)) {
                continue;
            }
            boolean live = yours && autoProgress && stage == activeStage;
            boolean reached = miningLevel >= stage.getMinLevel();
            JPanel row = new JPanel(new BorderLayout(3, 0));
            row.setBackground(column.getBackground());
            JLabel level = new JLabel(String.valueOf(stage.getMinLevel()));
            level.setFont(FontManager.getRunescapeSmallFont());
            level.setForeground(live ? ACTIVE : reached ? MUTED : LOCKED);
            JLabel name = fittedLabel((live ? "▸ " : "") + stage.getDisplayName(),
                    FontManager.getRunescapeSmallFont(), 120);
            name.setForeground(live ? ACTIVE : reached ? ColorScheme.TEXT_COLOR : LOCKED);
            row.add(level, BorderLayout.WEST);
            row.add(name, BorderLayout.CENTER);
            column.add(row);
        }
        return column;
    }

    private void buildManualCatalogue() {
        stageList.removeAll();
        JLabel hint = new JLabel("Choose a rock, then choose its location");
        hint.setFont(FontManager.getRunescapeSmallFont());
        hint.setForeground(UNLOCKED);
        hint.setBorder(new EmptyBorder(0, 2, 4, 0));
        stageList.add(hint);
        for (MiningStage stage : MiningStage.values()) {
            stageList.add(buildStageCard(stage));
        }
    }

    private JPanel buildStageCard(MiningStage stage) {
        boolean selected = stage == activeStage;
        String lock = lockReasons.get(stage);
        boolean unlocked = lock == null;
        JPanel card = new JPanel(new DynamicGridLayout(0, 1, 0, 3));
        card.setBackground(CARD_BG);
        Color accent = selected ? ACTIVE : unlocked ? UNLOCKED : LOCKED;
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, accent),
                new EmptyBorder(7, 8, 7, 8)));

        JPanel title = new JPanel(new BorderLayout(6, 0));
        title.setBackground(CARD_BG);
        String badgeText = selected ? "SELECTED" : unlocked ? "SELECT" : "LOCKED";
        JLabel badge = new JLabel(badgeText);
        badge.setFont(FontManager.getRunescapeSmallFont());
        badge.setHorizontalAlignment(SwingConstants.RIGHT);
        int badgeWidth = badge.getFontMetrics(badge.getFont()).stringWidth(badgeText) + 10;
        JLabel name = fittedLabel((selected ? "▶ " : "") + stage.getDisplayName(),
                FontManager.getRunescapeBoldFont(), badgeWidth);
        name.setForeground(selected ? ACTIVE : unlocked ? Color.WHITE : LOCKED);
        badge.setForeground(selected ? ACTIVE : unlocked ? MUTED : LOCKED);
        // CENTER can contract around the badge; WEST cannot and caused title collisions.
        title.add(name, BorderLayout.CENTER);
        title.add(badge, BorderLayout.EAST);
        card.add(title);

        String access = stage.isMembersOnly() ? "P2P" : "F2P + P2P";
        JLabel purpose = fittedLabel(stage.getPurpose() + "  ·  " + access,
                FontManager.getRunescapeSmallFont(), 8);
        purpose.setForeground(unlocked ? UNLOCKED : LOCKED);
        card.add(purpose);

        if (lock != null) {
            card.add(wrappedLabel("Requires: " + lock, ERROR));
        }

        boolean pickLocations = selected && unlocked;
        if (pickLocations) {
            JLabel choose = new JLabel("LOCATION");
            choose.setFont(FontManager.getRunescapeSmallFont());
            choose.setForeground(MUTED);
            choose.setBorder(new EmptyBorder(4, 0, 0, 0));
            card.add(choose);
            card.add(locationRow("Nearest accessible", "Automatically choose by route",
                    pinnedLocation.isEmpty(), ""));
            card.add(locationRow("Current area", "Use matching rocks near where you start",
                    SimpleMiningConfig.CURRENT_AREA.equals(pinnedLocation),
                    SimpleMiningConfig.CURRENT_AREA));
            for (MiningLocation location : stage.getLocations()) {
                String locationLock = location.lockReason(miningLevel, sailingLevel, membersWorld,
                        SimpleMiningPlugin.QUEST_STATES);
                String note = locationLock != null ? "Requires: " + locationLock
                        : location.hasNote() ? location.getNote() : "";
                if (locationLock == null) {
                    card.add(locationRow(location.getName(), note,
                            location.getName().equalsIgnoreCase(pinnedLocation), location.getName()));
                } else {
                    card.add(locationInfoRow(location.getName(), note, true));
                }
            }
        } else {
            int available = 0;
            for (MiningLocation location : stage.getLocations()) {
                if (location.lockReason(miningLevel, sailingLevel, membersWorld,
                        SimpleMiningPlugin.QUEST_STATES) == null) {
                    available++;
                }
            }
            String summary = available + " available · " + stage.getLocations().size()
                    + (stage.getLocations().size() == 1 ? " location" : " locations")
                    + " · Lv " + stage.getMinLevel();
            JLabel footer = fittedLabel(summary, FontManager.getRunescapeSmallFont(), 8);
            footer.setForeground(unlocked ? MUTED : LOCKED);
            card.add(footer);
        }
        if (!selected && unlocked) {
            attachSelection(card, stage);
        }
        return card;
    }

    private JPanel locationRow(String label, String note, boolean chosen, String value) {
        JPanel row = locationInfoRow(label, note, false);
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JLabel name = (JLabel) row.getComponent(0);
        name.setText((chosen ? "(o) " : "( ) ") + label);
        name.setForeground(chosen ? ACTIVE : ColorScheme.TEXT_COLOR);
        MouseAdapter listener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                selectLocation(value);
            }

            @Override
            public void mouseEntered(MouseEvent event) {
                setBackground(row, CARD_HOVER_BG);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                setBackground(row, CARD_BG);
            }
        };
        addListener(row, listener);
        return row;
    }

    /** A stacked location row keeps long mine names and notes from competing horizontally. */
    private JPanel locationInfoRow(String label, String note, boolean locked) {
        JPanel row = new JPanel(new DynamicGridLayout(0, 1, 0, 1));
        row.setBackground(CARD_BG);
        row.setBorder(new EmptyBorder(3, 5, 3, 3));
        JLabel name = fittedLabel((locked ? "- " : "") + label,
                FontManager.getRunescapeSmallFont(), 16);
        name.setForeground(locked ? LOCKED : ColorScheme.TEXT_COLOR);
        row.add(name);
        if (note != null && !note.isEmpty()) {
            row.add(wrappedLabel(note, locked ? ERROR : LOCKED));
        }
        return row;
    }

    private static JLabel wrappedLabel(String text, Color color) {
        JLabel label = new JLabel("<html><div style='width:" + CARD_TEXT_WIDTH + "px'>"
                + escapeHtml(text) + "</div></html>");
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setForeground(color);
        label.setToolTipText(text);
        return label;
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Truncate only as a final guard; the full value remains available as a tooltip. */
    private static JLabel fittedLabel(String text, Font font, int reservedForSibling) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        int available = Math.max(28, PluginPanel.PANEL_WIDTH - 34 - reservedForSibling);
        java.awt.FontMetrics metrics = label.getFontMetrics(font);
        if (metrics.stringWidth(text) <= available) {
            return label;
        }
        String shortened = text;
        while (shortened.length() > 1
                && metrics.stringWidth(shortened + "...") > available) {
            shortened = shortened.substring(0, shortened.length() - 1);
        }
        label.setText(shortened + "...");
        label.setToolTipText(text);
        return label;
    }

    private void attachSelection(JPanel card, MiningStage stage) {
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                selectStage(stage);
            }

            @Override
            public void mouseEntered(MouseEvent event) {
                setBackground(card, CARD_HOVER_BG);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                setBackground(card, CARD_BG);
            }
        };
        addListener(card, adapter);
    }

    private void addListener(java.awt.Container container, MouseAdapter adapter) {
        container.addMouseListener(adapter);
        for (java.awt.Component child : container.getComponents()) {
            if (child instanceof java.awt.Container) {
                addListener((java.awt.Container) child, adapter);
            } else {
                child.addMouseListener(adapter);
            }
        }
    }

    private void setBackground(java.awt.Container container, Color color) {
        container.setBackground(color);
        for (java.awt.Component child : container.getComponents()) {
            child.setBackground(color);
            if (child instanceof java.awt.Container) {
                setBackground((java.awt.Container) child, color);
            }
        }
        container.repaint();
    }

    private void paintTab(JLabel tab, boolean selected) {
        tab.setBackground(selected ? UNLOCKED : CARD_BG);
        tab.setForeground(selected ? Color.BLACK : MUTED);
    }

    private void refreshRuntime() {
        SimpleMiningPlugin current = plugin;
        if (current == null) {
            return;
        }
        boolean running = current.isScriptRunning();
        boolean paused = current.isScriptPaused();
        runState.setText(paused ? "PAUSED" : running
                ? current.getScript().getState().name() : "READY");
        runState.setForeground(paused ? UNLOCKED : running ? ACTIVE : MUTED);
        runtimeValue.setText(current.getFormattedRuntime());
        xpRateValue.setText(String.format("%,d xp/h", current.getXpPerHour()));
        startButton.setEnabled(!running);
        pauseButton.setEnabled(running);
        pauseButton.setText(paused ? "Resume" : "Pause");
        stopButton.setEnabled(running);
    }

    private static final class ProgressBar extends JPanel {
        private float fraction;

        private ProgressBar() {
            setBackground(CARD_BG);
            setPreferredSize(new Dimension(0, 6));
        }

        private void setFraction(float fraction) {
            this.fraction = fraction;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int height = getHeight();
                g.setColor(ColorScheme.SCROLL_TRACK_COLOR);
                g.fillRoundRect(0, 0, getWidth(), height, height, height);
                int filled = Math.round(getWidth() * fraction);
                if (filled > 0) {
                    g.setColor(ACTIVE);
                    g.fillRoundRect(0, 0, Math.max(filled, height), height, height, height);
                }
            } finally {
                g.dispose();
            }
        }
    }
}
