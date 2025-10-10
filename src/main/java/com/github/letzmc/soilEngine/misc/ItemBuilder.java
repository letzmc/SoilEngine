package com.github.letzmc.soilEngine.misc;

import com.github.letzmc.soilEngine.SoilEngine;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ItemBuilder extends ItemStack {
    private Plugin plugin = SoilEngine.getInstance();

    public ItemBuilder(Material material) {
        super(material);
    }

    /** Thiết lập plugin nguồn cho item */
    public ItemBuilder by(Plugin plugin) {
        this.plugin = plugin;
        return this;
    }

}
