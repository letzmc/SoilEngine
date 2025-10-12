package com.github.letzmc.soilEngine.head;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Triển khai của {@link HeadObject} dùng để tạo đầu tùy chỉnh từ chuỗi Base64 hoặc URL texture.
 * <p>
 * Sử dụng API của Bukkit để gán texture cho {@link PlayerProfile}, 
 * sau đó áp dụng vào {@link SkullMeta} của {@link ItemStack}.
 */
public class JHeadObject implements HeadObject {

    /** Vật phẩm đầu đại diện cho texture. */
    private final ItemStack item;

    /** Xác định texture có hợp lệ hay không. */
    private boolean valid = true;

    /** Gson parser dùng để đọc JSON từ chuỗi Base64. */
    private static final Gson GSON = new Gson();

    /**
     * Tạo mới đầu từ chuỗi Base64 hoặc URL mã hóa.
     *
     * @param url chuỗi Base64 chứa dữ liệu texture
     */
    JHeadObject(String url) {
        this.item = new ItemStack(Material.PLAYER_HEAD);
        this.apply(url);
    }

    /**
     * Tạo mới đầu dựa trên vật phẩm có sẵn và dữ liệu texture.
     *
     * @param item vật phẩm gốc cần giữ lại
     * @param url chuỗi Base64 chứa dữ liệu texture
     */
    JHeadObject(ItemStack item, String url) {
        this.item = new ItemStack(item);
        this.apply(url);
    }

    /**
     * Áp dụng texture vào đầu từ chuỗi Base64.
     *
     * @param base64Url chuỗi Base64 chứa JSON texture
     */
    private void apply(String base64Url) {
        if (Objects.isNull(base64Url) || base64Url.isEmpty()) {
            this.valid = false;
            return;
        }

        JsonObject json;
        try {
            json = GSON.fromJson(new String(java.util.Base64.getDecoder().decode(base64Url)), JsonObject.class);
        } catch (Exception e) {
            this.valid = false;
            return;
        }

        if (!json.has("textures") || !json.getAsJsonObject("textures").has("SKIN")) {
            this.valid = false;
            return;
        }

        String skinUrl = json.getAsJsonObject("textures")
                             .getAsJsonObject("SKIN")
                             .getAsJsonObject()
                             .get("url")
                             .getAsString();
        try {
            URL urlObject = new URL(skinUrl);
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(urlObject);
            profile.setTextures(textures);

            if (this.item.getType() != Material.PLAYER_HEAD) {
                this.item.setType(Material.PLAYER_HEAD);
            }

            SkullMeta meta = (SkullMeta) this.item.getItemMeta();
            meta.setOwnerProfile(profile);
            this.item.setItemMeta(meta);
        } catch (MalformedURLException e) {
            this.valid = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return valid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemStack getItem() {
        return this.getItem("");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemStack getItem(String displayName) {
        return this.getItem(displayName, Lists.newArrayList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemStack getItem(String displayName, List<String> lore) {
        return this.getItem(displayName, lore, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemStack getItem(String displayName, List<String> lore, boolean glowing) {
        ItemStack item = new ItemStack(this.item);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);

        if (glowing) {
            meta.addEnchant(Enchantment.DURABILITY, 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }
}