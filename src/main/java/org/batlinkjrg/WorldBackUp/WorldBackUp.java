package org.batlinkjrg.WorldBackUp;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class WorldBackUp extends JavaPlugin {

    private BackupHandler backupHandler;
    private CommandHandler commandHandler;

    BukkitTask taskID = null;

    @Override
    public void onEnable() {
        // Prepare Config files
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        // Setup the backup handler
        this.backupHandler = new BackupHandler(this);
        this.commandHandler = new CommandHandler(this.backupHandler);

        // Setup commands
        getCommand("backup").setExecutor(this.commandHandler);
        getCommand("backup").setTabCompleter(this.commandHandler.getTabCompleter());

        // Start auto backup
        this.taskID = this.backupHandler.runTaskAsynchronously(this);
    }

    @Override
    public void onDisable() {
        // Shutdown the running tab
        this.backupHandler.endThread();
        System.out.println("Bye ;)");
    }

    public static void printMessage(String message, CommandSender sender) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            p.sendMessage("[WorldBackup]: " + ChatColor.GREEN + message);
        } else {
            System.out.println("[WorldBackup]: " + message);
        }
    }
}
