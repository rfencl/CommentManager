import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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
        Map<Integer, String> store = getIntegerStringMap();
        assertEquals(store.size(), 5);

    }

    private Map<Integer, String> getIntegerStringMap() throws IOException {
        List<Path> allJavaFiles_src = FileUtils.getAllJavaFiles(src);
        Path path = allJavaFiles_src.stream().findFirst().get();
        List<String> lines = FileUtils.readFileToList(path);
        Map<Integer, String> store = new LinkedHashMap<>();
        Integer i = 1;
        for (String l : lines) {
            if (l.matches("^.*public.*class.*\\{|^.*(?:private|public).*\\(.*\\).*\\{")) {
                store.put(i, l);
            }
            i++;
        }
        log.info(store.toString());
        return store;
    }

    @Test
    public void parseFileForComments() throws IOException {
        Map<Integer, List<String>> store = getIntegerListMap();
        assertEquals(store.size(), 5);
    }

    private Map<Integer, List<String>> getIntegerListMap() throws IOException {
        List<Path> allJavaFiles_src = FileUtils.getAllJavaFiles(src);
        Path path = allJavaFiles_src.stream().findFirst().get();
        List<String> lines = FileUtils.readFileToList(path);
        String startOfJavaDoc = "^.*/\\*\\*.*$";
        String endOfJavaDoc = "^.*\\*/.*$";
        String middleOfJavaDoc = "^\s*\\*\s.*$";
        List<String> comment = new ArrayList<>();
        Map<Integer, List<String>> store = new LinkedHashMap<>();
        Integer i = 1;
        Integer startOfComment = 0;
        for (String l : lines) {
            if (l.matches(startOfJavaDoc)) {
                comment = new ArrayList<>();
                comment.add(l);
                startOfComment = i;
            }

            else if (l.matches(middleOfJavaDoc)) {
                comment.add(l);
            }

            else if (l.matches(endOfJavaDoc)) {
                comment.add(l);
                store.put(startOfComment, comment);
            }
            i++;
        }
        log.info(store.toString());
        return store;
    }

    @Test
    public void combineCommentsWithDeclaration() throws IOException {
        Map<Integer, String> integerStringMap = getIntegerStringMap();
        Map<Integer, List<String>> integerListMap = getIntegerListMap();
        log.info("function keys {}", integerStringMap.keySet());
        log.info("comment keys {}", integerListMap.keySet());
        Map<String, List<String>> combined = new LinkedHashMap<>();
        Queue<Integer> queue = new LinkedList<>(integerListMap.keySet());
        for (Integer fi : integerStringMap.keySet()) {
            for(Integer li : queue) {
                if (li < fi) {
                    combined.put(integerStringMap.get(fi), integerListMap.get(li));
                    queue.remove();
                    break;
                }
            }
        }
        assertEquals(5, combined.size());
    }




}
