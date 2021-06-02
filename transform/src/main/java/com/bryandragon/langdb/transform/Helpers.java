package com.bryandragon.langdb.transform;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Predicate;

public final class Helpers {
  public static final byte[] BOM_UTF8 = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

  /** Skips BOM if present at the start of UTF-8-encoded {@code bis}. */
  public static BufferedInputStream skipBomUtf8(BufferedInputStream bis) throws IOException {
    byte[] buf = new byte[BOM_UTF8.length];
    bis.mark(BOM_UTF8.length);
    int readCount = bis.read(buf, 0, BOM_UTF8.length);
    if (readCount < BOM_UTF8.length || !Arrays.equals(buf, BOM_UTF8)) {
      bis.reset();
    }
    return bis;
  }

  /** Consumes lines from {@code reader} while {@code predicate} is true. */
  public static BufferedReader skipLinesWhile(BufferedReader reader, Predicate<String> predicate)
      throws IOException {
    while (true) {
      reader.mark(1024);
      String line = reader.readLine();
      if (line != null && predicate.test(line)) {
        continue;
      }
      reader.reset();
      break;
    }
    return reader;
  }
}
