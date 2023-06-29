/* backupworldCommand.java
 * 
 * Purpose: Facilitate command when it is requested
 * 
 */

package org.batlinkjrg.worldbackup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class backupworldCommand implements CommandExecutor {
        private String worldName;

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                // Initialize all worlds first
                World[] loadedWorlds = new World[Bukkit.getServer().getWorlds().size()];
                Bukkit.getServer().getWorlds().toArray(loadedWorlds);

                if(args.length == 1) {

                        for (World currentWorld : loadedWorlds) { // Check if world exists
                                if(args[0] != currentWorld.getName()) {
                                        System.out.println("This is not a world");
                                        return false;
                                }
                        }

                        World selectedWorld = Bukkit.getWorld(args[0]);
                        System.out.println("Processing: " + selectedWorld.getName() + "...");

                        try {
                        specificWorld(selectedWorld); 
                        } catch (Exception e) {
                                System.out.println("Failed to process world");
                        }
                        

                } else if(args.length == 0) {
                        System.out.println("Processing all worlds");

                        try {
                        allWorlds(loadedWorlds);   
                        } catch (Exception e) {
                                System.out.println("Failed to process worlds");
                        }
                        
                } else {
                        tooManyWorldsSelected(sender);
                }        

                return true;
        }

        private void tooManyWorldsSelected(CommandSender sender) {
                if(sender instanceof Player) {
                        Player p = (Player) sender;
                        p.sendMessage(ChatColor.RED + "Only pick one world");
                } else {
                        System.out.println("Select only one world");
                }
    }

        // If there is no specified world, back them all up
        private void allWorlds(World[] loadedWorlds) throws InterruptedException {
                int sorterThreads = Runtime.getRuntime().availableProcessors()/2;
                ExecutorService threadPool = Executors.newFixedThreadPool(sorterThreads);

                for(World currentWorld : loadedWorlds) {
                        worldBackUpCopy worldProcess = new worldBackUpCopy(currentWorld);
                        threadPool.submit(worldProcess);
                }

                threadPool.shutdown();

                do {
                System.out.println("Processing...");
                TimeUnit.MILLISECONDS.sleep(750);
                } while(!threadPool.isTerminated());

                System.out.println("Done backing up all worlds!");
        }

        // Back up the specified world
        private void specificWorld(World world) throws InterruptedException {
                worldBackUpCopy worldProcess = new worldBackUpCopy(world);
                worldProcess.start();
                worldProcess.join();
        }
    
}
