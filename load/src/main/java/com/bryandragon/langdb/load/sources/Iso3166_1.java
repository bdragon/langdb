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

public class Iso3166_1 {
  public static void load(File source, Connection conn) throws IOException, SQLException {
    System.out.println("Loading ISO 3166-1");

    PreparedStatement insert =
        conn.prepareStatement("INSERT INTO iso_3166_1 (alpha2, alpha3, name) VALUES (?, ?, ?);");

    ObjectMapper objectMapper = new ObjectMapper();
    List<Map<String, String>> elems = objectMapper.readValue(source, new TypeReference<>() {});

    try {
      for (Map<String, String> elem : elems) {
        insert.setString(1, elem.get("Alpha-2 code"));
        insert.setString(2, elem.get("Alpha-3 code"));
        insert.setString(3, elem.get("English short name"));
        insert.execute();
        insert.clearParameters();
      }
    } finally {
      insert.close();
    }
  }
}
