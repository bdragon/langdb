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
    PreparedStatement stmt =
        conn.prepareStatement("INSERT INTO iso_639_5 (id, name) VALUES (?, ?);");

    ObjectMapper objectMapper = new ObjectMapper();
    List<Map<String, String>> elems = objectMapper.readValue(source, new TypeReference<>() {});

    try {
      System.out.println("Loading ISO 639-5...");

      for (Map<String, String> elem : elems) {
        System.out.println(elem.get("code"));

        stmt.setString(1, elem.get("code"));
        stmt.setString(2, elem.get("Label (English)"));
        stmt.execute();
        stmt.clearParameters();
      }

      System.out.println();
    } finally {
      stmt.close();
    }
  }
}
