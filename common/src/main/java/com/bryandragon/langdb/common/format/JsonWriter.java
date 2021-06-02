package com.bryandragon.langdb.common.format;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

public class JsonWriter {
  private static final int BATCH_SIZE = 100;

  public JsonWriter() {}

  /**
   * Consumes {@code it} and writes items as a JSON array to {@code writer}, flushing periodically,
   * and closes it.
   */
  public <T> void writeArray(Iterator<T> it, Writer writer) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonGenerator generator = (new JsonFactory()).createGenerator(writer);
    int pending = 0;

    generator.writeStartArray();

    while (it.hasNext()) {
      T item = it.next();

      mapper.writeValue(generator, item);

      if (++pending == BATCH_SIZE) {
        pending = 0;
        generator.flush();
      }
    }

    generator.writeEndArray();
    generator.close();
    writer.close();
  }
}
