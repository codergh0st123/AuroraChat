package ru.aurora.chat.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.aurora.chat.service.MessageService;
import ru.aurora.chat.service.SoundPreferenceService;

import java.util.Map;

public final class SoundMenuCommand implements CommandExecutor, Listener {

    private static final Map<Integer, SoundOption> SOUND_OPTIONS = Map.of(
            10, new SoundOption("Сфера опыта", Material.EXPERIENCE_BOTTLE, Sound.ENTITY_EXPERIENCE_ORB_PICKUP),
            12, new SoundOption("Колокольчик", Material.BELL, Sound.BLOCK_NOTE_BLOCK_CHIME),
            14, new SoundOption("Уровень", Material.EXPERIENCE_BOTTLE, Sound.ENTITY_PLAYER_LEVELUP),
            16, new SoundOption("Нота", Material.NOTE_BLOCK, Sound.BLOCK_NOTE_BLOCK_PLING)
    );

    private final MessageService messages;
    private final SoundPreferenceService soundPreferences;

    public SoundMenuCommand(MessageService messages, SoundPreferenceService soundPreferences) {
        this.messages = messages;
        this.soundPreferences = soundPreferences;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.message("messages.errors.only_players"));
            return true;
        }

        if (!player.hasPermission("aurorachat.sound")) {
            player.sendMessage(messages.message(player, "messages.errors.no_permission"));
            return true;
        }

        openMenu(player);
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder(false) instanceof SoundMenuHolder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getRawSlot() < 0 || event.getRawSlot() >= topInventory.getSize()) {
            return;
        }

        SoundOption option = SOUND_OPTIONS.get(event.getRawSlot());
        if (option == null) {
            return;
        }

        soundPreferences.setSound(player.getUniqueId(), option.sound());
        player.playSound(player.getLocation(), option.sound(), 1.0F, 1.0F);
        player.sendMessage(messages.message(player, "messages.menu.sound_selected", Map.of("звук", option.name())));
        player.closeInventory();
    }

    private void openMenu(Player player) {
        SoundMenuHolder holder = new SoundMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27, messages.message(player, "messages.menu.sound_selection"));
        holder.setInventory(inventory);

        SOUND_OPTIONS.forEach((slot, option) -> inventory.setItem(slot, createOption(option)));
        player.openInventory(inventory);
    }

    private ItemStack createOption(SoundOption option) {
        ItemStack item = new ItemStack(option.material());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(option.name()));
        item.setItemMeta(meta);
        return item;
    }

    private record SoundOption(String name, Material material, Sound sound) {
    }

    private static final class SoundMenuHolder implements InventoryHolder {

        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }
}
