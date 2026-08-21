//
// Decompiled by Procyon v0.6.0
//

package ru.aurora.chat.service;

import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class ChatFormatService
{
    private static final String MESSAGE_MARKER = "__AURORA_CHAT_MESSAGE__";
    private static final String TAB_PREFIX_PLACEHOLDER = "%tab_tabprefix%";
    private final MessageService messages;
    private final PrefixService prefixes;
    private final MessageColorService messageColors;

    public ChatFormatService(final MessageService messages, final PrefixService prefixes, final MessageColorService messageColors) {
        this.messages = messages;
        this.prefixes = prefixes;
        this.messageColors = messageColors;
    }

    public Component formatGlobal(final Player sender, final String message) {
        return this.format("messages.chat.global_format", sender, message);
    }

    public Component formatLocal(final Player sender, final String message) {
        return this.formatTemplate(this.messages.getString("messages.chat.local_format"), sender, message);
    }

    public Component formatStyleGlobal(final ChatStyle style, final Player sender, final String message) {
        return this.formatTemplate(style.globalFormat(), sender, message);
    }

    public Component formatStyleLocal(final ChatStyle style, final Player sender, final String message) {
        return this.formatTemplate(style.localFormat(), sender, message);
    }

    private Component format(final String path, final Player sender, final String message) {
        return this.formatTemplate(this.messages.getString(path), sender, message);
    }

    private Component formatTemplate(String template, final Player sender, final String message) {
        final String prefix = this.prefixes.getPrefix(sender, this.messages.getBoolean("useLuckPermsPrefix"));
        template = this.messages.replacePlaceholders(sender, template)
                .replace("%tab_tabprefix%", prefix)
                .replace("{\u0438\u0433\u0440\u043e\u043a}", sender.getName())
                .replace("{\u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435}", "__AURORA_CHAT_MESSAGE__");
        final Component formatted = this.messages.colored(sender, template);
        return formatted.replaceText((TextReplacementConfig)TextReplacementConfig.builder().matchLiteral("__AURORA_CHAT_MESSAGE__").replacement((ComponentLike)this.messages.formatPlayerText(sender, this.messageColors.apply(sender.getUniqueId(), message))).build());
    }
}
