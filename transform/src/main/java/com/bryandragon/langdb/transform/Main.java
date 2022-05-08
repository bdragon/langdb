package com.bryandragon.langdb.transform;

import com.bryandragon.langdb.transform.sources.*;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static com.bryandragon.langdb.transform.Helpers.skipBomUtf8;

public final class Main {
  public static void main(String[] args) {
    Path dataDir = Path.of(System.getProperty("dataDir"));

    try {
      File jsonDir = dataDir.resolve("json").toFile();
      jsonDir.mkdirs();

      Iso639_2.toJson(
          utf8Reader(dataDir.resolve("raw/ISO-639-2_utf-8.txt")),
          utf8Writer(dataDir.resolve("json/iso-639-2.json")));

      Iso639_3.toJson(
          utf8Reader(dataDir.resolve("raw/iso-639-3.tab")),
          utf8Writer(dataDir.resolve("json/iso-639-3.json")));

      Iso639_3.NameIndex.toJson(
          utf8Reader(dataDir.resolve("raw/iso-639-3_Name_Index.tab")),
          utf8Writer(dataDir.resolve("json/iso-639-3-name-index.json")));

      Iso639_3.Macrolanguages.toJson(
          utf8Reader(dataDir.resolve("raw/iso-639-3-macrolanguages.tab")),
          utf8Writer(dataDir.resolve("json/iso-639-3-macrolanguages.json")));

      Iso639_3.Retirements.toJson(
          utf8Reader(dataDir.resolve("raw/iso-639-3_Retirements.tab")),
          utf8Writer(dataDir.resolve("json/iso-639-3-retirements.json")));

      Iso639_5.toJson(
          utf8Reader(dataDir.resolve("raw/iso639-5.tsv")),
          utf8Writer(dataDir.resolve("json/iso-639-5.json")));

      Iso15924.toJson(
          utf8Reader(dataDir.resolve("raw/iso15924.txt")),
          utf8Writer(dataDir.resolve("json/iso-15924.json")));

      IanaLanguageSubtagRegistry.toJson(
          utf8Reader(dataDir.resolve("raw/language-subtag-registry")),
          utf8Writer(dataDir.resolve("json/language-subtag-registry.json")));

      IanaLanguageTagExtensionsRegistry.toJson(
          utf8Reader(dataDir.resolve("raw/language-tag-extensions-registry")),
          utf8Writer(dataDir.resolve("json/language-tag-extensions-registry.json")));

      CldrBcp47Extensions.toJson(
          dataDir.resolve("raw/cldr"),
          utf8Writer(dataDir.resolve("json/cldr-bcp47-extensions.json")));

      System.exit(0);
    } catch (IOException | XMLStreamException e) {
      e.printStackTrace(System.err);
      System.exit(1);
    }
  }

  /**
   * Returns a reader for the UTF-8-encoded file located at {@code path}, skipping BOM if present.
   */
  private static BufferedReader utf8Reader(Path path) throws IOException {
    BufferedInputStream is = new BufferedInputStream(new FileInputStream(path.toFile()));
    return new BufferedReader(new InputStreamReader(skipBomUtf8(is), StandardCharsets.UTF_8));
  }

  /**
   * Returns a writer for the UTF-8-encoded file located at {@code path}, creating it if necessary.
   */
  private static BufferedWriter utf8Writer(Path path) throws IOException {
    File file = path.toFile();
    if (!file.exists()) {
      file.createNewFile();
    }
    return new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8));
  }
}
