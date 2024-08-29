import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Log4j2
public class CommentManager {

    @SneakyThrows
    public static void main(String [] args) {
        Path src = Paths.get(args[0]);
        FileUtils.createDocDirectoryStructure(src);
        FileUtils.createMarkDowns(src);
        List<Path> allJavaFiles_src = FileUtils.getAllJavaFiles(src);

        Path docs = Paths.get(src.toAbsolutePath().toString().replace("src", "docs"));
        List<Path> allJavaFiles_docs = FileUtils.getAllMdFiles(docs);

        // todo loop through all the files and process them
    }

    /**
     * Remove the java doc comments from the lines of the java source file.
     * @param lines    - lines of java source file
     * @param comments - javadoc comments to remove
     * @return List<String> lines of code without documentation.
     */
    public static List<String> removeJavaDocComments(List<String> lines, Map<Integer, List<String>> comments) {
        List<String> trimmedLines = new LinkedList<>(lines);
        comments.values().forEach(trimmedLines::removeAll);
        trimmedLines.removeAll(Arrays.asList("     *"));
        trimmedLines.forEach(log::info);
        return trimmedLines;
    }
}
