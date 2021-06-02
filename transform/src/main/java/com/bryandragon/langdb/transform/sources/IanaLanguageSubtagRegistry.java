package com.bryandragon.langdb.transform.sources;

import com.bryandragon.langdb.common.format.JsonWriter;
import com.bryandragon.langdb.common.format.RecordJarReader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public final class IanaLanguageSubtagRegistry {
  public static void toJson(Reader in, Writer out) throws IOException {
    (new JsonWriter()).writeArray((new RecordJarReader(in)).iterator(), out);
  }
}
