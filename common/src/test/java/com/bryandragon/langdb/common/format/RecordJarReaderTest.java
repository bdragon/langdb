package com.bryandragon.langdb.common.format;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

import static com.bryandragon.langdb.common.format.RecordJarReader.FieldValue;
import static org.junit.jupiter.api.Assertions.*;

class RecordJarReaderTest {
  @Test
  void testEmptyReader() {
    BufferedReader source = new BufferedReader(Reader.nullReader());
    RecordJarReader subject = new RecordJarReader(source);

    assertDoesNotThrow(
        () -> {
          Iterator<Map<String, FieldValue>> it = subject.iterator();
          assertFalse(it.hasNext());
        });
  }

  @Test
  void testCommentsAndEmptyLines() {
    String text =
        "%% Comment 1\n\n"
            + "%% Comment 2\n"
            + "Name: foo\n\n"
            + "%%\n"
            + "%% Comment 3\n\n"
            + "%% Comment 4\n"
            + "Name: bar\n\n"
            + "%%\n"
            + "%%\n";
    BufferedReader source = new BufferedReader(new StringReader(text));
    RecordJarReader subject = new RecordJarReader(source);

    assertDoesNotThrow(
        () -> {
          Iterator<Map<String, FieldValue>> it = subject.iterator();

          assertTrue(it.hasNext());
          assertEquals(Map.of("Name", FieldValue.of("foo")), it.next());

          assertTrue(it.hasNext());
          assertEquals(Map.of("Name", FieldValue.of("bar")), it.next());

          assertFalse(it.hasNext());
        });
  }

  @Test
  void testCRLF() {
    String text = "%% Comment\r\n" + "Name: foo\r\n" + "%%\r\n\r\n" + "Name: bar\r\n" + "%%\r\n";
    BufferedReader source = new BufferedReader(new StringReader(text));
    RecordJarReader subject = new RecordJarReader(source);

    assertDoesNotThrow(
        () -> {
          Iterator<Map<String, FieldValue>> it = subject.iterator();

          assertTrue(it.hasNext());
          assertEquals(Map.of("Name", FieldValue.of("foo")), it.next());

          assertTrue(it.hasNext());
          assertEquals(Map.of("Name", FieldValue.of("bar")), it.next());

          assertFalse(it.hasNext());
        });
  }

  @Test
  void testFolding() {
    String text =
        "Name: A line beginning with one or more whitespace\n"
            + "      characters is joined to the previous field body\n"
            + "%% This is an interleaved comment.\n"
            + "      with one space.\n"
            + "%%\n"
            + "Name: The backslash (\\) at the end of this line causes the   \\\n"
            + "  three spaces before it to be preserved.\n"
            + "%%\n"
            + "Name: The trailing whitespace on this line will be stripped.   \n"
            + "%%\n"
            + "Name: The trailing whitespace on this line will be preserved.   \\\n"
            + "%%\n";
    BufferedReader source = new BufferedReader(new StringReader(text));
    RecordJarReader subject = new RecordJarReader(source);

    assertDoesNotThrow(
        () -> {
          Iterator<Map<String, FieldValue>> it = subject.iterator();

          assertTrue(it.hasNext());
          assertEquals(
              Map.of(
                  "Name",
                  FieldValue.of(
                      "A line beginning with one or more whitespace "
                          + "characters is joined to the previous field body with one space.")),
              it.next());

          assertTrue(it.hasNext());
          assertEquals(
              Map.of(
                  "Name",
                  FieldValue.of(
                      "The backslash (\\) at the end of this line causes the   "
                          + "three spaces before it to be preserved.")),
              it.next());

          assertTrue(it.hasNext());
          assertEquals(
              Map.of(
                  "Name", FieldValue.of("The trailing whitespace on this line will be stripped.")),
              it.next());

          assertTrue(it.hasNext());
          assertEquals(
              Map.of(
                  "Name",
                  FieldValue.of("The trailing whitespace on this line will be preserved.   ")),
              it.next());

          assertFalse(it.hasNext());
        });
  }

  @Test
  void testRepeatedFieldName() {
    String text =
        "Type: variant\n"
            + "Subtag: spanglis\n"
            + "Description: Spanglish\n"
            + "Added: 2017-02-23\n"
            + "Prefix: en\n"
            + "Prefix: es\n"
            + "Comments: A variety of contact dialects of English and Spanish\n";

    BufferedReader source = new BufferedReader(new StringReader(text));
    RecordJarReader subject = new RecordJarReader(source);

    assertDoesNotThrow(
        () -> {
          Iterator<Map<String, FieldValue>> it = subject.iterator();

          assertTrue(it.hasNext());
          assertEquals(
              Map.of(
                  "Type", FieldValue.of("variant"),
                  "Subtag", FieldValue.of("spanglis"),
                  "Description", FieldValue.of("Spanglish"),
                  "Added", FieldValue.of("2017-02-23"),
                  "Prefix", FieldValue.of("en", "es"),
                  "Comments",
                      FieldValue.of("A variety of contact dialects of English and Spanish")),
              it.next());

          assertFalse(it.hasNext());
        });
  }

  @Test
  void testSample() {
    String text =
        "Type: variant\n"
            + "Subtag: 1606nict\n"
            + "Description: Late Middle French (to 1606)\n"
            + "Added: 2007-03-20\n"
            + "Prefix: frm\n"
            + "Comments: 16th century French as in Jean Nicot, \"Thresor de la langue\n"
            + "  francoyse\", 1606, but also including some French similar to that of\n"
            + "  Rabelais\n"
            + "%%\n"
            + "Type: variant\n"
            + "Subtag: 1694acad\n"
            + "Description: Early Modern French\n"
            + "Added: 2007-03-20\n"
            + "Prefix: fr\n"
            + "Comments: 17th century French, as catalogued in the \"Dictionnaire de\n"
            + "  l'académie françoise\", 4eme ed. 1694; frequently includes\n"
            + "  elements of Middle French, as this is a transitional period\n";
    BufferedReader source = new BufferedReader(new StringReader(text));
    RecordJarReader subject = new RecordJarReader(source);

    assertDoesNotThrow(
        () -> {
          Iterator<Map<String, FieldValue>> it = subject.iterator();

          assertTrue(it.hasNext());
          assertEquals(
              Map.of(
                  "Type", FieldValue.of("variant"),
                  "Subtag", FieldValue.of("1606nict"),
                  "Description", FieldValue.of("Late Middle French (to 1606)"),
                  "Added", FieldValue.of("2007-03-20"),
                  "Prefix", FieldValue.of("frm"),
                  "Comments",
                      FieldValue.of(
                          "16th century French as in Jean Nicot, \"Thresor de la langue "
                              + "francoyse\", 1606, but also including some French similar to that of "
                              + "Rabelais")),
              it.next());

          assertTrue(it.hasNext());
          assertEquals(
              Map.of(
                  "Type", FieldValue.of("variant"),
                  "Subtag", FieldValue.of("1694acad"),
                  "Description", FieldValue.of("Early Modern French"),
                  "Added", FieldValue.of("2007-03-20"),
                  "Prefix", FieldValue.of("fr"),
                  "Comments",
                      FieldValue.of(
                          "17th century French, as catalogued in the \"Dictionnaire de "
                              + "l'académie françoise\", 4eme ed. 1694; frequently includes "
                              + "elements of Middle French, as this is a transitional period")),
              it.next());

          assertFalse(it.hasNext());
        });
  }

  @Nested
  class FieldValueTest {
    @Test
    void testHasMany() {
      FieldValue subject = FieldValue.of("a");
      assertFalse(subject.hasMany());

      subject.add("b");
      assertTrue(subject.hasMany());
    }

    @Test
    void testEquals() {
      FieldValue a = FieldValue.of("x", "y");
      FieldValue b = FieldValue.of("x", "y");
      FieldValue c = FieldValue.of("y", "x");

      assertEquals(a, a);
      assertEquals(a, b);
      assertEquals(b, a);
      assertNotEquals(b, c);
      assertNotEquals(c, b);
      assertNotEquals(a, c);
    }

    @Test
    void testJsonSerialization() {
      assertDoesNotThrow(
          () -> {
            JsonFactory jsonFactory = new JsonFactory();
            JsonMapper jsonMapper = new JsonMapper();
            StringWriter writer = new StringWriter();

            try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(writer)) {
              Map<String, FieldValue> a = Map.of("a", FieldValue.of("x", "y"));
              Map<String, FieldValue> b = Map.of("b", FieldValue.of("z"));

              jsonGenerator.writeStartArray();
              jsonMapper.writeValue(jsonGenerator, a);
              jsonMapper.writeValue(jsonGenerator, b);
              jsonGenerator.writeEndArray();
              jsonGenerator.flush();

              assertEquals("[{\"a\":[\"x\",\"y\"]},{\"b\":\"z\"}]", writer.toString());
            }
          });
    }
  }
}
