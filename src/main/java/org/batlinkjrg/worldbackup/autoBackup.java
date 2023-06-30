package org.batlinkjrg.worldbackup;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class autoBackup extends BukkitRunnable {

        @Override
        public synchronized void run() {
                System.out.println("Auto Back Up Started...");
                World[] loadedWorlds = new World[Bukkit.getServer().getWorlds().size()];
                Bukkit.getServer().getWorlds().toArray(loadedWorlds);

                try {
                        backupworldCommand.allWorlds(loadedWorlds);
                } catch (InterruptedException e) {
                        System.err.println("Failed to process worlds");
                }
        }
}