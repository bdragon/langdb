package com.bryandragon.langdb.transform;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class HelpersTest {
  @ParameterizedTest
  @MethodSource("skipBomUtf8Arguments")
  void testSkipBomUtf8(byte[] input, byte[] want) {
    assertDoesNotThrow(
        () -> {
          InputStream inputStream = new ByteArrayInputStream(input);
          inputStream = Helpers.skipBomUtf8(new BufferedInputStream(inputStream));
          byte[] got = readAll(inputStream);

          assertArrayEquals(want, got);
        });
  }

  static Stream<Arguments> skipBomUtf8Arguments() throws Throwable {
    String text = "The quick brown fox jumped over the lazy dog.";
    byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);

    return Stream.of(
        Arguments.of(new byte[0], new byte[0]),
        Arguments.of(prependBomUtf8(new byte[0]), new byte[0]),
        Arguments.of(prependBomUtf8(textBytes), textBytes),
        Arguments.of(textBytes, textBytes));
  }

  @ParameterizedTest
  @MethodSource("skipLinesWhileArguments")
  void testSkipLinesWhile(Predicate<String> predicate, String text, String want) {
    BufferedReader reader = new BufferedReader(new StringReader(text));
    assertDoesNotThrow(
        () -> {
          Helpers.skipLinesWhile(reader, predicate);
          String got = readAll(reader);

          assertEquals(want, got);
        });
  }

  static Stream<Arguments> skipLinesWhileArguments() throws Throwable {
    Predicate<String> predicate = line -> line.equals("1");

    return Stream.of(
        Arguments.of(predicate, "", ""),
        Arguments.of(predicate, "1\n1\n1\n2\n1\n", "2\n1\n"),
        Arguments.of(predicate, "2\n1\n", "2\n1\n"),
        Arguments.of(predicate, "\n1\n", "\n1\n"));
  }

  static byte[] prependBomUtf8(byte[] utf8Bytes) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    outputStream.write(Helpers.BOM_UTF8);
    outputStream.write(utf8Bytes);
    return outputStream.toByteArray();
  }

  static String readAll(Reader r) throws IOException {
    StringBuilder sb = new StringBuilder();
    int readLimit = 1024;
    char[] buf = new char[readLimit];
    int readCount;

    while ((readCount = r.read(buf)) > 0) {
      sb.append(buf, 0, readCount);
      if (readCount < readLimit) {
        break;
      }
    }

    r.close();
    return sb.toString();
  }

  static byte[] readAll(InputStream inputStream) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    inputStream.transferTo(outputStream);
    return outputStream.toByteArray();
  }
}
