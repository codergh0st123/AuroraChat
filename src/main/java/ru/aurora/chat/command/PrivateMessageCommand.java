package ru.aurora.chat.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.aurora.chat.service.MessageService;
import ru.aurora.chat.service.SoundPreferenceService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PrivateMessageCommand implements CommandExecutor {

    private final MessageService messages;
    private final SoundPreferenceService soundPreferences;
    private final Set<UUID> spyPlayers = new HashSet<>();

    public PrivateMessageCommand(MessageService messages, SoundPreferenceService soundPreferences) {
        this.messages = messages;
        this.soundPreferences = soundPreferences;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.message("messages.errors.only_players"));
            return true;
        }

        if (!player.hasPermission("aurorachat.pm")) {
            player.sendMessage(messages.message(player, "messages.errors.no_permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(messages.message(player, "messages.pm.usage"));
            return true;
        }

        Player recipient = Bukkit.getPlayerExact(args[0]);
        if (recipient == null || !recipient.isOnline()) {
            player.sendMessage(messages.message(player, "messages.errors.player_not_found"));
            return true;
        }

        if (recipient.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(messages.message(player, "messages.errors.pm_self"));
            return true;
        }

        String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Component senderMessage = messages.messageWithRawText(
                player,
                "messages.pm.sender",
                Map.of("получатель", recipient.getName()),
                text
        );
        Component recipientMessage = messages.messageWithRawText(
                recipient,
                "messages.pm.recipient",
                Map.of("отправитель", player.getName()),
                text
        );

        player.sendMessage(senderMessage);
        recipient.sendMessage(recipientMessage);
        playNotification(recipient);
        sendSpyMessages(player, recipient, text);
        return true;
    }

    public boolean toggleSpy(Player player, boolean enabled) {
        if (enabled) {
            return spyPlayers.add(player.getUniqueId());
        }
        return spyPlayers.remove(player.getUniqueId());
    }

    public boolean isSpyEnabled(Player player) {
        return spyPlayers.contains(player.getUniqueId());
    }

    public void removeSpy(Player player) {
        spyPlayers.remove(player.getUniqueId());
    }

    private void playNotification(Player recipient) {
        Sound sound = soundPreferences.getSound(recipient.getUniqueId());
        recipient.playSound(recipient.getLocation(), sound, 1.0F, 1.0F);
    }

    private void sendSpyMessages(Player sender, Player recipient, String text) {
        Component spyMessage = Component.text("[SPY] " + sender.getName() + " -> " + recipient.getName() + ": " + text);
        Bukkit.getOnlinePlayers().stream()
                .filter(player -> !player.getUniqueId().equals(sender.getUniqueId()))
                .filter(player -> !player.getUniqueId().equals(recipient.getUniqueId()))
                .filter(player -> player.hasPermission("aurorachat.spy"))
                .filter(this::isSpyEnabled)
                .forEach(player -> player.sendMessage(spyMessage));
    }
}
