package de.codingair.tradesystem.spigot.ext;

import de.codingair.tradesystem.spigot.utils.Lang;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Extension {
    private final String name;
    private final String description;
    private final String downloadLink;

    public Extension(@NotNull String name, @Nullable String description, @Nullable String downloadLink) {
        this.name = name;
        this.description = description;
        this.downloadLink = downloadLink;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled(getName());
    }

    @Nullable
    public Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin(name);
    }

    @Nullable
    public String getCurrentVersion() {
        Plugin plugin = getPlugin();
        if (plugin == null) return null;
        return plugin.getDescription().getVersion();
    }

    public void sendDownloadInfo(@NotNull CommandSender sender) {
        TextComponent tc = new TextComponent(TextComponent.fromLegacyText(Lang.getPrefix() + "This function is only available with ", ChatColor.GRAY));

        TextComponent link = new TextComponent(name);
        link.setColor(ChatColor.of(Extensions.COLOR));

        if (downloadLink != null) {
            link.setItalic(true);
            link.setUnderlined(true);
            link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, downloadLink));
        }
        tc.addExtra(link);

        if (description != null) {
            tc.addExtra(new TextComponent(TextComponent.fromLegacyText("§7: " + description, ChatColor.GRAY)));
        } else {
            tc.addExtra(new TextComponent(TextComponent.fromLegacyText("§7.", ChatColor.GRAY)));
        }

        sender.spigot().sendMessage(tc);
    }

    @Nullable
    public String getDownloadLink() {
        return downloadLink;
    }
}
