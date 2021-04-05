package com.bryandragon.langdb.load.sources;

import com.bryandragon.langdb.common.util.LexString;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.bryandragon.langdb.load.Helpers.nonEmptyText;

public final class Iso639_2 {
  public static void load(File source, Connection conn) throws IOException, SQLException {
    System.out.println("Loading ISO 639-2");

    PreparedStatement insert =
        conn.prepareStatement(
            "INSERT INTO iso_639_2 (t_id, b_id, part1, name, reserved)"
                + " VALUES (?, ?, ?, ?, ?);");

    ObjectMapper objectMapper = new ObjectMapper();
    List<Map<String, String>> elems = objectMapper.readValue(source, new TypeReference<>() {});

    try {
      for (Map<String, String> elem : elems) {
        String part2B = elem.get("Part2B");
        String part2T = elem.get("Part2T");
        String part1 = nonEmptyText(elem.get("Part1"));
        String name = elem.get("English_Name");
        boolean reserved = false;

        List<String> ts;
        List<String> bs;

        // ISO 639-2 includes qaa-qtz as a reserved range for local use.
        // Here we expand the range and insert a row for each value.

        String[] range = part2T.split("-", 2);

        if (part2T.equals(part2B) && range.length == 2) {
          ts = bs = LexString.fill(range[0], range[1]);
          reserved = true;
        } else {
          ts = List.of(part2T);
          bs = List.of(part2B);
        }

        for (int i = 0; i < ts.size(); i++) {
          insert.setString(1, ts.get(i));
          insert.setString(2, bs.get(i));
          insert.setString(3, nonEmptyText(elem.get("Part1")));
          insert.setString(4, elem.get("English_Name"));
          insert.setBoolean(5, reserved);
          insert.execute();
        }
      }
    } finally {
      insert.close();
    }
  }
}
