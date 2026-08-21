//
// Decompiled by Procyon v0.6.0
//

package ru.aurora.chat.command;

import org.bukkit.Sound;
import java.util.function.Predicate;
import net.kyori.adventure.text.Component;
import java.util.Map;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import ru.aurora.chat.service.SpyPreferenceService;
import ru.aurora.chat.service.SoundPreferenceService;
import ru.aurora.chat.service.MessageService;
import org.bukkit.command.CommandExecutor;

public final class PrivateMessageCommand implements CommandExecutor
{
    private final MessageService messages;
    private final SoundPreferenceService soundPreferences;
    private final SpyPreferenceService spyPreferences;

    public PrivateMessageCommand(final MessageService messages, final SoundPreferenceService soundPreferences, final SpyPreferenceService spyPreferences) {
        this.messages = messages;
        this.soundPreferences = soundPreferences;
        this.spyPreferences = spyPreferences;
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.messages.message("messages.errors.only_players"));
            return true;
        }
        final Player player = (Player)sender;
        if (!player.hasPermission("aurorachat.pm")) {
            player.sendMessage(this.messages.message(player, "messages.errors.no_permission"));
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(this.messages.message(player, "messages.pm.usage"));
            return true;
        }
        final Player recipient = Bukkit.getPlayerExact(args[0]);
        if (recipient == null || !recipient.isOnline()) {
            player.sendMessage(this.messages.message(player, "messages.errors.player_not_found"));
            return true;
        }
        if (recipient.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(this.messages.message(player, "messages.errors.pm_self"));
            return true;
        }
        final String text = String.join(" ", (CharSequence[])Arrays.copyOfRange(args, 1, args.length));
        final Component senderMessage = this.messages.messageWithRawText(player, "messages.pm.sender", Map.of("\u043f\u043e\u043b\u0443\u0447\u0430\u0442\u0435\u043b\u044c", recipient.getName()), text);
        final Component recipientMessage = this.messages.messageWithRawText(recipient, "messages.pm.recipient", Map.of("\u043e\u0442\u043f\u0440\u0430\u0432\u0438\u0442\u0435\u043b\u044c", player.getName()), text);
        player.sendMessage(senderMessage);
        recipient.sendMessage(recipientMessage);
        this.playNotification(recipient);
        this.sendPrivateMessageSpy(player, recipient, text);
        return true;
    }

    public boolean toggleSpy(final Player player, final boolean enabled) {
        final boolean currentlyEnabled = this.isSpyEnabled(player);
        if (currentlyEnabled == enabled) {
            return false;
        }
        this.spyPreferences.setEnabled(player.getUniqueId(), enabled);
        return true;
    }

    public boolean isSpyEnabled(final Player player) {
        return this.spyPreferences.isEnabled(player.getUniqueId());
    }

    public void sendLocalChatSpy(final Player sender, final Component formattedMessage, final int radius) {
        Bukkit.getOnlinePlayers().stream().filter(player -> !player.getUniqueId().equals(sender.getUniqueId())).filter(player -> player.hasPermission("aurorachat.spy")).filter(this::isSpyEnabled).filter(player -> !this.isWithinLocalChatRange(sender, player, radius)).forEach(player -> player.sendMessage(formattedMessage));
    }

    private void playNotification(final Player recipient) {
        final Sound sound = this.soundPreferences.getSound(recipient.getUniqueId());
        recipient.playSound(recipient.getLocation(), sound, 1.0f, 1.0f);
    }

    private void sendPrivateMessageSpy(final Player sender, final Player recipient, final String text) {
        final Component spyMessage = (Component)Component.text("[SPY] " + sender.getName() + " -> " + recipient.getName() + ": " + text);
        Bukkit.getOnlinePlayers().stream().filter(player -> !player.getUniqueId().equals(sender.getUniqueId())).filter(player -> !player.getUniqueId().equals(recipient.getUniqueId())).filter(player -> player.hasPermission("aurorachat.spy")).filter(this::isSpyEnabled).forEach(player -> player.sendMessage(spyMessage));
    }

    private boolean isWithinLocalChatRange(final Player sender, final Player recipient, final int configuredRadius) {
        if (!sender.getWorld().equals((Object)recipient.getWorld())) {
            return false;
        }
        final int radius = Math.max(0, configuredRadius);
        final double radiusSquared = radius * (double)radius;
        return sender.getLocation().distanceSquared(recipient.getLocation()) <= radiusSquared;
    }
}
