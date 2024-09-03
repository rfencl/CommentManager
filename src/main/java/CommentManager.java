import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Log4j2
public class CommentManager {

    static Map<String, List<String>> combined = new LinkedHashMap<>();
    static Map<Integer, String> classesAndMethods = new LinkedHashMap<>();
    static Map<Integer, List<String>> comments = new LinkedHashMap<>();
    static List<String> lines;

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
        combined.keySet().forEach(methodSignature -> {
            if (methodSignature.contains("class")) {
                addCommentsToMarkDown(combined, methodSignature, markdownLines);
                markdownLines.add("## _Methods_");
            } else {
                markdownLines.add(boldMethodName(methodSignature, StringUtils.getMethodName(methodSignature.trim())));
                addCommentsToMarkDown(combined, methodSignature, markdownLines);
                markdownLines.add("---");
            }
        });
        FileUtils.writeFile(outputFile, markdownLines);
    }

    private static String boldMethodName(String s, String methodName) {
        return s.trim().replace(methodName, "**" + methodName + "**");
    }

    private static void addCommentsToMarkDown(Map<String, List<String>> combined, String s, List<String> markdownLines) {
        combined.get(s).forEach(e ->
                markdownLines.add("\n\t" + e));
    }

    /**
     * Restore the javadoc comments to the trimmed file.
     *
     * @param trimmedJavaFile
     * @param markdown
     * @param javaOutputFile
     */
    public static void writeRestoredFile(Path trimmedJavaFile, Path markdown, Path javaOutputFile) {
        List<String> javaLines = FileUtils.readFile(trimmedJavaFile);
        List<String> markdownLines = FileUtils.readFile(markdown);
        Map<Integer, String> declarations = Parser.parseClassAndMethods(javaLines);
        List<Integer> reversed = declarations.keySet().stream().toList().reversed();
        reversed.forEach(lineNumberKey -> {
            String methodDeclaration = declarations.get(lineNumberKey);
            String method = StringUtils.getMethodName(methodDeclaration);
            int methodDeclarationIndex = 0;
            if (!"No method name found.".equals(method)) {
                String find = methodDeclaration.replace(method, "**" + method + "**").trim();
                methodDeclarationIndex = markdownLines.indexOf(find);
            }
            List<String> comment = getComment(methodDeclarationIndex, markdownLines);
            if (!comment.isEmpty()) {
                int lineIndex = lineNumberKey-1;
                while(!javaLines.get(lineIndex-1).trim().isEmpty()) { lineIndex--; }
                javaLines.addAll(lineIndex, comment);
            }

        });
        FileUtils.writeFile(javaOutputFile, javaLines);
    }

    /**
     * Returns the JavaDoc comments for the method at the given index.
     * @param methodDeclarationIndex - index of the method declaration
     * @param markdownLines - list of markdown lines
     * @return - List<String> - JavaDoc comments
     */
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

    /**
     * Parse the comments and declarations into maps keyed by line number.
     * @param inputJavaFile
     * @throws IOException
     */
    static void parseFile(String inputJavaFile) throws IOException {
        lines = FileUtils.readFile(FileUtils.getFileFromResources(Path.of(inputJavaFile)));
        classesAndMethods = Parser.parseClassAndMethods(lines);
        comments = Parser.parseJavaDocs(lines);
    }

    static void combineCommentsAndDeclarations() {
        log.debug("function keys {}", classesAndMethods.keySet());
        log.debug("comment keys {}", comments.keySet());
        combined = new LinkedHashMap<>();
        Queue<Integer> commentQueue = new LinkedList<>(comments.keySet());
        Queue<Integer> methodQueue = new LinkedList<>(classesAndMethods.keySet());
        List<Integer> methodsWithoutComments = new ArrayList<>();
        while(!methodQueue.isEmpty()) {
            int fi = methodQueue.poll();
            int startCount = commentQueue.size();
            for (Integer li : commentQueue) {
                if (li < fi) {
                    combined.put(classesAndMethods.get(fi), comments.get(li));
                    commentQueue.remove();
                    break;
                }
            }
            if (commentQueue.size() == startCount) {
                methodsWithoutComments.add(fi);
            }
        }

        addDefaultMessageToMethodsWithoutComments(methodsWithoutComments);
    }

    /**
     * Build a new map by inserting methods without a comment back into their correct place
     * with a default message.
     * @param methodsWithoutComments
     */
    static void addDefaultMessageToMethodsWithoutComments(List<Integer> methodsWithoutComments) {
        Map<String, List<String>> newMap = new LinkedHashMap<>();
        Queue<String> methods = new LinkedList<>(combined.keySet());
        while (!methodsWithoutComments.isEmpty()) {
            int mwcIndex = methodsWithoutComments.getFirst();
            int currentIndexOfCombined = getIndexOfMethodWithComment(methods.peek(), classesAndMethods);
            if (isInsertMethodWithDefaultJavaDoc(currentIndexOfCombined, mwcIndex)) {
                newMap.put(classesAndMethods.get(mwcIndex), List.of("\tNo JavaDoc Comments Found."));
                methodsWithoutComments.removeFirst();
            } else {
                String methodName = methods.poll();
                newMap.put(methodName, combined.get(methodName));
            }
        }
        if (!newMap.isEmpty()) {
            combined = newMap;
        }
    }

    private static boolean isInsertMethodWithDefaultJavaDoc(int currentIndexOfCombined, int mwcIndex) {
        return currentIndexOfCombined == -1 || mwcIndex < currentIndexOfCombined;
    }

    static int getIndexOfMethodWithComment(String method, Map<Integer, String> classesAndMethods) {
        Integer keyByValue = MapUtils.findKeyByValue(classesAndMethods, method);
        return null == keyByValue ? -1 : keyByValue ;
    }
}
