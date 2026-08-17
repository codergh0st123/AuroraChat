package ru.aurora.chat.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.entity.Player;

import java.util.Map;

public final class ChatFormatService {

    private static final String MESSAGE_MARKER = "__AURORA_CHAT_MESSAGE__";
    private static final String TAB_PREFIX_PLACEHOLDER = "%tab_tabprefix%";

    private final MessageService messages;
    private final PrefixService prefixes;

    public ChatFormatService(MessageService messages, PrefixService prefixes) {
        this.messages = messages;
        this.prefixes = prefixes;
    }

    public Component formatGlobal(Player sender, String message) {
        return format("messages.chat.global_format", sender, message);
    }

    public Component formatLocal(Player sender, String message) {
        return format("messages.chat.local_format", sender, message);
    }

    private Component format(String path, Player sender, String message) {
        String prefix = prefixes.getPrefix(sender, messages.getBoolean("useLuckPermsPrefix"));
        String template = messages.getString(path)
                .replace(TAB_PREFIX_PLACEHOLDER, prefix)
                .replace("{игрок}", sender.getName())
                .replace("{сообщение}", MESSAGE_MARKER);

        Component formatted = messages.colored(sender, template);
        return formatted.replaceText(TextReplacementConfig.builder()
                .matchLiteral(MESSAGE_MARKER)
                .replacement(messages.formatPlayerText(sender, message))
                .build());
    }
}
