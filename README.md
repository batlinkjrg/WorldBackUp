# WorldBackUp

First Plugin For Spigot ;)

Very simple backup plugin adding commands to back up all worlds or specific worlds, as well as an auto back up feature.

Will back up into WorldBackUp folder inside of the Spigot plugins directory.
The plugin will keep the two most recent backups just to be safe ;)

Command Usage:
  /[bw, buw, backup, backupworld] <worldname>

config.yml
  Set whether or not you want auto back up (Defualt: Enabled)
    AutoBackUp: true 
  Ser what time of day you would like an auto back up to take place in 24 hour time (Defualt: 2AM)
    TimeOfDay24: 2
