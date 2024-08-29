# [ReportByteWriter](../../../src/main/java/com/powin/utilities/ReportByteWriter.java)

## Write Report Data as little-endian ascii-hex

## _Methods_

 public static String **formatHex**(long value, int hexCharCount);

    format the value as little-endian hex padding with leading zeros as needed and limiting the converted value to hexCharCount hexDigits.
---
private static String **toLittleEndian**(StringBuilder sb)

    convert the hex-ascii string in sb to little-endian
    convert the string to a byte array
    iterate through the array and swap the bytes.
---


private static void **swapBytes**(char[] ca, int index)
  
        swap the cells of an array ca at index with index + 1 using exclusive-or 
---    





