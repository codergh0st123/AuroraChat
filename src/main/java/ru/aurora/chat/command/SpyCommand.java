//
// Decompiled by Procyon v0.6.0
//

package ru.aurora.chat.command;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import ru.aurora.chat.service.MessageService;
import org.bukkit.command.CommandExecutor;

public final class SpyCommand implements CommandExecutor
{
    private final MessageService messages;
    private final PrivateMessageCommand privateMessages;

    public SpyCommand(final MessageService messages, final PrivateMessageCommand privateMessages) {
        this.messages = messages;
        this.privateMessages = privateMessages;
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.messages.message("messages.errors.only_players"));
            return true;
        }
        final Player player = (Player)sender;
        if (!player.hasPermission("aurorachat.spy")) {
            player.sendMessage(this.messages.message(player, "messages.errors.no_permission"));
            return true;
        }
        if (args.length != 1) {
            player.sendMessage(this.messages.message(player, "messages.spy.usage"));
            return true;
        }
        if (args[0].equalsIgnoreCase("on")) {
            if (this.privateMessages.toggleSpy(player, true)) {
                player.sendMessage(this.messages.message(player, "messages.spy.enabled"));
            }
            else {
                player.sendMessage(this.messages.message(player, "messages.spy.already_enabled"));
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("off")) {
            if (this.privateMessages.toggleSpy(player, false)) {
                player.sendMessage(this.messages.message(player, "messages.spy.disabled"));
            }
            else {
                player.sendMessage(this.messages.message(player, "messages.spy.already_disabled"));
            }
            return true;
        }
        player.sendMessage(this.messages.message(player, "messages.spy.usage"));
        return true;
    }
}
