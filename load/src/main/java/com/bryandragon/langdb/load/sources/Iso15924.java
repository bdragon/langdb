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

import static com.bryandragon.langdb.load.Helpers.nonEmptyDate;
import static com.bryandragon.langdb.load.Helpers.nonEmptyText;

public final class Iso15924 {
  private static final String PRIVATE_USE_START = "Qaaa";
  private static final String PRIVATE_USE_END = "Qabx";

  public static void load(File source, Connection conn) throws IOException, SQLException {
    System.out.println("Loading ISO 15924");

    PreparedStatement insert =
        conn.prepareStatement(
            "INSERT INTO iso_15924 (alpha, num, name, pva, added_on, reserved)"
                + " VALUES (?, ?, ?, ?, ?, ?);");

    ObjectMapper objectMapper = new ObjectMapper();
    List<Map<String, String>> elems = objectMapper.readValue(source, new TypeReference<>() {});

    try {
      for (Map<String, String> elem : elems) {
        String startCode = elem.get("code");
        String name = elem.get("englishName");
        int num = Integer.parseInt(elem.get("number"));
        boolean reserved = false;

        List<String> codes;

        // Qaaa..Qabx are reserved for private use.
        // Here we expand the range and insert a row for each value.
        if (PRIVATE_USE_START.equals(startCode)) {
          codes = LexString.fill(PRIVATE_USE_START, PRIVATE_USE_END);
          reserved = true;

          // "Reserved for private use (start)"
          //                  Removes ^^^^^^^^.
          name = name.replaceFirst(" \\(start\\)$", "");
        } else if (PRIVATE_USE_END.equals(startCode)) {
          // We processed the entire range when we encountered the range start, so skip range end.
          continue;
        } else {
          codes = List.of(startCode);
        }

        for (String code : codes) {
          insert.setString(1, code);
          insert.setString(2, String.format("%1$3d", num).replace(" ", "0"));
          insert.setString(3, name);
          insert.setString(4, nonEmptyText(elem.get("pva")));
          insert.setDate(5, nonEmptyDate(elem.get("date")));
          insert.setBoolean(6, reserved);
          insert.execute();

          insert.clearParameters();
          num++;
        }
      }
    } finally {
      insert.close();
    }
  }
}
