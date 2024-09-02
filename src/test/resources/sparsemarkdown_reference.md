# CommandByteReader

## _Methods_
public static int **formatInt**(String in, HexField field) {

	    /**

	     * Formats the hex-ascii little-endian value to an int.

	     *

	     * @param in    - hex ascii little endian string

	     * @param field - position and length of the field to decode

	     * @return - int value

	     */
---
public static long **formatLong**(String in, HexField field) {

---
public static String **toBigEndian**(String command) {

	    /**

	     * Convert a hex-ascii string from little-endian to big-endian

	     * so the string can be parsed back to int and long.

	     *

	     * @param command

	     * @return

	     */
---
private static String **swapAllChars**(char[] ca) {

---
