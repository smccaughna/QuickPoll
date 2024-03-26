package com.ibakedpotato.quickpoll;

import org.bukkit.plugin.java.JavaPlugin;

public final class QuickPoll extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getCommand("poll").setExecutor(new CommandPoll());
        this.getCommand("vote").setExecutor(new CommandVote());
        this.getServer().getPluginManager().registerEvents(new GuiPoll(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
