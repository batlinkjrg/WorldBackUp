package org.batlinkjrg.WorldBackUp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BackupHandler extends BukkitRunnable {

    // This will lock this class if its already in use
    private boolean inUse = false;
    private boolean enableAutoBackup;
    private boolean threadEnd = false;

    // Java plugin class for timer
    JavaPlugin pluginHandle;

    // Backup metadata
    private int backupTime;
    private int backupCount;
    private String backupDir;
    private List<String> exclusions;

    // Local Storage
    private World[] loadedWorlds;
    private BlockingQueue<World> backupQueue = new LinkedBlockingQueue<>(20); 

    // Autobackup variables
    private long currentHour; // Time Variables
    private boolean hasFinished = false;

    public BackupHandler(JavaPlugin plugin) {
        this.pluginHandle = plugin;
        this.enableAutoBackup = plugin.getConfig().getBoolean("AutoBackUp");
        this.backupTime = plugin.getConfig().getInt("TimeOfDay24");
        this.backupCount = plugin.getConfig().getInt("BackupCount");
        this.exclusions = plugin
            .getConfig()
            .getList("Exclusions")
            .stream()
            .map(e -> (String) e)
            .collect(Collectors.toList());

        this.backupDir = plugin.getConfig().getString("BackupPath");

        // Check if the backup dir is valid, if not set it default or fix it
        if(this.backupDir.equals(""))
            this.backupDir = this.pluginHandle.getDataFolder().getAbsolutePath();
        else if (this.backupDir.startsWith("..")) 
            this.backupDir = this.pluginHandle.getDataFolder().getAbsolutePath() + File.separator + this.backupDir;

        if (this.backupCount <= 0) this.backupCount = 1;
        updateWorldList();
    }

    public boolean isInUse() {
        return inUse;
    }

    public synchronized void updateWorldList() {
        this.loadedWorlds = new World[Bukkit.getServer().getWorlds().size()];
        Bukkit.getServer().getWorlds().toArray(this.loadedWorlds);
    }

    public synchronized World[] getWorldList() {
        return this.loadedWorlds;
    }

    // Old moveAndCopy method
    public synchronized boolean archiveWorlds() {
        int success = 0;
        this.updateWorldList();

        // Loop through worlds and skip ones on the exclusion list
        for (World world : this.loadedWorlds) {
            // Check the exclusion list and skip the world if its in the list.
            if (this.exclusions.contains(world.getName())) {
                WorldBackUp.printMessage("Skipping World: " + world.getName(), null);
                success++;
                continue;
            }

            WorldBackUp.printMessage("Adding: " + world.getName() + " to queue", null);
            if (this.backupQueue.add(world)) success++;
        }

        return (success == this.loadedWorlds.length) ? true : false;
    }

    public synchronized boolean archiveWorld(String worldName) {
        World world = getWorld(worldName);
        if (world == null)
            return false;

        // Backup the world selected, and store the results
        WorldBackUp.printMessage("Adding: " + world.getName() + " to queue", null);
        this.backupQueue.add(world);
  
        return true;
    }

    public synchronized void endThread() {
        this.threadEnd = true;
    }


    // This run method will pull from the que of worlds to process and process them one by one. 
    // It will also handle the autobackup.
    @Override
    public void run() {
        while (!this.threadEnd) {
            currentHour = LocalTime.now(ZoneId.systemDefault()).getHour();

            if (this.threadEnd)
                this.cancel();

            if (currentHour != this.backupTime)
                this.hasFinished = false;

            // Add all worlds to the queue
            if (this.enableAutoBackup && !this.hasFinished && (currentHour == this.backupTime)) {
                WorldBackUp.printMessage("Adding Autobackup", null);
                this.archiveWorlds();
                this.hasFinished = true;
            }

            // Backup all worlds in the Queue
            while (!this.backupQueue.isEmpty()) {
                WorldBackUp.printMessage("Processing.., this may take time.", null);
                WorldBackUp.printMessage("Do not stop server!", null);
                this.Backup(this.backupQueue.poll());
            }
        }
    }

    // This function will back up a world and move any old copies.
    private boolean Backup(World world) {
        WorldBackUp.printMessage("Backing up: " + world.getName(), null);

        File src, dest;
        // The for loop goes backwards through the backups
        for (int i = this.backupCount; i > 0; i--) {
            if (i-1 == 0) { // This will check if we are at the most recent back up.
                src = new File(getWorldPath(world));
                dest = new File(getBackupPath(world, i));
                copyLockedDir(src, dest);
            } else { // In all other cases check to see if there is an older back up.
                src = new File(getBackupPath(world, i - 1)); // Newer backup
                dest = new File(getBackupPath(world, i)); // Older backup

                // Check to see if there is a previous back up first.
                if (!src.exists()) continue;
                copyLockedDir(src, dest);
            }
        }

        WorldBackUp.printMessage("Done backing up: " + world.getName(), null);
        return true;
    }

    // Get the path to copy the world backups too.
    private String getBackupPath(World world, int currentCount) {
        String path = this.backupDir;
        new File(path).mkdirs();
        return (path + File.separator + world.getName() + "(" + currentCount + ")");
    }

    // This function is to get the path of the world data on the current server.
    private String getWorldPath(World world) {
        String path = this.pluginHandle.getServer().getWorldContainer().getAbsolutePath();
        return (path + File.separator + world.getName());
    }

    private static boolean copyLockedFile(File src, File dest) {
        try {
            if (!dest.exists()) dest.createNewFile();

            try (
                FileChannel sourceChannel = new FileInputStream(src).getChannel();
                FileChannel destChannel = new FileOutputStream(dest).getChannel();
            ) {
                // Copy data from source to destination
                destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            } catch (IOException e) {
                // Fallback to Apache FileUtils if the basic copy fails
                System.err.println("File is locked or inaccessible, attempting fallback with FileUtils...");
                FileUtils.copyFile(src, dest);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean copyLockedDir(File srcDir, File destDir) {
        try {
            if (!srcDir.isDirectory()) {
                throw new IllegalArgumentException("Source must be a directory!");
            }

            if (destDir.exists()) {
                FileUtils.deleteDirectory(destDir);
                destDir.mkdirs();
            } else destDir.mkdirs();

            for (File file : srcDir.listFiles()) {
                File destinationFile = new File(destDir, file.getName());
                if (file.isDirectory()) {
                    // Recursively copy subdirectory
                    copyLockedDir(file, destinationFile);
                } else {
                    // Copy individual file
                    copyLockedFile(file, destinationFile);
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get a world from the loaded worlds based on string
    // Returns null if there is no world with that name.
    private World getWorld(String worldName) {
        this.updateWorldList();
        for (World world : this.loadedWorlds) 
            if (worldName.equals(world.getName())) return world;

        return null;
    }
}
