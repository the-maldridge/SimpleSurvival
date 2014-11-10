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
            System.out.println(this.plugin.getDataFolder().getCanonicalPath()+ ":"+ template);
            Files.walkFileTree(Paths.get(this.plugin.getDataFolder().getCanonicalPath(), template), new CopyFileVisitor(Paths.get(Bukkit.getWorldContainer().getCanonicalPath(), newName)));
        } catch(IOException e) {
            this.plugin.getLogger().severe("Could not load world " + template);
        }
    }


    private class CopyFileVisitor extends SimpleFileVisitor<Path> {
        private final Path targetPath;
        private Path sourcePath = null;
        public CopyFileVisitor(Path targetPath) {
            this.targetPath = targetPath;
        }

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
            if (sourcePath == null) {
                sourcePath = dir;
            } else {
                Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)));
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.copy(file, targetPath.resolve(sourcePath.relativize(file)));
            return FileVisitResult.CONTINUE;
        }
    }
}
