public class StringUtils {
    public static final String MARKDOWN_INDENT_FOR_CODE_BLOCK = "    ";

    public static String padWithLeadingSpaces(String str) {
        if (!str.startsWith(MARKDOWN_INDENT_FOR_CODE_BLOCK)) {
            return MARKDOWN_INDENT_FOR_CODE_BLOCK + str;
        }
        return str;
    }
}
