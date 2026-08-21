//
// Decompiled by Procyon v0.6.0
//

package ru.aurora.chat.service;

import java.io.IOException;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public final class MessageColorService
{
    private static final int MAX_COLOR_FORMAT_LENGTH = 128;
    private final JavaPlugin plugin;
    private final File storageFile;
    private final YamlConfiguration storage;

    public MessageColorService(final JavaPlugin plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "message-colors.yml");
        this.storage = YamlConfiguration.loadConfiguration(this.storageFile);
    }

    public String getColor(final UUID playerId) {
        return this.storage.getString(this.path(playerId), "");
    }

    public boolean setColor(final UUID playerId, final String color) {
        final String normalized = color.trim();
        if (normalized.isEmpty() || normalized.length() > 128) {
            return false;
        }
        this.storage.set(this.path(playerId), (Object)normalized);
        return this.save();
    }

    public String apply(final UUID playerId, final String message) {
        final String color = this.getColor(playerId);
        if (color.isEmpty()) {
            return message;
        }
        if (this.isGradientOpeningTag(color)) {
            return color + message + "</gradient>";
        }
        return color + message;
    }

    private boolean isGradientOpeningTag(final String value) {
        return value.regionMatches(true, 0, "<gradient:", 0, "<gradient:".length()) && value.endsWith(">") && !value.contains("</gradient>");
    }

    private String path(final UUID playerId) {
        return "PLAYERS." + playerId.toString().toUpperCase() + ".MESSAGE_COLOR";
    }

    private boolean save() {
        try {
            this.storage.save(this.storageFile);
            return true;
        }
        catch (final IOException exception) {
            this.plugin.getLogger().severe("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0446\u0432\u0435\u0442 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u044f \u0438\u0433\u0440\u043e\u043a\u0430.");
            return false;
        }
    }
}
