package com.bryandragon.langdb.load.sources;

import com.bryandragon.langdb.common.domain.cldr.Key;
import com.bryandragon.langdb.common.domain.cldr.Type;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static com.bryandragon.langdb.load.Helpers.nonEmptyText;

public final class CldrBcp47Extensions {
  public static void load(File source, Connection conn) throws IOException, SQLException {
    PreparedStatement insertKey =
        conn.prepareStatement(
            "INSERT INTO subtag_ext_key ("
                + "  ext_id, id, description, deprecated, preferred, alias, value_type, since"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?);");

    PreparedStatement insertType =
        conn.prepareStatement(
            "INSERT INTO subtag_ext_key_type ("
                + "  ext_id, key_id, id, description, deprecated, preferred, alias, since"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?);");

    ObjectMapper objectMapper = new ObjectMapper();
    List<Key> keys = objectMapper.readValue(source, new TypeReference<>() {});

    try {
      System.out.println("Loading Unicode CLDR BCP 47 Extensions ...");

      for (Key key : keys) {
        System.out.printf("Key: %s\n", key.getName());

        insertKey.setString(1, key.getExtension());
        insertKey.setString(2, key.getName());
        insertKey.setString(3, key.getDescription());
        insertKey.setBoolean(4, key.getDeprecated());
        insertKey.setString(5, nonEmptyText(key.getPreferred()));
        insertKey.setString(6, nonEmptyText(key.getAlias()));
        insertKey.setString(7, nonEmptyText(key.getValueType()));
        insertKey.setString(8, nonEmptyText(key.getSince()));
        insertKey.execute();
        insertKey.clearParameters();

        for (Type type : key.getTypes()) {
          System.out.printf("Type: %s\n", type.getName());

          insertType.setString(1, key.getExtension());
          insertType.setString(2, key.getName());
          insertType.setString(3, type.getName());
          insertType.setString(4, type.getDescription());
          insertType.setBoolean(5, type.getDeprecated());
          insertType.setString(6, nonEmptyText(key.getPreferred()));
          insertType.setString(7, nonEmptyText(key.getAlias()));
          insertType.setString(8, nonEmptyText(key.getSince()));
          insertType.execute();
          insertType.clearParameters();
        }
      }

      System.out.println();
    } finally {
      insertKey.close();
      insertType.close();
    }
  }
}
