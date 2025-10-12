package com.github.letzmc.soilEngine.head;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Quản lý và tạo đầu tùy chỉnh (Custom Head) trong Minecraft.
 * <p>
 * Hỗ trợ ba loại nguồn dữ liệu:
 * <ul>
 *   <li>Base64 (chuỗi dữ liệu mã hóa của texture)</li>
 *   <li>URL hoặc ID texture trên máy chủ Mojang</li>
 *   <li>Tự động lấy đầu của người chơi từ Mojang API</li>
 * </ul>
 * Dữ liệu được lưu trong bộ nhớ tạm qua {@link HeadObject}.
 */
public class CustomHead {
    /** Lưu trữ tất cả đầu đã đăng ký. */
    private static final Map<String, HeadObject> storages = Maps.newHashMap();

    /** Đầu mặc định (trống). */
    private static final HeadObject def = CustomHead.create("");

    /** Đối tượng Gson dùng để phân tích JSON từ Mojang API. */
    private static final Gson GSON = new Gson();

    /** 
     * Đăng ký đầu mới bằng URL gốc của texture.
     * 
     * @deprecated Nên dùng {@link #registerByUrl(String, String)} hoặc {@link #registerByUrlId(String, String)}.
     * @param key Tên định danh (tự động chuyển sang chữ thường, thay dấu cách và gạch dưới thành gạch ngang)
     * @param url URL của texture (định dạng Mojang)
     * @return {@code true} nếu đăng ký thành công
     */
    @Deprecated
    public static boolean register(String key, String url) {
        HeadObject object = CustomHead.create(url);
        if (!object.isValid()) return false;
        storages.put(formatKey(key), object);
        return true;
    }

    /**
     * Đăng ký đầu mới bằng chuỗi base64.
     *
     * @param key   khóa định danh
     * @param base64 chuỗi texture được mã hóa
     * @return {@code true} nếu đăng ký thành công
     */
    public static boolean registerByBase64(String key, String base64) {
        HeadObject object = CustomHead.create(base64);
        if (!object.isValid()) return false;
        storages.put(formatKey(key), object);
        return true;
    }

    /**
     * Đăng ký đầu mới bằng URL texture đầy đủ.
     *
     * @param key tên định danh
     * @param url URL texture (ví dụ: https://textures.minecraft.net/texture/abc123)
     * @return {@code true} nếu đăng ký thành công
     */
    public static boolean registerByUrl(String key, String url) {
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}";
        String base64 = Base64.getEncoder().encodeToString(json.getBytes());
        HeadObject object = CustomHead.create(base64);
        if (!object.isValid()) return false;
        storages.put(formatKey(key), object);
        return true;
    }

    /**
     * Đăng ký đầu mới bằng ID texture của Mojang (chỉ phần sau /texture/).
     *
     * @param key tên định danh
     * @param urlId ID texture (ví dụ: abc123)
     * @return {@code true} nếu đăng ký thành công
     */
    public static boolean registerByUrlId(String key, String urlId) {
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/" + urlId + "\"}}}";
        String base64 = Base64.getEncoder().encodeToString(json.getBytes());
        HeadObject object = CustomHead.create(base64);
        if (!object.isValid()) return false;
        storages.put(formatKey(key), object);
        return true;
    }

    /**
     * Kiểm tra xem đầu với khóa cho trước đã được đăng ký hay chưa.
     *
     * @param key tên định danh
     * @return {@code true} nếu tồn tại
     */
    public static boolean contains(String key) {
        return storages.containsKey(formatKey(key));
    }

    /**
     * Lấy {@link HeadObject} từ khóa định danh.
     *
     * @param key tên định danh
     * @return đối tượng {@link HeadObject}, hoặc đầu mặc định nếu không tồn tại
     */
    public static HeadObject get(String key) {
        key = formatKey(key);
        return storages.getOrDefault(key, def);
    }

    /**
     * Trả về bản sao của danh sách tất cả đầu đã đăng ký.
     *
     * @return bản sao của map chứa tất cả đầu
     */
    public static Map<String, HeadObject> getStorages() {
        return Maps.newHashMap(storages);
    }

    /**
     * Lấy đầu tương ứng với người chơi (PLAYER_HEAD).
     *
     * @param player người chơi
     * @return đầu người chơi
     */
    public static HeadObject getByPlayer(OfflinePlayer player) {
        return getByPlayer(new ItemStack(Material.PLAYER_HEAD), player);
    }

    /**
     * Lấy đầu của người chơi kèm vật phẩm giữ nguyên meta.
     *
     * @param keep   ItemStack gốc cần giữ lại dữ liệu
     * @param player người chơi cần lấy đầu
     * @return {@link HeadObject} của người chơi, hoặc đầu mặc định nếu không hợp lệ
     */
    public static HeadObject getByPlayer(ItemStack keep, OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        if (storages.containsKey(uuid.toString())) return storages.get(uuid.toString());
        HeadObject object = CustomHead.create(keep, CustomHead.getTextures(player.getName()));
        if (!object.isValid()) return def;
        storages.put(uuid.toString(), object);
        return object;
    }

    /**
     * Lấy chuỗi Base64 texture của người chơi qua Mojang API.
     *
     * @param profile tên người chơi
     * @return chuỗi Base64 hoặc chuỗi rỗng nếu thất bại
     */
    public static String getTextures(String profile) {
        try {
            String profileId = getProfileId(profile);
            return getProfileTextures(profileId);
        } catch (Exception ignored) {}
        return "";
    }

    private static HeadObject create(String url) {
        return new JHeadObject(url);
    }

    private static HeadObject create(ItemStack item, String url) {
        return new JHeadObject(item, url);
    }

    private static String getProfileId(String profile) throws Exception {
        String content = getWebContent("https://api.mojang.com/users/profiles/minecraft/" + profile);
        JsonObject json = GSON.fromJson(content, JsonObject.class);
        return json.get("id").getAsString();
    }

    private static String getProfileTextures(String profileId) throws Exception {
        String content = getWebContent("https://sessionserver.mojang.com/session/minecraft/profile/" + profileId);
        JsonObject json = GSON.fromJson(content, JsonObject.class);
        return json.getAsJsonArray("properties").get(0)
                   .getAsJsonObject().get("value").getAsString();
    }

    private static String getWebContent(String webUrl) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(new URL(webUrl).openStream(), StandardCharsets.UTF_8))) {
            String str;
            while ((str = in.readLine()) != null) {
                sb.append(str);
            }
        } catch (IOException ignored) {}
        return sb.toString();
    }

    private static String formatKey(String key) {
        return key.replace(" ", "-").replace("_", "-").toLowerCase();
    }
}