package ru.aurora.chat.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.aurora.chat.service.ChatFormatService;
import ru.aurora.chat.service.MessageService;

public final class ChatListener implements Listener {

    private static final PlainTextComponentSerializer PLAIN_TEXT = PlainTextComponentSerializer.plainText();

    private final JavaPlugin plugin;
    private final MessageService messages;
    private final ChatFormatService chatFormat;

    public ChatListener(JavaPlugin plugin, MessageService messages, ChatFormatService chatFormat) {
        this.plugin = plugin;
        this.messages = messages;
        this.chatFormat = chatFormat;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        String content = PLAIN_TEXT.serialize(event.message());
        String globalPrefix = messages.getString("globalChatPrefix");
        boolean globalMessage = !globalPrefix.isEmpty() && content.startsWith(globalPrefix);

        event.setCancelled(true);
        Bukkit.getScheduler().runTask(plugin, () -> deliverMessage(sender, content, globalPrefix, globalMessage));
    }

    private void deliverMessage(Player sender, String content, String globalPrefix, boolean globalMessage) {
        if (!sender.isOnline()) {
            return;
        }

        if (globalMessage) {
            if (!messages.getBoolean("globalChatEnabled")) {
                return;
            }

            String message = content.substring(globalPrefix.length()).trim();
            if (message.isEmpty()) {
                return;
            }

            Component formattedMessage = chatFormat.formatGlobal(sender, message);
            Bukkit.getOnlinePlayers().forEach(recipient -> recipient.sendMessage(formattedMessage));
            Bukkit.getConsoleSender().sendMessage(formattedMessage);
            return;
        }

        if (!messages.getBoolean("localChatEnabled") || content.isBlank()) {
            return;
        }

        int radius = Math.max(0, messages.getInt("localChatRadius"));
        double radiusSquared = (double) radius * radius;
        Component formattedMessage = chatFormat.formatLocal(sender, content);
        Bukkit.getOnlinePlayers().stream()
                .filter(recipient -> recipient.getWorld().equals(sender.getWorld()))
                .filter(recipient -> recipient.getLocation().distanceSquared(sender.getLocation()) <= radiusSquared)
                .forEach(recipient -> recipient.sendMessage(formattedMessage));
        Bukkit.getConsoleSender().sendMessage(formattedMessage);
    }
}
