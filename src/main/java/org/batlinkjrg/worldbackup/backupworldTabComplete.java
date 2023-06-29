/* backupworldTabComplete.java
 * 
 * Purpose: Show a list of worlds as the tab complete for first command argument
 * 
 */

package org.batlinkjrg.worldbackup;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class backupworldTabComplete implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
                
                if (args.length == 1) {
                        List<String> worldList = new ArrayList<>();
                        World[] loadedWorlds = new World[Bukkit.getServer().getWorlds().size()];
                        Bukkit.getServer().getWorlds().toArray(loadedWorlds);

                        for(int i = 0; i < loadedWorlds.length; i++) {
                                worldList.add(loadedWorlds[i].getName());
                        }

                        return worldList;
                } 

                return null;
        }

}
