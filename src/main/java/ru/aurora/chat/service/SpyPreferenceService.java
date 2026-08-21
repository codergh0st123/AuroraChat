//
// Decompiled by Procyon v0.6.0
//

package ru.aurora.chat.service;

import java.io.IOException;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpyPreferenceService
{
    private final JavaPlugin plugin;
    private final File storageFile;
    private final YamlConfiguration storage;

    public SpyPreferenceService(final JavaPlugin plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "spy-preferences.yml");
        this.storage = YamlConfiguration.loadConfiguration(this.storageFile);
    }

    public boolean isEnabled(final UUID playerId) {
        return this.storage.getBoolean(this.path(playerId), false);
    }

    public void setEnabled(final UUID playerId, final boolean enabled) {
        this.storage.set(this.path(playerId), (Object)enabled);
        this.save();
    }

    private String path(final UUID playerId) {
        return "PLAYERS." + playerId.toString().toUpperCase() + ".SPY_ENABLED";
    }

    private void save() {
        try {
            this.storage.save(this.storageFile);
        }
        catch (final IOException exception) {
            this.plugin.getLogger().severe("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u0440\u0435\u0436\u0438\u043c\u0430 \u0448\u043f\u0438\u043e\u043d\u0430.");
        }
    }
}
