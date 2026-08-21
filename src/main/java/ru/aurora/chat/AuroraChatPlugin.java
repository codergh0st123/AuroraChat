//
// Decompiled by Procyon v0.6.0
//

package ru.aurora.chat;

import java.util.Objects;
import org.bukkit.command.PluginCommand;
import ru.aurora.chat.listener.PlayerSessionListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.Listener;
import ru.aurora.chat.listener.ChatListener;
import ru.aurora.chat.service.ChatFormatService;
import ru.aurora.chat.command.ChatAdminCommand;
import ru.aurora.chat.command.SpyCommand;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.PluginManager;
import ru.aurora.chat.command.SoundMenuCommand;
import ru.aurora.chat.command.PrivateMessageCommand;
import ru.aurora.chat.service.ChatRestrictionService;
import ru.aurora.chat.service.MessageColorService;
import ru.aurora.chat.service.SpyPreferenceService;
import ru.aurora.chat.service.SoundPreferenceService;
import ru.aurora.chat.service.PrefixService;
import ru.aurora.chat.service.MessageService;
import ru.aurora.chat.service.TextFormatService;
import ru.aurora.chat.service.PlaceholderService;
import ru.aurora.chat.service.ChatStyleService;
import ru.aurora.chat.placeholder.ChatStylePlaceholderExpansion;
import org.bukkit.plugin.java.JavaPlugin;

public final class AuroraChatPlugin extends JavaPlugin
{
    private ChatStylePlaceholderExpansion chatStyleExpansion;
    private ChatStyleService chatStyleService;

    public void onEnable() {
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        this.getConfig().set("CHAT_STYLES", (Object)null);
        this.saveConfig();
        final PlaceholderService placeholders = new PlaceholderService(this);
        final TextFormatService textFormats = new TextFormatService(placeholders);
        final MessageService messages = new MessageService(this, placeholders);
        final PrefixService prefixes = new PrefixService(this);
        prefixes.initialize();
        final SoundPreferenceService soundPreferences = new SoundPreferenceService(this);
        final SpyPreferenceService spyPreferences = new SpyPreferenceService(this);
        final MessageColorService messageColors = new MessageColorService(this);
        final ChatRestrictionService restrictions = new ChatRestrictionService(this);
        this.chatStyleService = new ChatStyleService(this, messages);
        final PrivateMessageCommand privateMessages = new PrivateMessageCommand(messages, soundPreferences, spyPreferences);
        final SoundMenuCommand soundMenu = new SoundMenuCommand(messages, soundPreferences);
        this.registerCommands(messages, prefixes, this.chatStyleService, restrictions, messageColors, privateMessages, soundMenu);
        this.registerListeners(messages, prefixes, this.chatStyleService, restrictions, messageColors, privateMessages, soundMenu);
        this.registerPlaceholderExpansion(this.chatStyleService, messageColors, textFormats);
    }

    public void onDisable() {
        if (this.chatStyleService != null) {
            this.chatStyleService.saveNow();
        }
        if (this.chatStyleExpansion != null && this.chatStyleExpansion.isRegistered()) {
            this.chatStyleExpansion.unregister();
        }
    }

    private void registerPlaceholderExpansion(final ChatStyleService chatStyles, final MessageColorService messageColors, final TextFormatService textFormats) {
        final PluginManager pluginManager = this.getServer().getPluginManager();
        if (!pluginManager.isPluginEnabled("PlaceholderAPI")) {
            return;
        }
        this.chatStyleExpansion = new ChatStylePlaceholderExpansion(this, chatStyles, messageColors, textFormats);
        if (!this.chatStyleExpansion.register()) {
            this.getLogger().warning("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u043f\u043b\u0435\u0439\u0441\u0445\u043e\u043b\u0434\u0435\u0440\u044b AuroraChat \u0432 PlaceholderAPI.");
        }
    }

    private void registerCommands(final MessageService messages, final PrefixService prefixes, final ChatStyleService chatStyles, final ChatRestrictionService restrictions, final MessageColorService messageColors, final PrivateMessageCommand privateMessages, final SoundMenuCommand soundMenu) {
        this.requireCommand("pm").setExecutor((CommandExecutor)privateMessages);
        this.requireCommand("spy").setExecutor((CommandExecutor)new SpyCommand(messages, privateMessages));
        this.requireCommand("pmsound").setExecutor((CommandExecutor)soundMenu);
        this.requireCommand("aurorachat").setExecutor((CommandExecutor)new ChatAdminCommand(messages, prefixes, chatStyles, restrictions, messageColors));
    }

    private void registerListeners(final MessageService messages, final PrefixService prefixes, final ChatStyleService chatStyles, final ChatRestrictionService restrictions, final MessageColorService messageColors, final PrivateMessageCommand privateMessages, final SoundMenuCommand soundMenu) {
        final PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents((Listener)new ChatListener(this, messages, new ChatFormatService(messages, prefixes, messageColors), chatStyles, restrictions, privateMessages), (Plugin)this);
        pluginManager.registerEvents((Listener)new PlayerSessionListener(chatStyles), (Plugin)this);
        pluginManager.registerEvents((Listener)soundMenu, (Plugin)this);
    }

    private PluginCommand requireCommand(final String name) {
        return Objects.requireNonNull(this.getCommand(name), "\u041a\u043e\u043c\u0430\u043d\u0434\u0430 " + name + " \u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442 \u0432 plugin.yml");
    }
}
