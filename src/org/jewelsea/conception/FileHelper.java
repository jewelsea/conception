package org.jewelsea.conception;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileHelper {
  private final static Logger log = Logger.getLogger(FileHelper.class.getName());     

  static public void recursivelyDeleteDir(Path dir) {
    if (dir != null) {
      try {  
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
          @Override public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }

          @Override public FileVisitResult postVisitDirectory(Path dir,
                   IOException exc) throws IOException {
            if (exc == null) {
              Files.delete(dir);
              return FileVisitResult.CONTINUE;
            } else {
              throw exc;
            }
          }                    
        });
      } catch (IOException ex) {
        log.log(Level.WARNING, "Unable to cleanup temporary storage used by compilation", ex);                  
      }  
    }
  }
}
