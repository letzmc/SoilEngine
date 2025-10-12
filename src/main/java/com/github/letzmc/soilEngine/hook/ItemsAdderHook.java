package com.github.letzmc.soilEngine.hook;

import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

public class ItemsAdderHook {

    public static boolean available() {
        return Bukkit.getPluginManager().getPlugin("ItemsAdder") != null;
    }

    public static String adapt(String input) {
        if (!available()) return input;
        return FontImageWrapper.replaceFontImages(input);
    }

    public static Component adapt(Component input) {
        if (!available()) return input;
        return FontImageWrapper.replaceFontImages(input);
    }

}
