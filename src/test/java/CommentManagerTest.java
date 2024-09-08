import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
class CommentManagerTest {
  static final String BASE_JAVA_TEST_FILE = "CommandByteReader.java";
  static final String TRIMMED_FILE_REFERENCE_JAVA = "trimmedFile_reference.java";
  static final String RESTORED_FILE_JAVA = "restoredFile.java";
  static final String FILES_DO_NOT_MATCH = "Files do not match.";
  static final String MARKDOWN_REFERENCE_MD = "markdown_reference.md";
  static final String MARKDOWN_OUTPUT_MD = "markdown.md";
  static final String TRIMMED_OUTPUT_JAVA = "trimmedFile.java";
  static final Path linuxSrc = Path.of("/home/rick/Documents/powin/localmanager-test/src/");
  static final Path linuxDocs = Path.of("/home/rick/Documents/powin/localmanager-test/docs/");
  static final Path winsrc = Path.of("F:\\Powin\\localmanager-test\\src\\");
  static final Path windocs = Path.of("F:\\Powin\\localmanager-test\\docs\\");
  private static final String SPARSE_JAVA_TEST_FILE = "SparseCommandByteReader.java";
  private static final String MARKDOWN_SPARSE_OUTPUT_MD = "sparsemarkdown.md";
  private static final String SPARSE_MARKDOWN_REFERENCE_MD = "sparsemarkdown_reference.md";
  static Path src;
  static Path docs;

  @BeforeAll
  static void initBeforeClass() {
    if (FileUtils.isWindows()) {
      src = winsrc;
      docs = windocs;
    } else {
      src = linuxSrc;
      docs = linuxDocs;
    }
  }

  private static void assertFilesMatch(Path outputFile, Path referenceFile) {
    assertTrue(outputFile.toFile().exists());
    List<String> newFile = FileUtils.readFile(outputFile);
    List<String> refFile = FileUtils.readFile(referenceFile);
    assertEquals(newFile, refFile, FILES_DO_NOT_MATCH);
  }

  @Test
  void verifyParseOfBaseTestInputFile() throws IOException {
    CommentManager.parseFile(BASE_JAVA_TEST_FILE);
    assertEquals(5, CommentManager.classesAndMethods.size());
    assertEquals(5, CommentManager.comments.size());
    assertEquals(7, CommentManager.comments.get(13).size());
  }

  @Test
  void verifyParseOfSparseTestInputFile() throws IOException {
    CommentManager.parseFile(SPARSE_JAVA_TEST_FILE);
    assertEquals(5, CommentManager.classesAndMethods.size());
    assertEquals(2, CommentManager.comments.size());
    assertEquals(7, CommentManager.comments.get(30).size());
  }

  @Test
  void verifyPathsExist() {
    assertTrue(src.toFile().exists());
    assertTrue(docs.toFile().exists());
  }

  @Test
  void verifySrcStructureMatchesDocs() throws IOException {
    FileUtils.createDocDirectoryStructure(src);
    List<Path> subdirectories_src = FileUtils.getAllSubdirectories(src);
    subdirectories_src.forEach(log::debug);
    List<Path> subdirectories_doc = FileUtils.getAllSubdirectories(docs);
    subdirectories_doc.forEach(log::debug);
    assertEquals(subdirectories_src.size(), subdirectories_doc.size());
  }

  @Test
  void verifyThatAMarkDownFileExistsForEachJavaFile() throws IOException {
    FileUtils.createMarkDowns(src);
    List<Path> allJavaFiles_src = FileUtils.getAllJavaFiles(src);
    List<Path> allJavaFiles_docs = FileUtils.getAllMdFiles(docs);
    assertEquals(allJavaFiles_src.size(), allJavaFiles_docs.size());
  }

  @Test
  void combineCommentsWithDeclaration() throws IOException {
    CommentManager.parseFile(BASE_JAVA_TEST_FILE);
    CommentManager.combineCommentsAndDeclarations();
    assertEquals(5, CommentManager.combined.size());
  }

  @Test
  void removeCommentsFromFile() {
    List<String> trimmed = CommentManager.removeJavaDocComments(CommentManager.lines, CommentManager.comments);
    assertEquals(trimmed.size(),
      CommentManager.lines.size() - CommentManager.comments.values().stream().mapToInt(List::size).sum());
  }

  @Test
  void writeTrimmedFileToDisk() throws IOException {
    Path outputFile = Path.of(TRIMMED_OUTPUT_JAVA);
    Path referenceFile = FileUtils.getFileFromResources(TRIMMED_FILE_REFERENCE_JAVA);
    FileUtils.writeFile(outputFile, CommentManager.removeJavaDocComments(CommentManager.lines, CommentManager.comments));
    assertFilesMatch(outputFile, referenceFile);
  }

  @Test
  void writeTrimmedSparseFileToDisk() throws IOException {
    CommentManager.parseFile(SPARSE_JAVA_TEST_FILE);
    Path outputFile = Path.of(TRIMMED_OUTPUT_JAVA);
    Path referenceFile = FileUtils.getFileFromResources(TRIMMED_FILE_REFERENCE_JAVA);
    FileUtils.writeFile(outputFile, CommentManager.removeJavaDocComments(CommentManager.lines, CommentManager.comments));
    assertFilesMatch(outputFile, referenceFile);
  }

  @Test
  void createMarkDown() throws IOException {
    Path outputFile = Path.of(MARKDOWN_OUTPUT_MD);
    Path referenceFile = FileUtils.getFileFromResources(MARKDOWN_REFERENCE_MD);
    CommentManager.parseFile(BASE_JAVA_TEST_FILE);
    CommentManager.combineCommentsAndDeclarations();
    CommentManager.writeMarkDown(FileUtils.getFileFromResources(BASE_JAVA_TEST_FILE), outputFile, CommentManager.combined);
    assertFilesMatch(outputFile, referenceFile);
  }

  @Test
  void createMarkDownFromSparseInput() throws IOException {
    CommentManager.parseFile(SPARSE_JAVA_TEST_FILE);
    Path outputFile = Path.of(MARKDOWN_SPARSE_OUTPUT_MD);
    Path referenceFile = FileUtils.getFileFromResources(SPARSE_MARKDOWN_REFERENCE_MD);
    CommentManager.combineCommentsAndDeclarations();
    CommentManager.writeMarkDown(FileUtils.getFileFromResources(BASE_JAVA_TEST_FILE), outputFile, CommentManager.combined);
    assertFilesMatch(outputFile, referenceFile);
  }

  @Test
  void restoreJavaDocsToJava() throws IOException {
    Path outputFile = Path.of(RESTORED_FILE_JAVA);
    Path referenceFile = FileUtils.getFileFromResources(BASE_JAVA_TEST_FILE);
    CommentManager.writeRestoredFile(FileUtils.getFileFromResources(TRIMMED_FILE_REFERENCE_JAVA), FileUtils.getFileFromResources("markdown_reference.md"), outputFile);
    assertFilesMatch(outputFile, referenceFile);
  }

}
