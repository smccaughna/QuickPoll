package com.ibakedpotato.quickpoll;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class CommandPoll implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String arg, @NotNull String[] args) {
        if (sender instanceof Player player) {
            GuiPoll pollGui;
            JavaPlugin.getPlugin(QuickPoll.class).getServer().getPluginManager().registerEvents(pollGui = new GuiPoll(), JavaPlugin.getPlugin(QuickPoll.class));
            player.openInventory(pollGui.getInventory());
            return true;
        }

        return false;
    }
}
