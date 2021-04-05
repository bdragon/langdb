package com.bryandragon.langdb.load.sources;

import com.bryandragon.langdb.common.util.LexString;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.bryandragon.langdb.load.Helpers.nonEmptyDate;
import static com.bryandragon.langdb.load.Helpers.nonEmptyText;

public final class IanaLanguageSubtagRegistry {
  public static void load(File source, Connection conn) throws IOException, SQLException {
    System.out.println("Loading IANA Language Subtag Registry");

    PreparedStatement insertSubtag =
        conn.prepareStatement(
            "INSERT INTO subtag ("
                + "  type, id, description, added_on, deprecated_on,"
                + "  preferred_value, suppress_script, macrolanguage, scope, comments"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

    PreparedStatement insertPrefix =
        conn.prepareStatement(
            "INSERT INTO subtag_prefix (subtag_type, subtag_id, prefix) VALUES (?, ?, ?);");

    ObjectMapper objectMapper = new ObjectMapper();
    List<Map<String, Object>> elems = objectMapper.readValue(source, new TypeReference<>() {});

    try {
      for (Map<String, Object> elem : elems) {
        if (elem.containsKey("File-Date")) {
          continue;
        }

        String type = (String) elem.get("Type");
        List<String> ids;
        String scope;

        if (!elem.containsKey("Scope") && (type.equals("language") || type.equals("extlang"))) {
          scope = "individual";
        } else {
          scope = (String) elem.get("Scope");
        }

        if (type.equals("grandfathered") || type.equals("redundant")) {
          String tag = (String) elem.get("Tag");
          ids = List.of(tag);
        } else {
          String subtag = (String) elem.get("Subtag");
          String[] range = subtag.split("\\.\\.", 2);
          if (range.length == 2) {
            ids = LexString.fill(range[0], range[1]);
          } else {
            ids = List.of(subtag);
          }
        }

        for (String id : ids) {
          insertSubtag.setString(1, type);
          insertSubtag.setString(2, id);

          // Description may be a string or a list of strings.
          String description;
          if (elem.get("Description") instanceof List) {
            description = String.join("; ", castList(String.class, (List<?>) elem.get("Description")));
          } else {
           description = (String) elem.get("Description");
          }
          insertSubtag.setString(3, nonEmptyText(description));

          insertSubtag.setDate(4, nonEmptyDate((String) elem.get("Added")));
          insertSubtag.setDate(5, nonEmptyDate((String) elem.get("Deprecated")));
          insertSubtag.setString(6, nonEmptyText((String) elem.get("Preferred-Value")));
          insertSubtag.setString(7, nonEmptyText((String) elem.get("Suppress-Script")));
          insertSubtag.setString(8, nonEmptyText((String) elem.get("Macrolanguage")));
          insertSubtag.setString(9, scope);
          insertSubtag.setString(10, nonEmptyText((String) elem.get("Comments")));
          insertSubtag.execute();
          insertSubtag.clearParameters();
        }

        // Prefix may be a string or a list of strings.
        if (elem.containsKey("Prefix")) {
          String id = ids.get(0);
          List<String> prefixes;

          if (elem.get("Prefix") instanceof List) {
            prefixes = castList(String.class, (List<?>) elem.get("Prefix"));
          } else {
            prefixes = List.of((String) elem.get("Prefix"));
          }

          System.out.printf("%s - inserting %d prefixes\n", id, prefixes.size());

          for (String prefix : prefixes) {
            insertPrefix.setString(1, type);
            insertPrefix.setString(2, id);
            insertPrefix.setString(3, prefix);
            insertPrefix.execute();
            insertPrefix.clearParameters();
          }
        }
      }
    } finally {
      insertSubtag.close();
      insertPrefix.close();
    }
  }

  private static <T> List<T> castList(Class<? extends T> clazz, List<?> list)
      throws ClassCastException {
    List<T> result = new ArrayList<>(list.size());
    for (Object o : list) {
      result.add(clazz.cast(o));
    }
    return result;
  }
}
