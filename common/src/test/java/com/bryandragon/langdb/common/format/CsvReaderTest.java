package com.bryandragon.langdb.common.format;

import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CsvReaderTest {
  @Test
  void testEmpty() {
    Reader reader = Reader.nullReader();
    CsvReader subject = new CsvReader(reader, ',', false, new String[] {});

    assertDoesNotThrow(
        () -> {
          Iterator<Map<String, String>> it = subject.iterator();
          assertFalse(it.hasNext());
        });
  }

  @Test
  void testWithNoDataRows() {
    Reader reader = new StringReader("a,b,c\n");
    CsvReader subject = new CsvReader(reader, ',', true, null);

    assertDoesNotThrow(
        () -> {
          Iterator<Map<String, String>> it = subject.iterator();
          assertFalse(it.hasNext());
        });
  }

  @Test
  void testFirstRowIsHeader() {
    String text = "a,b,c\n" + "1,2,3\n" + "1,,\n" + ",2,\n" + ",,3\n";
    Reader reader = new StringReader(text);
    CsvReader subject = new CsvReader(reader, ',');

    assertDoesNotThrow(
        () -> {
          Iterator<Map<String, String>> it = subject.iterator();

          assertEquals(
              List.of(
                  Map.of("a", "1", "b", "2", "c", "3"),
                  Map.of("a", "1", "b", "", "c", ""),
                  Map.of("a", "", "b", "2", "c", ""),
                  Map.of("a", "", "b", "", "c", "3")),
              drain(it));
        });
  }

  @Test
  void testRenameHeaderColumns() {
    String text = "a,b,c\n" + "1,2,3\n" + "1,,\n" + ",2,\n" + ",,3\n";
    Reader reader = new StringReader(text);
    String[] newColumnNames = new String[] {"x", "y", "z"};
    CsvReader subject = new CsvReader(reader, ',', true, newColumnNames);

    assertDoesNotThrow(
        () -> {
          Iterator<Map<String, String>> it = subject.iterator();

          assertEquals(
              List.of(
                  Map.of("x", "1", "y", "2", "z", "3"),
                  Map.of("x", "1", "y", "", "z", ""),
                  Map.of("x", "", "y", "2", "z", ""),
                  Map.of("x", "", "y", "", "z", "3")),
              drain(it));
        });
  }

  @Test
  void testFirstRowIsData() {
    String text = "1,2,3\n" + "1,,\n" + ",2,\n" + ",,3\n";
    Reader reader = new StringReader(text);
    String[] newColumnNames = new String[] {"a", "b", "c"};
    CsvReader subject = new CsvReader(reader, ',', false, newColumnNames);

    assertDoesNotThrow(
        () -> {
          Iterator<Map<String, String>> it = subject.iterator();

          assertEquals(
              List.of(
                  Map.of("a", "1", "b", "2", "c", "3"),
                  Map.of("a", "1", "b", "", "c", ""),
                  Map.of("a", "", "b", "2", "c", ""),
                  Map.of("a", "", "b", "", "c", "3")),
              drain(it));
        });
  }

  static <E> List<E> drain(Iterator<E> it) {
    List<E> items = new ArrayList<>();
    it.forEachRemaining(items::add);
    return items;
  }
}
