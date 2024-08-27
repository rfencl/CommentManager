import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class FileUtils {
        public static List<Path> getAllSubdirectories(Path rootDir) throws IOException {
            try (var stream = Files.walk(rootDir)) {
                return stream
                        .filter(Files::isDirectory) // Filter to include only directories
                        .filter(path -> !path.equals(rootDir)) // Exclude the root directory itself
                        .collect(Collectors.toList());
            }
        }

    public static List<Path> getAllJavaFiles(Path rootDir) throws IOException {
         return getFiles(rootDir, "java");
    }

    public static List<Path> getAllMdFiles(Path rootDir) throws IOException {
            return getFiles(rootDir, "md");
    }

    public static List<Path> getFiles(Path rootDir, String extension) throws IOException {
        try (var stream = Files.walk(rootDir)) {
            return stream
                    .filter(Files::isRegularFile) // Filter to include only regular files
                    .filter(path -> path.toString().endsWith(extension)) // Filter for .java files
                    .collect(Collectors.toList());
        }
    }

    public static List<String> readFileToList(final Path filePath) {
        List<String> ret = new ArrayList<>();
        try (Stream<String> stringStream = Files.lines(filePath)) {
            ret = stringStream.collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Exception reading {}", filePath.toString(), e);
        }
        return ret;
    }

}
