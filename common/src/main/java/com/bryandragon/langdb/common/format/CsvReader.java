package com.bryandragon.langdb.common.format;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Map;

public class CsvReader {
  protected final Reader source;
  protected final CsvSchema schema;

  public static CsvReader tsv(Reader source) {
    return new CsvReader(source, '\t');
  }

  public CsvReader(Reader source, char separator) {
    this(source, separator, true, null);
  }

  public CsvReader(Reader source, char separator, boolean hasHeader, String[] columnNames) {
    this.source = source;

    CsvSchema.Builder builder =
        CsvSchema.builder().setColumnSeparator(separator).setUseHeader(hasHeader);

    if (columnNames != null) {
      builder.addColumns(Arrays.asList(columnNames), CsvSchema.ColumnType.STRING);
    }

    this.schema = builder.build();
  }

  public MappingIterator<Map<String, String>> iterator() throws IOException {
    return (new CsvMapper()).readerFor(Map.class).with(this.schema).readValues(this.source);
  }
}
