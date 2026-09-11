package net.runelite.client.plugins.microbot.simplemining.enums;

import java.util.function.BooleanSupplier;

public enum WorldMode {
    AUTO("Auto (detect world)"),
    MEMBERS("Members"),
    FREE("Free-to-play");

    private final String displayName;

    WorldMode(String displayName) {
        this.displayName = displayName;
    }

    public boolean isMembersWorld(BooleanSupplier detector) {
        if (this == MEMBERS) {
            return true;
        }
        if (this == FREE) {
            return false;
        }
        try {
            return detector != null && detector.getAsBoolean();
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}
