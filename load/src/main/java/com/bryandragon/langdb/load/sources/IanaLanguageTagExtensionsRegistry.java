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
    PreparedStatement stmt =
        conn.prepareStatement(
            "INSERT INTO subtag_ext ("
                + "  id, description, comments, added, rfc, authority, contact_email, "
                + "  mailing_list, url"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");

    ObjectMapper objectMapper = new ObjectMapper();
    List<Map<String, String>> elems = objectMapper.readValue(source, new TypeReference<>() {});

    try {
      System.out.println("Loading IANA Language Tag Extensions Registry ...");

      for (Map<String, String> elem : elems) {
        if (elem.containsKey("File-Date")) {
          continue;
        }

        String id = elem.get("Identifier");
        System.out.println(id);

        stmt.setString(1, id);
        stmt.setString(2, elem.get("Description"));
        stmt.setString(3, nonEmptyText(elem.get("Comments")));
        stmt.setDate(4, nonEmptyDate(elem.get("Added")));
        stmt.setString(5, elem.get("RFC"));
        stmt.setString(6, elem.get("Authority"));
        stmt.setString(7, fixEmail(elem.get("Contact_Email")));
        stmt.setString(8, fixEmail(elem.get("Mailing_List")));
        stmt.setString(9, elem.get("URL"));
        stmt.execute();
        stmt.clearParameters();
      }

      System.out.println();
    } finally {
      stmt.close();
    }
  }

  /**
   * Email addresses in the IANA Language Tag Extensions Registry file are invalid for some reason,
   * e.g., "cldr-contact&unicode.org". This method fixes them.
   */
  private static String fixEmail(String email) {
    if (!email.contains("@") && email.contains("&")) {
      return email.replace("&", "@");
    }
    return email;
  }
}
