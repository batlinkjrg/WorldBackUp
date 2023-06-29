package org.batlinkjrg.worldbackup;

import org.bukkit.plugin.java.JavaPlugin;

public final class WorldBackUp extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getCommand("backup").setExecutor(new backupworldCommand());
        getCommand("backup").setTabCompleter(new backupworldTabComplete());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
