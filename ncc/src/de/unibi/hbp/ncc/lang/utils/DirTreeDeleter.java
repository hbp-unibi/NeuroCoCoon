package de.unibi.hbp.ncc.lang.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class DirTreeDeleter {

   public static void deleteRecursively (Path rootDir)
         throws IOException {
      Files.walkFileTree(
            rootDir,
            new FileVisitor<Path>() {
               @Override
               public FileVisitResult preVisitDirectory (Path dir, BasicFileAttributes attrs) throws IOException {
                  return FileVisitResult.CONTINUE;
               }

               @Override
               public FileVisitResult visitFile (Path file, BasicFileAttributes attrs) throws IOException {
                  Files.delete(file);
                  return FileVisitResult.CONTINUE;
               }

               @Override
               public FileVisitResult visitFileFailed (Path file, IOException exc) throws IOException {
                  throw exc;  // could also try to continue so that further files might be deleted
               }

               @Override
               public FileVisitResult postVisitDirectory (Path dir, IOException exc) throws IOException {
                  if (exc == null) {
                     Files.delete(dir);
                     return FileVisitResult.CONTINUE;
                  }
                  else
                     throw exc;  // directory iteration failed
               }
            });

   }
}
