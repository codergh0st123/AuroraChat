//
// Decompiled by Procyon v0.6.0
//

package ru.aurora.chat.listener;

import net.kyori.adventure.text.Component;
import ru.aurora.chat.service.ChatStyle;
import java.util.Map;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import io.papermc.paper.event.player.AsyncChatEvent;
import ru.aurora.chat.command.PrivateMessageCommand;
import ru.aurora.chat.service.ChatRestrictionService;
import ru.aurora.chat.service.ChatStyleService;
import ru.aurora.chat.service.ChatFormatService;
import ru.aurora.chat.service.MessageService;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.Listener;

public final class ChatListener implements Listener
{
    private static final PlainTextComponentSerializer PLAIN_TEXT;
    private static final String RESTRICTION_BYPASS_PERMISSION = "aurorachat.restriction.bypass";
    private final JavaPlugin plugin;
    private final MessageService messages;
    private final ChatFormatService chatFormat;
    private final ChatStyleService chatStyles;
    private final ChatRestrictionService restrictions;
    private final PrivateMessageCommand privateMessages;

    public ChatListener(final JavaPlugin plugin, final MessageService messages, final ChatFormatService chatFormat, final ChatStyleService chatStyles, final ChatRestrictionService restrictions, final PrivateMessageCommand privateMessages) {
        this.plugin = plugin;
        this.messages = messages;
        this.chatFormat = chatFormat;
        this.chatStyles = chatStyles;
        this.restrictions = restrictions;
        this.privateMessages = privateMessages;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(final AsyncChatEvent event) {
        final Player sender = event.getPlayer();
        final String content = ChatListener.PLAIN_TEXT.serialize(event.message());
        if (!sender.hasPermission("aurorachat.restriction.bypass")) {
            final Optional<String> forbiddenCharacter = this.restrictions.findForbiddenCharacter(content);
            if (forbiddenCharacter.isPresent()) {
                event.setCancelled(true);
                Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> this.sendForbiddenCharacterMessage(sender, forbiddenCharacter.get()));
                return;
            }
        }
        final String globalPrefix = this.messages.getString("globalChatPrefix");
        final boolean globalMessage = !globalPrefix.isEmpty() && content.startsWith(globalPrefix);
        event.setCancelled(true);
        Bukkit.getScheduler().runTask((Plugin)this.plugin, () -> this.deliverMessage(sender, content, globalPrefix, globalMessage));
    }

    private void sendForbiddenCharacterMessage(final Player player, final String character) {
        if (!player.isOnline()) {
            return;
        }
        this.messages.sendMessageList(player, "CHAT_RESTRICTION.MESSAGE", Map.of("\u0421\u0418\u041c\u0412\u041e\u041b", character));
    }

    private void deliverMessage(final Player sender, final String content, final String globalPrefix, final boolean globalMessage) {
        if (!sender.isOnline() || content.isBlank()) {
            return;
        }
        final String previewMessage = globalMessage ? content.substring(globalPrefix.length()).trim() : content;
        if (!previewMessage.isEmpty()) {
            this.chatStyles.updateLastMessage(sender.getUniqueId(), previewMessage);
        }
        final Optional<ChatStyle> selectedStyle = this.chatStyles.getSelectedStyle(sender.getUniqueId());
        if (selectedStyle.isPresent()) {
            this.deliverCustomStyle(sender, content, globalPrefix, globalMessage, selectedStyle.get());
            return;
        }
        if (globalMessage) {
            if (!this.messages.getBoolean("globalChatEnabled")) {
                return;
            }
            final String message = content.substring(globalPrefix.length()).trim();
            if (message.isEmpty()) {
                return;
            }
            this.sendGlobal(this.chatFormat.formatGlobal(sender, message));
        }
        else {
            if (!this.messages.getBoolean("localChatEnabled")) {
                return;
            }
            this.sendLocal(sender, this.chatFormat.formatLocal(sender, content), this.messages.getInt("localChatRadius"));
        }
    }

    private void deliverCustomStyle(final Player sender, final String content, final String globalPrefix, final boolean globalMessage, final ChatStyle style) {
        if (globalMessage) {
            final String message = content.substring(globalPrefix.length()).trim();
            if (!message.isEmpty()) {
                this.sendGlobal(this.chatFormat.formatStyleGlobal(style, sender, message));
            }
            return;
        }
        this.sendLocal(sender, this.chatFormat.formatStyleLocal(style, sender, content), style.radius());
    }

    private void sendGlobal(final Component message) {
        Bukkit.getOnlinePlayers().forEach(recipient -> recipient.sendMessage(message));
        Bukkit.getConsoleSender().sendMessage(message);
    }

    private void sendLocal(final Player sender, final Component message, final int configuredRadius) {
        final int radius = Math.max(0, configuredRadius);
        final double radiusSquared = radius * (double)radius;
        Bukkit.getOnlinePlayers().stream().filter(recipient -> recipient.getWorld().equals((Object)sender.getWorld())).filter(recipient -> recipient.getLocation().distanceSquared(sender.getLocation()) <= radiusSquared).forEach(recipient -> recipient.sendMessage(message));
        this.privateMessages.sendLocalChatSpy(sender, message, radius);
        Bukkit.getConsoleSender().sendMessage(message);
    }

    static {
        PLAIN_TEXT = PlainTextComponentSerializer.plainText();
    }
}
