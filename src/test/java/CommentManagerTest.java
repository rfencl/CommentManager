import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
public class CommentManagerTest {

    Path src = Paths.get("/home/rick/Documents/powin/localmanager-test/src/");
    Path docs = Paths.get("/home/rick/Documents/powin/localmanager-test/docs/");
    @Test
    public void verifyPathsExist() {
        assertTrue(src.toFile().exists());
        assertTrue(docs.toFile().exists());
    }

    @Test
    public void verifySrcStructureMatchesDocs() throws IOException {
        CommentManager.createDocDirectoryStructure(src);
        List<Path> subdirectories_src = FileUtils.getAllSubdirectories(src);
//        subdirectories_src.forEach(System.out::println);
        List<Path> subdirectories_doc = FileUtils.getAllSubdirectories(docs);
//        subdirectories_doc.forEach(System.out::println);
        assertEquals(subdirectories_src.size(), subdirectories_doc.size());
    }

    @Test
    public void verifyThatAMarkDownFileExistsForEachJavaFile() throws IOException {
        CommentManager.createMarkDowns(src);
        List<Path> allJavaFiles_src = FileUtils.getAllJavaFiles(src);
        List<Path> allJavaFiles_docs = FileUtils.getAllMdFiles(docs);
        assertEquals(allJavaFiles_src.size(), allJavaFiles_docs.size());
    }

    @Test
    public void readFirstJavaFile() throws IOException {
        List<Path> allJavaFiles_src = FileUtils.getAllJavaFiles(src);
        Path path = allJavaFiles_src.stream().findFirst().get();
        List<String> lines = FileUtils.readFileToList(path);
        log.info(lines);
        assertTrue(lines.size() > 0);
    }

    @Test
    public void parseFileForClassesAndMethods() throws IOException {
        List<Path> allJavaFiles_src = FileUtils.getAllJavaFiles(src);
        Path path = allJavaFiles_src.stream().findFirst().get();
        List<String> lines = FileUtils.readFileToList(path);
        List<String> methods = new ArrayList<>();
        lines.forEach(l ->  {
            if (l.matches("^.*public.*class.*\\{|^.*(?:private|public).*\\(.*\\).*\\{")) {
                methods.add(l);
            }
        });
        assertEquals(methods.size(), 5);
    }


}
