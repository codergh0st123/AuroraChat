package ru.aurora.chat.service;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlaceholderService {

    private final boolean placeholderApiAvailable;

    public PlaceholderService(JavaPlugin plugin) {
        placeholderApiAvailable = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public String apply(Player player, String text) {
        if (!placeholderApiAvailable || player == null || text.isEmpty()) {
            return text;
        }

        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
