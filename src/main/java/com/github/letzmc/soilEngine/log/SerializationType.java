package com.github.letzmc.soilEngine.log;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

enum SerializationType {
    LEGACY_AMPERSAND,
    LEGACY_SECTION,
    MINI_MESSAGE;

    static SerializationType check(String text) {
        if (text.contains("&")) return LEGACY_AMPERSAND;
        if (text.contains("§")) return LEGACY_SECTION;
        return MINI_MESSAGE;
    }

    private static final LegacyComponentSerializer ampersand_serializer = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer section_serializer = LegacyComponentSerializer.legacySection();
    private static final MiniMessage mini_message = MiniMessage.miniMessage();

    static Component deserialize(String text) {
        return switch (check(text)) {
            case LEGACY_AMPERSAND -> ampersand_serializer.deserialize(text);
            case LEGACY_SECTION -> section_serializer.deserialize(text);
            case MINI_MESSAGE -> mini_message.deserialize(text);
        };
    }
}
