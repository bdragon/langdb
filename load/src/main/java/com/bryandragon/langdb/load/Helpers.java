package com.bryandragon.langdb.load;

import java.sql.Date;

public final class Helpers {
  public static String nonEmptyText(String value) {
    return (value == null || value.isEmpty()) ? null : value;
  }

  public static Date nonEmptyDate(String value) {
    return (value == null || value.isEmpty()) ? null : Date.valueOf(value);
  }
}
