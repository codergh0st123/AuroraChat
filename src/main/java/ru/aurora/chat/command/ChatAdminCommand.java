//
// Decompiled by Procyon v0.6.0
//

package ru.aurora.chat.command;

import java.util.Optional;
import ru.aurora.chat.service.ChatStyle;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import ru.aurora.chat.service.MessageColorService;
import ru.aurora.chat.service.ChatRestrictionService;
import ru.aurora.chat.service.ChatStyleService;
import ru.aurora.chat.service.PrefixService;
import ru.aurora.chat.service.MessageService;
import org.bukkit.command.CommandExecutor;

public final class ChatAdminCommand implements CommandExecutor
{
    private final MessageService messages;
    private final PrefixService prefixes;
    private final ChatStyleService chatStyles;
    private final ChatRestrictionService restrictions;
    private final MessageColorService messageColors;

    public ChatAdminCommand(final MessageService messages, final PrefixService prefixes, final ChatStyleService chatStyles, final ChatRestrictionService restrictions, final MessageColorService messageColors) {
        this.messages = messages;
        this.prefixes = prefixes;
        this.chatStyles = chatStyles;
        this.restrictions = restrictions;
        this.messageColors = messageColors;
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            return this.reload(sender);
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("set")) {
            return this.setStyle(sender, args);
        }
        return args.length >= 1 && args[0].equalsIgnoreCase("setcolor") && this.setMessageColor(sender, args);
    }

    private boolean reload(final CommandSender sender) {
        if (!sender.hasPermission("aurorachat.reload")) {
            sender.sendMessage(this.messages.message("messages.errors.no_permission"));
            return true;
        }
        this.messages.reload();
        this.restrictions.reload();
        this.prefixes.initialize();
        return true;
    }

    private boolean setStyle(final CommandSender sender, final String[] args) {
        if (args.length == 2 && sender instanceof Player) {
            final Player player = (Player)sender;
            return this.setOwnStyle(player, args[1]);
        }
        if (args.length != 3) {
            this.sendTargetUsage(sender);
            return true;
        }
        if (!sender.hasPermission("aurorachat.set.other")) {
            sender.sendMessage(this.messages.message("messages.errors.no_permission"));
            return true;
        }
        final Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(this.messages.message("messages.errors.player_not_found"));
            return true;
        }
        return this.setTargetStyle(sender, target, args[2]);
    }

    private boolean setMessageColor(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission("aurorachat.setcolor")) {
            sender.sendMessage(this.messages.message("messages.errors.no_permission"));
            return true;
        }
        if (args.length != 3) {
            sender.sendMessage(this.messages.message("messages.CHAT_COLOR.USAGE"));
            return true;
        }
        final Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(this.messages.message("messages.errors.player_not_found"));
            return true;
        }
        if (!this.messageColors.setColor(target.getUniqueId(), args[2])) {
            sender.sendMessage(this.messages.message("messages.CHAT_COLOR.INVALID"));
            return true;
        }
        target.sendMessage(this.messages.message(target, "messages.CHAT_COLOR.TARGET_SET"));
        sender.sendMessage(this.messages.message("messages.CHAT_COLOR.SET", Map.of("\u0418\u0413\u0420\u041e\u041a", target.getName(), "\u0426\u0412\u0415\u0422", args[2])));
        return true;
    }

    private boolean setOwnStyle(final Player player, final String styleId) {
        if (!player.hasPermission("aurorachat.set")) {
            player.sendMessage(this.messages.message(player, "messages.errors.no_permission"));
            return true;
        }
        if (styleId.equalsIgnoreCase("auto")) {
            this.chatStyles.setAutomaticStyle(player.getUniqueId());
            this.messages.sendMessageList(player, "messages.CHAT_STYLE.AUTOMATIC_SELECTED", Map.of());
            return true;
        }
        final Optional<ChatStyle> style = this.chatStyles.findStyle(styleId);
        if (style.isEmpty()) {
            this.messages.sendMessageList(player, "messages.CHAT_STYLE.NOT_FOUND", Map.of("\u0421\u0422\u0418\u041b\u042c", styleId));
            return true;
        }
        this.chatStyles.setSelectedStyle(player.getUniqueId(), style.get().id());
        this.messages.sendMessageList(player, "messages.CHAT_STYLE.SELECTED", Map.of("\u0421\u0422\u0418\u041b\u042c", style.get().id()));
        return true;
    }

    private boolean setTargetStyle(final CommandSender sender, final Player target, final String styleId) {
        if (styleId.equalsIgnoreCase("auto")) {
            this.chatStyles.setAutomaticStyle(target.getUniqueId());
            this.messages.sendMessageList(target, "messages.CHAT_STYLE.AUTOMATIC_SELECTED", Map.of());
            sender.sendMessage(this.messages.message("messages.CHAT_STYLE.TARGET_AUTOMATIC_SELECTED", Map.of("\u0418\u0413\u0420\u041e\u041a", target.getName())));
            return true;
        }
        final Optional<ChatStyle> style = this.chatStyles.findStyle(styleId);
        if (style.isEmpty()) {
            sender.sendMessage(this.messages.message("messages.CHAT_STYLE.NOT_FOUND_SINGLE", Map.of("\u0421\u0422\u0418\u041b\u042c", styleId)));
            return true;
        }
        this.chatStyles.setSelectedStyle(target.getUniqueId(), style.get().id());
        this.messages.sendMessageList(target, "messages.CHAT_STYLE.SELECTED", Map.of("\u0421\u0422\u0418\u041b\u042c", style.get().id()));
        sender.sendMessage(this.messages.message("messages.CHAT_STYLE.TARGET_SELECTED", Map.of("\u0418\u0413\u0420\u041e\u041a", target.getName(), "\u0421\u0422\u0418\u041b\u042c", style.get().id())));
        return true;
    }

    private void sendTargetUsage(final CommandSender sender) {
        if (sender instanceof final Player player) {
            this.sendStyleUsage(player);
            return;
        }
        sender.sendMessage(this.messages.message("messages.CHAT_STYLE.CONSOLE_USAGE"));
    }

    private void sendStyleUsage(final Player player) {
        final String styles = String.join(", ", this.chatStyles.getStyleIds());
        this.messages.sendMessageList(player, "messages.CHAT_STYLE.USAGE", Map.of("\u0421\u0422\u0418\u041b\u0418", styles));
    }
}
