package com.github.letzmc.soilEngine.log;

import com.github.letzmc.soilEngine.SoilEngine;
import com.github.letzmc.soilEngine.hook.PlaceholderAPIHook;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class MessageLogger {
    public final MessageConfig config;
    private final Plugin plugin;

    public MessageLogger(Plugin plugin) {
        this.plugin = plugin;
        this.config = new MessageConfig(plugin);
    }

    public void log(String message, @Nullable CommandSender audience, Function<String, String> parser, Map<String, String> replacements) {
        if (audience == null) audience = Bukkit.getConsoleSender();
        audience.sendMessage(config.get(message, i -> {
            if (replacements != null) {
                for (var entry : replacements.entrySet()) {
                    i = i.replace("{"+entry.getKey()+"}", entry.getValue());
                }
            }
            if (parser != null) i = parser.apply(i);
            return i;
        }));
    }

    public void log(String message, CommandSender audience, Function<String, String> parser) {
        log(message, audience, parser, null);
    }

    public void log(String message, CommandSender audience, Map<String, String> replacements) {
        log(message, audience, null, replacements);
    }

    public void log(String message, Function<String, String> parser, Map<String, String> replacements) {
        log(message, null, parser, replacements);
    }
    
    public void log(String message, CommandSender audience) {
        log(message, audience, null, null);
    }

    public void log(String message, Function<String, String> parser) {
        log(message, null, parser, null);
    }

    public void log(String message, Map<String, String> replacements) {
        log(message, null, null, replacements);
    }

    public void log(String message) {
        log(message, null, null, null);
    }


}
