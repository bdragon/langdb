package com.bryandragon.langdb.load.sources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public final class Iso639_5 {
  public static void load(File source, Connection conn) throws IOException, SQLException {
    System.out.println("Loading ISO 639-5");

    PreparedStatement insert =
        conn.prepareStatement("INSERT INTO iso_639_5 (id, name) VALUES (?, ?);");

    ObjectMapper objectMapper = new ObjectMapper();
    List<Map<String, String>> elems = objectMapper.readValue(source, new TypeReference<>() {});

    try {
      for (Map<String, String> elem : elems) {
        insert.setString(1, elem.get("code"));
        insert.setString(2, elem.get("Label (English)"));
        insert.execute();
        insert.clearParameters();
      }
    } finally {
      insert.close();
    }
  }
}
