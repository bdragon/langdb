package com.bryandragon.langdb.transform.format;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * Parses text in record-jar format and produces an iterator of parsed records.
 * Closes the underlying reader when it has finished reading.
 *
 * @see <a href="https://tools.ietf.org/html/draft-phillips-record-jar-01">draft-phillips-record-jar-01</a>
 */
public class RecordJarReader {
  private final Reader source;

  public RecordJarReader(Reader source) {
    this.source = source;
  }

  public Iterator<Map<String, String>> iterator() {
    return new RecordIterator(this.source);
  }

  static class RecordIterator implements Iterator<Map<String, String>> {
    private final BufferedReader source;
    private Map<String, String> peekedRecord;
    private String prevLine, nextLine;
    private boolean eof;

    public RecordIterator(Reader source) {
      this((source instanceof BufferedReader)
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
    public Map<String, String> next() {
      try {
        return nextRecord();
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }
      return null;
    }

    private Map<String, String> nextRecord() throws IOException {
      Map<String, String> record = peekRecord();
      peekedRecord = null;
      return record;
    }

    private Map<String, String> peekRecord() throws IOException {
      if (peekedRecord == null) {
        peekedRecord = parseRecord();
      }
      return peekedRecord;
    }

    /**
     * Parses the next record from the underlying source and returns it.
     */
    private Map<String, String> parseRecord() throws IOException {
      if (eof) {
        return null;
      }

      Stack<Map.Entry<String, String>> fields = new Stack<>();
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

        fields.push(Map.entry(name, body));
      }

      if (!fields.isEmpty()) {
        Map<String, String> record = new HashMap<>();
        for (Map.Entry<String, String> entry : fields) {
          record.put(entry.getKey(), entry.getValue());
        }
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
}
