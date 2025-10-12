package com.github.letzmc.soilEngine.inventory.item;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.letzmc.soilEngine.inventory.placeholder.ComponentPlaceholder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

//import nade.empty.utils.parser.MaterialParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Đại diện cho một trình xây dựng {@link ItemStack} có hỗ trợ {@link Component}
 * và hệ thống placeholder động.
 * <p>
 * Cung cấp API dạng chuỗi tương tự Paper API, giúp việc tạo item trở nên rõ ràng,
 * an toàn và có thể tùy biến bằng các thành phần văn bản Adventure.
 */
public class ItemBuilder {

    private ItemStack material;
    private Component displayName;
    private List<Component> lore = Lists.newArrayList();
    private Set<ComponentPlaceholder> placeholders = Sets.newHashSet();

    private ItemBuilder() {}

    private ItemBuilder(Material material) {
        this.material(material);
    }

    private ItemBuilder(String material, Material def) {
        this.material(material, def);
    }

    private ItemBuilder(String material) {
        this.material(material);
    }

    /**
     * Lấy vật liệu hiện tại của item.
     *
     * @return {@link ItemStack} vật liệu hiện tại
     */
    public ItemStack material() {
        return this.material;
    }

    /**
     * Đặt vật liệu cho item.
     *
     * @param material vật liệu {@link Material} cần đặt
     * @return chính builder hiện tại
     */
    public ItemBuilder material(Material material) {
        this.material = new ItemStack(material);
        return this;
    }

    /**
     * Phân tích chuỗi vật liệu và đặt nó cho item.
     * Nếu chuỗi không hợp lệ, sẽ sử dụng vật liệu mặc định.
     *
     * @param material tên vật liệu, ví dụ "STONE", "DIAMOND_SWORD"
     * @param def vật liệu mặc định nếu phân tích thất bại
     * @return chính builder hiện tại
     */
    public ItemBuilder material(String material, Material def) {
        //this.material = MaterialParser.parse(material, def);
        return this;
    }

    /**
     * Phân tích chuỗi vật liệu và đặt nó cho item.
     * Mặc định sẽ sử dụng {@link Material#AIR} nếu phân tích thất bại.
     *
     * @param material tên vật liệu
     * @return chính builder hiện tại
     */
    public ItemBuilder material(String material) {
        return this.material(material, Material.AIR);
    }

    /**
     * Lấy tên hiển thị hiện tại của item.
     *
     * @return {@link Component} tên hiển thị
     */
    public Component displayName() {
        return this.displayName;
    }

    /**
     * Đặt tên hiển thị của item dưới dạng {@link Component}.
     *
     * @param displayName component tên hiển thị
     * @return chính builder hiện tại
     */
    public ItemBuilder displayName(Component displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Đặt tên hiển thị bằng chuỗi legacy (ký tự &).
     *
     * @param displayName chuỗi legacy
     * @return chính builder hiện tại
     */
    public ItemBuilder displayName(String displayName) {
        this.displayName = this.legacyDeserialize(displayName);
        return this;
    }

    /**
     * Thêm một dòng lore dưới dạng {@link Component}.
     *
     * @param lore component lore cần thêm
     * @return chính builder hiện tại
     */
    public ItemBuilder addLore(Component lore) {
        this.lore.add(lore);
        return this;
    }

    /**
     * Thêm một dòng lore bằng chuỗi legacy (ký tự &).
     *
     * @param lore chuỗi lore cần thêm
     * @return chính builder hiện tại
     */
    public ItemBuilder addLore(String lore) {
        this.lore.add(this.legacyDeserialize(lore));
        return this;
    }

    /**
     * Lấy danh sách lore hiện tại của item.
     *
     * @return danh sách lore
     */
    public List<Component> lore() {
        return this.lore;
    }

    /**
     * Đặt danh sách lore mới cho item.
     *
     * @param lore danh sách lore mới
     * @return chính builder hiện tại
     */
    public ItemBuilder lore(List<Component> lore) {
        this.lore = lore;
        return this;
    }

    /**
     * Thêm một {@link ComponentPlaceholder} vào danh sách placeholder.
     *
     * @param placeholder đối tượng placeholder
     * @return chính builder hiện tại
     */
    public ItemBuilder addPlaceholder(ComponentPlaceholder placeholder) {
        this.placeholders.add(placeholder);
        return this;
    }

    /**
     * Lấy tất cả placeholder hiện tại.
     *
     * @return tập hợp placeholder
     */
    public Collection<ComponentPlaceholder> placeholders() {
        return this.placeholders;
    }

    /**
     * Hoàn tất quá trình xây dựng item.
     * <p>
     * Toàn bộ placeholder sẽ được áp dụng lên {@link #displayName} và {@link #lore()}
     * trước khi tạo {@link ItemStack} kết quả.
     *
     * @return {@link ItemStack} đã hoàn thiện
     */
    public ItemStack build() {
        ItemStack result = new ItemStack(material);

        Component displayName = this.displayName;
        List<Component> lore = this.lore;

        for (var placeholder : this.placeholders) {
            displayName = placeholder.apply(displayName);
            lore = placeholder.apply(lore);
        }

        if (result.getType() != Material.AIR) {
            ItemMeta meta = result.getItemMeta();
            meta.displayName(displayName);
            meta.lore(lore);
            result.setItemMeta(meta);
        }
        return result;
    }

    /**
     * Chuyển đổi chuỗi legacy (&) sang {@link Component},
     * đồng thời loại bỏ định dạng chữ nghiêng mặc định.
     *
     * @param legacy chuỗi legacy cần chuyển
     * @return {@link Component} đã chuyển đổi
     */
    private Component legacyDeserialize(String legacy) {
        return LegacyComponentSerializer.legacyAmpersand()
                .deserialize(legacy)
                .decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Tạo một builder trống.
     *
     * @return đối tượng {@link ItemBuilder} mới
     */
    public static ItemBuilder builder() {
        return new ItemBuilder();
    }

    /**
     * Tạo builder từ một {@link Material}.
     *
     * @param material vật liệu của item
     * @return đối tượng {@link ItemBuilder} mới
     */
    public static ItemBuilder builder(Material material) {
        return new ItemBuilder(material);
    }

    /**
     * Tạo builder từ chuỗi vật liệu và vật liệu mặc định.
     *
     * @param material tên vật liệu
     * @param def vật liệu mặc định nếu parse thất bại
     * @return đối tượng {@link ItemBuilder} mới
     */
    public static ItemBuilder builder(String material, Material def) {
        return new ItemBuilder(material, def);
    }

    /**
     * Tạo builder từ chuỗi vật liệu.
     * <p>
     * Nếu không hợp lệ, mặc định sử dụng {@link Material#AIR}.
     *
     * @param material tên vật liệu
     * @return đối tượng {@link ItemBuilder} mới
     */
    public static ItemBuilder builder(String material) {
        return new ItemBuilder(material);
    }
}