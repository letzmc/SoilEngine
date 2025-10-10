package com.github.letzmc.soilEngine.config;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.io.StringReader;

/**
 * ConfigManager cho ItemStack persistent data container
 */
public class ItemConfigManager extends ConfigManager {

    private ItemStack item;
    private NamespacedKey configKey;

    private ItemConfigManager(Plugin plugin, ItemStack item) {
        super(plugin);
        this.item = item;
        this.configKey = new NamespacedKey(plugin, "config_data");
    }



    @Override
    protected void initializeDataSource() {
        setConfig(loadConfiguration());
    }

    @Override
    protected FileConfiguration loadConfiguration() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return new YamlConfiguration();
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        String configData = container.get(configKey, PersistentDataType.STRING);
        
        if (configData == null || configData.isEmpty()) {
            return new YamlConfiguration();
        }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(new StringReader(configData));
        } catch (Exception e) {
            e.printStackTrace();
            return new YamlConfiguration();
        }
        
        return config;
    }

    @Override
    protected void saveConfiguration(FileConfiguration config) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            // Thử tạo ItemMeta cho loại item này
            ItemStack tempItem = new ItemStack(item.getType());
            meta = tempItem.getItemMeta();
            if (meta == null) return;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        String configData = config.saveToString();
        container.set(configKey, PersistentDataType.STRING, configData);
        
        item.setItemMeta(meta);
    }

    public static ItemConfigManager create(Plugin plugin, ItemStack item) {
        return new ItemConfigManager(plugin, item);
    }

    /** Tạo ItemConfigManager cho ItemStack (không cần plugin) */
    public static ItemConfigManager create(ItemStack item) {
        return new ItemConfigManager(com.github.letzmc.soilEngine.SoilEngine.getInstance(), item);
    }

    public ItemStack getItem() {
        return item;
    }

    public NamespacedKey getConfigKey() {
        return configKey;
    }

    /** Đặt NamespacedKey tùy chỉnh để lưu config */
    public ItemConfigManager setConfigKey(NamespacedKey configKey) {
        this.configKey = configKey;
        return this;
    }

    /** Đặt key bằng tên (sử dụng plugin hiện tại) */
    public ItemConfigManager key(String keyName) {
        // Lấy plugin từ configKey hiện tại
        String namespace = this.configKey.getNamespace();
        this.configKey = new NamespacedKey(namespace, keyName);
        return this;
    }

    /** Đặt key bằng NamespacedKey */
    public ItemConfigManager key(NamespacedKey configKey) {
        this.configKey = configKey;
        return this;
    }
}