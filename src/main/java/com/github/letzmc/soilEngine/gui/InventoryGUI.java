package com.github.letzmc.soilEngine.gui;

import com.github.letzmc.soilEngine.SoilEngine;
import com.github.letzmc.soilEngine.log.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * GUI inventory với hệ thống button và slot mở
 * @author Redempt
 */
public class InventoryGUI implements Listener {

    /** Kính xám không tên để lấp đầy slot trống */
    public static final ItemStack FILLER;

    static {
        FILLER = ItemUtil.gen(Material.GRAY_STAINED_GLASS_PANE, " ");
    }

    private final Inventory inventory;
    private final Set<Integer> openSlots = new LinkedHashSet<>();
    private final List<Integer> excludedFillerSlots = new ArrayList<>();
    private Runnable onDestroy;
    private BiConsumer<InventoryClickEvent, List<Integer>> onClickOpenSlot = (e, i) -> {
    };
    private Consumer<InventoryDragEvent> onDragOpenSlot = e -> {
    };
    private final Map<Integer, ItemButton> buttons = new HashMap<>();

    private boolean returnItems = true;
    private boolean destroyOnClose = true;

    /** Tạo GUI từ inventory có sẵn */
    public InventoryGUI(Inventory inventory) {
        this.inventory = inventory;
        Bukkit.getPluginManager().registerEvents(this, SoilEngine.getInstance());
    }


    /** Tạo GUI mới với kích thước và tên */
    public InventoryGUI(int size, String name, Player owner) {
        this(Bukkit.createInventory(null, size, TextUtil.parse(owner, name)));
    }

    public InventoryGUI(int size, String name) {
        this(size, name, null);
    }

    /** Lấy inventory được wrap */
    public Inventory getInventory() {
        return inventory;
    }

    /** Lấy kích thước inventory */
    public int getSize() {
        return this.getInventory().getSize();
    }

    /** Thêm button vào slot */
    public void addButton(ItemButton button, int slot) {
        button.setSlot(slot);
        inventory.setItem(slot, button.getItem());
        buttons.put(slot, button);
    }

    /** Thêm button vào slot (thứ tự tham số khác) */
    public void addButton(int slot, ItemButton button) {
        addButton(button, slot);
    }

    /** Thêm button tại vị trí x, y */
    public void addButton(ItemButton button, int x, int y) {
        int slot = x + (y * 9);
        addButton(button, slot);
    }

    /** Lấp đầy khu vực từ start đến end với item */
    public void fill(int start, int end, ItemStack item) {
        for (int i = start; i < end; i++) {
            if (!excludedFillerSlots.contains(i)) {
                inventory.setItem(i, item == null ? null : item.clone());
            }
        }
    }

    /** Lấp đầy vùng từ x1,y1 đến x2,y2 với item */
    public void fill(int x1, int y1, int x2, int y2, ItemStack item) {
        for (int x = x1; x < x2; x++) {
            for (int y = y1; y < y2; y++) {
                inventory.setItem(x + (y * 9), item == null ? null : item.clone());
            }
        }
    }

    /** Xóa button khỏi inventory */
    public void removeButton(ItemButton button) {
        inventory.setItem(button.getSlot(), new ItemStack(Material.AIR));
        buttons.remove(button.getSlot());
    }

    /** Lấy tất cả button trong GUI */
    public List<ItemButton> getButtons() {
        return new ArrayList<>(buttons.values());
    }

    /** Lấy button tại slot (null nếu không có) */
    public ItemButton getButton(int slot) {
        return buttons.get(slot);
    }

    /** Xóa slot (bao gồm button nếu có) */
    public void clearSlot(int slot) {
        ItemButton button = buttons.get(slot);
        if (button != null) {
            removeButton(button);
            return;
        }
        inventory.setItem(slot, new ItemStack(Material.AIR));
    }

    /** Làm mới inventory */
    public void update() {
        for (ItemButton button : buttons.values()) {
            inventory.setItem(button.getSlot(), button.getItem());
        }
    }

    /** Mở slot để có thể đặt item */
    public void openSlot(int slot) {
        openSlots.add(slot);
    }

    /** Mở nhiều slot từ start đến end */
    public void openSlots(int start, int end) {
        for (int i = start; i < end; i++) {
            openSlots.add(i);
        }
    }

    /** Mở vùng slot từ x1,y1 đến x2,y2 */
    public void openSlots(int x1, int y1, int x2, int y2) {
        for (int y = y1; y < y2; y++) {
            for (int x = x1; x < x2; x++) {
                openSlots.add(y * 9 + x);
            }
        }
    }

    /** Đóng slot không cho đặt item */
    public void closeSlot(int slot) {
        openSlots.remove(slot);
    }

    /** Đóng nhiều slot từ start đến end */
    public void closeSlots(int start, int end) {
        for (int i = start; i < end; i++) {
            openSlots.remove(i);
        }
    }

    /** Đóng vùng slot từ x1,y1 đến x2,y2 */
    public void closeSlots(int x1, int y1, int x2, int y2) {
        for (int y = y1; y < y2; y++) {
            for (int x = x1; x < x2; x++) {
                openSlots.remove(y * 9 + x);
            }
        }
    }

    /** Lấy danh sách slot đang mở */
    public Set<Integer> getOpenSlots() {
        return openSlots;
    }

    /** Mở GUI cho player */
    public void open(Player player) {
        player.openInventory(inventory);
    }

    /** Có trả lại item khi destroy GUI không */
    public boolean returnsItems() {
        return returnItems;
    }

    /** Đặt có trả lại item khi destroy không */
    public void setReturnsItems(boolean returnItems) {
        this.returnItems = returnItems;
    }

    /** Có tự destroy khi tất cả player đóng không */
    public boolean destroysOnClose() {
        return destroyOnClose;
    }

    /** Đặt có tự destroy khi đóng không */
    public void setDestroyOnClose(boolean destroyOnClose) {
        this.destroyOnClose = destroyOnClose;
    }

    /** Đặt callback khi destroy */
    public void setOnDestroy(Runnable onDestroy) {
        this.onDestroy = onDestroy;
    }

    /** Đặt handler khi click slot mở */
    public void setOnClickOpenSlot(Consumer<InventoryClickEvent> handler) {
        this.onClickOpenSlot = (e, i) -> handler.accept(e);
    }

    /** Đặt handler khi click slot mở (có danh sách slot) */
    public void setOnClickOpenSlot(BiConsumer<InventoryClickEvent, List<Integer>> handler) {
        this.onClickOpenSlot = handler;
    }

    /** Destroy GUI và dọn dẹp memory */
    public void destroy(Player lastViewer) {
        if (onDestroy != null) {
            onDestroy.run();
        }
        HandlerList.unregisterAll(this);
        if (returnItems && lastViewer != null) {
            for (int slot : openSlots) {
                ItemStack item = inventory.getItem(slot);
                if (item == null) {
                    continue;
                }
                lastViewer.getInventory().addItem(item).values().forEach(i -> lastViewer.getWorld().dropItem(lastViewer.getLocation(), i));
            }
        }
        inventory.clear();
        buttons.clear();
    }

    /** Destroy GUI không trả item */
    public void destroy() {
        destroy(null);
    }

    /** Xóa tất cả inventory và button */
    public void clear() {
        inventory.clear();
        buttons.clear();
    }

    /** Đặt handler khi drag item vào slot mở */
    public void setOnDragOpenSlot(Consumer<InventoryDragEvent> onDrag) {
        this.onDragOpenSlot = onDrag;
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        List<Integer> slots = e.getRawSlots().stream().filter(s -> getInventory(e.getView(), s).equals(inventory)).toList();
        if (slots.isEmpty()) {
            return;
        }
        if (!openSlots.containsAll(slots)) {
            e.setCancelled(true);
            return;
        }
        onDragOpenSlot.accept(e);
    }

    private Inventory getInventory(InventoryView view, int rawSlot) {
        return rawSlot < view.getTopInventory().getSize() ? view.getTopInventory() : view.getBottomInventory();
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!inventory.equals(e.getView().getTopInventory())) {
            return;
        }
        if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR && !Objects.equals(e.getClickedInventory(), inventory)) {
            e.setCancelled(true);
            return;
        }
        if (!inventory.equals(e.getClickedInventory()) && e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            if (!openSlots.isEmpty()) {
                Map<Integer, ItemStack> slots = new HashMap<>();
                int amount = Objects.requireNonNull(e.getCurrentItem()).getAmount();
                for (int slot : openSlots) {
                    if (amount <= 0) {
                        break;
                    }
                    ItemStack item = inventory.getItem(slot);
                    if (item == null) {
                        int diff = Math.min(amount, e.getCurrentItem().getType().getMaxStackSize());
                        amount -= diff;
                        ItemStack clone = e.getCurrentItem().clone();
                        clone.setAmount(diff);
                        slots.put(slot, clone);
                        continue;
                    }
                    if (e.getCurrentItem().isSimilar(item)) {
                        int max = item.getType().getMaxStackSize() - item.getAmount();
                        int diff = Math.min(max, e.getCurrentItem().getAmount());
                        amount -= diff;
                        ItemStack clone = item.clone();
                        clone.setAmount(clone.getAmount() + diff);
                        slots.put(slot, clone);
                    }
                }
                if (slots.isEmpty()) {
                    return;
                }
                onClickOpenSlot.accept(e, new ArrayList<>(slots.keySet()));
                if (e.isCancelled()) {
                    return;
                }
                e.setCancelled(true);
                ItemStack item = e.getCurrentItem();
                item.setAmount(amount);
                e.setCurrentItem(item);
                slots.forEach(inventory::setItem);
                Bukkit.getScheduler().scheduleSyncDelayedTask(SoilEngine.getInstance(), () -> ((Player) e.getWhoClicked()).updateInventory());
                return;
            }
            e.setCancelled(true);
        }
        if (e.getInventory().equals(e.getClickedInventory())) {
            if (openSlots.contains(e.getSlot())) {
                List<Integer> list = new ArrayList<>();
                list.add(e.getSlot());
                onClickOpenSlot.accept(e, list);
                return;
            }
            e.setCancelled(true);
            ItemButton button = buttons.get(e.getSlot());
            if (button != null) {
                button.onClick(e);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().equals(inventory) && destroyOnClose) {
            if (e.getViewers().size() <= 1) {
                destroy((Player) e.getPlayer());
            }
        }
    }

    public void excludeFillerSlot(List<Integer> excludedSlots) {
        excludedFillerSlots.addAll(excludedSlots);
    }

}