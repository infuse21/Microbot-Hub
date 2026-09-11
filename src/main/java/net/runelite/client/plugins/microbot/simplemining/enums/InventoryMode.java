package net.runelite.client.plugins.microbot.simplemining.enums;

public enum InventoryMode {
    BANK("Bank everything"),
    DROP("Drop mined items");

    private final String displayName;

    InventoryMode(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
