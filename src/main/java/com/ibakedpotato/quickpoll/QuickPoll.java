package com.ibakedpotato.quickpoll;

import org.bukkit.plugin.java.JavaPlugin;

public final class QuickPoll extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getCommand("poll").setExecutor(new CommandPoll());
        this.getCommand("vote").setExecutor(new CommandVote());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
