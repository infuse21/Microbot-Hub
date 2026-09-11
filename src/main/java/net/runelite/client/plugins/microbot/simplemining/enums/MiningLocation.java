package net.runelite.client.plugins.microbot.simplemining.enums;

import lombok.Getter;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.coords.WorldPoint;

import java.util.function.Function;

@Getter
public final class MiningLocation {
    private final String name;
    private final String note;
    private final WorldPoint point;
    private final boolean membersOnly;
    private final int minMiningLevel;
    private final int minSailingLevel;
    private final Quest quest;
    private final boolean questMustBeFinished;
    private final boolean desertHeat;
    private final boolean autoPreferred;

    private MiningLocation(String name, String note, WorldPoint point, boolean membersOnly,
                           int minMiningLevel, int minSailingLevel, Quest quest,
                           boolean questMustBeFinished, boolean desertHeat,
                           boolean autoPreferred) {
        this.name = name;
        this.note = note;
        this.point = point;
        this.membersOnly = membersOnly;
        this.minMiningLevel = minMiningLevel;
        this.minSailingLevel = minSailingLevel;
        this.quest = quest;
        this.questMustBeFinished = questMustBeFinished;
        this.desertHeat = desertHeat;
        this.autoPreferred = autoPreferred;
    }

    public static MiningLocation of(String name, int x, int y, String note) {
        return new MiningLocation(name, note, new WorldPoint(x, y, 0), false, 1, 1,
                null, true, false, true);
    }

    public static MiningLocation members(String name, int x, int y, String note) {
        return new MiningLocation(name, note, new WorldPoint(x, y, 0), true, 1, 1,
                null, true, false, true);
    }

    public static MiningLocation membersHot(String name, int x, int y, String note) {
        return new MiningLocation(name, note, new WorldPoint(x, y, 0), true, 1, 1,
                null, true, true, true);
    }

    public static MiningLocation atMiningLevel(String name, int x, int y, String note, int minMiningLevel) {
        return new MiningLocation(name, note, new WorldPoint(x, y, 0), false, minMiningLevel, 1,
                null, true, false, true);
    }

    public static MiningLocation membersAtMiningLevel(String name, int x, int y, String note,
                                                       int minMiningLevel) {
        return new MiningLocation(name, note, new WorldPoint(x, y, 0), true, minMiningLevel, 1,
                null, true, false, true);
    }

    public static MiningLocation gated(String name, int x, int y, String note, Quest quest) {
        return new MiningLocation(name, note, new WorldPoint(x, y, 0), true, 1, 1,
                quest, true, false, true);
    }

    public static MiningLocation manualGated(String name, int x, int y, String note, Quest quest) {
        return new MiningLocation(name, note, new WorldPoint(x, y, 0), true, 1, 1,
                quest, true, false, false);
    }

    public static MiningLocation gatedAtMiningLevel(String name, int x, int y, String note,
                                                     int minMiningLevel, Quest quest) {
        return new MiningLocation(name, note, new WorldPoint(x, y, 0), true, minMiningLevel, 1,
                quest, true, false, false);
    }

    public static MiningLocation gatedHot(String name, int x, int y, String note, Quest quest) {
        return new MiningLocation(name, note, new WorldPoint(x, y, 0), true, 1, 1,
                quest, true, true, true);
    }

    public static MiningLocation sailingGated(String name, int x, int y, String note,
                                               int minSailingLevel, Quest quest) {
        return new MiningLocation(name, note, new WorldPoint(x, y, 0), true, 1,
                minSailingLevel, quest, true, false, true);
    }

    public String lockReason(int miningLevel, int sailingLevel, boolean membersWorld,
                             Function<Quest, QuestState> questStates) {
        if (miningLevel < minMiningLevel) {
            return "Lv " + minMiningLevel + " Mining";
        }
        if (membersOnly && !membersWorld) {
            return "P2P only";
        }
        if (sailingLevel < minSailingLevel) {
            return "Lv " + minSailingLevel + " Sailing";
        }
        if (quest != null) {
            QuestState state;
            try {
                state = questStates.apply(quest);
            } catch (Exception ignored) {
                state = null;
            }
            if (state == null || state == QuestState.NOT_STARTED
                    || (questMustBeFinished && state != QuestState.FINISHED)) {
                return quest.getName();
            }
        }
        return null;
    }

    public boolean isUnlocked(int miningLevel, int sailingLevel, boolean membersWorld,
                              Function<Quest, QuestState> questStates) {
        return lockReason(miningLevel, sailingLevel, membersWorld, questStates) == null;
    }

    public boolean hasNote() {
        return note != null && !note.isEmpty();
    }

    @Override
    public String toString() {
        return name;
    }
}
