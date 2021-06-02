package com.bryandragon.langdb.transform.sources;

import com.bryandragon.langdb.common.format.CsvReader;
import com.bryandragon.langdb.common.format.JsonWriter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public final class Iso639_5 {
  /**
   * Columns:
   *   URI, code, Label (English), Label (French)
   */
  public static void toJson(Reader in, Writer out) throws IOException {
    (new JsonWriter()).writeArray(CsvReader.tsv(in).iterator(), out);
  }
}
