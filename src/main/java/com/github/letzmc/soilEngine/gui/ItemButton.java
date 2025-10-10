package com.github.letzmc.soilEngine.gui;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Lớp nút button trong inventory GUI
 */
public abstract class ItemButton {

    protected ItemStack item;
    private int slot;

    /**
     * Tạo ItemButton từ ItemStack và listener
     */
    public static ItemButton create(ItemStack item, Consumer<InventoryClickEvent> listener) {
        return new ItemButton(item) {

            @Override
            public void onClick(InventoryClickEvent e) {
                listener.accept(e);
            }

        };
    }

    /**
     * Tạo ItemButton với listener nhận cả event và button
     */
    public static ItemButton create(ItemStack item, BiConsumer<InventoryClickEvent, ItemButton> listener) {
        return new ItemButton(item) {

            @Override
            public void onClick(InventoryClickEvent e) {
                listener.accept(e, this);
            }

        };
    }

    /**
     * Tạo ItemButton mới với ItemStack làm icon
     */
    public ItemButton(ItemStack item) {
        this.item = item;
    }

    /**
     * Lấy ItemStack làm icon của button
     */
    public ItemStack getItem() {
        return item;
    }

    protected int getSlot() {
        return slot;
    }

    protected void setSlot(int slot) {
        this.slot = slot;
    }

    /**
     * Cập nhật item của button (cần gọi InventoryGUI.update() để hiển thị thay đổi)
     */
    public void setItem(ItemStack item) {
        this.item = item;
    }

    public abstract void onClick(InventoryClickEvent e);

}