package ru.aurora.chat.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class MessageService {

    private final JavaPlugin plugin;
    private final TextFormatService textFormatter;

    public MessageService(JavaPlugin plugin, PlaceholderService placeholders) {
        this.plugin = plugin;
        this.textFormatter = new TextFormatService(placeholders);
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public boolean getBoolean(String path) {
        return plugin.getConfig().getBoolean(path);
    }

    public int getInt(String path) {
        return plugin.getConfig().getInt(path);
    }

    public String getString(String path) {
        FileConfiguration config = plugin.getConfig();
        return config.getString(path, "");
    }

    public Component message(String path) {
        return textFormatter.format(null, getString(path));
    }

    public Component message(Player player, String path) {
        return textFormatter.format(player, getString(path));
    }

    public Component message(Player player, String path, Map<String, String> replacements) {
        return textFormatter.format(player, replaceValues(getString(path), replacements));
    }

    public Component messageWithRawText(Player player, String path, Map<String, String> replacements, String rawText) {
        String marker = "__AURORA_RAW_TEXT__";
        String template = replaceValues(getString(path), replacements).replace("{сообщение}", marker);
        Component formatted = textFormatter.format(player, template);

        return formatted.replaceText(TextReplacementConfig.builder()
                .matchLiteral(marker)
                .replacement(formatPlayerText(player, rawText))
                .build());
    }

    public Component colored(Player player, String text) {
        return textFormatter.format(player, text);
    }

    public Component formatPlayerText(Player player, String text) {
        return textFormatter.format(player, text);
    }

    private String replaceValues(String text, Map<String, String> replacements) {
        String result = text;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
