package ru.aurora.chat.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.aurora.chat.service.MessageService;
import ru.aurora.chat.service.PrefixService;

public final class ChatAdminCommand implements CommandExecutor {

    private final MessageService messages;
    private final PrefixService prefixes;

    public ChatAdminCommand(MessageService messages, PrefixService prefixes) {
        this.messages = messages;
        this.prefixes = prefixes;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("aurorachat.reload")) {
            sender.sendMessage(messages.message("messages.errors.no_permission"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            messages.reload();
            prefixes.initialize();
            return true;
        }

        return false;
    }
}
