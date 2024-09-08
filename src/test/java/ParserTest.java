import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
class ParserTest {


  /**
   * Had to write this test to find the bug with the Middle Javadoc regex.
   * The change I made fixed this test but then was too greedy using the regex.
   * I punted and changed that to a simple string startsWith.
   */
  @Test
  void parseJavaDocs() {
    Map<Integer, List<String>> comments;
    List<String> lines = List.of(
      "/**",
      " * Formats the hex-ascii little-endian value to an int.",
      "     *",
      "     * @param in    - hex ascii little endian string",
      "     * @param field - position and length of the field to decode",
      "     * @return - int value",
      "  */"
    );
    comments = Parser.parseJavaDocs(lines);
    assertEquals(7, comments.get(1).size());
  }

}





