import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
public class CommentManagerTest {
    public static final String BASE_JAVA_TEST_FILE = "CommandByteReader.java";
    private static final String SPARSE_JAVA_TEST_FILE = "SparseCommandByteReader.java";
    public static final String TRIMMED_FILE_REFERENCE_JAVA = "trimmedFile_reference.java";
    public static final String RESTORED_FILE_JAVA = "restoredFile.java";
    public static final String FILES_DO_NOT_MATCH = "Files do not match.";
    public static final String MARKDOWN_REFERENCE_MD = "markdown_reference.md";
    public static final String MARKDOWN_OUTPUT_MD = "markdown.md";
    public static final String TRIMMED_OUTPUT_JAVA = "trimmedFile.java";
    private static final String MARKDOWN_SPARSE_OUTPUT_MD = "sparsemarkdown.md";
    private static final String SPARSE_MARKDOWN_REFERENCE_MD = "sparsemarkdown_reference.md";
    static Map<String, List<String>> combined = new LinkedHashMap<>();
    static Map<Integer, String> classesAndMethods = new LinkedHashMap<>();
    static Map<Integer, List<String>> comments = new LinkedHashMap<>();
    static List<String> lines;

    static Path src;
    static Path docs;
    static Path linuxSrc = Path.of("/home/rick/Documents/powin/localmanager-test/src/");
    static Path linuxDocs = Path.of("/home/rick/Documents/powin/localmanager-test/docs/");
    static Path winsrc = Path.of("F:\\Powin\\localmanager-test\\src\\");
    static Path windocs = Path.of("F:\\Powin\\localmanager-test\\docs\\");

    @BeforeAll
    public static void initBeforeClass() throws IOException {
        if (FileUtils.isWindows()) {
            src = winsrc;
            docs = windocs;
        } else {
            src = linuxSrc;
            docs = linuxDocs;
        }
    }

    @Test
    void verifyParseOfBaseTestInputFile() throws IOException {
        parseFile(BASE_JAVA_TEST_FILE);
        assertEquals(5, classesAndMethods.size());
        assertEquals(5, comments.size());
        assertEquals(7, comments.get(13).size());
    }

    @Test
    void verifyParseOfSparseTestInputFile() throws IOException {
        parseFile(SPARSE_JAVA_TEST_FILE);
        assertEquals(5, classesAndMethods.size());
        assertEquals(2, comments.size());
        assertEquals(7, comments.get(30).size());
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
    public void combineCommentsWithDeclaration() throws IOException {
        parseFile(BASE_JAVA_TEST_FILE);
        combineCommentsAndDeclarations();
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
        Path outputFile = Path.of(TRIMMED_OUTPUT_JAVA);
        Path referenceFile = FileUtils.getFileFromResources(TRIMMED_FILE_REFERENCE_JAVA);
        FileUtils.writeFile(outputFile, CommentManager.removeJavaDocComments(lines, comments));
        assertFilesMatch(outputFile, referenceFile);
    }

    @Test
    public void writeTrimmedSparseFileToDisk() throws IOException {
        parseFile(SPARSE_JAVA_TEST_FILE);
        Path outputFile = Path.of(TRIMMED_OUTPUT_JAVA);
        Path referenceFile = FileUtils.getFileFromResources(TRIMMED_FILE_REFERENCE_JAVA);
        FileUtils.writeFile(outputFile, CommentManager.removeJavaDocComments(lines, comments));
        assertFilesMatch(outputFile, referenceFile);
    }

    @Test
    public void createMarkDown() throws IOException {
        Path outputFile = Path.of(MARKDOWN_OUTPUT_MD);
        Path referenceFile = FileUtils.getFileFromResources(MARKDOWN_REFERENCE_MD);
        parseFile(BASE_JAVA_TEST_FILE);
        combineCommentsAndDeclarations();
        CommentManager.writeMarkDown(FileUtils.getFileFromResources(BASE_JAVA_TEST_FILE), outputFile, combined);
        assertFilesMatch(outputFile, referenceFile);
    }

    @Disabled
    @Test
    public void createMarkDownFromSparseInput() throws IOException {
        Path outputFile = Path.of(MARKDOWN_SPARSE_OUTPUT_MD);
        Path referenceFile = FileUtils.getFileFromResources(SPARSE_MARKDOWN_REFERENCE_MD);
        combineCommentsAndDeclarations();
        CommentManager.writeMarkDown(FileUtils.getFileFromResources(BASE_JAVA_TEST_FILE), outputFile, combined);
        assertFilesMatch(outputFile, referenceFile);
    }

    @Test
    public void restoreJavaDocsToJava() throws IOException {
        Path outputFile = Path.of(RESTORED_FILE_JAVA);
        Path referenceFile = FileUtils.getFileFromResources(BASE_JAVA_TEST_FILE);
        CommentManager.writeRestoredFile(FileUtils.getFileFromResources(TRIMMED_FILE_REFERENCE_JAVA), FileUtils.getFileFromResources("markdown_reference.md"), outputFile);
        assertFilesMatch(outputFile, referenceFile);
    }

    private static void assertFilesMatch(Path outputFile, Path referenceFile) {
        assertTrue(outputFile.toFile().exists());
        List<String> newFile = FileUtils.readFile(outputFile);
        List<String> refFile = FileUtils.readFile(referenceFile);
        assertEquals(newFile, refFile, FILES_DO_NOT_MATCH);
    }

    private static void parseFile(String inputTestJavaFile) throws IOException {
        lines = FileUtils.readFile(FileUtils.getFileFromResources(Path.of(inputTestJavaFile)));
        classesAndMethods = Parser.parseClassAndMethods(lines);
        comments = Parser.parseJavaDocs(lines);
    }

    private static void combineCommentsAndDeclarations() {
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
    }

}
