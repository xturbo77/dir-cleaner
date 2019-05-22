package de.schmidt.dir.cleaner;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 *
 * @author schmidt
 */
public class DirCleaner implements FileVisitor<Path> {

    public static void main(String[] args) {
        if (args.length == 2) {
            String argPath = args[0];
            String argDays = args[1];
            try {
                int days = Integer.parseInt(argDays);
                Path startingDir = Paths.get(argPath);
                DirCleaner dirCleaner = new DirCleaner(days);
                try {
                    Files.walkFileTree(startingDir, dirCleaner);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } catch (NumberFormatException ex) {
                System.err.format("Invalid argument for days: %s%n", argDays);
            }
        } else {
            System.err.println("Missing argument(s). First argument must be a path, second argument a number (delete files older than x days)");
            System.err.println("Example");
            System.err.println("DirCleaner.exe c:\temp 30");
        }
    }

    private final Date cutoffDate;

    DirCleaner(int days) {
        LocalDate today = LocalDate.now();
        cutoffDate = Date.from(today.minusDays(days).atStartOfDay(ZoneId.systemDefault()).toInstant());
        System.out.println("CutOff date: " + this.cutoffDate);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        System.out.format("Start directory: %s%n", dir);
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Date modifiedDate = new Date(Files.getLastModifiedTime(file).toMillis());
        if (modifiedDate.before(cutoffDate)) {
            try {
                boolean deleted = file.toFile().delete();
                System.out.format("Deleted file: %s success:%s%n", file, deleted);
            } catch (Exception ex) {
                System.err.format("Could not delete file: %s%n", file);
                ex.printStackTrace();
            }
        } else {
            System.out.format("Skipping file: %s%n", file);
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        System.err.println(exc);
        return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        System.out.format("Finished directory: %s%n", dir);
        return CONTINUE;
    }

}
