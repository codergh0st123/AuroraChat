package ru.aurora.chat.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.aurora.chat.command.PrivateMessageCommand;

public final class PlayerLifecycleListener implements Listener {

    private final PrivateMessageCommand privateMessages;

    public PlayerLifecycleListener(PrivateMessageCommand privateMessages) {
        this.privateMessages = privateMessages;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        privateMessages.removeSpy(event.getPlayer());
    }
}
