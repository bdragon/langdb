package com.bryandragon.langdb.transform.sources;

import com.bryandragon.langdb.common.format.CsvReader;
import com.bryandragon.langdb.common.format.JsonWriter;
import com.bryandragon.langdb.common.util.DecoratingIterator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class Iso639_2 {
  public static final String[] JSON_FIELD_NAMES =
      new String[] {"Part2B", "Part2T", "Part1", "English_Name", "French_Name"};

  public static void toJson(Reader in, Writer out) throws IOException {
    System.out.println("Processing ISO 639-2");

    CsvReader csvReader = new CsvReader(in, '|', false, JSON_FIELD_NAMES);

    Iterator<Map<String, String>> it =
        new DecoratingIterator<>(
            csvReader.iterator(),
            item -> {
              // For records having the same value for their B and T codes, the Part2T field is
              // empty in the source data. Here we ensure it is populated with the value of Part2B.
              if (item.get("Part2T").isEmpty()) {
                HashMap<String, String> newItem = new HashMap<>(item);
                newItem.put("Part2T", item.get("Part2B"));
                return newItem;
              }
              return item;
            });

    (new JsonWriter()).writeArray(it, out);
  }
}
