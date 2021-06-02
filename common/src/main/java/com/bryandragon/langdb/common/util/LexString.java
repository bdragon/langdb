package com.bryandragon.langdb.common.util;

import java.util.ArrayList;
import java.util.List;

public final class LexString {
  /**
   * Produces a list of all strings between {@code lo} and {@code hi}, inclusive, in lexicographical
   * order.
   */
  public static List<String> fill(String lo, String hi) {
    List<String> values = new ArrayList<>();
    String s = lo;
    do {
      values.add(s);
    } while ((s = nextAlpha(s)).compareTo(hi) <= 0);
    return values;
  }

  /** Returns the next string after {@code s} with the same length in lexicographical order. */
  public static String nextAlpha(String s) {
    char[] buf = new char[s.length()];
    intToCharArray(charArrayToInt(s.toLowerCase().toCharArray()) + 1, buf);
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) <= 'Z') {
        buf[i] -= 'a' - 'A';
      }
    }
    return new String(buf);
  }

  private static final char base = 26;

  private static int charArrayToInt(char[] cs) {
    int v = 0;
    for (char c : cs) {
      v *= base;
      v += c - 'a';
    }
    return v;
  }

  private static void intToCharArray(int v, char[] cs) {
    for (int i = cs.length - 1; i >= 0; i--) {
      cs[i] = (char) ((v % base) + 'a');
      v /= base;
    }
  }
}
