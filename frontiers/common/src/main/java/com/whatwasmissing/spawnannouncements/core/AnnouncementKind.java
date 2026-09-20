package com.whatwasmissing.spawnannouncements.core;

import java.util.Locale;

/**
 * The public-facing reason for an announcement. The order is intentional:
 * combined traits get a more meaningful message than a generic rare message.
 */
public enum AnnouncementKind {
    SHINY_LEGENDARY("shiny_legendary", "shiny legendary"),
    SHINY_MYTHICAL("shiny_mythical", "shiny mythical"),
    SHINY_ULTRA_BEAST("shiny_ultra_beast", "shiny Ultra Beast"),
    SHINY_ALPHA("shiny_alpha", "shiny alpha"),
    SHINY_PARADOX("shiny_paradox", "shiny Paradox"),
    SHINY("shiny", "shiny"),
    LEGENDARY("legendary", "legendary"),
    MYTHICAL("mythical", "mythical"),
    ULTRA_BEAST("ultra_beast", "Ultra Beast"),
    PARADOX("paradox", "Paradox"),
    ALPHA("alpha", "alpha"),
    ULTRA_RARE("ultra_rare", "exceptionally rare"),
    RARE("rare", "unusual");

    private final String id;
    private final String displayName;

    AnnouncementKind(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public static AnnouncementKind fromId(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).replace('-', '_');
        for (AnnouncementKind kind : values()) {
            if (kind.id.equals(normalized)) {
                return kind;
            }
        }
        return null;
    }
}
