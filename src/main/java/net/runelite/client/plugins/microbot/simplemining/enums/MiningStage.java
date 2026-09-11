package net.runelite.client.plugins.microbot.simplemining.enums;

import lombok.Getter;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntBiFunction;

/**
 * The deliberately small Simple Mining catalogue.
 *
 * <p>The automatic ladder is about XP, not ore value: copper/tin to 15, iron to 45,
 * then granite. Manual mode exposes the standard ore catalogue without allowing the
 * slower middle-tier ores to dilute automatic progression.</p>
 */
@Getter
public enum MiningStage {
    COPPER_TIN("Copper & tin", "XP starter", 1, false, true,
            new String[]{"Copper rocks", "Tin rocks"},
            new int[]{ObjectID.COPPER_ROCKS, ObjectID.TIN_ROCKS, ObjectID.COPPER_ROCKS_10943,
                    ObjectID.COPPER_ROCKS_11161, ObjectID.TIN_ROCKS_11360, ObjectID.TIN_ROCKS_11361},
            new String[]{"Copper ore", "Tin ore"}, ItemID.COPPER_ORE,
            new MiningLocation[]{
                    MiningLocation.of("Lumbridge swamp east", 3227, 3148,
                            "5 copper · 5 tin · beginner mine"),
                    MiningLocation.of("Varrock south-east", 3286, 3365, "15 rocks"),
                    MiningLocation.members("West Falador", 2908, 3362,
                            "2 copper · 6 tin · bank wall (5 Agility)"),
                    MiningLocation.of("Rimmington", 2978, 3239,
                            "5 copper · 2 tin · no monsters"),
                    MiningLocation.of("Dwarven Mine", 3036, 9805,
                            "11 copper · 10 tin · F2P underground")
            }),

    CLAY("Clay", "Money", 1, false, false,
            new String[]{"Clay rocks"},
            new int[]{ObjectID.CLAY_ROCKS, ObjectID.CLAY_ROCKS_11363,
                    ObjectID.SOFT_CLAY_ROCKS, ObjectID.SOFT_CLAY_ROCKS_34957,
                    ObjectID.SOFT_CLAY_ROCKS_36210},
            new String[]{"Clay", "Soft clay"}, ItemID.CLAY,
            new MiningLocation[]{
                    MiningLocation.of("Varrock south-west", 3176, 3370, "bankable"),
                    MiningLocation.of("Rimmington", 2978, 3239,
                            "2 rocks · useful beside Rimmington POHs"),
                    MiningLocation.of("Dwarven Mine", 3041, 9815,
                            "5 rocks · F2P underground"),
                    MiningLocation.gatedAtMiningLevel("Trahaearn soft clay", 3271, 6050,
                            "10 rocks · deposit minecart · crystal shards", 70,
                            Quest.SONG_OF_THE_ELVES)
            }),

    IRON("Iron", "Fast XP", 15, false, true,
            new String[]{"Iron rocks"},
            new int[]{ObjectID.IRON_ROCKS, ObjectID.IRON_ROCKS_11365, ObjectID.IRON_ROCKS_36203},
            new String[]{"Iron ore"}, ItemID.IRON_ORE,
            new MiningLocation[]{
                    MiningLocation.of("Varrock south-east", 3286, 3365, "4 rocks"),
                    MiningLocation.of("Al Kharid mine", 3298, 3312,
                            "9 rocks · three-rock triangle"),
                    MiningLocation.of("Dwarven Mine", 3043, 9800,
                            "9 rocks · F2P underground"),
                    MiningLocation.members("West Falador", 2908, 3362,
                            "3 rocks · bank wall (5 Agility)"),
                    MiningLocation.atMiningLevel("Mining Guild (F2P)", 3040, 9740,
                            "4 rocks · level 60 entry", 60),
                    MiningLocation.membersAtMiningLevel("Mining Guild (members)", 3032, 9720,
                            "8 rocks · bank chest", 60),
                    MiningLocation.members("Lovakengj mine", 1477, 3778,
                            "4 rocks · three-rock triangle"),
                    MiningLocation.manualGated("Trahaearn mine", 3271, 6050,
                            "26 rocks · deposit minecart · crystal shards",
                            Quest.SONG_OF_THE_ELVES)
            }),

    SILVER("Silver", "Manual ore", 20, false, false,
            new String[]{"Silver rocks"},
            new int[]{ObjectID.SILVER_ROCKS, ObjectID.SILVER_ROCKS_11369, ObjectID.SILVER_ROCKS_36205},
            new String[]{"Silver ore"}, ItemID.SILVER_ORE,
            new MiningLocation[]{
                    MiningLocation.of("Al Kharid mine", 3298, 3312, "5 rocks · F2P"),
                    MiningLocation.members("Mor Ul Rek north", 2458, 5167,
                            "3 rocks · beside bank · fire cape access"),
                    MiningLocation.gated("Trahaearn mine", 3271, 6050,
                            "8 rocks · deposit minecart · crystal shards",
                            Quest.SONG_OF_THE_ELVES)
            }),

    COAL("Coal", "Manual ore", 30, false, false,
            new String[]{"Coal rocks"},
            new int[]{ObjectID.COAL_ROCKS, ObjectID.COAL_ROCKS_11366,
                    ObjectID.COAL_ROCKS_11367, ObjectID.COAL_ROCKS_36204},
            new String[]{"Coal"}, ItemID.COAL,
            new MiningLocation[]{
                    MiningLocation.of("Barbarian Village", 3078, 3421, "4 rocks · closest F2P bank"),
                    MiningLocation.of("Lumbridge swamp west", 3146, 3147,
                            "7 rocks · F2P alternative"),
                    MiningLocation.of("Dwarven Mine", 3044, 9781,
                            "11 rocks · F2P underground"),
                    MiningLocation.atMiningLevel("Mining Guild (F2P)", 3040, 9740,
                            "37 rocks · level 60 entry", 60),
                    MiningLocation.members("Lovakite Mine", 1430, 3849,
                            "45 coal rocks · bank inside"),
                    MiningLocation.membersAtMiningLevel("Mining Guild (members)", 3032, 9720,
                            "20 rocks · bank chest", 60),
                    MiningLocation.gated("Trahaearn mine", 3271, 6050,
                            "19 rocks · deposit minecart · crystal shards",
                            Quest.SONG_OF_THE_ELVES)
            }),

    GOLD("Gold", "Manual ore", 40, false, false,
            new String[]{"Gold rocks"},
            new int[]{ObjectID.GOLD_ROCKS, ObjectID.GOLD_ROCKS_11371, ObjectID.GOLD_ROCKS_36206},
            new String[]{"Gold ore"}, ItemID.GOLD_ORE,
            new MiningLocation[]{
                    MiningLocation.of("Al Kharid mine south", 3298, 3282, "2 rocks · F2P bank nearby"),
                    MiningLocation.of("Rimmington", 2978, 3239,
                            "2 rocks · safe F2P alternative"),
                    MiningLocation.of("Dwarven Mine", 3049, 9762,
                            "2 rocks · F2P underground"),
                    MiningLocation.members("Mor Ul Rek north", 2458, 5167,
                            "3 rocks · beside bank · fire cape access"),
                    MiningLocation.gated("North Brimhaven", 2732, 3225,
                            "10 rocks · Shilo cart banking", Quest.SHILO_VILLAGE),
                    MiningLocation.gated("Trahaearn mine", 3271, 6050,
                            "14 rocks · deposit minecart · crystal shards",
                            Quest.SONG_OF_THE_ELVES)
            }),

    GRANITE("Granite", "Fastest rock XP", 45, true, true,
            new String[0], new int[]{ObjectID.GRANITE_ROCKS},
            new String[]{"Granite (500g)", "Granite (2kg)", "Granite (5kg)"}, -1,
            new MiningLocation[]{
                    MiningLocation.membersHot("Desert Quarry", 3171, 2912,
                            "34 rocks · heat protection"),
                    MiningLocation.gatedHot("Necropolis mine", 3319, 2708,
                            "5 rocks · heat protection", Quest.BENEATH_CURSED_SANDS),
                    MiningLocation.sailingGated("Cape Conch mine", 3248, 2352,
                            "9 rocks · no desert heat", 45, Quest.TROUBLED_TORTUGANS)
            }),

    GEM("Gem rocks", "Money", 40, true, false,
            new String[]{"Gem rocks"},
            new int[]{ObjectID.GEM_ROCKS, ObjectID.GEM_ROCKS_11381},
            new String[]{"Uncut opal", "Uncut jade", "Uncut red topaz", "Uncut sapphire",
                    "Uncut emerald", "Uncut ruby", "Uncut diamond"}, -1,
            new MiningLocation[]{
                    MiningLocation.gated("Shilo Village", 2822, 3001, "7 rocks", Quest.SHILO_VILLAGE)
            }),

    MITHRIL("Mithril", "Manual ore", 55, false, false,
            new String[]{"Mithril rocks"},
            new int[]{ObjectID.MITHRIL_ROCKS, ObjectID.MITHRIL_ROCKS_11373, ObjectID.MITHRIL_ROCKS_36207},
            new String[]{"Mithril ore"}, ItemID.MITHRIL_ORE,
            new MiningLocation[]{
                    MiningLocation.of("Al Kharid mine", 3298, 3312, "2 rocks · levels 55–59"),
                    MiningLocation.of("Dwarven Mine", 3036, 9774,
                            "2 rocks · F2P underground"),
                    MiningLocation.atMiningLevel("Mining Guild (F2P)", 3040, 9740,
                            "5 rocks · level 60 entry", 60),
                    MiningLocation.membersAtMiningLevel("Mining Guild (members)", 3032, 9720,
                            "10 rocks · bank chest", 60),
                    MiningLocation.of("Lumbridge swamp west", 3146, 3147,
                            "5 rocks · F2P alternative"),
                    MiningLocation.gated("Trahaearn mine", 3271, 6050,
                            "7 rocks · deposit minecart · crystal shards",
                            Quest.SONG_OF_THE_ELVES)
            }),

    ADAMANT("Adamantite", "Manual ore", 70, false, false,
            new String[]{"Adamantite rocks"},
            new int[]{ObjectID.ADAMANTITE_ROCKS, ObjectID.ADAMANTITE_ROCKS_11375,
                    ObjectID.ADAMANTITE_ROCKS_36208},
            new String[]{"Adamantite ore"}, ItemID.ADAMANTITE_ORE,
            new MiningLocation[]{
                    MiningLocation.of("Al Kharid mine", 3298, 3312, "2 rocks · low risk"),
                    MiningLocation.of("Dwarven Mine", 3038, 9771,
                            "3 rocks · F2P underground"),
                    MiningLocation.atMiningLevel("Mining Guild (F2P)", 3040, 9740,
                            "2 rocks · best F2P", 60),
                    MiningLocation.membersAtMiningLevel("Mining Guild (members)", 3032, 9720,
                            "8 rocks · bank chest", 60),
                    MiningLocation.of("Lumbridge swamp west", 3146, 3147,
                            "2 rocks · F2P alternative"),
                    MiningLocation.gated("Trahaearn mine", 3271, 6050,
                            "7 rocks · deposit minecart · crystal shards",
                            Quest.SONG_OF_THE_ELVES)
            }),

    URT_SALT("Urt salt", "Teleport supplies", 72, true, false,
            new String[]{"Urt salt rocks"}, new int[]{ObjectID.URT_SALT_ROCKS},
            new String[]{"Urt salt"}, -1,
            new MiningLocation[]{
                    MiningLocation.gated("Weiss Salt Mine", 2833, 10339,
                            "5 rocks", Quest.MAKING_FRIENDS_WITH_MY_ARM)
            }),

    EFH_SALT("Efh salt", "Teleport supplies", 72, true, false,
            new String[]{"Efh salt rocks"}, new int[]{ObjectID.EFH_SALT_ROCKS},
            new String[]{"Efh salt"}, -1,
            new MiningLocation[]{
                    MiningLocation.gated("Weiss Salt Mine", 2837, 10338,
                            "5 rocks", Quest.MAKING_FRIENDS_WITH_MY_ARM)
            }),

    TE_SALT("Te salt", "Teleport supplies", 72, true, false,
            new String[]{"Te salt rocks"}, new int[]{ObjectID.TE_SALT_ROCKS},
            new String[]{"Te salt"}, -1,
            new MiningLocation[]{
                    MiningLocation.gated("Weiss Salt Mine", 2836, 10333,
                            "5 rocks", Quest.MAKING_FRIENDS_WITH_MY_ARM)
            }),

    BASALT("Basalt", "Teleport supplies", 72, true, false,
            new String[]{"Basalt rocks"}, new int[]{ObjectID.BASALT_ROCKS},
            new String[]{"Basalt"}, ItemID.BASALT,
            new MiningLocation[]{
                    MiningLocation.gated("Weiss Salt Mine", 2838, 10338,
                            "5 rocks · Snowflake can note basalt",
                            Quest.MAKING_FRIENDS_WITH_MY_ARM)
            }),

    RUNITE("Runite", "Money", 85, true, false,
            new String[]{"Runite rocks"},
            new int[]{ObjectID.RUNITE_ROCKS, ObjectID.RUNITE_ROCKS_11377, ObjectID.RUNITE_ROCKS_36209},
            new String[]{"Runite ore"}, ItemID.RUNITE_ORE,
            new MiningLocation[]{
                    MiningLocation.members("Mining Guild", 3032, 9720, "2 rocks · bank close"),
                    MiningLocation.gated("Trahaearn mine", 3271, 6050,
                            "4 rocks · deposit minecart · crystal shards",
                            Quest.SONG_OF_THE_ELVES)
            }),

    AMETHYST("Amethyst", "AFK money", 92, true, false,
            new String[]{"Amethyst crystals"},
            new int[]{ObjectID.AMETHYST_CRYSTALS, ObjectID.AMETHYST_CRYSTALS_11389},
            new String[]{"Amethyst"}, ItemID.AMETHYST,
            new MiningLocation[]{
                    MiningLocation.members("Mining Guild", 3022, 9704, "26 crystals · bank close")
            });

    private final String displayName;
    private final String purpose;
    private final int minLevel;
    private final boolean membersOnly;
    private final boolean inAutoLadder;
    private final String[] objectNames;
    private final int[] objectIds;
    private final String[] outputNames;
    private final int priceItemId;
    private final List<MiningLocation> locations;

    MiningStage(String displayName, String purpose, int minLevel, boolean membersOnly,
                boolean inAutoLadder, String[] objectNames, int[] objectIds,
                String[] outputNames, int priceItemId, MiningLocation[] locations) {
        this.displayName = displayName;
        this.purpose = purpose;
        this.minLevel = minLevel;
        this.membersOnly = membersOnly;
        this.inAutoLadder = inAutoLadder;
        this.objectNames = objectNames;
        this.objectIds = objectIds;
        this.outputNames = outputNames;
        this.priceItemId = priceItemId;
        this.locations = Collections.unmodifiableList(Arrays.asList(locations));
    }

    public List<MiningLocation> availableLocations(int miningLevel, int sailingLevel,
                                                    boolean membersWorld,
                                                    Function<Quest, QuestState> questStates) {
        List<MiningLocation> available = new ArrayList<>();
        for (MiningLocation location : locations) {
            if (location.isUnlocked(miningLevel, sailingLevel, membersWorld, questStates)) {
                available.add(location);
            }
        }
        return available;
    }

    public MiningLocation findLocation(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (MiningLocation location : locations) {
            if (location.getName().equalsIgnoreCase(name)) {
                return location;
            }
        }
        return null;
    }

    public MiningLocation fastestLocation(WorldPoint from, int miningLevel, int sailingLevel,
                                          boolean membersWorld,
                                          Function<Quest, QuestState> questStates,
                                          ToIntBiFunction<WorldPoint, WorldPoint> pathTiles,
                                          boolean autoProgress) {
        List<MiningLocation> available = availableLocations(miningLevel, sailingLevel,
                membersWorld, questStates);
        if (autoProgress) {
            List<MiningLocation> preferred = new ArrayList<>();
            for (MiningLocation location : available) {
                if (location.isAutoPreferred()) {
                    preferred.add(location);
                }
            }
            if (!preferred.isEmpty()) {
                available = preferred;
            }
        }
        if (available.isEmpty()) {
            return null;
        }
        if (available.size() == 1 || from == null || pathTiles == null) {
            return available.get(0);
        }
        MiningLocation best = available.get(0);
        int bestTiles = Integer.MAX_VALUE;
        for (MiningLocation location : available) {
            int tiles = pathTiles.applyAsInt(from, location.getPoint());
            if (tiles < bestTiles) {
                bestTiles = tiles;
                best = location;
            }
        }
        return best;
    }

    public String lockReason(int miningLevel, int sailingLevel, boolean membersWorld,
                             Function<Quest, QuestState> questStates) {
        if (miningLevel < minLevel) {
            return "Lv " + minLevel;
        }
        if (membersOnly && !membersWorld) {
            return "P2P only";
        }
        if (availableLocations(miningLevel, sailingLevel, membersWorld, questStates).isEmpty()) {
            MiningLocation location = locations.get(0);
            String reason = location.lockReason(miningLevel, sailingLevel, membersWorld, questStates);
            return reason == null ? "No accessible location" : reason;
        }
        return null;
    }

    public boolean isAvailable(int miningLevel, int sailingLevel, boolean membersWorld,
                               Function<Quest, QuestState> questStates) {
        return lockReason(miningLevel, sailingLevel, membersWorld, questStates) == null;
    }

    public boolean isAutoStage(boolean membersWorld) {
        return inAutoLadder && (!membersOnly || membersWorld);
    }

    public static MiningStage bestFor(int miningLevel, int sailingLevel, boolean membersWorld,
                                      Function<Quest, QuestState> questStates) {
        MiningStage best = COPPER_TIN;
        for (MiningStage stage : values()) {
            if (stage.isAutoStage(membersWorld)
                    && stage.minLevel >= best.minLevel
                    && stage.isAvailable(miningLevel, sailingLevel, membersWorld, questStates)) {
                best = stage;
            }
        }
        return best;
    }

    public MiningStage next(boolean membersWorld) {
        MiningStage next = null;
        for (MiningStage stage : values()) {
            if (!stage.isAutoStage(membersWorld) || stage.minLevel <= minLevel) {
                continue;
            }
            if (next == null || stage.minLevel < next.minLevel) {
                next = stage;
            }
        }
        return next;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
