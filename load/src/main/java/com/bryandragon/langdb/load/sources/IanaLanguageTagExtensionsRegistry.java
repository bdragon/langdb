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

import static com.bryandragon.langdb.load.Helpers.nonEmptyDate;
import static com.bryandragon.langdb.load.Helpers.nonEmptyText;

public final class IanaLanguageTagExtensionsRegistry {
  public static void load(File source, Connection conn) throws IOException, SQLException {
    System.out.println("Loading IANA Language Tag Extensions Registry");

    PreparedStatement insert =
        conn.prepareStatement(
            "INSERT INTO subtag_ext (id, description, comments, added_on) VALUES (?, ?, ?, ?);");

    ObjectMapper objectMapper = new ObjectMapper();
    List<Map<String, String>> elems = objectMapper.readValue(source, new TypeReference<>() {});

    try {
      for (Map<String, String> elem : elems) {
        if (elem.containsKey("File-Date")) {
          continue;
        }

        String id = elem.get("Identifier");

        insert.setString(1, id);
        insert.setString(2, elem.get("Description"));
        insert.setString(3, nonEmptyText(elem.get("Comments")));
        insert.setDate(4, nonEmptyDate(elem.get("Added")));
        insert.execute();
        insert.clearParameters();
      }
    } finally {
      insert.close();
    }
  }

  /**
   * Email addresses in the IANA Language Tag Extensions Registry file are invalid for some reason,
   * e.g., "cldr-contact&unicode.org". This method fixes them.
   */
  private static String fixEmail(String email) {
    if (!email.contains("@") && email.contains("&")) {
      return email.replaceFirst("&", "@");
    }
    return email;
  }
}
