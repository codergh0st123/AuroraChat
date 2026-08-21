//
// Decompiled by Procyon v0.6.0
//

package ru.aurora.chat.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.aurora.chat.service.ChatStyleService;
import org.bukkit.event.Listener;

public final class PlayerSessionListener implements Listener
{
    private final ChatStyleService chatStyles;

    public PlayerSessionListener(final ChatStyleService chatStyles) {
        this.chatStyles = chatStyles;
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        this.chatStyles.clearLastMessage(event.getPlayer().getUniqueId());
    }
}
