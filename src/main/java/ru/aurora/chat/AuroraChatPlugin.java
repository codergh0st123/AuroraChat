package ru.aurora.chat;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.aurora.chat.command.ChatAdminCommand;
import ru.aurora.chat.command.PrivateMessageCommand;
import ru.aurora.chat.command.SoundMenuCommand;
import ru.aurora.chat.command.SpyCommand;
import ru.aurora.chat.listener.ChatListener;
import ru.aurora.chat.listener.PlayerLifecycleListener;
import ru.aurora.chat.service.ChatFormatService;
import ru.aurora.chat.service.MessageService;
import ru.aurora.chat.service.PlaceholderService;
import ru.aurora.chat.service.PrefixService;
import ru.aurora.chat.service.SoundPreferenceService;

import java.util.Objects;

public final class AuroraChatPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        PlaceholderService placeholders = new PlaceholderService(this);
        MessageService messages = new MessageService(this, placeholders);
        PrefixService prefixes = new PrefixService(this);
        prefixes.initialize();

        SoundPreferenceService soundPreferences = new SoundPreferenceService(this);
        PrivateMessageCommand privateMessages = new PrivateMessageCommand(messages, soundPreferences);
        SoundMenuCommand soundMenu = new SoundMenuCommand(messages, soundPreferences);

        registerCommands(messages, prefixes, privateMessages, soundMenu);
        registerListeners(messages, prefixes, privateMessages, soundMenu);
    }

    private void registerCommands(
            MessageService messages,
            PrefixService prefixes,
            PrivateMessageCommand privateMessages,
            SoundMenuCommand soundMenu
    ) {
        requireCommand("pm").setExecutor(privateMessages);
        requireCommand("spy").setExecutor(new SpyCommand(messages, privateMessages));
        requireCommand("pmsound").setExecutor(soundMenu);
        requireCommand("aurorachat").setExecutor(new ChatAdminCommand(messages, prefixes));
    }

    private void registerListeners(
            MessageService messages,
            PrefixService prefixes,
            PrivateMessageCommand privateMessages,
            SoundMenuCommand soundMenu
    ) {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new ChatListener(this, messages, new ChatFormatService(messages, prefixes)), this);
        pluginManager.registerEvents(new PlayerLifecycleListener(privateMessages), this);
        pluginManager.registerEvents(soundMenu, this);
    }

    private PluginCommand requireCommand(String name) {
        return Objects.requireNonNull(getCommand(name), "Команда " + name + " отсутствует в plugin.yml");
    }
}
