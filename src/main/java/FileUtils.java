import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public static List<String> readFile(final Path filePath) {
        List<String> ret = new ArrayList<>();
        try (Stream<String> stringStream = Files.lines(filePath)) {
            ret = stringStream.collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Exception reading {}", filePath.toString(), e);
        }
        return ret;
    }

    public static void writeFile(final Path filename, List<String> list) {
        try (FileWriter writer = new FileWriter(filename.toString())) {
            for (String str : list) {
                writer.write(str + System.lineSeparator());
            }
        } catch (IOException e) {
            log.error("Exception writing {}", filename, e);
        }
    }

    public static void createDocDirectoryStructure(Path src) throws IOException {
        List<Path> subdirectories_src = getAllSubdirectories(src);
        subdirectories_src.forEach(FileUtils::createFolder);
    }

    private static void createFolder(Path path) {
        String replace = path.toAbsolutePath().toString().replace("src", "docs");
        File file = Paths.get(replace).toFile();
        //noinspection ResultOfMethodCallIgnored
        file.mkdirs();
    }

    public static void createMarkDowns(Path src) throws IOException {
        List<Path> allJavaFiles_src = getAllJavaFiles(src);
        allJavaFiles_src.forEach(FileUtils::createMdFiles);
    }

    private static void createMdFiles(Path e) {
        String replace = e.toAbsolutePath().toString().replace("src", "docs").replace(".java", ".md");
        File file = Paths.get(replace).toFile();
        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
