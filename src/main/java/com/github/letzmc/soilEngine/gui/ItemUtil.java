package com.github.letzmc.soilEngine.gui;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class ItemUtil {

    public static ItemStack hideAll(ItemStack item){
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return item;
        var meta = item.getItemMeta();
        if (meta == null) return item;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            item.getType().getDefaultAttributeModifiers(slot).forEach(meta::addAttributeModifier);
        }
        Arrays.stream(ItemFlag.values()).forEach(meta::addItemFlags);
        return item;
    }

    public static ItemStack gen(Material material, String name, String ...lore){
        var item = new ItemStack(material);
        item.editMeta(meta -> {
            var serializer = LegacyComponentSerializer.legacyAmpersand();
            meta.displayName(serializer.deserialize(name));
            if (lore != null && lore.length > 0) {
                meta.lore(Arrays.stream(lore).map(serializer::deserialize).toList());
            }
        });
        return hideAll(item);
    }

    public static ItemStack glow(ItemStack item){
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return item;
        var meta = item.getItemMeta();
        if (meta == null) return item;
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

}
