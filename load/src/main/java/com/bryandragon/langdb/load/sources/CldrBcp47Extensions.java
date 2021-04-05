package com.bryandragon.langdb.load.sources;

import com.bryandragon.langdb.common.domain.cldr.Attribute;
import com.bryandragon.langdb.common.domain.cldr.Key;
import com.bryandragon.langdb.common.domain.cldr.LdmlBcp47;
import com.bryandragon.langdb.common.domain.cldr.Type;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.bryandragon.langdb.load.Helpers.nonEmptyText;

public final class CldrBcp47Extensions {
  public static void load(File source, Connection conn) throws IOException, SQLException {
    System.out.println("Loading Unicode CLDR BCP 47 Extensions");

    PreparedStatement insertKey =
        conn.prepareStatement(
            "INSERT INTO subtag_ext_key ("
                + "  ext_id, id, description, deprecated, preferred, alias, value_type"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?);");

    PreparedStatement insertType =
        conn.prepareStatement(
            "INSERT INTO subtag_ext_key_type ("
                + "  ext_id, key_id, id, description, deprecated, preferred, alias"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?);");

    PreparedStatement insertAttribute =
        conn.prepareStatement(
            "INSERT INTO subtag_ext_attr ("
                + "  id, description, deprecated, preferred"
                + ") VALUES (?, ?, ?, ?);");

    ObjectMapper objectMapper = new ObjectMapper();
    LdmlBcp47 elem = objectMapper.readValue(source, new TypeReference<>() {});

    try {
      for (Key key : elem.getKeys()) {
        insertKey.setString(1, key.getExtension());
        insertKey.setString(2, key.getName());
        insertKey.setString(3, nonEmptyText(key.getDescription()));
        insertKey.setBoolean(4, key.getDeprecated());
        insertKey.setString(5, nonEmptyText(key.getPreferred()));
        insertKey.setString(6, nonEmptyText(key.getAlias()));
        insertKey.setString(7, nonEmptyText(key.getValueType()));
        insertKey.execute();
        insertKey.clearParameters();

        for (Type type : key.getTypes()) {
          insertType.setString(1, key.getExtension());
          insertType.setString(2, key.getName());
          insertType.setString(3, type.getName());
          insertType.setString(4, nonEmptyText(type.getDescription()));
          insertType.setBoolean(5, type.getDeprecated());
          insertType.setString(6, nonEmptyText(key.getPreferred()));
          insertType.setString(7, nonEmptyText(key.getAlias()));
          insertType.execute();
          insertType.clearParameters();
        }
      }

      for (Attribute attribute : elem.getAttributes()) {
        insertAttribute.setString(1, attribute.getName());
        insertAttribute.setString(2, nonEmptyText(attribute.getDescription()));
        insertAttribute.setBoolean(3, attribute.getDeprecated());
        insertAttribute.setString(4, nonEmptyText(attribute.getPreferred()));
        insertAttribute.execute();
        insertAttribute.clearParameters();
      }
    } finally {
      insertKey.close();
      insertType.close();
      insertAttribute.close();
    }
  }
}
