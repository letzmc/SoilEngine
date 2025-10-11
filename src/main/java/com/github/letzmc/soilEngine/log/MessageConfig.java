package com.github.letzmc.soilEngine.log;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Function;

public class MessageConfig {
    private final Plugin plugin;
    private final YamlConfiguration config = new YamlConfiguration();
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final LegacyComponentSerializer lcs = LegacyComponentSerializer.legacySection();

    public MessageConfig(Plugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void registerDefault(String key, String value) {
        config.addDefault(key, value);
    }

    public void registerDefaults(HashMap<String, String> defaults) {
        for (String key : defaults.keySet()) {
            registerDefault(key, defaults.get(key));
        }
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "lang.yml");
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().severe("Error when trying to load lang.yml: " + e.getMessage());
        }
    }

    public void save() {
        File file = new File(plugin.getDataFolder(), "lang.yml");
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Error when trying to save lang.yml: " + e.getMessage());
        }
    }

    public Component deserialize(String raw) {
        if (raw == null) return Component.text("null").color(NamedTextColor.RED);
        if (raw.isEmpty()) return Component.text("");
        if (raw.contains("&")) {
            raw = raw.replace("&", "§");
        }
        if (raw.contains("§")) {
            return lcs.deserialize(raw);
        }
        return mm.deserialize(raw);
    }

    public Component get(String key, Function<String, String> adapt) {
        String raw = config.getString(key);
        if (raw == null) raw = key;
        String adapted = adapt.apply(raw);
        return deserialize(adapted);
    }
    public Component get(String key) {
        return deserialize(config.getString(key));
    }
    public Component get(String key, String param, String value) {
        return get(key, s -> s.replace("{" + param + "}", value));
    }

    public Component get(String key,
                         String param1, String value1,
                         String param2, String value2
    ) {
        return get(key, param1, value1, s -> s.replace("{" + param2 + "}", value2));
    }

    public Component get(String key,
                         String param1, String value1,
                         String param2, String value2,
                         String param3, String value3
    ) {
        return get(key, param1, value1, param2, value2, s -> s.replace("{" + param3 + "}", value3));
    }

    public Component get(String key, HashMap<String, String> params) {
        return get(key, s -> {
            for (String param : params.keySet()) {
                s = s.replace("{" + param + "}", params.get(param));
            }
            return s;
        });
    }

    public Component get(String key, String param, String value, Function<String, String> adapt) {
        return get(key, s -> adapt.apply(s.replace("{" + param + "}", value)));
    }

    public Component get(String key,
                         String param1, String value1,
                         String param2, String value2,
                         Function<String, String> adapt) {
        return get(key, param1, value1, s -> adapt.apply(s.replace("{" + param2 + "}", value2)));
    }

    public Component get(String key,
                         String param1, String value1,
                         String param2, String value2,
                         String param3, String value3,
                         Function<String, String> adapt) {
        return get(key, param1, value1, param2, value2, s -> adapt.apply(s.replace("{" + param3 + "}", value3)));
    }

}
