package com.github.letzmc.soilEngine.misc;

import com.github.letzmc.soilEngine.SoilEngine;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class Task {
    
    public static Task syncDelayed(Runnable run) {
        return syncDelayed(SoilEngine.getInstance(), run);
    }

    public static Task syncDelayed(Plugin plugin, Runnable run) {
        return syncDelayed(plugin, run, 0);
    }

    public static Task syncDelayed(Consumer<Task> run) {
        return syncDelayed(SoilEngine.getInstance(), run);
    }

    public static Task syncDelayed(Plugin plugin, Consumer<Task> run) {
        return syncDelayed(plugin, run, 0);
    }

    public static Task syncDelayed(Runnable run, long delay) {
        return syncDelayed(SoilEngine.getInstance(), run, delay);
    }

    public static Task syncDelayed(Plugin plugin, Runnable run, long delay) {
        return syncDelayed(plugin, t -> run.run(), delay);
    }

    public static Task syncDelayed(Consumer<Task> run, long delay) {
        return syncDelayed(SoilEngine.getInstance(), run, delay);
    }

    public static Task syncDelayed(Plugin plugin, Consumer<Task> run, long delay) {
        Task[] task = {null};
        task[0] = new Task(Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> run.accept(task[0]), delay), TaskType.SYNC_DELAYED, plugin);
        return task[0];
    }

    public static Task syncRepeating(Runnable run, long delay, long period) {
        return syncRepeating(SoilEngine.getInstance(), run, delay, period);
    }

    public static Task syncRepeating(Plugin plugin, Runnable run, long delay, long period) {
        return syncRepeating(plugin, t -> run.run(), delay, period);
    }

    public static Task syncRepeating(Consumer<Task> run, long delay, long period) {
        return syncRepeating(SoilEngine.getInstance(), run, delay, period);
    }

    public static Task syncRepeating(Plugin plugin, Consumer<Task> run, long delay, long period) {
        Task[] task = {null};
        task[0] = new Task(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> run.accept(task[0]), delay, period), TaskType.SYNC_REPEATING, plugin);
        return task[0];
    }

    public static Task asyncDelayed(Runnable run) {
        return asyncDelayed(SoilEngine.getInstance(), run);
    }

    public static Task asyncDelayed(Plugin plugin, Runnable run) {
        return asyncDelayed(plugin, t -> run.run(), 0);
    }

    public static Task asyncDelayed(Consumer<Task> run) {
        return asyncDelayed(SoilEngine.getInstance(), run);
    }

    public static Task asyncDelayed(Plugin plugin, Consumer<Task> run) {
        return asyncDelayed(plugin, run, 0);
    }

    public static Task asyncDelayed(Runnable run, long delay) {
        return asyncDelayed(SoilEngine.getInstance(), run, delay);
    }

    public static Task asyncDelayed(Plugin plugin, Runnable run, long delay) {
        return asyncDelayed(plugin, t -> run.run(), delay);
    }

    public static Task asyncDelayed(Consumer<Task> run, long delay) {
        return asyncDelayed(SoilEngine.getInstance(), run, delay);
    }

    public static Task asyncDelayed(Plugin plugin, Consumer<Task> run, long delay) {
        Task[] task = {null};
        task[0] = new Task(Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, () -> run.accept(task[0]), delay), TaskType.ASYNC_DELAYED, plugin);
        return task[0];
    }

    public static Task asyncRepeating(Consumer<Task> run, long delay, long period) {
        return asyncRepeating(SoilEngine.getInstance(), run, delay, period);
    }

    public static Task asyncRepeating(Plugin plugin, Consumer<Task> run, long delay, long period) {
        Task[] task = {null};
        task[0] = new Task(Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, () -> run.accept(task[0]), delay, period), TaskType.ASYNC_REPEATING, plugin);
        return task[0];
    }

    public static Task asyncRepeating(Runnable run, long delay, long period) {
        return asyncRepeating(SoilEngine.getInstance(), run, delay, period);
    }

    public static Task asyncRepeating(Plugin plugin, Runnable run, long delay, long period) {
        return asyncRepeating(plugin, t -> run.run(), delay, period);
    }

    private int task;
    private TaskType type;
    private Plugin plugin;

    private Task(int task, TaskType type, Plugin plugin) {
        this.task = task;
        this.type = type;
        this.plugin = plugin;
    }

    public TaskType getType() {
        return type;
    }

    public boolean isQueued() {
        return Bukkit.getScheduler().isQueued(task);
    }

    public boolean isCurrentlyRunning() {
        return Bukkit.getScheduler().isCurrentlyRunning(task);
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(task);
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    public enum TaskType {
        SYNC_DELAYED,
        ASYNC_DELAYED,
        SYNC_REPEATING,
        ASYNC_REPEATING;
    }

}