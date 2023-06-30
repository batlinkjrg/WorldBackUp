/* worldBackUpCopy.java
 * 
 * Purpose: Back up a specified world, leaving a current copy and the last backup
 * 
 */
package org.batlinkjrg.worldbackup;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class worldBackUpCopy extends Thread {

        private World world;
        private String worldPath;
        private String worldBackUpPath;
        private String backupPath;

        public worldBackUpCopy (World currentWorld) {
                this.world = currentWorld;
        }

        @Override
        public void run() {
                try {
                        getWorldPath(this.world);
                        getBackUpPath();
                        moveAndCopy();
                        backupworldCommand.printMessage("Done backing up world: " + this.world.getName() + "!");
                } catch (Exception e) {
                        System.out.println("Failed to process: " + this.world.getName());
                        e.printStackTrace();
                }
        }

        private void deleteDirectory(File file) throws IOException {
                if(isDirEmpty(file.toPath())) {
                        return;
                }

                for (File subfile : file.listFiles()) {
                        if (subfile.isDirectory()) {
                                deleteDirectory(subfile);
                        }
                        subfile.delete();
                }
        }

        private static boolean isDirEmpty(final Path directory) throws IOException {
                try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
                return !dirStream.iterator().hasNext();
                }
        }

        private void moveAndCopy() {
                File current = new File(this.worldPath);
                File backupPath = new File(this.backupPath);
                File worldbackup = new File(this.worldBackUpPath);
                File oldBackUp = new File(this.worldBackUpPath + "-Old");
                
                try {
                        createFolder(worldbackup.toString()); // Ensure this folder exists
                        deleteDirectory(oldBackUp);
                        Files.move(worldbackup.toPath(), oldBackUp.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                        backupworldCommand.printMessage("Re-creating Directory -> " + oldBackUp.toString());
                        createFolder(oldBackUp.toString()); // Ensure this folder exists
                }

                try {
                        createFolder(worldbackup.toString()); // Ensure this folder exists
                        deleteDirectory(worldbackup);
                        FileUtils.copyDirectoryToDirectory(current, backupPath);   
                } catch (IOException e) {
                        // No need to take action here
                        // Two unnessary lock files cause this exception ;)
                }
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
                this.backupPath = backupPath;
                

                String worldBackupPath = backupPath + File.separator + this.world.getName();
                createFolder(worldBackupPath);
                this.worldBackUpPath = worldBackupPath;

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
                String path = WorldBackUp.WORLD_CONTAINER;

                if(path.endsWith(File.separator + ".")) {
                      path = path.substring(0, WorldBackUp.WORLD_CONTAINER.length() - 2);  
                }
                
                path = path + File.separator + world.getName();
                this.worldPath = path;
        }
}
