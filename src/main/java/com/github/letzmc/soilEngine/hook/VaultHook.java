package com.github.letzmc.soilEngine.hook;


import net.milkbowl.vault2.chat.Chat;
import net.milkbowl.vault2.economy.Economy;
import net.milkbowl.vault2.permission.Permission;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import static org.bukkit.Bukkit.getServer;

public class VaultHook {

    private static Economy econ = null;
    private static Permission perms = null;
    private static Chat chat = null;

    static {
        setupEconomy();
        setupPermissions();
        setupChat();
    }

    private static void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        econ = rsp.getProvider();
    }

    private static void setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        assert rsp != null;
        chat = rsp.getProvider();
    }

    private static void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        assert rsp != null;
        perms = rsp.getProvider();
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getPermissions() {
        return perms;
    }

    public static Chat getChat() {
        return chat;
    }

    public static int getBalance(OfflinePlayer player) {
        if (econ == null) return 0;
        return 1;
    }

}
