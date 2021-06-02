package com.bryandragon.langdb.transform.sources;

import com.bryandragon.langdb.common.format.CsvReader;
import com.bryandragon.langdb.common.format.JsonWriter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public final class Iso639_2 {
  public static final String[] JSON_FIELD_NAMES =
      new String[]{"Part2B", "Part2T", "Part1", "English_Name", "French_Name"};

  public static void toJson(Reader in, Writer out) throws IOException {
    CsvReader csvReader = new CsvReader(in, '|', false, JSON_FIELD_NAMES);
    (new JsonWriter()).writeArray(csvReader.iterator(), out);
  }
}
