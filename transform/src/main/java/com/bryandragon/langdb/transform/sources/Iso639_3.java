package com.bryandragon.langdb.transform.sources;

import com.bryandragon.langdb.common.format.CsvReader;
import com.bryandragon.langdb.common.format.JsonWriter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public final class Iso639_3 {
  /** Columns: Id, Part2B, Part2T, Part1, Scope, Language_Type, Ref_Name, Comment */
  public static void toJson(Reader in, Writer out) throws IOException {
    System.out.println("Processing ISO 639-3");
    (new JsonWriter()).writeArray(CsvReader.tsv(in).iterator(), out);
  }

  public static final class Macrolanguages {
    /** Columns: M_Id, I_Id, I_Status */
    public static void toJson(Reader in, Writer out) throws IOException {
      System.out.println("Processing ISO 639-3 macrolanguages");
      (new JsonWriter()).writeArray(CsvReader.tsv(in).iterator(), out);
    }
  }

  public static final class NameIndex {
    /** Columns: Id, Print_Name, Inverted_Name */
    public static void toJson(Reader in, Writer out) throws IOException {
      System.out.println("Processing ISO 639-3 name index");
      (new JsonWriter()).writeArray(CsvReader.tsv(in).iterator(), out);
    }
  }

  public static final class Retirements {
    /** Columns: Id, Ref_Name, Ret_Reason, Change_To, Ret_Remedy, Effective */
    public static void toJson(Reader in, Writer out) throws IOException {
      System.out.println("Processing ISO 639-3 deprecations");
      (new JsonWriter()).writeArray(CsvReader.tsv(in).iterator(), out);
    }
  }
}
