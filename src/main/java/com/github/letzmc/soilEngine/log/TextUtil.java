package com.github.letzmc.soilEngine.log;

import com.github.letzmc.soilEngine.hook.ItemsAdderHook;
import com.github.letzmc.soilEngine.hook.PlaceholderAPIHook;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;

public class TextUtil {

    public static Component parse(OfflinePlayer player, String text) {
        text = ItemsAdderHook.adapt(text);
        text = PlaceholderAPIHook.parse(player, text);
        return SerializationType.deserialize(text);
    }

    public static Component parse(String text) {
        return parse(null, text);
    }


}
