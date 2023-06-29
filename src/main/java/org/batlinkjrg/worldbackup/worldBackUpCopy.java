/* worldBackUpCopy.java
 * 
 * Purpose: Back up a specified world, leaving a current copy and the last backup
 * 
 */
package org.batlinkjrg.worldbackup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.bukkit.World;

public class worldBackUpCopy extends Thread {

        private World world;
        private String worldPath;
        private String backUpPath;

        public worldBackUpCopy (World currentWorld) {
                this.world = currentWorld;
        }

        @Override
        public void run() {
                try {
                        getWorldPath(world);
                        getBackUpPath();
                        moveAndCopy();
                } catch (Exception e) {
                        System.out.println("Failed to process: " + this.world.getName());
                }

                System.out.println("Done backing up world: " + this.world.getName() + "!");
        }

        private void deleteDirectory(File file) {
                for (File subfile : file.listFiles()) {
                        if (subfile.isDirectory()) {
                                deleteDirectory(subfile);
                        }
                subfile.delete();
                }
        }

        private void moveAndCopy() throws IOException {
                File current = new File(this.worldPath);
                File backup = new File(this.backUpPath);
                File oldBackUp = new File(this.backUpPath + "-Old");

                deleteDirectory(oldBackUp);
                FileUtils.moveDirectory(backup, oldBackUp);

                deleteDirectory(backup);
                FileUtils.copyDirectory(current, backup);
        }

        private void createFolder(String folderPath) {
                if (Files.notExists(Paths.get(folderPath))) {
                        File folder = new File(folderPath);
                        folder.mkdir();
                }
        }

        private void checkBackUpFolder(String currentPath) {
                String backupPath = currentPath + File.separator + "WorldBackUp";
                createFolder(backupPath);

                String worldBackupPath = backupPath + File.separator + this.world.getName();
                createFolder(worldBackupPath);
                this.backUpPath = worldBackupPath;

                String worldOldBackUpPath = worldBackupPath + "-Old";
                createFolder(worldOldBackUpPath);
                }

        private void getBackUpPath() {
                Path currentRelativePath = Paths.get("");
                String path = currentRelativePath.toAbsolutePath().toString();
                path = path + File.separator + "plugins";
                checkBackUpFolder(path);
        }

        private void getWorldPath(World world) {
                Path currentRelativePath = Paths.get("");
                String path = currentRelativePath.toAbsolutePath().toString();
                path = path + File.separator + world.getName();
                this.worldPath = path;
        }
}
