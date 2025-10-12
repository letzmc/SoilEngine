package com.github.letzmc.soilEngine.parser;

import java.io.File;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import nade.empty.configuration.simple.ConfigBuild;
import nade.empty.configuration.simple.YamlBuild;
import nade.empty.plugin.JPlugin;
import com.github.letzmc.soilEngine.head.CustomHead;
import nade.lemon.utils.bukkit.ItemStacks;
import nade.lemon.utils.bukkit.Materials;

/**
 * Trình phân tích cú pháp cho các định danh vật phẩm {@link Material} hoặc các loại đầu người chơi tùy chỉnh.
 * <p>
 * Lớp này cho phép chuyển đổi chuỗi đầu vào thành {@link ItemStack}, bao gồm:
 * <ul>
 *   <li>Material mặc định của Minecraft (ví dụ "STONE", "DIAMOND_SWORD")</li>
 *   <li>Đầu từ plugin HeadDatabase (bắt đầu bằng {@code hdb:})</li>
 *   <li>Đầu tùy chỉnh được định nghĩa trong {@code local-head.yml} (bắt đầu bằng {@code lh:})</li>
 * </ul>
 */
public class MaterialParser {
    private static HeadDatabaseAPI hdb;

    /** Ngăn chặn khởi tạo lớp này. */
    private MaterialParser() {}

    /**
     * Khởi tạo hệ thống parser.
     * <p>
     * Tải và đăng ký các đầu tùy chỉnh từ tệp {@code local-head.yml}.
     * Nếu plugin HeadDatabase tồn tại, hệ thống sẽ khởi tạo {@link HeadDatabaseAPI}.
     *
     * @param plugin plugin chính của bạn, dùng để truy cập thư mục dữ liệu và tài nguyên
     */
    public static void init(JPlugin plugin) {
        if (plugin.getServer().getPluginManager().isPluginEnabled("HeadDatabase")) {
            hdb = new HeadDatabaseAPI();
        }

        File file = new File(plugin.getDataFolder(), "local-head.yml");
        if (!file.exists()) {
            plugin.saveResource("local-head.yml", false);
        }

        ConfigBuild config = YamlBuild.build(file);
        for (String key : config.getKeys(false)) {
            CustomHead.registerByBase64(
                key.replace("_", "-").toLowerCase(),
                config.get(key, String.class)
            );
        }
    }

    /**
     * Phân tích chuỗi đầu vào để tạo {@link ItemStack}.
     * <p>
     * Quy tắc:
     * <ul>
     *   <li>{@code null}, rỗng hoặc chỉ chứa khoảng trắng → trả về vật phẩm mặc định</li>
     *   <li>Bắt đầu bằng {@code hdb:} → lấy đầu từ HeadDatabase (nếu khả dụng)</li>
     *   <li>Bắt đầu bằng {@code lh:} → lấy đầu từ hệ thống đầu tùy chỉnh</li>
     *   <li>Khác → chuyển đổi thành {@link Material}</li>
     * </ul>
     *
     * @param input chuỗi đầu vào (ví dụ "DIAMOND_SWORD", "hdb:12345", "lh:zombie")
     * @param def   vật liệu mặc định nếu đầu vào không hợp lệ
     * @return {@link ItemStack} đã được tạo
     */
    public static @NotNull ItemStack parse(String input, Material def) {
        if (input == null || input.isEmpty() || input.isBlank()) {
            return ItemStacks.newItemStack(def);
        }

        if (input.startsWith("hdb:") && hdb != null) {
            String id = input.substring(4);
            ItemStack head = hdb.getItemHead(id);
            if (head != null) {
                return head.clone();
            }
            return ItemStacks.newItemStack(def);
        }

        if (input.startsWith("lh:")) {
            String id = input.substring(3);
            if (CustomHead.contains(id)) {
                return CustomHead.get(id).getItem();
            }
            return ItemStacks.newItemStack(def);
        }

        return ItemStacks.newItemStack(Materials.getMaterialOrDefault(input, def));
    }
}