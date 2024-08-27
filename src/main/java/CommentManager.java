import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CommentManager {

    public static void createDocDirectoryStructure(Path src) throws IOException {
        List<Path> subdirectories_src = FileUtils.getAllSubdirectories(src);
        subdirectories_src.forEach(CommentManager::createFolder);
    }

    private static void createFolder(Path path) {
        String replace = path.toAbsolutePath().toString().replace("src", "docs");
        File file = Paths.get(replace).toFile();
        boolean mkdirs = file.mkdirs();
    }

    public static void createMarkDowns(Path src) throws IOException {
        List<Path> allJavaFiles_src = FileUtils.getAllJavaFiles(src);
        allJavaFiles_src.forEach(CommentManager::createMdFiles);
    }

    private static void createMdFiles(Path e) {
        String replace = e.toAbsolutePath().toString().replace("src", "docs").replace(".java", ".md");
        File file = Paths.get(replace).toFile();
        try {
            boolean newFile = file.createNewFile();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
