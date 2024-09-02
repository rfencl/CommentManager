import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
        trimmedLines.forEach(log::debug);
        return trimmedLines;
    }

    /**
     * Write the current class comments to markdown
     *
     * @param file
     * @param outputFile
     * @param combined
     */
    public static void writeMarkDown(Path file, Path outputFile, Map<String, List<String>> combined) {
        List<String> markdownLines = new ArrayList<>();
        markdownLines.add("# " + file.getFileName().toString().replace(".java", ""));
        combined.keySet().forEach(s -> {
            if (s.contains("class")) {
                addCommentsToMarkDown(combined, s, markdownLines);
                markdownLines.add("## _Methods_");
            } else {
                String methodName = StringUtils.getMethodName(s.trim());
                markdownLines.add(s.trim().replace(methodName, "**" + methodName + "**"));
                addCommentsToMarkDown(combined, s, markdownLines);
                markdownLines.add("---");
            }
        });
        FileUtils.writeFile(outputFile, markdownLines);
    }

    private static void addCommentsToMarkDown(Map<String, List<String>> combined, String s, List<String> markdownLines) {
        combined.get(s).forEach(e ->
                markdownLines.add("\n\t" + e));
    }

    public static void writeRestoredFile(Path trimmedJavaFile, Path markdown, Path javaOutputFile) {
        // Read trimmed java file into a list
        List<String> javaLines = FileUtils.readFile(trimmedJavaFile);
        List<String> markdownLines = FileUtils.readFile(markdown);
        Map<Integer, String> declarations = Parser.parseClassAndMethods(javaLines);
        List<Integer> reversed = declarations.keySet().stream().toList().reversed();
        reversed.forEach(e -> {
            String methodDeclaration = declarations.get(e);
            String method = StringUtils.getMethodName(methodDeclaration);
            int methodDeclarationIndex = 0;
            if (!"No method name found.".equals(method)) {
                String find = methodDeclaration.replace(method, "**" + method + "**").trim();
                methodDeclarationIndex = markdownLines.indexOf(find);
            }
            List<String> comment = getComment(methodDeclarationIndex, markdownLines);
            if (!comment.isEmpty()) {
                int lineIndex = e-1;
                while(!javaLines.get(lineIndex-1).trim().isEmpty()) { lineIndex--; }
                javaLines.addAll(lineIndex, comment);
            }

        });
        FileUtils.writeFile(javaOutputFile, javaLines);
    }

    private static List<String> getComment(int methodDeclarationIndex, List<String> markdownLines) {
        List<String> comment = new ArrayList<>();
        List<String> loopTerminators = List.of("---", "## _Methods_");
        for (int i = methodDeclarationIndex + 1; loopTerminators.stream().noneMatch(markdownLines.get(i)::contains); i++ ) {
            if (markdownLines.get(i).contains("*")) {
                comment.add(markdownLines.get(i).replace("\t", ""));
            }
        }
        return comment;
    }
}
