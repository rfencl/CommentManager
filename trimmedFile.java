package com.powin.utilities;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class CommandByteReader {
    public static final int RADIX_HEX = 16;

    public static int formatInt(String in, HexField field) {
        return (int) formatLong(in, field);
    }

    public static long formatLong(String in, HexField field) {
        log.debug("in {}", in);
        StringBuilder reversed = new StringBuilder(toBigEndian(new StringBuilder(in).
                                                                substring(field.getStartChar(), field.getEndChar())))
                                                                .reverse();
        long l = Long.parseLong(reversed.toString(), RADIX_HEX);
        log.debug("reversed 0x:{}, dec:{}", reversed, l);
        return l;
    }

    public static String toBigEndian(String command) {
        char[] ca = command.toCharArray();
        return swapAllChars(ca);
    }

    private static String swapAllChars(char[] ca) {
        for (int i = 0; i < ca.length; i += 2) {
            ArrayUtils.swapChars(ca, i);
        }
        return new String(ca);
    }

}
