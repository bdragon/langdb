package com.bryandragon.langdb.common.format;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonWriterTest {
  @Test
  void testWriteArrayEmptyIterator() {
    List<Map<String, String>> items = Collections.emptyList();
    Iterator<Map<String, String>> it = items.iterator();
    StringWriter writer = new StringWriter();
    JsonWriter subject = new JsonWriter();

    assertDoesNotThrow(
        () -> {
          subject.writeArray(it, writer);
          assertEquals("[]", writer.toString());
        });
  }

  @Test
  void testWriteArray() {
    List<Map<String, String>> items = List.of(Map.of("x", "1"), Map.of("x", "2"), Map.of("x", "3"));
    Iterator<Map<String, String>> it = items.iterator();
    StringWriter writer = new StringWriter();
    JsonWriter subject = new JsonWriter();

    assertDoesNotThrow(
        () -> {
          subject.writeArray(it, writer);
          assertEquals("[{\"x\":\"1\"},{\"x\":\"2\"},{\"x\":\"3\"}]", writer.toString());
        });
  }
}
