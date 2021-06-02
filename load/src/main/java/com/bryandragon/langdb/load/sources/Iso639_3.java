package com.bryandragon.langdb.load.sources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.bryandragon.langdb.load.Helpers.nonEmptyText;

public final class Iso639_3 {
  public static final Map<String, String> SCOPE =
      Map.of(
          "I", "individual",
          "M", "macrolanguage",
          "S", "special");

  public static final Map<String, String> TYPE =
      Map.of(
          "A", "ancient",
          "C", "constructed",
          "E", "extinct",
          "H", "historical",
          "L", "living",
          "S", "special");

  public static void load(File source, Connection conn) throws IOException, SQLException {
    PreparedStatement stmt =
        conn.prepareStatement(
            "INSERT INTO iso_639_3 (id, part2t, part2b, part1, scope, type, name, comment) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?);");

    ObjectMapper objectMapper = new ObjectMapper();
    List<Map<String, String>> elems = objectMapper.readValue(source, new TypeReference<>() {});

    try {
      System.out.println("Loading ISO 639-3...");

      for (Map<String, String> elem : elems) {
        System.out.println(elem.get("Id"));

        stmt.setString(1, elem.get("Id"));
        stmt.setString(2, nonEmptyText(elem.get("Part2T")));
        stmt.setString(3, nonEmptyText(elem.get("Part2B")));
        stmt.setString(4, nonEmptyText(elem.get("Part1")));
        stmt.setString(5, SCOPE.get(elem.get("Scope")));
        stmt.setString(6, TYPE.get(elem.get("Language_Type")));
        stmt.setString(7, elem.get("Ref_Name"));
        stmt.setString(8, nonEmptyText(elem.get("Comment")));
        stmt.execute();
      }

      System.out.println();
    } finally {
      stmt.close();
    }
  }

  public static final class NameIndex {
    public static void load(File source, Connection conn) throws IOException, SQLException {
      PreparedStatement stmt =
          conn.prepareStatement(
              "INSERT INTO iso_639_3_name (id, print, inverted) VALUES (?, ?, ?);");

      ObjectMapper objectMapper = new ObjectMapper();
      List<Map<String, String>> elems = objectMapper.readValue(source, new TypeReference<>() {});

      try {
        System.out.println("Loading ISO 639-3 name index ...");

        for (Map<String, String> elem : elems) {
          System.out.println(elem.get("Id"));

          stmt.setString(1, elem.get("Id"));
          stmt.setString(2, elem.get("Print_Name"));
          stmt.setString(3, elem.get("Inverted_Name"));
          stmt.execute();
        }

        System.out.println();
      } finally {
        stmt.close();
      }
    }
  }

  public static final class Macrolanguage {
    public static void load(File source, Connection conn) throws IOException, SQLException {
      PreparedStatement stmt =
          conn.prepareStatement("INSERT INTO iso_639_3_macrolanguage (m_id, i_id) VALUES (?, ?);");

      ObjectMapper objectMapper = new ObjectMapper();
      List<Map<String, String>> elems = objectMapper.readValue(source, new TypeReference<>() {});

      try {
        System.out.println("Loading ISO 639-3 macrolanguages ...");

        for (Map<String, String> elem : elems) {
          System.out.printf("%s > %s\n", elem.get("M_Id"), elem.get("I_Id"));

          stmt.setString(1, elem.get("M_Id"));
          stmt.setString(2, elem.get("I_Id"));
          stmt.execute();
        }

        System.out.println();
      } finally {
        stmt.close();
      }
    }
  }

  public static final class Deprecation {
    public static final Map<String, String> REASON =
        Map.of(
            "C", "change",
            "D", "duplicate",
            "M", "merge",
            "N", "nonexistent",
            "S", "split");

    public static void load(File source, Connection conn) throws IOException, SQLException {
      PreparedStatement stmt =
          conn.prepareStatement(
              "INSERT INTO iso_639_3_deprecation (id, reason, change_to, remedy, effective) "
                  + "VALUES (?, ?, ?, ?, ?);");

      ObjectMapper objectMapper = new ObjectMapper();
      List<Map<String, String>> elems = objectMapper.readValue(source, new TypeReference<>() {});

      try {
        System.out.println("Loading ISO 639-3 deprecations ...");

        for (Map<String, String> elem : elems) {
          System.out.println(elem.get("Id"));

          stmt.setString(1, elem.get("Id"));
          stmt.setString(2, REASON.get(elem.get("Ret_Reason")));
          stmt.setString(3, elem.get("Change_To"));
          stmt.setString(4, elem.get("Ret_Remedy"));
          stmt.setDate(5, Date.valueOf(elem.get("Effective")));
          stmt.execute();
        }

        System.out.println();
      } finally {
        stmt.close();
      }
    }
  }
}
