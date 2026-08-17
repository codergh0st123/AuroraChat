package ru.aurora.chat.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.aurora.chat.service.MessageService;

public final class SpyCommand implements CommandExecutor {

    private final MessageService messages;
    private final PrivateMessageCommand privateMessages;

    public SpyCommand(MessageService messages, PrivateMessageCommand privateMessages) {
        this.messages = messages;
        this.privateMessages = privateMessages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.message("messages.errors.only_players"));
            return true;
        }

        if (!player.hasPermission("aurorachat.spy")) {
            player.sendMessage(messages.message(player, "messages.errors.no_permission"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(messages.message(player, "messages.spy.usage"));
            return true;
        }

        if (args[0].equalsIgnoreCase("on")) {
            if (privateMessages.toggleSpy(player, true)) {
                player.sendMessage(messages.message(player, "messages.spy.enabled"));
            } else {
                player.sendMessage(messages.message(player, "messages.spy.already_enabled"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("off")) {
            if (privateMessages.toggleSpy(player, false)) {
                player.sendMessage(messages.message(player, "messages.spy.disabled"));
            } else {
                player.sendMessage(messages.message(player, "messages.spy.already_disabled"));
            }
            return true;
        }

        player.sendMessage(messages.message(player, "messages.spy.usage"));
        return true;
    }
}
