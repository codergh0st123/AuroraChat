package ru.aurora.chat.service;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Optional;

public final class ChatRestrictionService {

    private static final String CONFIG_PATH = "CHAT_RESTRICTION";

    private final JavaPlugin plugin;
    private volatile boolean enabled;
    private volatile List<String> forbiddenCharacters = List.of();

    public ChatRestrictionService(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        enabled = plugin.getConfig().getBoolean(CONFIG_PATH + ".ENABLED", true);
        forbiddenCharacters = plugin.getConfig().getStringList(CONFIG_PATH + ".FORBIDDEN_CHARACTERS").stream()
                .filter(character -> !character.isEmpty())
                .distinct()
                .toList();
    }

    public Optional<String> findForbiddenCharacter(String message) {
        if (!enabled || message.isEmpty()) {
            return Optional.empty();
        }

        return forbiddenCharacters.stream()
                .filter(message::contains)
                .findFirst();
    }
}
