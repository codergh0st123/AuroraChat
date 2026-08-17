package ru.aurora.chat.service;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrefixService {

    private final JavaPlugin plugin;
    private LuckPerms luckPerms;

    public PrefixService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (plugin.getServer().getPluginManager().getPlugin("LuckPerms") == null) {
            return;
        }

        try {
            luckPerms = LuckPermsProvider.get();
        } catch (IllegalStateException exception) {
            plugin.getLogger().warning("LuckPerms обнаружен, но его API пока недоступен.");
        }
    }

    public String getPrefix(Player player, boolean enabled) {
        if (!enabled || luckPerms == null) {
            return "";
        }

        String prefix = luckPerms.getPlayerAdapter(Player.class)
                .getMetaData(player)
                .getPrefix();
        return prefix == null ? "" : prefix;
    }
}
