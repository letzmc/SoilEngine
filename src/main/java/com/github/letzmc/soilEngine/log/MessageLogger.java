package com.github.letzmc.soilEngine.log;

import com.github.letzmc.soilEngine.SoilEngine;
import com.github.letzmc.soilEngine.hook.PlaceholderAPIHook;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MessageLogger {
    public final MessageConfig config;
    private final Plugin plugin;

    public MessageLogger(Plugin plugin) {
        this.plugin = plugin;
        this.config = new MessageConfig(plugin);
    }

    public void log(String message) {
        plugin.getComponentLogger().info(config.get(message));
    }

    public void log(String message, String param, String value) {
        plugin.getComponentLogger().info(config.get(message, param, value));
    }

    public void log(String message,
                   String param1, String value1,
                   String param2, String value2) {
        plugin.getComponentLogger().info(config.get(message, param1, value1, param2, value2));
    }

    public void log(String message,
                   String param1, String value1,
                   String param2, String value2,
                   String param3, String value3) {
        plugin.getComponentLogger().info(config.get(message, param1, value1, param2, value2, param3, value3));
    }

    public void log(Player player, String message) {
        player.sendMessage(config.get(message, str -> PlaceholderAPIHook.parse(player, str)));
    }

    public void log(Player player, String message, String param, String value) {
        player.sendMessage(config.get(message, param, value, str -> PlaceholderAPIHook.parse(player, str)));
    }

    public void log(Player player, String message,
                   String param1, String value1,
                   String param2, String value2) {
        player.sendMessage(config.get(message, param1, value1, param2, value2, str -> PlaceholderAPIHook.parse(player, str)));
    }

    public void log(Player player, String message,
                   String param1, String value1,
                   String param2, String value2,
                   String param3, String value3) {
        player.sendMessage(config.get(message, param1, value1, param2, value2, param3, value3, str -> PlaceholderAPIHook.parse(player, str)));
    }
}
