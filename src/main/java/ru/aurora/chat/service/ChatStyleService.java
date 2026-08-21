package ru.aurora.chat.service;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChatStyleService {

    private static final String AUTOMATIC_STYLE = "AUTO";
    private static final String CHAT_SECTION_PATH = "messages.chat";

    private final JavaPlugin plugin;
    private final MessageService messages;
    private final File storageFile;
    private final YamlConfiguration storage;
    private final Map<UUID, String> lastMessages = new ConcurrentHashMap<>();

    public ChatStyleService(JavaPlugin plugin, MessageService messages) {
        this.plugin = plugin;
        this.messages = messages;
        this.storageFile = new File(plugin.getDataFolder(), "chat-style-preferences.yml");
        this.storage = YamlConfiguration.loadConfiguration(storageFile);
    }

    public Optional<ChatStyle> findStyle(String input) {
        ConfigurationSection chat = plugin.getConfig().getConfigurationSection(CHAT_SECTION_PATH);
        if (chat == null) {
            return Optional.empty();
        }

        for (String key : chat.getKeys(false)) {
            if (chat.isConfigurationSection(key) && sameStyleId(key, input)) {
                return Optional.of(readNestedStyle(chat.getConfigurationSection(key), key));
            }
        }

        return findFlatStyle(chat, input);
    }

    public List<String> getStyleIds() {
        ConfigurationSection chat = plugin.getConfig().getConfigurationSection(CHAT_SECTION_PATH);
        if (chat == null) {
            return List.of();
        }

        return chat.getKeys(false).stream()
                .filter(chat::isConfigurationSection)
                .sorted()
                .toList();
    }

    public Optional<ChatStyle> getSelectedStyle(UUID playerId) {
        String styleId = storage.getString(path(playerId), AUTOMATIC_STYLE);
        if (AUTOMATIC_STYLE.equalsIgnoreCase(styleId)) {
            return Optional.empty();
        }

        Optional<ChatStyle> style = findStyle(styleId);
        if (style.isEmpty()) {
            setAutomaticStyle(playerId);
        }
        return style;
    }

    public String getCurrentLocalFormat(UUID playerId) {
        return getSelectedStyle(playerId)
                .map(ChatStyle::localFormat)
                .orElseGet(() -> messages.getString("messages.chat.local_format"));
    }

    public Optional<String> getStyleFormat(String styleId, Channel channel) {
        if (styleId.equalsIgnoreCase("GLOBAL")) {
            return Optional.of(messages.getString("messages.chat.global_format"));
        }
        if (styleId.equalsIgnoreCase("LOCAL")) {
            return Optional.of(messages.getString("messages.chat.local_format"));
        }

        return findStyle(styleId).map(style -> channel == Channel.GLOBAL
                ? style.globalFormat()
                : style.localFormat());
    }

    public void updateLastMessage(UUID playerId, String message) {
        lastMessages.put(playerId, message);
    }

    public String getLastMessage(UUID playerId) {
        String message = lastMessages.get(playerId);
        return message == null || message.isBlank() ? "сообщение" : message;
    }

    public void clearLastMessage(UUID playerId) {
        lastMessages.remove(playerId);
    }

    public void setSelectedStyle(UUID playerId, String styleId) {
        storage.set(path(playerId), styleId.toUpperCase(Locale.ROOT));
        save();
    }

    public void setAutomaticStyle(UUID playerId) {
        storage.set(path(playerId), AUTOMATIC_STYLE);
        save();
    }

    public void saveNow() {
        save();
    }

    private ChatStyle readNestedStyle(ConfigurationSection section, String key) {
        String globalFormat = section.getString("GLOBAL", messages.getString("messages.chat.global_format"));
        String localFormat = section.getString("LOCAL", messages.getString("messages.chat.local_format"));
        int radius = Math.max(0, section.getInt("RADIUS", messages.getInt("localChatRadius")));
        return new ChatStyle(key, globalFormat, localFormat, radius);
    }

    private Optional<ChatStyle> findFlatStyle(ConfigurationSection chat, String input) {
        for (String key : chat.getKeys(false)) {
            if (!key.toUpperCase(Locale.ROOT).endsWith("_GLOBAL")) {
                continue;
            }

            String styleId = key.substring(0, key.length() - "_GLOBAL".length());
            if (!sameStyleId(styleId, input)) {
                continue;
            }

            String globalFormat = chat.getString(key, messages.getString("messages.chat.global_format"));
            String localFormat = chat.getString(styleId + "_LOCAL", messages.getString("messages.chat.local_format"));
            int radius = Math.max(0, chat.getInt(styleId + "_RADIUS", messages.getInt("localChatRadius")));
            return Optional.of(new ChatStyle(styleId, globalFormat, localFormat, radius));
        }

        return Optional.empty();
    }

    private boolean sameStyleId(String first, String second) {
        return normalizeStyleId(first).equals(normalizeStyleId(second));
    }

    private String normalizeStyleId(String styleId) {
        return styleId.toUpperCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private String path(UUID playerId) {
        return "PLAYERS." + playerId.toString().toUpperCase(Locale.ROOT) + ".CHAT_STYLE";
    }

    private void save() {
        try {
            storage.save(storageFile);
        } catch (IOException exception) {
            plugin.getLogger().severe("Не удалось сохранить выбранный стиль чата.");
        }
    }

    public enum Channel {
        GLOBAL,
        LOCAL
    }
}
