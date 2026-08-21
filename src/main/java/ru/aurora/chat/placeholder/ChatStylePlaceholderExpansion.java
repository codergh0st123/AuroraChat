//
// Decompiled by Procyon v0.6.0
//

package ru.aurora.chat.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import org.jetbrains.annotations.Nullable;
import org.bukkit.entity.Player;
import java.util.Locale;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import ru.aurora.chat.service.TextFormatService;
import ru.aurora.chat.service.MessageColorService;
import ru.aurora.chat.service.ChatStyleService;
import org.bukkit.plugin.java.JavaPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public final class ChatStylePlaceholderExpansion extends PlaceholderExpansion
{
    private static final String PLAYER_PARAMETER = "PLAYER";
    private static final String SAMPLE_MESSAGE = "\u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435";
    private static final String CURRENT_STYLE_PARAMETER = "STYLE_CHAT_ME";
    private static final String COLORED_MESSAGE_STYLE_PARAMETER = "COLOR_MESSAGE_ME_STYLE_CHAT";
    private static final String STYLE_PARAMETER_PREFIX = "STYLE_CHAT_";
    private final JavaPlugin plugin;
    private final ChatStyleService chatStyles;
    private final MessageColorService messageColors;
    private final TextFormatService textFormats;

    public ChatStylePlaceholderExpansion(final JavaPlugin plugin, final ChatStyleService chatStyles, final MessageColorService messageColors, final TextFormatService textFormats) {
        this.plugin = plugin;
        this.chatStyles = chatStyles;
        this.messageColors = messageColors;
        this.textFormats = textFormats;
    }

    @NotNull
    public String getIdentifier() {
        return "chat";
    }

    @NotNull
    public String getAuthor() {
        return "Aurora";
    }

    @NotNull
    public String getVersion() {
        return this.plugin.getPluginMeta().getVersion();
    }

    public boolean persist() {
        return true;
    }

    @Nullable
    public String onRequest(final OfflinePlayer player, @NotNull final String parameter) {
        if (player == null) {
            return "";
        }
        final String normalizedParameter = parameter.toUpperCase(Locale.ROOT);
        if ("PLAYER".equals(normalizedParameter)) {
            final Player onlinePlayer = player.getPlayer();
            final String playerName = (onlinePlayer == null) ? player.getName() : onlinePlayer.getName();
            return (playerName == null) ? "" : playerName;
        }
        if ("STYLE_CHAT_ME".equals(normalizedParameter)) {
            return this.resolveFormat(player, this.chatStyles.getCurrentLocalFormat(player.getUniqueId()));
        }
        if ("COLOR_MESSAGE_ME_STYLE_CHAT".equals(normalizedParameter)) {
            final String coloredMessage = this.messageColors.apply(player.getUniqueId(), "\u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435");
            final String resolved = this.resolveFormat(player, this.chatStyles.getCurrentLocalFormat(player.getUniqueId()), coloredMessage);
            return this.textFormats.formatLegacy(player.getPlayer(), resolved);
        }
        if (!normalizedParameter.startsWith("STYLE_CHAT_")) {
            return null;
        }
        if ("STYLE_CHAT_GLOBAL".equals(normalizedParameter)) {
            return this.chatStyles.getStyleFormat("GLOBAL", ChatStyleService.Channel.GLOBAL).map(format -> this.resolveFormat(player, format)).orElse("");
        }
        if ("STYLE_CHAT_LOCAL".equals(normalizedParameter)) {
            return this.chatStyles.getStyleFormat("LOCAL", ChatStyleService.Channel.LOCAL).map(format -> this.resolveFormat(player, format)).orElse("");
        }
        String styleId = parameter.substring("STYLE_CHAT_".length());
        ChatStyleService.Channel channel = ChatStyleService.Channel.LOCAL;
        if (normalizedParameter.endsWith("_GLOBAL")) {
            styleId = styleId.substring(0, styleId.length() - "_GLOBAL".length());
            channel = ChatStyleService.Channel.GLOBAL;
        }
        else if (normalizedParameter.endsWith("_LOCAL")) {
            styleId = styleId.substring(0, styleId.length() - "_LOCAL".length());
        }
        return this.chatStyles.getStyleFormat(styleId, channel).map(format -> this.resolveFormat(player, format)).orElse("");
    }

    private String resolveFormat(final OfflinePlayer player, final String format) {
        return this.resolveFormat(player, format, this.chatStyles.getLastMessage(player.getUniqueId()));
    }

    private String resolveFormat(final OfflinePlayer player, final String format, final String message) {
        final Player onlinePlayer = player.getPlayer();
        final String playerName = (onlinePlayer == null) ? player.getName() : onlinePlayer.getName();
        String resolved = format;
        for (int pass = 0; pass < 3; ++pass) {
            final String previous = resolved;
            resolved = ((onlinePlayer == null) ? PlaceholderAPI.setPlaceholders(player, previous) : PlaceholderAPI.setPlaceholders(onlinePlayer, previous));
            if (resolved.equals(previous)) {
                break;
            }
        }
        return resolved.replace("{\u0438\u0433\u0440\u043e\u043a}", (playerName == null) ? "" : playerName).replace("{\u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435}", message);
    }
}
