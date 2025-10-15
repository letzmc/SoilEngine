package com.github.letzmc.soilEngine.config;

import com.github.letzmc.soilEngine.SoilEngine;
import com.github.letzmc.soilEngine.config.conversion.ObjectConverter;
import com.github.letzmc.soilEngine.config.conversion.StaticRootConverter;
import com.github.letzmc.soilEngine.config.conversion.TypeConverter;
import com.github.letzmc.soilEngine.config.data.ConfigurationSectionDataHolder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Function;

/**
 * Lớp cơ sở trừu tượng để quản lý serialize/deserialize config
 */
public abstract class ConfigManager {

    private static Boolean commentsSupported;

    /** Kiểm tra hỗ trợ comments */
    public static boolean areCommentsSupported() {
        if (commentsSupported == null) {
            commentsSupported = Arrays.stream(ConfigurationSection.class.getMethods()).anyMatch(m -> m.getName().equals("setComments"));
        }
        return commentsSupported;
    }

    /** Tạo FileConfigManager cho file cụ thể */
    public static FileConfigManager create(Plugin plugin, File file) {
        return FileConfigManager.create(plugin, file);
    }

    /** Tạo FileConfigManager cho path cụ thể */
    public static FileConfigManager create(Plugin plugin, Path path) {
        return FileConfigManager.create(plugin, path);
    }

    /** Tạo FileConfigManager trong thư mục plugin */
    public static FileConfigManager create(Plugin plugin, String configName) {
        return FileConfigManager.create(plugin, configName);
    }

    /** Tạo FileConfigManager mặc định (config.yml) */
    public static FileConfigManager create(Plugin plugin) {
        return FileConfigManager.create(plugin);
    }

    /** Tạo ItemConfigManager cho ItemStack */
    public static ItemConfigManager create(Plugin plugin, ItemStack item) {
        return ItemConfigManager.create(plugin, item);
    }

    /** Tạo ItemConfigManager cho ItemStack (không cần plugin) */
    public static ItemConfigManager create(ItemStack item) {
        return ItemConfigManager.create(SoilEngine.getInstance(), item);
    }

    private ConfigurationSectionDataHolder holder;
    private TypeConverter<?> converter;
    private Object target;
    private Class<?> targetClass;
    private ConversionManager conversionManager;

    protected ConfigManager(Plugin plugin) {
        conversionManager = new ConversionManager(plugin);
        initializeDataSource();
    }

    /** Khởi tạo nguồn dữ liệu */
    protected abstract void initializeDataSource();

    /** Đọc config từ nguồn */
    protected abstract FileConfiguration loadConfiguration();

    /** Ghi config ra nguồn */
    protected abstract void saveConfiguration(FileConfiguration config);

    public ConversionManager getConversionManager() {
        return conversionManager;
    }

    public void setConversionManager(ConversionManager conversionManager) {
        this.conversionManager = conversionManager;
    }

    /** Thêm string converter */
    public <T> ConfigManager addConverter(Class<T> clazz, Function<String, T> loader, Function<T, String> saver) {
        conversionManager.addConverter(clazz, loader, saver);
        return this;
    }

    /** Thêm type converter */
    public <T> ConfigManager addConverter(ConfigType<T> type, TypeConverter<T> converter) {
        conversionManager.addConverter(type, converter);
        return this;
    }

    protected void setConfig(FileConfiguration config) {
        this.holder = new ConfigurationSectionDataHolder(config);
    }

    /** Đặt target object để load/save config */
    public ConfigManager target(Object obj) {
        if (target != null || targetClass != null) {
            throw new IllegalStateException("ConfigManager already has a target");
        }
        target = obj;

        // Thêm cơ chế xử ký đặc biệt cho map
        if (obj instanceof java.util.Map) {
            conversionManager.addConverter(new ConfigType<>(java.util.Map.class), new TypeConverter<java.util.Map<String, Object>>() {
                @Override
                public void loadFrom(ConfigurationSectionDataHolder section, String key, java.util.Map<Object, Object> target) {
                    ConfigurationSection subSection = section.getConfigurationSection(key);
                    if (subSection == null) return;
                    for (String subKey : subSection.getKeys(false)) {
                        target.put(subKey, subSection.get(subKey));
                    }
                }

                @Override
                public void saveTo(java.util.Map<Object, Object> source, ConfigurationSectionDataHolder section, String key, boolean overwrite) {
                    ConfigurationSection subSection = section.getOrCreateSection(key);
                    for (Object mapKey : source.keySet()) {
                        Object value = source.get(mapKey);
                        if (overwrite || !subSection.contains(mapKey.toString())) {
                            subSection.set(mapKey.toString(), value);
                        }
                    }
                }
            });
        }

        converter = ObjectConverter.create(conversionManager, new ConfigType<>(obj.getClass()));
        return this;
    }

    /** Đặt target class (static fields) để load/save config */
    public ConfigManager target(Class<?> clazz) {
        if (target != null || targetClass != null) {
            throw new IllegalStateException("ConfigManager already has a target");
        }
        targetClass = clazz;
        converter = StaticRootConverter.create(conversionManager, clazz);
        return this;
    }

    /** Lưu tất cả giá trị từ target ra config */
    public ConfigManager save() {
        save(converter, true);
        return this;
    }

    /** Chỉ lưu giá trị chưa có trong config */
    public ConfigManager saveDefaults() {
        save(converter, false);
        return this;
    }

    /** Đọc tất cả giá trị từ config vào target */
    public ConfigManager load() {
        load(converter);
        return this;
    }

    private <T> void load(TypeConverter<T> converter) {
        converter.loadFrom(holder, null, (T) target);
    }

    /** Tải lại config từ nguồn và load vào target */
    public ConfigManager reload() {
        setConfig(loadConfiguration());
        return load();
    }

    private <T> void save(TypeConverter<T> converter, boolean overwrite) {
        holder.clearComments();
        converter.saveTo((T) target, holder, null, overwrite);
        
        // Create a new config based on current data
        FileConfiguration config = new YamlConfiguration();
        
        // Copy all data from holder to config - we need to copy the underlying section
        if (holder.unwrap() instanceof ConfigurationSection) {
            ConfigurationSection sourceSection = (ConfigurationSection) holder.unwrap();
            copySection(sourceSection, config);
        }
        
        if (areCommentsSupported()) {
            holder.getComments().forEach(config::setComments);
        }
        
        saveConfiguration(config);
    }

    /** Sao chép tất cả giá trị từ section nguồn sang target */
    private void copySection(ConfigurationSection source, ConfigurationSection target) {
        for (String key : source.getKeys(false)) {
            Object value = source.get(key);
            if (value instanceof ConfigurationSection) {
                ConfigurationSection targetSubsection = target.createSection(key);
                copySection((ConfigurationSection) value, targetSubsection);
            } else {
                target.set(key, value);
            }
        }
    }

    public FileConfiguration getConfig() {
        return loadConfiguration();
    }

}
