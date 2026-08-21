//
// Decompiled by Procyon v0.6.0
//

package ru.aurora.chat.command;

import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import ru.aurora.chat.service.SoundPreferenceService;
import ru.aurora.chat.service.MessageService;
import java.util.Map;
import org.bukkit.event.Listener;
import org.bukkit.command.CommandExecutor;

public final class SoundMenuCommand implements CommandExecutor, Listener
{
    private static final Map<Integer, SoundOption> SOUND_OPTIONS;
    private final MessageService messages;
    private final SoundPreferenceService soundPreferences;

    public SoundMenuCommand(final MessageService messages, final SoundPreferenceService soundPreferences) {
        this.messages = messages;
        this.soundPreferences = soundPreferences;
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.messages.message("messages.errors.only_players"));
            return true;
        }
        final Player player = (Player)sender;
        if (!player.hasPermission("aurorachat.sound")) {
            player.sendMessage(this.messages.message(player, "messages.errors.no_permission"));
            return true;
        }
        this.openMenu(player);
        return true;
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        final HumanEntity whoClicked = event.getWhoClicked();
        if (!(whoClicked instanceof Player)) {
            return;
        }
        final Player player = (Player)whoClicked;
        final Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder(false) instanceof SoundMenuHolder)) {
            return;
        }
        event.setCancelled(true);
        if (event.getRawSlot() < 0 || event.getRawSlot() >= topInventory.getSize()) {
            return;
        }
        final SoundOption option = SoundMenuCommand.SOUND_OPTIONS.get(event.getRawSlot());
        if (option == null) {
            return;
        }
        this.soundPreferences.setSound(player.getUniqueId(), option.sound());
        player.playSound(player.getLocation(), option.sound(), 1.0f, 1.0f);
        player.sendMessage(this.messages.message(player, "messages.menu.sound_selected", Map.of("\u0437\u0432\u0443\u043a", option.name())));
        player.closeInventory();
    }

    private void openMenu(final Player player) {
        final SoundMenuHolder holder = new SoundMenuHolder();
        final Inventory inventory = Bukkit.createInventory((InventoryHolder)holder, 27, this.messages.message(player, "messages.menu.sound_selection"));
        holder.setInventory(inventory);
        SoundMenuCommand.SOUND_OPTIONS.forEach((slot, option) -> inventory.setItem((int)slot, this.createOption(option)));
        player.openInventory(inventory);
    }

    private ItemStack createOption(final SoundOption option) {
        final ItemStack item = new ItemStack(option.material());
        final ItemMeta meta = item.getItemMeta();
        meta.displayName((Component)Component.text(option.name()));
        item.setItemMeta(meta);
        return item;
    }

    static {
        SOUND_OPTIONS = Map.of(10, new SoundOption("\u0421\u0444\u0435\u0440\u0430 \u043e\u043f\u044b\u0442\u0430", Material.EXPERIENCE_BOTTLE, Sound.ENTITY_EXPERIENCE_ORB_PICKUP), 12, new SoundOption("\u041a\u043e\u043b\u043e\u043a\u043e\u043b\u044c\u0447\u0438\u043a", Material.BELL, Sound.BLOCK_NOTE_BLOCK_CHIME), 14, new SoundOption("\u0423\u0440\u043e\u0432\u0435\u043d\u044c", Material.EXPERIENCE_BOTTLE, Sound.ENTITY_PLAYER_LEVELUP), 16, new SoundOption("\u041d\u043e\u0442\u0430", Material.NOTE_BLOCK, Sound.BLOCK_NOTE_BLOCK_PLING));
    }

    record SoundOption(String name, Material material, Sound sound) {}

    private static final class SoundMenuHolder implements InventoryHolder
    {
        private Inventory inventory;

        public Inventory getInventory() {
            return this.inventory;
        }

        public void setInventory(final Inventory inventory) {
            this.inventory = inventory;
        }
    }
}
