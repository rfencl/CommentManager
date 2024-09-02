import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
public class CommentManagerTest {
    static Map<Integer, String> classesAndMethods  = new LinkedHashMap<>();
    static Map<Integer, List<String>> comments = new LinkedHashMap<>();
    public static Map<String, List<String>> combined = new LinkedHashMap<>();
    static List<String> lines;
    static Path src;
    static Path docs;

    static Path linuxSrc = Path.of("/home/rick/Documents/powin/localmanager-test/src/");
    static Path linuxDocs = Path.of("/home/rick/Documents/powin/localmanager-test/docs/");

    static Path winsrc = Path.of("F:\\Powin\\localmanager-test\\src\\");
    static Path windocs = Path.of("F:\\Powin\\localmanager-test\\docs\\");
    private static Path currentFile;

    @BeforeAll
    public static void initBeforeClass() throws IOException {
        if (FileUtils.isWindows()) {
            src = winsrc;
            docs = windocs;
        } else {
            src = linuxSrc;
            docs = linuxDocs;
        }
        lines = readFirstFile();
        classesAndMethods = Parser.parseClassAndMethods(lines);
        comments = Parser.parseJavaDocs(lines);
        assertEquals(classesAndMethods.size(), 5);
        assertEquals(comments.size(), 5);
        assertEquals(7, comments.get(13).size());
    }

    @Test
    public void verifyPathsExist() {
        assertTrue(src.toFile().exists());
        assertTrue(docs.toFile().exists());
    }

    @Test
    public void verifySrcStructureMatchesDocs() throws IOException {
        FileUtils.createDocDirectoryStructure(src);
        List<Path> subdirectories_src = FileUtils.getAllSubdirectories(src);
        subdirectories_src.forEach(log::debug);
        List<Path> subdirectories_doc = FileUtils.getAllSubdirectories(docs);
        subdirectories_doc.forEach(log::debug);
        assertEquals(subdirectories_src.size(), subdirectories_doc.size());
    }

    @Test
    public void verifyThatAMarkDownFileExistsForEachJavaFile() throws IOException {
        FileUtils.createMarkDowns(src);
        List<Path> allJavaFiles_src = FileUtils.getAllJavaFiles(src);
        List<Path> allJavaFiles_docs = FileUtils.getAllMdFiles(docs);
        assertEquals(allJavaFiles_src.size(), allJavaFiles_docs.size());
    }


    @Test
    public void combineCommentsWithDeclaration() {
        log.debug("function keys {}", classesAndMethods.keySet());
        log.debug("comment keys {}", comments.keySet());
        combined = new LinkedHashMap<>();
        Queue<Integer> queue = new LinkedList<>(comments.keySet());
        for (Integer fi : classesAndMethods.keySet()) {
            for(Integer li : queue) {
                if (li < fi) {
                    combined.put(classesAndMethods.get(fi), comments.get(li));
                    queue.remove();
                    break;
                }
            }
        }
        assertEquals(5, combined.size());
    }

    @Test
    public void removeCommentsFromFile() {
        List<String> trimmed = CommentManager.removeJavaDocComments(lines, comments);
        assertEquals(trimmed.size(),
                lines.size() - comments.values().stream().mapToInt(List::size).sum());
    }

    @Test
    public void writeTrimmedFileToDisk() throws IOException {
        Path outputFile = Path.of("trimmedFile.java");
        Path referenceFile = FileUtils.getFileFromResources("trimmedFile_reference.java");
//        outputFile.toFile().delete();
        FileUtils.writeFile(outputFile, CommentManager.removeJavaDocComments(lines, comments));
        assertFilesMatch(outputFile, referenceFile);
    }

    @Test
    public void createMarkDown() throws IOException {
        Path outputFile = Path.of("markdown.md");
        Path referenceFile = FileUtils.getFileFromResources("markdown_reference.md");
        combineCommentsWithDeclaration();
        CommentManager.writeMarkDown(FileUtils.getFileFromResources("CommandByteReader.java"), outputFile, combined);
        assertFilesMatch(outputFile, referenceFile);
    }

//    @Test
//    public void restoreJavaDocsToJava() throws IOException {
//        Path outputFile = Path.of("restoredFile.java");
//        Path referenceFile = FileUtils.getFileFromResources("CommandByteReader.java");
//        combineCommentsWithDeclaration();
//        CommentManager.writeRestoredFile(FileUtils.getFileFromResources("markdown_reference.md"), outputFile);
//
//        assertFilesMatch(outputFile, referenceFile);
//    }

    private static void assertFilesMatch(Path outputFile, Path referenceFile) {
        assertTrue(outputFile.toFile().exists());
        List<String> newFile = FileUtils.readFile(outputFile);
        List<String> refFile = FileUtils.readFile(referenceFile);
        assertEquals(newFile, refFile, "Files do not match.");
    }

    private static List<String> readFirstFile() throws IOException {
        // List<Path> allJavaFiles_src = FileUtils.getAllJavaFiles(src);
        // @SuppressWarnings("OptionalGetWithoutIsPresent")
        // Path path = allJavaFiles_src.stream().findFirst().get();
        currentFile = FileUtils.getFileFromResources(Path.of("CommandByteReader.java"));
        return FileUtils.readFile(currentFile);
    }


}
