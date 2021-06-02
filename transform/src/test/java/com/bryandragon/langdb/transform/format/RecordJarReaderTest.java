package com.bryandragon.langdb.transform.format;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RecordJarReaderTest {
  @Test
  void testEmptyReader() {
    BufferedReader source = new BufferedReader(Reader.nullReader());
    com.bryandragon.langdb.common.format.RecordJarReader subject = new com.bryandragon.langdb.common.format.RecordJarReader(source);

    assertDoesNotThrow(() -> {
      Iterator<Map<String, String>> it = subject.iterator();
      assertFalse(it.hasNext());
    });
  }

  @Test
  void testCommentsAndEmptyLines() {
    String text =
        "%% Comment 1\n\n" +
            "%% Comment 2\n" +
            "Name: foo\n\n" +
            "%%\n" +
            "%% Comment 3\n\n" +
            "%% Comment 4\n" +
            "Name: bar\n\n" +
            "%%\n" +
            "%%\n";
    BufferedReader source = new BufferedReader(new StringReader(text));
    com.bryandragon.langdb.common.format.RecordJarReader subject = new com.bryandragon.langdb.common.format.RecordJarReader(source);

    assertDoesNotThrow(() -> {
      Iterator<Map<String, String>> it = subject.iterator();

      assertTrue(it.hasNext());
      assertEquals(Map.of("Name", "foo"), it.next());

      assertTrue(it.hasNext());
      assertEquals(Map.of("Name", "bar"), it.next());

      assertFalse(it.hasNext());
    });
  }

  @Test
  void testCRLF() {
    String text =
        "%% Comment\r\n" +
            "Name: foo\r\n" +
            "%%\r\n\r\n" +
            "Name: bar\r\n" +
            "%%\r\n";
    BufferedReader source = new BufferedReader(new StringReader(text));
    com.bryandragon.langdb.common.format.RecordJarReader subject = new com.bryandragon.langdb.common.format.RecordJarReader(source);

    assertDoesNotThrow(() -> {
      Iterator<Map<String, String>> it = subject.iterator();

      assertTrue(it.hasNext());
      assertEquals(Map.of("Name", "foo"), it.next());

      assertTrue(it.hasNext());
      assertEquals(Map.of("Name", "bar"), it.next());

      assertFalse(it.hasNext());
    });
  }

  @Test
  void testFolding() {
    String text =
        "Name: A line beginning with one or more whitespace\n" +
            "      characters is joined to the previous field body\n" +
            "%% This is an interleaved comment.\n" +
            "      with one space.\n" +
            "%%\n" +
            "Name: The backslash (\\) at the end of this line causes the   \\\n" +
            "  three spaces before it to be preserved.\n" +
            "%%\n" +
            "Name: The trailing whitespace on this line will be stripped.   \n" +
            "%%\n" +
            "Name: The trailing whitespace on this line will be preserved.   \\\n" +
            "%%\n";
    BufferedReader source = new BufferedReader(new StringReader(text));
    com.bryandragon.langdb.common.format.RecordJarReader subject = new com.bryandragon.langdb.common.format.RecordJarReader(source);

    assertDoesNotThrow(() -> {
      Iterator<Map<String, String>> it = subject.iterator();

      assertTrue(it.hasNext());
      assertEquals(
          Map.of("Name", "A line beginning with one or more whitespace " +
              "characters is joined to the previous field body with one space."),
          it.next()
      );

      assertTrue(it.hasNext());
      assertEquals(
          Map.of("Name", "The backslash (\\) at the end of this line causes the   " +
              "three spaces before it to be preserved."),
          it.next()
      );

      assertTrue(it.hasNext());
      assertEquals(
          Map.of("Name", "The trailing whitespace on this line will be stripped."),
          it.next()
      );

      assertTrue(it.hasNext());
      assertEquals(
          Map.of("Name", "The trailing whitespace on this line will be preserved.   "),
          it.next()
      );

      assertFalse(it.hasNext());
    });
  }

  @Test
  void testSample() {
    String text =
        "Type: variant\n" +
            "Subtag: 1606nict\n" +
            "Description: Late Middle French (to 1606)\n" +
            "Added: 2007-03-20\n" +
            "Prefix: frm\n" +
            "Comments: 16th century French as in Jean Nicot, \"Thresor de la langue\n" +
            "  francoyse\", 1606, but also including some French similar to that of\n" +
            "  Rabelais\n" +
            "%%\n" +
            "Type: variant\n" +
            "Subtag: 1694acad\n" +
            "Description: Early Modern French\n" +
            "Added: 2007-03-20\n" +
            "Prefix: fr\n" +
            "Comments: 17th century French, as catalogued in the \"Dictionnaire de\n" +
            "  l'académie françoise\", 4eme ed. 1694; frequently includes\n" +
            "  elements of Middle French, as this is a transitional period\n";
    BufferedReader source = new BufferedReader(new StringReader(text));
    RecordJarReader subject = new RecordJarReader(source);

    assertDoesNotThrow(() -> {
      Iterator<Map<String, String>> it = subject.iterator();

      assertTrue(it.hasNext());
      assertEquals(
          Map.of(
              "Type", "variant",
              "Subtag", "1606nict",
              "Description", "Late Middle French (to 1606)",
              "Added", "2007-03-20",
              "Prefix", "frm",
              "Comments", "16th century French as in Jean Nicot, \"Thresor de la langue " +
                  "francoyse\", 1606, but also including some French similar to that of " +
                  "Rabelais"
          ),
          it.next()
      );

      assertTrue(it.hasNext());
      assertEquals(
          Map.of(
              "Type", "variant",
              "Subtag", "1694acad",
              "Description", "Early Modern French",
              "Added", "2007-03-20",
              "Prefix", "fr",
              "Comments", "17th century French, as catalogued in the \"Dictionnaire de " +
                  "l'académie françoise\", 4eme ed. 1694; frequently includes " +
                  "elements of Middle French, as this is a transitional period"
          ),
          it.next()
      );

      assertFalse(it.hasNext());
    });
  }
}
