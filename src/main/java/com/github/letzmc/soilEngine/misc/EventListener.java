package com.github.letzmc.soilEngine.misc;

import com.github.letzmc.soilEngine.SoilEngine;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.plugin.Plugin;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Listener sử dụng lambda
 */
public class EventListener<T extends Event> implements Listener {

    private final BiConsumer<EventListener<T>, T> handler;
    private final Class<T> eventClass;

    public EventListener(Plugin plugin, Class<T> eventClass, EventPriority priority, BiConsumer<EventListener<T>, T> handler) {
        this.handler = handler;
        this.eventClass = eventClass;
        Bukkit.getPluginManager().registerEvent(eventClass, this, priority, (l, e) -> handleEvent((T) e), plugin);
    }

    public EventListener(Plugin plugin, Class<T> eventClass, EventPriority priority, Consumer<T> handler) {
        this(plugin, eventClass, priority, (l, e) -> handler.accept(e));
    }

    public EventListener(Plugin plugin, Class<T> eventClass, BiConsumer<EventListener<T>, T> handler) {
        this(plugin, eventClass, EventPriority.NORMAL, handler);
    }

    public EventListener(Plugin plugin, Class<T> eventClass, Consumer<T> handler) {
        this(plugin, eventClass, EventPriority.NORMAL, handler);
    }

    public EventListener(Class<T> eventClass, BiConsumer<EventListener<T>, T> handler) {
        this(SoilEngine.getInstance(), eventClass, EventPriority.NORMAL, handler);
    }

    public EventListener(Class<T> eventClass, Consumer<T> handler) {
        this(SoilEngine.getInstance(), eventClass, EventPriority.NORMAL, handler);
    }

    @EventHandler
    public void handleEvent(T event) {
        if (eventClass.isAssignableFrom(event.getClass())) {
            handler.accept(this, event);
        }
    }

    /**
     * Hủy đăng ký listener
     */
    public void unregister() {
        HandlerList.unregisterAll(this);
    }

}
