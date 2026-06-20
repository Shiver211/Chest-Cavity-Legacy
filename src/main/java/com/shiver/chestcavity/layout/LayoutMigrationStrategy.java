package com.shiver.chestcavity.layout;

public enum LayoutMigrationStrategy {
    KEEP_BY_INDEX,
    DROP_OVERFLOW,
    MOVE_TO_PLAYER,
    CLEAR,
    SCRIPTED_MIGRATION;

    public static LayoutMigrationStrategy byName(String name) {
        if (name == null || name.isEmpty()) {
            return KEEP_BY_INDEX;
        }
        for (LayoutMigrationStrategy strategy : values()) {
            if (strategy.name().equalsIgnoreCase(name)) {
                return strategy;
            }
        }
        return KEEP_BY_INDEX;
    }
}
