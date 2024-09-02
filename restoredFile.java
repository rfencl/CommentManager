package com.powin.utilities;

import lombok.extern.log4j.Log4j2;

/**
 * Responsible for processing the hex-ascii commands in little-endian and
 * converting the fields into decimal.
 */
@Log4j2
public class CommandByteReader {
    public static final int RADIX_HEX = 16;

    /**
     * Formats the hex-ascii little-endian value to an int.
     *
     * @param in    - hex ascii little endian string
     * @param field - position and length of the field to decode
     * @return - int value
     */
    public static int formatInt(String in, HexField field) {
        return (int) formatLong(in, field);
    }

    /**
     * Formats the hex-ascii little-endian value to a long.
     *
     * @param in    - hex ascii little endian string
     * @param field - position and length of the field to decode
     * @return - long value
     */
    public static long formatLong(String in, HexField field) {
        log.debug("in {}", in);
        StringBuilder reversed = new StringBuilder(toBigEndian(new StringBuilder(in).
                                                                substring(field.getStartChar(), field.getEndChar())))
                                                                .reverse();
        long l = Long.parseLong(reversed.toString(), RADIX_HEX);
        log.debug("reversed 0x:{}, dec:{}", reversed, l);
        return l;
    }

    /**
     * Convert a hex-ascii string from little-endian to big-endian
     * so the string can be parsed back to int and long.
     *
     * @param command
     * @return
     */
    public static String toBigEndian(String command) {
        char[] ca = command.toCharArray();
        return swapAllChars(ca);
    }

    /**
     * Swap all the chars in a char array. abcd to badc
     *
     * @param ca - array of chars
     * @return String with chars swapped.
     */
    private static String swapAllChars(char[] ca) {
        for (int i = 0; i < ca.length; i += 2) {
            ArrayUtils.swapChars(ca, i);
        }
        return new String(ca);
    }

}
