package com.github.letzmc.soilEngine.inventory.placeholder;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.kyori.adventure.text.Component;

/**
 * Đại diện cho một hệ thống thay thế văn bản trong {@link Component}.
 * <p>
 * {@code ComponentPlaceholder} cho phép bạn định nghĩa các cặp khóa - giá trị
 * để thay thế trực tiếp các đoạn văn bản khớp trong {@link Component} hoặc danh sách {@link Component}.
 * <p>
 * Ví dụ:
 * <pre>{@code
 * ComponentPlaceholder placeholder = new ComponentPlaceholder();
 * placeholder.add("{player}", Component.text("Steve"));
 *
 * Component message = Component.text("Xin chào, {player}!");
 * Component result = placeholder.apply(message);
 * // Kết quả: "Xin chào, Steve!"
 * }</pre>
 */
public class ComponentPlaceholder {

    /** Lưu trữ danh sách các placeholder cần thay thế */
    private final Map<String, Component> placeholders = Maps.newLinkedHashMap();

    /**
     * Thêm một placeholder mới.
     *
     * @param key   chuỗi khóa cần thay thế (ví dụ "{player}")
     * @param value giá trị {@link Component} thay thế
     */
    public void add(String key, Component value) {
        this.placeholders.put(key, value);
    }

    /**
     * Thêm một placeholder mới, giá trị được chuyển sang {@link Component.text()}.
     *
     * @param key   chuỗi khóa cần thay thế
     * @param value giá trị bất kỳ (sẽ được {@code toString()})
     */
    public void add(String key, Object value) {
        this.placeholders.put(key, Component.text(value.toString()));
    }

    /**
     * Áp dụng tất cả placeholder lên một {@link Component}.
     * <p>
     * Sử dụng phương thức {@link Component#replaceText} để thay thế literal chính xác.
     *
     * @param origin {@link Component} gốc
     * @return {@link Component} mới đã được thay thế
     */
    public Component apply(Component origin) {
        Component result = origin;

        for (var entry : this.placeholders.entrySet()) {
            result = result.replaceText(builder -> builder
                .matchLiteral(entry.getKey())
                .replacement(entry.getValue()));
        }

        return result;
    }

    /**
     * Áp dụng tất cả placeholder lên danh sách {@link Component}.
     *
     * @param list danh sách {@link Component}
     * @return danh sách mới đã được thay thế
     */
    public List<Component> apply(List<Component> list) {
        List<Component> result = Lists.newArrayList();

        list.forEach(origin -> result.add(this.apply(origin)));

        return result;
    }
}