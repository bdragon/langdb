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
    System.out.println("Loading ISO 639-3");

    PreparedStatement insert =
        conn.prepareStatement(
            "INSERT INTO iso_639_3 (id, part2t, part1, scope, type, name, comment)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?);");

    ObjectMapper objectMapper = new ObjectMapper();
    List<Map<String, String>> elems = objectMapper.readValue(source, new TypeReference<>() {});

    try {
      for (Map<String, String> elem : elems) {
        insert.setString(1, elem.get("Id"));
        insert.setString(2, nonEmptyText(elem.get("Part2T")));
        insert.setString(3, nonEmptyText(elem.get("Part1")));
        insert.setString(4, SCOPE.get(elem.get("Scope")));
        insert.setString(5, TYPE.get(elem.get("Language_Type")));
        insert.setString(6, elem.get("Ref_Name"));
        insert.setString(7, nonEmptyText(elem.get("Comment")));
        insert.execute();
      }
    } finally {
      insert.close();
    }
  }

  public static final class NameIndex {
    public static void load(File source, Connection conn) throws IOException, SQLException {
      System.out.println("Loading ISO 639-3 name index");

      PreparedStatement insert =
          conn.prepareStatement(
              "INSERT INTO iso_639_3_name (id, print, inverted) VALUES (?, ?, ?);");

      ObjectMapper objectMapper = new ObjectMapper();
      List<Map<String, String>> elems = objectMapper.readValue(source, new TypeReference<>() {});

      try {
        for (Map<String, String> elem : elems) {
          insert.setString(1, elem.get("Id"));
          insert.setString(2, elem.get("Print_Name"));
          insert.setString(3, elem.get("Inverted_Name"));
          insert.execute();
        }
      } finally {
        insert.close();
      }
    }
  }

  public static final class Macrolanguage {
    public static void load(File source, Connection conn) throws IOException, SQLException {
      System.out.println("Loading ISO 639-3 macrolanguages");

      PreparedStatement insert =
          conn.prepareStatement("INSERT INTO iso_639_3_macrolanguage (m_id, i_id) VALUES (?, ?);");

      ObjectMapper objectMapper = new ObjectMapper();
      List<Map<String, String>> elems = objectMapper.readValue(source, new TypeReference<>() {});

      try {
        for (Map<String, String> elem : elems) {
          insert.setString(1, elem.get("M_Id"));
          insert.setString(2, elem.get("I_Id"));
          insert.execute();
        }
      } finally {
        insert.close();
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
      System.out.println("Loading ISO 639-3 deprecations");

      PreparedStatement insert =
          conn.prepareStatement(
              "INSERT INTO iso_639_3_deprecation (id, reason, change_to, remedy, effective_on)"
                  + " VALUES (?, ?, ?, ?, ?);");

      ObjectMapper objectMapper = new ObjectMapper();
      List<Map<String, String>> elems = objectMapper.readValue(source, new TypeReference<>() {});

      try {
        for (Map<String, String> elem : elems) {
          insert.setString(1, elem.get("Id"));
          insert.setString(2, REASON.get(elem.get("Ret_Reason")));
          insert.setString(3, elem.get("Change_To"));
          insert.setString(4, elem.get("Ret_Remedy"));
          insert.setDate(5, nonEmptyDate(elem.get("Effective")));
          insert.execute();
        }
      } finally {
        insert.close();
      }
    }
  }
}
