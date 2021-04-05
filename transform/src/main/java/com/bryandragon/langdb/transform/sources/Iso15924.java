package com.bryandragon.langdb.transform.sources;

import com.bryandragon.langdb.common.format.CsvReader;
import com.bryandragon.langdb.common.format.JsonWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import static com.bryandragon.langdb.transform.Helpers.skipLinesWhile;

public final class Iso15924 {
  public static final String[] JSON_FIELD_NAMES =
      new String[] {"code", "number", "englishName", "frenchName", "pva", "unicodeVersion", "date"};

  public static void toJson(BufferedReader in, Writer out) throws IOException {
    System.out.println("Processing ISO 15924");

    // Skip empty lines and comments as defined by Unicode Character Database file format.
    // See: https://www.unicode.org/reports/tr44/tr44-26.html#Comments
    skipLinesWhile(in, line -> line.isEmpty() || line.startsWith("#"));

    CsvReader csvReader = new CsvReader(in, ';', false, JSON_FIELD_NAMES);
    (new JsonWriter()).writeArray(csvReader.iterator(), out);
  }
}
