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

public final class IanaLanguageSubtagRegistry {
  public static void load(File source, Connection conn) throws IOException, SQLException {
    PreparedStatement stmt = conn.prepareStatement(
        "INSERT INTO subtag (" +
            "  type, id, description, added, deprecated, preferred_value, suppress_script, " +
            "  macrolanguage, scope, comments" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

    ObjectMapper objectMapper = new ObjectMapper();
    List<Map<String, String>> elems = objectMapper.readValue(source, new TypeReference<>() {});

    try {
      System.out.println("Loading IANA Language Subtag Registry...");

      for (Map<String, String> elem : elems) {
        if (elem.containsKey("File-Date")) {
          continue;
        }

        String type = elem.get("Type");
        List<String> ids;
        String scope;

        if (!elem.containsKey("Scope") && (type.equals("language") || type.equals("extlang"))) {
          scope = "individual";
        } else {
          scope = elem.get("Scope");
        }

        if (type.equals("grandfathered") || type.equals("redundant")) {
          String tag = elem.get("Tag");
          ids = List.of(tag);
        } else {
          String subtag = elem.get("Subtag");
          String[] parts = subtag.split("\\.\\.", 2);
          if (parts.length == 2) {
            ids = LexString.fill(parts[0], parts[1]);
          } else {
            ids = List.of(subtag);
          }
        }

        for (String id : ids) {
          System.out.println(id);

          stmt.setString(1, type);
          stmt.setString(2, id);
          stmt.setString(3, nonEmptyText(elem.get("Description")));
          stmt.setDate(4, nonEmptyDate(elem.get("Added")));
          stmt.setDate(5, nonEmptyDate(elem.get("Deprecated")));
          stmt.setString(6, nonEmptyText(elem.get("Preferred-Value")));
          stmt.setString(7, nonEmptyText(elem.get("Suppress-Script")));
          stmt.setString(8, nonEmptyText(elem.get("Macrolanguage")));
          stmt.setString(9, scope);
          stmt.setString(10, nonEmptyText(elem.get("Comments")));
          stmt.execute();
          stmt.clearParameters();
        }
      }

      System.out.println();
    } finally {
      stmt.close();
    }
  }
}
