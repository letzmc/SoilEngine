package com.github.letzmc.soilEngine.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * ConfigManager cho lưu trữ file
 */
public class FileConfigManager extends ConfigManager {

    private final File file;

    private FileConfigManager(Plugin plugin, File file) {
        super(plugin);
        this.file = file;
    }

    @Override
    protected void initializeDataSource() {
        file.getParentFile().mkdirs();
        setConfig(loadConfiguration());
    }

    @Override
    protected FileConfiguration loadConfiguration() {
        if (file.exists()) {
            return YamlConfiguration.loadConfiguration(file);
        } else {
            return new YamlConfiguration();
        }
    }

    @Override
    protected void saveConfiguration(FileConfiguration config) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FileConfigManager create(Plugin plugin, File file) {
        return new FileConfigManager(plugin, file);
    }

    public static FileConfigManager create(Plugin plugin, Path path) {
        return create(plugin, path.toFile());
    }

    public static FileConfigManager create(Plugin plugin, String configName) {
        return create(plugin, plugin.getDataFolder().toPath().resolve(configName));
    }

    public static FileConfigManager create(Plugin plugin) {
        return create(plugin, "config.yml");
    }

    public File getFile() {
        return file;
    }
}