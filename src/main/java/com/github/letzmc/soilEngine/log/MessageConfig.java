package com.github.letzmc.soilEngine.log;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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

    public Component get(String key, OfflinePlayer operator, Function<String, String> adapt) {
        String raw = config.getString(key);
        if (raw == null) raw = key;
        String adapted = adapt != null ? adapt.apply(raw) : raw;
        return TextUtil.parse(operator, adapted);
    }

    public Component get(String key, Function<String, String> adapt) {
        return get(key, null, adapt);
    }

    public Component get(String key, OfflinePlayer operator) {
        return get(key, operator, null);
    }

    public Component get(String key) {
        return get(key, null, null);
    }

}
