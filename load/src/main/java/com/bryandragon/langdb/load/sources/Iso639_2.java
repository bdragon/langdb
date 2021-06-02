package com.bryandragon.langdb.load.sources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Must run after Iso639_3 runs.
 */
public final class Iso639_2 {
  public static void load(File source, Connection conn) throws IOException, SQLException {
    PreparedStatement stmt = conn.prepareStatement(
        "SELECT count(*) FROM iso_639_3 WHERE part2b = ?;"
    );

    ObjectMapper objectMapper = new ObjectMapper();
    List<Map<String, String>> elems = objectMapper.readValue(source, new TypeReference<>() {});

    try {
      System.out.println("Verifying ISO 639-2 coverage...");

      for (Map<String, String> elem : elems) {
        stmt.setString(1, elem.get("Part2B"));
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
          int n = rs.getInt(1);

          if (n == 0) {
            System.out.printf("Missing part2: %s\n", elem.get("Part2B"));
          }
        }

        rs.close();
        stmt.clearParameters();
      }

      System.out.println();
    } finally {
      stmt.close();
    }
  }
}
