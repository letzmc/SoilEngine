package com.github.letzmc.soilEngine.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceholderAPIHook {

    public static boolean iaAvailable() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null;
    }

    public static String parse(Player player, String text) {
        if (iaAvailable()) { return text; }
        return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
    }

}
