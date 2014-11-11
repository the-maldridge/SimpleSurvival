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
            Path source = Paths.get(this.plugin.getDataFolder().getCanonicalPath(), template);
            Files.walkFileTree(source, new CopyFileVisitor(source, Paths.get(Bukkit.getWorldContainer().getCanonicalPath(), newName)));
        } catch(IOException e) {
            this.plugin.getLogger().severe("Could not load world " + template);
            this.plugin.getLogger().severe(e.toString());
        }
    }


    /* private class CopyFileVisitor extends SimpleFileVisitor<Path> {
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
             System.out.println(file.toString());
             Files.copy(file, targetPath.resolve(sourcePath.relativize(file)));
             return FileVisitResult.CONTINUE;
         }
     }*/
    private class CopyFileVisitor extends SimpleFileVisitor<Path> {
        private final Path target;
        private final Path source;

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
