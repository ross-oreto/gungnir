package io.oreto.gungnir.http;

/**
 * Extracted from Guava.
 * <p>
 * Static methods pertaining to ASCII characters (those in the range of values {@code 0x00} through
 * {@code 0x7F}), and to strings containing such characters.
 *
 * <p>ASCII utilities also exist in other classes of this package:
 * <ul>
 * <li>{@link CharMatcher#ascii} matches ASCII characters and provides text processing methods which
 * operate only on the ASCII characters of a string.
 * </ul>
 */
final class Ascii {
    private Ascii() {}

    /**
     * Returns a copy of the input string in which all {@linkplain #isUpperCase(char) uppercase ASCII
     * characters} have been converted to lowercase. All other characters are copied without
     * modification.
     */
    public static String toLowerCase(String string) {
        int length = string.length();
        for (int i = 0; i < length; i++) {
            if (isUpperCase(string.charAt(i))) {
                char[] chars = string.toCharArray();
                for (; i < length; i++) {
                    char c = chars[i];
                    if (isUpperCase(c)) {
                        chars[i] = (char) (c ^ 0x20);
                    }
                }
                return String.valueOf(chars);
            }
        }
        return string;
    }

    /**
     * Returns a copy of the input character sequence in which all {@linkplain #isUpperCase(char)
     * uppercase ASCII characters} have been converted to lowercase. All other characters are copied
     * without modification.
     */
    public static String toLowerCase(CharSequence chars) {
        if (chars instanceof String) {
            return toLowerCase((String) chars);
        }
        char[] newChars = new char[chars.length()];
        for (int i = 0; i < newChars.length; i++) {
            newChars[i] = toLowerCase(chars.charAt(i));
        }
        return String.valueOf(newChars);
    }

    /**
     * If the argument is an {@linkplain #isUpperCase(char) uppercase ASCII character} returns the
     * lowercase equivalent. Otherwise, returns the argument.
     */
    public static char toLowerCase(char c) {
        return isUpperCase(c) ? (char) (c ^ 0x20) : c;
    }

    /**
     * Indicates whether {@code c} is one of the twenty-six lowercase ASCII alphabetic characters
     * between {@code 'a'} and {@code 'z'} inclusive. All others (including non-ASCII characters)
     * return {@code false}.
     */
    public static boolean isLowerCase(char c) {
        // Note: This was benchmarked against the alternate expression "(char)(c - 'a') < 26" (Nov '13)
        // and found to perform at least as well, or better.
        return (c >= 'a') && (c <= 'z');
    }

    /**
     * Indicates whether {@code c} is one of the twenty-six uppercase ASCII alphabetic characters
     * between {@code 'A'} and {@code 'Z'} inclusive. All others (including non-ASCII characters)
     * return {@code false}.
     */
    public static boolean isUpperCase(char c) {
        return (c >= 'A') && (c <= 'Z');
    }
}
