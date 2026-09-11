package net.runelite.client.plugins.microbot.simplemining;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.plugins.microbot.simplemining.enums.InventoryMode;
import net.runelite.client.plugins.microbot.simplemining.enums.MiningStage;
import net.runelite.client.plugins.microbot.simplemining.enums.WorldMode;

@ConfigGroup("SimpleMining")
public interface SimpleMiningConfig extends Config {

    String CURRENT_AREA = "__current_area__";

    @ConfigSection(name = "Progression", description = "What to mine and when to switch", position = 0)
    String PROGRESSION_SECTION = "progression";

    @ConfigSection(name = "Inventory", description = "What to do when the inventory is full", position = 1)
    String INVENTORY_SECTION = "inventory";

    @ConfigSection(name = "Mining", description = "Mining behaviour", position = 2,
            closedByDefault = true)
    String MINING_SECTION = "mining";

    // Mode, rock and location live in the sidebar. These hidden values persist the selection.
    @ConfigItem(keyName = "autoProgress", name = "Auto progression",
            description = "Automatically use the fastest conventional rock-mining method unlocked for your level",
            position = 0, section = PROGRESSION_SECTION, hidden = true)
    default boolean autoProgress() {
        return true;
    }

    @ConfigItem(keyName = "manualStage", name = "Manual rock",
            description = "Rock to mine when auto progression is off",
            position = 1, section = PROGRESSION_SECTION, hidden = true)
    default MiningStage manualStage() {
        return MiningStage.CLAY;
    }

    @ConfigItem(keyName = "manualLocation", name = "Manual location",
            description = "Pinned preset, nearest predefined route, or the area where the script starts",
            position = 2, section = PROGRESSION_SECTION, hidden = true)
    default String manualLocation() {
        return "";
    }

    @ConfigItem(keyName = "worldMode", name = "World type",
            description = "Auto follows the world you are logged into. Members enables Shilo, the expanded Mining Guild and amethyst",
            position = 3, section = PROGRESSION_SECTION)
    default WorldMode worldMode() {
        return WorldMode.AUTO;
    }

    @Range(min = 2, max = 99)
    @ConfigItem(keyName = "targetLevel", name = "Target level",
            description = "Stop once your Mining level reaches this",
            position = 4, section = PROGRESSION_SECTION)
    default int targetLevel() {
        return 99;
    }

    @ConfigItem(keyName = "inventoryMode", name = "When full",
            description = "Bank valuable output or power-mine by dropping it",
            position = 0, section = INVENTORY_SECTION)
    default InventoryMode inventoryMode() {
        return InventoryMode.BANK;
    }

    @ConfigItem(keyName = "usePickaxeSpec", name = "Use pickaxe special",
            description = "Use a Dragon, Infernal or 3rd age pickaxe special attack when available",
            position = 0, section = MINING_SECTION)
    default boolean usePickaxeSpec() {
        return true;
    }

    @Range(min = 0, max = 10)
    @ConfigItem(keyName = "maxPlayers", name = "Hop above players",
            description = "Hop when this many other players are within the mining area. 0 disables hopping",
            position = 1, section = MINING_SECTION)
    default int maxPlayers() {
        return 0;
    }
}
