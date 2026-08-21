//
// Decompiled by Procyon v0.6.0
//

package ru.aurora.chat.service;

import org.bukkit.plugin.Plugin;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlaceholderService
{
    private static final int MAXIMUM_RESOLUTION_PASSES = 3;
    private final JavaPlugin plugin;

    public PlaceholderService(final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public String apply(final Player player, final String text) {
        if (player == null || text.isEmpty() || !this.isPlaceholderApiAvailable()) {
            return text;
        }
        String resolved = text;
        for (int pass = 0; pass < 3; ++pass) {
            final String previous = resolved;
            resolved = PlaceholderAPI.setPlaceholders(player, previous);
            if (resolved.equals(previous)) {
                break;
            }
        }
        return resolved;
    }

    private boolean isPlaceholderApiAvailable() {
        final Plugin placeholderApi = this.plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
        return placeholderApi != null && placeholderApi.isEnabled();
    }
}
