//
// Decompiled by Procyon v0.6.0
//

package ru.aurora.chat.service;

import org.bukkit.entity.Player;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.LuckPerms;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrefixService
{
    private final JavaPlugin plugin;
    private LuckPerms luckPerms;

    public PrefixService(final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (this.plugin.getServer().getPluginManager().getPlugin("LuckPerms") == null) {
            return;
        }
        try {
            this.luckPerms = LuckPermsProvider.get();
        }
        catch (final IllegalStateException exception) {
            this.plugin.getLogger().warning("LuckPerms \u043e\u0431\u043d\u0430\u0440\u0443\u0436\u0435\u043d, \u043d\u043e \u0435\u0433\u043e API \u043f\u043e\u043a\u0430 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d.");
        }
    }

    public String getPrefix(final Player player, final boolean enabled) {
        if (!enabled || this.luckPerms == null) {
            return "";
        }
        final String prefix = this.luckPerms.getPlayerAdapter((Class)Player.class).getMetaData((Object)player).getPrefix();
        return (prefix == null) ? "" : prefix;
    }
}
