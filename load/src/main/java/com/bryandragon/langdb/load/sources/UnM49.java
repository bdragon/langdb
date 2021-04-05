package com.bryandragon.langdb.load.sources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bryandragon.langdb.load.Helpers.nonEmptyText;

public final class UnM49 {
  public static void load(File source, Connection conn) throws IOException, SQLException {
    System.out.println("Loading UN M49");

    PreparedStatement insert =
        conn.prepareStatement(
            "INSERT INTO un_m49 (type, id, name, parent_id, iso_alpha2)"
                + " VALUES (?, ?, ?, ?, ?)");

    ObjectMapper objectMapper = new ObjectMapper();

    List<Map<String, String>> entries = objectMapper.readValue(source, new TypeReference<>() {});

    // Build lookup table of Type -> id -> Record.
    Map<Type, Map<String, Record>> lookup = new HashMap<>();
    Arrays.stream(Type.values()).forEach(type -> lookup.put(type, new HashMap<>()));

    try {
      for (Map<String, String> entry : entries) {
        // Add country or area record to lookup.
        Record record =
            getOrPut(
                lookup, Type.COUNTRY_OR_AREA, entry.get("M49 Code"), entry.get("Country or Area"));
        record.setIsoAlpha2(entry.get("ISO-alpha2 Code"));

        // Set intermediate region id as parent_id on current record ('country or area'),
        // and ensure intermediate region record is present in lookup.
        String intRegionCode = entry.get("Intermediate Region Code");
        if (!intRegionCode.isEmpty()) {
          record.setParentId(intRegionCode);
          record =
              getOrPut(
                  lookup,
                  Type.INTERMEDIATE_REGION,
                  intRegionCode,
                  entry.get("Intermediate Region Name"));
        }

        // Set sub-region id as parent_id on current record ('intermediate region' or
        // 'country or area'), and ensure sub-region record is present in lookup.
        String subRegionCode = entry.get("Sub-region Code");
        if (!subRegionCode.isEmpty()) {
          record.setParentId(subRegionCode);
          record = getOrPut(lookup, Type.SUB_REGION, subRegionCode, entry.get("Sub-region Name"));
        }

        // Set region id as parent_id on current record ('sub-region', 'intermediate region', or
        // 'country or area'), and ensure region record is present in lookup.
        String regionCode = entry.get("Region Code");
        if (!regionCode.isEmpty()) {
          record.setParentId(regionCode);
          record = getOrPut(lookup, Type.REGION, regionCode, entry.get("Region Name"));
        }

        // Set global id as parent_id on current record ('region', 'sub-region',
        // 'intermediate region', or 'country or area'), and ensure global record is present
        // in lookup.
        String globalCode = entry.get("Global Code");
        record.setParentId(globalCode);
        getOrPut(lookup, Type.GLOBAL, globalCode, entry.get("Global Name"));
      }

      // Insert a row for each record in Type enum order (global before region, region before
      // sub-region, and so on).
      for (Type type : Type.values()) {
        Map<String, Record> byId = lookup.get(type);

        for (Record record : byId.values()) {
          insert.setString(1, type.getId());
          insert.setString(2, record.getId());
          insert.setString(3, record.getName());
          insert.setString(4, nonEmptyText(record.getParentId()));
          insert.setString(5, nonEmptyText(record.getIsoAlpha2()));
          insert.execute();
          insert.clearParameters();
        }
      }
    } finally {
      insert.close();
    }
  }

  public enum Type {
    GLOBAL("global"),
    REGION("region"),
    SUB_REGION("sub-region"),
    INTERMEDIATE_REGION("intermediate region"),
    COUNTRY_OR_AREA("country or area");

    private final String id;

    Type(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }
  }

  /** Ensures a Record with id, type, and name is present in map[type] and returns it. * */
  private static Record getOrPut(
      Map<Type, Map<String, Record>> lookup, Type type, String id, String name) {
    Map<String, Record> byId = lookup.get(type);
    if (!byId.containsKey(id)) {
      byId.put(id, new Record(type, id, name));
    }
    return byId.get(id);
  }

  private static final class Record {
    private final Type type;
    private final String id;
    private final String name;
    private String parentId = null;
    private String isoAlpha2 = null;

    public Record(Type type, String id, String name) {
      this.type = type;
      this.id = id;
      this.name = name;
    }

    Type getType() {
      return type;
    }

    String getId() {
      return id;
    }

    String getName() {
      return name;
    }

    String getParentId() {
      return parentId;
    }

    void setParentId(String parentId) {
      this.parentId = parentId;
    }

    String getIsoAlpha2() {
      return isoAlpha2;
    }

    void setIsoAlpha2(String isoAlpha2) {
      this.isoAlpha2 = isoAlpha2;
    }
  }
}
