/* backupworldCommand.java
 * 
 * Purpose: Facilitate command when it is requested
 * 
 */

package org.batlinkjrg.worldbackup;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class backupworldCommand implements CommandExecutor {
        private String worldName;
        private static CommandSender sender;

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                // Initialize all worlds first
                World[] loadedWorlds = new World[Bukkit.getServer().getWorlds().size()];
                Bukkit.getServer().getWorlds().toArray(loadedWorlds);
                this.sender = sender;

                if(args.length == 1) {
                        String worldPicked = args[0];
                        System.out.println("Chosen World -> " + worldPicked);

                        World selectedWorld = Bukkit.getWorld(worldPicked);
                        printMessage("Processing: " + selectedWorld.getName() + "...");

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

                        printMessage("Select only one world");;
                }        

                return true;
        }

        // If there is no specified world, back them all up
        public static synchronized void allWorlds(World[] loadedWorlds) throws InterruptedException {
                int sorterThreads = Runtime.getRuntime().availableProcessors()/2;
                ExecutorService threadPool = Executors.newFixedThreadPool(sorterThreads);

                for(World currentWorld : loadedWorlds) {
                        worldBackUpCopy worldProcess = new worldBackUpCopy(currentWorld);
                        currentWorld.save();
                        threadPool.submit(worldProcess);
                }

                threadPool.shutdown();

                do {
                printMessage("Processing...");
                TimeUnit.MILLISECONDS.sleep(750);
                } while(!threadPool.isTerminated());

                printMessage("Done backing up all worlds!");
        }

        // Back up the specified world
        private void specificWorld(World world) throws InterruptedException {
                worldBackUpCopy worldProcess = new worldBackUpCopy(world);
                world.save();
                worldProcess.start();
                worldProcess.join();
        }

        public static void printMessage(String message) {
                if(sender instanceof Player) {
                        Player p = (Player) sender;
                        p.sendMessage(ChatColor.GREEN + message);
                } else {
                        System.out.println(message);
                }
        }
    
}
