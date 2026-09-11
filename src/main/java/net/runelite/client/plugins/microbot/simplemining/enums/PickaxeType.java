package net.runelite.client.plugins.microbot.simplemining.enums;

import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

public enum PickaxeType {
    BRONZE("Bronze pickaxe", 1),
    IRON("Iron pickaxe", 1),
    STEEL("Steel pickaxe", 6),
    BLACK("Black pickaxe", 11),
    MITHRIL("Mithril pickaxe", 21),
    ADAMANT("Adamant pickaxe", 31),
    RUNE("Rune pickaxe", 41),
    GILDED("Gilded pickaxe", 41),
    DRAGON("Dragon pickaxe", 61),
    DRAGON_OR("Dragon pickaxe (or)", 61),
    INFERNAL("Infernal pickaxe", 61),
    THIRD_AGE("3rd age pickaxe", 61),
    CRYSTAL("Crystal pickaxe", 71);

    private final String itemName;
    private final int miningLevel;

    PickaxeType(String itemName, int miningLevel) {
        this.itemName = itemName;
        this.miningLevel = miningLevel;
    }

    public String getItemName() {
        return itemName;
    }

    public boolean meetsLevel() {
        return Rs2Player.getRealSkillLevel(Skill.MINING) >= miningLevel;
    }

    public boolean isHeld() {
        return Rs2Inventory.hasItem(itemName) || Rs2Equipment.isWearing(itemName);
    }

    public static PickaxeType bestHeld() {
        PickaxeType best = null;
        for (PickaxeType pickaxe : values()) {
            if (pickaxe.meetsLevel() && pickaxe.isHeld()) {
                best = pickaxe;
            }
        }
        return best;
    }
}
