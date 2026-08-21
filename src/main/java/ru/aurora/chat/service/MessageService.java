//
// Decompiled by Procyon v0.6.0
//

package ru.aurora.chat.service;

import java.util.Iterator;
import java.util.List;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextReplacementConfig;
import java.util.Map;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class MessageService
{
    private final JavaPlugin plugin;
    private final TextFormatService textFormatter;

    public MessageService(final JavaPlugin plugin, final PlaceholderService placeholders) {
        this.plugin = plugin;
        this.textFormatter = new TextFormatService(placeholders);
    }

    public void reload() {
        this.plugin.reloadConfig();
    }

    public boolean getBoolean(final String path) {
        return this.plugin.getConfig().getBoolean(path);
    }

    public int getInt(final String path) {
        return this.plugin.getConfig().getInt(path);
    }

    public String getString(final String path) {
        final FileConfiguration config = this.plugin.getConfig();
        return config.getString(path, "");
    }

    public String replacePlaceholders(final Player player, final String text) {
        return this.textFormatter.replacePlaceholders(player, text);
    }

    public Component message(final String path) {
        return this.textFormatter.format(null, this.getString(path));
    }

    public Component message(final Player player, final String path) {
        return this.textFormatter.format(player, this.getString(path));
    }

    public Component message(final String path, final Map<String, String> replacements) {
        return this.textFormatter.format(null, this.replaceValues(this.getString(path), replacements));
    }

    public Component message(final Player player, final String path, final Map<String, String> replacements) {
        final String template = this.replacePlaceholders(player, this.getString(path));
        return this.textFormatter.format(player, this.replaceValues(template, replacements));
    }

    public Component messageWithRawText(final Player player, final String path, final Map<String, String> replacements, final String rawText) {
        final String marker = "__AURORA_RAW_TEXT__";
        final String template = this.replaceValues(this.replacePlaceholders(player, this.getString(path)), replacements).replace("{\u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435}", marker);
        final Component formatted = this.textFormatter.format(player, template);
        return formatted.replaceText((TextReplacementConfig)TextReplacementConfig.builder().matchLiteral(marker).replacement((ComponentLike)this.formatPlayerText(player, rawText)).build());
    }

    public void sendMessageList(final Player player, final String path, final Map<String, String> replacements) {
        final List<String> lines = this.plugin.getConfig().getStringList(path);
        for (final String line : lines) {
            final String template = this.replacePlaceholders(player, line);
            player.sendMessage(this.textFormatter.format(player, this.replaceValues(template, replacements)));
        }
    }

    public Component colored(final Player player, final String text) {
        return this.textFormatter.format(player, text);
    }

    public Component formatPlayerText(final Player player, final String text) {
        return this.textFormatter.format(player, text);
    }

    private String replaceValues(final String text, final Map<String, String> replacements) {
        String result = text;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            result = result.replace("{" + (String)entry.getKey(), (CharSequence)entry.getValue());
        }
        return result;
    }
}
