package net.SimpleSurvival;


import org.bukkit.Bukkit;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by maldridge on 11/8/14.
 */
public class WorldManager {
    private SimpleSurvival plugin;

    public WorldManager(SimpleSurvival plugin) {
        this.plugin = plugin;
    }

    public void newWorldFromTemplate(String template, String newName) {
        boolean errors = false;
        try {
            //TODO figure out why this doesn't actually copy the world
            // NOTE: it has an IOException after the player is registered into a game and the SimpleSurvival instance attempts to copy the world template over into the worlds directory
            System.out.println(this.plugin.getDataFolder().getCanonicalPath() + ":" + template);
            Path source = Paths.get(this.plugin.getDataFolder().getCanonicalPath(), template);
            Files.walkFileTree(source, new CopyFileVisitor(source, Paths.get(Bukkit.getWorldContainer().getCanonicalPath(), newName)));
        } catch (IOException e) {
            this.plugin.getLogger().severe("Could not load world " + template);
            this.plugin.getLogger().severe(e.toString());
        }
    }

    public void destroyWorld(String worldToDestroy) {
        //unload the world without saving, we are about to delete it anyway
        if(this.plugin.getServer().unloadWorld(worldToDestroy,false)) {
            this.plugin.getLogger().info("successfully unloaded world");
        } else {
            this.plugin.getLogger().severe("could not unload world");
            this.plugin.getLogger().severe("the following players weren't removed from the world: " + Bukkit.getWorld(worldToDestroy).getEntities().toString());
        }
        try {
            Path toDelete = Paths.get(this.plugin.getServer().getWorldContainer().getCanonicalPath(), worldToDestroy);
            Files.walkFileTree(toDelete, new DeletingFileVisitor(toDelete));
        } catch(IOException e) {
            this.plugin.getLogger().severe("Could not destroy world " + worldToDestroy);
            this.plugin.getLogger().severe(e.toString());
        }
    }


    private class DeletingFileVisitor extends SimpleFileVisitor<Path>{
        private Path toDelete;

        public DeletingFileVisitor(Path toDelete) {
            this.toDelete = toDelete;
        }
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
            if(attributes.isRegularFile()){
                System.out.println("Deleting Regular File: " + file.getFileName());
                Files.delete(file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path directory, IOException ioe) throws IOException {
            System.out.println("Deleting Directory: " + directory.getFileName());
            Files.delete(directory);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException ioe) throws IOException {
            System.out.println("Something went wrong while working on : " + file.getFileName());
            ioe.printStackTrace();
            return FileVisitResult.CONTINUE;
        }
    }


    private class CopyFileVisitor extends SimpleFileVisitor<Path> {
        private Path target;
        private Path source;

        public CopyFileVisitor(Path source, Path target) {
            this.source = source;
            this.target = target;
        }


        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetdir = target.resolve(source.relativize(dir));
            try {
                Files.copy(dir, targetdir);
            } catch (FileAlreadyExistsException e) {
                if (!Files.isDirectory(targetdir))
                    throw e;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            System.out.println(file.toString());
            if(!file.toString().endsWith("/uid.dat")) {
                Files.copy(file, target.resolve(source.relativize(file)));
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
