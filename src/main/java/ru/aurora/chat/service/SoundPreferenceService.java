package ru.aurora.chat.service;

import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public final class SoundPreferenceService {

    private static final String DEFAULT_SOUND_ID = "ORBIT";
    private static final Sound DEFAULT_SOUND = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    private static final Map<String, Sound> SOUNDS_BY_ID = Map.of(
            "ORBIT", Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
            "CHIME", Sound.BLOCK_NOTE_BLOCK_CHIME,
            "LEVEL", Sound.ENTITY_PLAYER_LEVELUP,
            "NOTE", Sound.BLOCK_NOTE_BLOCK_PLING
    );
    private static final Map<Sound, String> IDS_BY_SOUND = Map.of(
            Sound.ENTITY_EXPERIENCE_ORB_PICKUP, "ORBIT",
            Sound.BLOCK_NOTE_BLOCK_CHIME, "CHIME",
            Sound.ENTITY_PLAYER_LEVELUP, "LEVEL",
            Sound.BLOCK_NOTE_BLOCK_PLING, "NOTE"
    );

    private final JavaPlugin plugin;
    private final File storageFile;
    private final YamlConfiguration storage;

    public SoundPreferenceService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "preferences.yml");
        this.storage = YamlConfiguration.loadConfiguration(storageFile);
    }

    public Sound getSound(UUID playerId) {
        String soundId = storage.getString(path(playerId), DEFAULT_SOUND_ID);
        return SOUNDS_BY_ID.getOrDefault(soundId, DEFAULT_SOUND);
    }

    public void setSound(UUID playerId, Sound sound) {
        String soundId = IDS_BY_SOUND.getOrDefault(sound, DEFAULT_SOUND_ID);
        storage.set(path(playerId), soundId);
        save();
    }

    private String path(UUID playerId) {
        return "PLAYERS." + playerId.toString().toUpperCase() + ".SOUND";
    }

    private void save() {
        try {
            storage.save(storageFile);
        } catch (IOException exception) {
            plugin.getLogger().severe("Не удалось сохранить настройки звука личных сообщений.");
        }
    }
}
