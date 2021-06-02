package com.bryandragon.langdb.load;

import com.bryandragon.langdb.load.sources.*;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Main {
  public static void main(String[] args) {
    Properties props = new Properties();
    props.setProperty("user", System.getenv("PGUSER"));
    props.setProperty("password", System.getenv("PGPASSWORD"));
    props.setProperty("sslmode", "disable");

    String url =
        String.format(
            "jdbc:postgresql://%s:5432/%s", System.getenv("PGHOST"), System.getenv("PGDATABASE"));

    Path dataDir = Path.of(System.getProperty("dataDir"));

    try {
      try (Connection conn = DriverManager.getConnection(url, props)) {
        conn.setAutoCommit(true);

        Iso3166_1.load(dataDir.resolve("json/iso-3166-1.json").toFile(), conn);

        Iso15924.load(dataDir.resolve("json/iso-15924.json").toFile(), conn);

        Iso639_5.load(dataDir.resolve("json/iso-639-5.json").toFile(), conn);

        Iso639_3.load(dataDir.resolve("json/iso-639-3.json").toFile(), conn);

        Iso639_3.Macrolanguage.load(
            dataDir.resolve("json/iso-639-3-macrolanguages.json").toFile(), conn);

        Iso639_3.NameIndex.load(dataDir.resolve("json/iso-639-3-name-index.json").toFile(), conn);

        Iso639_3.Deprecation.load(
            dataDir.resolve("json/iso-639-3-retirements.json").toFile(), conn);

        Iso639_2.load(dataDir.resolve("json/iso-639-2.json").toFile(), conn);

        IanaLanguageSubtagRegistry.load(
            dataDir.resolve("json/language-subtag-registry.json").toFile(), conn);

        IanaLanguageTagExtensionsRegistry.load(
            dataDir.resolve("json/language-tag-extensions-registry.json").toFile(), conn);

        CldrBcp47Extensions.load(dataDir.resolve("json/cldr-bcp47-extensions.json").toFile(), conn);

        System.exit(0);
      }
    } catch (IOException | SQLException e) {
      e.printStackTrace(System.out);
      System.exit(1);
    }
  }
}
