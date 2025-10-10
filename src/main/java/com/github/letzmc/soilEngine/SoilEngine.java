package com.github.letzmc.soilEngine;

import org.bukkit.plugin.java.JavaPlugin;

public final class SoilEngine extends JavaPlugin {

    private static SoilEngine instance;
    public static SoilEngine getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
