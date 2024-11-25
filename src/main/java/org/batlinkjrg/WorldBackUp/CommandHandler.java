package org.batlinkjrg.WorldBackUp;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class CommandHandler implements CommandExecutor {

    private BackupHandler backupHandler;
    private CommandTabComplete tabComplete;

    public CommandHandler(BackupHandler backupHandler) {
        this.backupHandler = backupHandler;
        this.tabComplete = new CommandTabComplete(backupHandler);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Boolean success = false;

        if (args.length == 1) {
            String worldPicked = args[0];
            System.out.println("Chosen World -> " + worldPicked);

            WorldBackUp.printMessage("Processing: " + worldPicked + "...", sender);

            World worlds[] = this.backupHandler.getWorldList();
            for (World world : worlds) if (world.getName().equals(worldPicked)) success = true;

            if (!success) {
                WorldBackUp.printMessage("invalid world: " + worldPicked, sender);
                WorldBackUp.printMessage("Backup Failed", sender);
                return success;
            }

            success = this.backupHandler.archiveWorld(worldPicked);
        } else if (args.length == 0) {
            WorldBackUp.printMessage("Processing all worlds", sender);
            success = this.backupHandler.archiveWorlds();
        } else {
            WorldBackUp.printMessage("Select only one world", sender);
            success = false;
        }

        if (success) WorldBackUp.printMessage("Backup Successful!", sender);
        else WorldBackUp.printMessage("Backup Failed", sender);

        return success;
    }

    public TabCompleter getTabCompleter() {
        return this.tabComplete;
    }

    private class CommandTabComplete implements TabCompleter {

        private BackupHandler backupHandler;

        public CommandTabComplete(BackupHandler backupHandler) {
            this.backupHandler = backupHandler;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            if (args.length == 1) {
                List<String> worldList = new ArrayList<>();
                World[] loadedWorlds = this.backupHandler.getWorldList();

                for (int i = 0; i < loadedWorlds.length; i++) {
                    worldList.add(loadedWorlds[i].getName());
                }

                return worldList;
            }

            return null;
        }
    }
}
