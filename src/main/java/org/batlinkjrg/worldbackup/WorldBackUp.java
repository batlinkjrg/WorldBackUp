package org.batlinkjrg.worldbackup;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Timer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public final class WorldBackUp extends JavaPlugin {
    public static String WORLD_CONTAINER;
    private BukkitTask timer;

    FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        // Plugin startup logic
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        WORLD_CONTAINER = getServer().getWorldContainer().getAbsolutePath();
        getCommand("backup").setExecutor(new backupworldCommand());
        getCommand("backup").setTabCompleter(new backupworldTabComplete());
        prepareAutoBackUp();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("Bye ;)");
    }

    private void prepareAutoBackUp() {
        Boolean autoBackUp = getConfig().getBoolean("AutoBackUp");
        int selectedHour = getConfig().getInt("TimeOfDay24");
        int currentHour = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).getHour();
        int delayMin = calcTimeDifMin(currentHour, selectedHour);
        long delayTicks = (long) delayMin*1200; // 1200 ticks per min 

        if(autoBackUp) {
            System.out.println("Auto Back Up - Enabled");
            autoBackup backup = new autoBackup();
            backup.runTaskTimer(this, delayTicks, 1728000); // 1728000 ticks per day
        } else {
            System.out.println("Auto Back Up - Disabled");
        }
    }

    private int calcTimeDifMin(int current, int selected) {
        int finalMin = 0;

        if(current < selected) {
            finalMin = selected-current;
        } else if(current > selected) {
            selected = selected + 24;
            finalMin = selected-current;
        }

        finalMin = finalMin * 60; // convert hours to minutes
        return finalMin;
    }
}