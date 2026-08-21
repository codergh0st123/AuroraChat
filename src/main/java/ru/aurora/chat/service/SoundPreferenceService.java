//
// Decompiled by Procyon v0.6.0
//

package ru.aurora.chat.service;

import java.io.IOException;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Map;
import org.bukkit.Sound;

public final class SoundPreferenceService
{
    private static final String DEFAULT_SOUND_ID = "ORBIT";
    private static final Sound DEFAULT_SOUND;
    private static final Map<String, Sound> SOUNDS_BY_ID;
    private static final Map<Sound, String> IDS_BY_SOUND;
    private final JavaPlugin plugin;
    private final File storageFile;
    private final YamlConfiguration storage;

    public SoundPreferenceService(final JavaPlugin plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "preferences.yml");
        this.storage = YamlConfiguration.loadConfiguration(this.storageFile);
    }

    public Sound getSound(final UUID playerId) {
        final String soundId = this.storage.getString(this.path(playerId), "ORBIT");
        return SoundPreferenceService.SOUNDS_BY_ID.getOrDefault(soundId, SoundPreferenceService.DEFAULT_SOUND);
    }

    public void setSound(final UUID playerId, final Sound sound) {
        final String soundId = SoundPreferenceService.IDS_BY_SOUND.getOrDefault(sound, "ORBIT");
        this.storage.set(this.path(playerId), (Object)soundId);
        this.save();
    }

    private String path(final UUID playerId) {
        return "PLAYERS." + playerId.toString().toUpperCase() + ".SOUND";
    }

    private void save() {
        try {
            this.storage.save(this.storageFile);
        }
        catch (final IOException exception) {
            this.plugin.getLogger().severe("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u0437\u0432\u0443\u043a\u0430 \u043b\u0438\u0447\u043d\u044b\u0445 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0439.");
        }
    }

    static {
        DEFAULT_SOUND = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        SOUNDS_BY_ID = Map.of("ORBIT", Sound.ENTITY_EXPERIENCE_ORB_PICKUP, "CHIME", Sound.BLOCK_NOTE_BLOCK_CHIME, "LEVEL", Sound.ENTITY_PLAYER_LEVELUP, "NOTE", Sound.BLOCK_NOTE_BLOCK_PLING);
        IDS_BY_SOUND = Map.of(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, "ORBIT", Sound.BLOCK_NOTE_BLOCK_CHIME, "CHIME", Sound.ENTITY_PLAYER_LEVELUP, "LEVEL", Sound.BLOCK_NOTE_BLOCK_PLING, "NOTE");
    }
}
