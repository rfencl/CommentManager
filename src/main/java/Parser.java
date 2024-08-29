import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class Parser {
    public static final String START_OF_JAVA_DOC = "^.*/\\*\\*.*$";
    public static final String END_OF_JAVA_DOC = "^\\s*\\*/.*$";

    /**
     * Copy the javadoc lines.
     * @param lines - The lines of the file to parse.
     * @return - Map of javadocs indexed by the line where they were found.
     */
    public static Map<Integer, List<String>> parseJavaDocs(List<String> lines) {
        List<String> comment = new ArrayList<>();
        Map<Integer, List<String>> store = new LinkedHashMap<>();
        Integer i = 1;
        Integer startOfComment = 0;
        for (String l : lines) {
            if (l.matches(START_OF_JAVA_DOC)) {
                comment = new ArrayList<>();
                comment.add(l);
                startOfComment = i;
            }
            else if (l.matches(END_OF_JAVA_DOC)) {
                comment.add(l);
                store.put(startOfComment, comment);
            }
            else if (l.replaceAll("^\\s*", "").startsWith("*"))  {
                comment.add(l);
            }
            i++;
        }
        log.debug(store.toString());
        return store;
    }

    /**
     * Copy the class and method declarations
     * @param lines  - lines of the input file
     * @return       - map of declarations indexed by the line number.
     * @throws IOException
     */
    public static Map<Integer, String> parseClassAndMethods(List<String> lines) throws IOException {
        Map<Integer, String> store = new LinkedHashMap<>();
        Integer i = 1;
        for (String l : lines) {
            if (l.matches("^.*public.*class.*\\{|^.*(?:private|public).*\\(.*\\).*\\{")) {
                store.put(i, l);
            }
            i++;
        }
        log.debug(store.toString());
        return store;
    }
}
