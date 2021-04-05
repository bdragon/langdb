package com.bryandragon.langdb.common.format;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.*;

/**
 * Parses text in record-jar format and produces an iterator of parsed records. Closes the
 * underlying reader when it has finished reading.
 *
 * @see <a
 *     href="https://tools.ietf.org/html/draft-phillips-record-jar-01">draft-phillips-record-jar-01</a>
 */
public class RecordJarReader {
  private final Reader source;

  public RecordJarReader(Reader source) {
    this.source = source;
  }

  public Iterator<Map<String, FieldValue>> iterator() {
    return new RecordIterator(this.source);
  }

  static class RecordIterator implements Iterator<Map<String, FieldValue>> {
    private final BufferedReader source;
    private Map<String, FieldValue> peekedRecord;
    private String prevLine, nextLine;
    private boolean eof;

    public RecordIterator(Reader source) {
      this(
          (source instanceof BufferedReader)
              ? (BufferedReader) source
              : new BufferedReader(source));
    }

    public RecordIterator(BufferedReader source) {
      this.source = source;
      eof = false;
    }

    @Override
    public boolean hasNext() {
      try {
        return (peekRecord() != null);
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }
      return false;
    }

    @Override
    public Map<String, FieldValue> next() {
      try {
        return nextRecord();
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }
      return null;
    }

    private Map<String, FieldValue> nextRecord() throws IOException {
      Map<String, FieldValue> record = peekRecord();
      peekedRecord = null;
      return record;
    }

    private Map<String, FieldValue> peekRecord() throws IOException {
      if (peekedRecord == null) {
        peekedRecord = parseRecord();
      }
      return peekedRecord;
    }

    private Map<String, FieldValue> parseRecord() throws IOException {
      if (eof) {
        return null;
      }

      Map<String, FieldValue> record = new HashMap<>();
      String line;

      while ((line = readLine()) != null) {
        if (line.equals("%%")) { // Record separator.
          break;
        }

        if (line.isEmpty() || line.startsWith("%% ")) { // Empty line or comment.
          continue;
        }

        String[] field = line.split(":", 2);
        String name = field[0].stripTrailing();
        String body = field[1].stripLeading();
        String bodyJoiner;
        boolean explicitContinuation = false;

        if (body.endsWith("\\")) { // Line continuation character.
          explicitContinuation = true;
          body = body.substring(0, body.length() - 1);
          bodyJoiner = "";
        } else {
          body = body.stripTrailing();
          bodyJoiner = " ";
        }

        // Consume any continuation lines.
        while (true) {
          String nextLine = readLine();
          if (nextLine == null) {
            break;
          }
          if (explicitContinuation && nextLine.isBlank()) {
            // This is technically illegal, according to the linked Internet-Draft,
            // but we're going to skip blank continuation lines.
          }
          if (nextLine.isEmpty() || nextLine.startsWith("%% ")) { // Empty line or comment.
            continue;
          }
          if (!Character.isWhitespace(nextLine.codePointAt(0))) {
            unreadLine();
            break;
          }
          body = String.join(bodyJoiner, body, nextLine.stripLeading());
        }

        if (record.containsKey(name)) {
          record.get(name).add(body);
        } else {
          record.put(name, FieldValue.of(body));
        }
      }

      if (!record.isEmpty()) {
        return record;
      }

      return null;
    }

    private String readLine() throws IOException {
      if (eof) {
        return null;
      }
      if (nextLine != null) {
        prevLine = nextLine;
        nextLine = null;
      } else {
        prevLine = source.readLine();
        if (prevLine == null) {
          eof = true;
          source.close();
        }
      }
      return prevLine;
    }

    private void unreadLine() {
      nextLine = prevLine;
      prevLine = null;
    }
  }

  /**
   * Facilitates handling multiple field values for the same field name, as a record
   * may contain the same field name multiple times.
   */
  @JsonSerialize(using = FieldValue.FieldValueSerializer.class)
  public static final class FieldValue implements Serializable {
    private final List<String> values = new ArrayList<>();

    public static FieldValue of(String s1) {
      FieldValue f = new FieldValue();
      f.add(s1);
      return f;
    }

    public static FieldValue of(String s1, String s2) {
      FieldValue f = new FieldValue();
      f.add(s1);
      f.add(s2);
      return f;
    }

    FieldValue() {}

    void add(String value) {
      values.add(value);
    }

    boolean hasMany() {
      return values.size() > 1;
    }

    String getString() {
      if (!values.isEmpty()) {
        return values.get(0);
      }
      return null;
    }

    List<String> getList() {
      return Collections.unmodifiableList(values);
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof FieldValue)) {
        return false;
      }
      FieldValue other = (FieldValue) o;
      return getList().equals(other.getList());
    }

    @Override
    public int hashCode() {
      int result = 1;
      for (String value : values) {
        result = 31 * result + (value == null ? 0 : value.hashCode());
      }
      return result;
    }

    /** Serializes a field value with one item as a string, with more than one as an array. */
    public static final class FieldValueSerializer extends JsonSerializer<FieldValue> {
      @Override
      public void serialize(
          FieldValue value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
          throws IOException {
        if (value.hasMany()) {
          jsonGenerator.writeStartArray();
          for (String s : value.getList()) {
            jsonGenerator.writeString(s);
          }
          jsonGenerator.writeEndArray();
        } else {
          jsonGenerator.writeString(value.getString());
        }
      }
    }
  }
}
