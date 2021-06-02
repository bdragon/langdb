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
import java.util.Set;
import java.util.stream.Collectors;

import static com.bryandragon.langdb.load.Helpers.nonEmptyText;

public final class UnM49 {
  public static void load(File source, Connection conn) throws IOException, SQLException {
    PreparedStatement insertRegion =
        conn.prepareStatement("INSERT INTO un_m49_region (id, name) VALUES (?, ?);");

    PreparedStatement insertCountryOrArea =
        conn.prepareStatement(
            "INSERT INTO un_m49_country_area (id, region, alpha2, name) VALUES (?, ?, ?, ?)");

    ObjectMapper objectMapper = new ObjectMapper();

    List<Map<String, String>> countriesOrAreas =
        objectMapper.readValue(source, new TypeReference<>() {});

    Set<Map.Entry<String, String>> regions =
        countriesOrAreas.stream()
            .map(c -> Map.entry(c.get("Region Code"), c.get("Region Name")))
            .collect(Collectors.toSet());

    try {
      System.out.println("Loading UN M49 regions ...");

      for (Map.Entry<String, String> elem : regions) {
        System.out.println(elem.getKey());

        insertRegion.setString(1, elem.getKey());
        insertRegion.setString(2, elem.getValue());
        insertRegion.execute();
        insertRegion.clearParameters();
      }

      System.out.println();

      System.out.println("Loading UN M49 countries/areas ...");

      for (Map<String, String> elem : countriesOrAreas) {
        System.out.println(elem.get("M49 Code"));

        insertCountryOrArea.setString(1, elem.get("M49 Code"));
        insertCountryOrArea.setString(2, elem.get("Region Code"));
        insertCountryOrArea.setString(3, nonEmptyText(elem.get("ISO-alpha2 Code")));
        insertCountryOrArea.setString(4, elem.get("Country or Area"));
        insertCountryOrArea.execute();
        insertCountryOrArea.clearParameters();
      }

      System.out.println();
    } finally {
      insertRegion.close();
      insertCountryOrArea.close();
    }
  }
}
