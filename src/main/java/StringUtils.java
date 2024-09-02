import lombok.extern.log4j.Log4j2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class StringUtils {
    public static final String MARKDOWN_INDENT_FOR_CODE_BLOCK = "    ";

    public static String padWithLeadingSpaces(String str) {
        if (!str.startsWith(MARKDOWN_INDENT_FOR_CODE_BLOCK)) {
            return MARKDOWN_INDENT_FOR_CODE_BLOCK + str;
        }
        return str;
    }

    /**
     * Extract the method name from a method declaration.
     * @param methodDeclaration
     * @return
     */
    public static String getMethodName(String methodDeclaration) {
        String regex = "\\b(?:public|private|protected|static|final|abstract|synchronized|native|transient|volatile|\\s)*\\s+\\w+\\s+(\\w+)\\s*\\(";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(methodDeclaration);
        if (matcher.find()) {
            log.debug("Method name: " + matcher.group(1));
            return matcher.group(1);
        } else {
            log.error("No method name found.");
            return "";
        }
    }
}
