package com.github.letzmc.soilEngine.head;

import java.util.List;

import org.bukkit.inventory.ItemStack;

/**
 * Đại diện cho một đầu tùy chỉnh (Custom Head) có thể được hiển thị trong Minecraft.
 * <p>
 * Interface này định nghĩa các phương thức để lấy {@link ItemStack} của đầu
 * với các tùy chọn khác nhau về tên hiển thị, lore và hiệu ứng phát sáng.
 */
public interface HeadObject {

    /**
     * Kiểm tra tính hợp lệ của đầu.
     *
     * @return {@code true} nếu đầu có dữ liệu hợp lệ và có thể sử dụng
     */
    boolean isValid();

    /**
     * Lấy vật phẩm đầu gốc.
     *
     * @return {@link ItemStack} đại diện cho đầu này
     */
    ItemStack getItem();

    /**
     * Lấy vật phẩm đầu với tên hiển thị tùy chỉnh.
     *
     * @param displayName tên hiển thị của đầu
     * @return {@link ItemStack} với tên hiển thị được đặt
     */
    ItemStack getItem(String displayName);

    /**
     * Lấy vật phẩm đầu với tên hiển thị và lore tùy chỉnh.
     *
     * @param displayName tên hiển thị
     * @param lore danh sách lore (dòng mô tả)
     * @return {@link ItemStack} đã gán tên và lore
     */
    ItemStack getItem(String displayName, List<String> lore);

    /**
     * Lấy vật phẩm đầu với tên, lore và hiệu ứng phát sáng.
     *
     * @param displayName tên hiển thị
     * @param lore danh sách lore (dòng mô tả)
     * @param glowing {@code true} nếu muốn làm đầu phát sáng
     * @return {@link ItemStack} đã gán đầy đủ dữ liệu
     */
    ItemStack getItem(String displayName, List<String> lore, boolean glowing);
}