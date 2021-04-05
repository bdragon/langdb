package com.bryandragon.langdb.transform.sources;

import com.bryandragon.langdb.common.domain.cldr.Attribute;
import com.bryandragon.langdb.common.domain.cldr.Key;
import com.bryandragon.langdb.common.domain.cldr.LdmlBcp47;
import com.bryandragon.langdb.common.domain.cldr.Version;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.xml.stream.*;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CldrBcp47Extensions {
  private static final String DTD_PATH = "common/dtd/ldmlBCP47.dtd";

  /**
   * Transforms a directory of LDML XML files, which contain valid attributes, keys, and types for
   * BCP 47 extension U and T, into a single JSON file.
   *
   * <p>A single file is emitted because, per UTS #35, "The ldmlBCP47 files and supplementalData
   * files that have the same root are all logically part of the same file; they are simply split
   * into separate files for convenience."
   *
   * @see <a href="https://unicode.org/reports/tr35/#Unicode_Locale_Extension_Data_Files">Unicode
   *     Technical Standard #35</a>
   */
  public static void toJson(Path cldrDir, BufferedWriter out)
      throws IOException, XMLStreamException {
    System.out.println("Processing CLDR BCP 47 Extensions");

    File[] files =
        cldrDir.resolve("common/bcp47").toFile().listFiles((dir, name) -> name.endsWith(".xml"));

    if (files == null) {
      throw new IllegalArgumentException("CLDR directory is invalid or empty");
    }

    // The LDML XML files reference a DTD file with a relative path, which does not work
    // when invoked from Java. Here we resolve the referenced DTD file with an absolute path.
    XMLResolver xmlResolver =
        (publicId, systemId, baseURI, namespace) -> {
          if (systemId != null && systemId.endsWith(DTD_PATH)) {
            try {
              return new FileInputStream(cldrDir.resolve(DTD_PATH).toFile());
            } catch (FileNotFoundException e) {
              e.printStackTrace(System.err);
            }
          }
          return null;
        };

    XmlMapper xmlMapper = new XmlMapper();
    XMLInputFactory xmlFactory = XMLInputFactory.newFactory();

    // DTD resolution can be skipped altogether by uncommenting the next line.
    // xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    xmlFactory.setXMLResolver(xmlResolver);

    JsonMapper jsonMapper = new JsonMapper();
    JsonFactory jsonFactory = JsonFactory.builder().build();
    LdmlBcp47 combinedElem = new LdmlBcp47();
    Version version = null;
    List<Key> keys = new ArrayList<>();
    List<Attribute> attributes = new ArrayList<>();

    try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(out)) {
      for (File file : files) {
        XMLStreamReader xmlStreamReader =
            xmlFactory.createXMLStreamReader(new FileInputStream(file));
        LdmlBcp47 elem;

        try {
          // Advance to root element, <ldmlBCP47>.
          while (xmlStreamReader.getEventType() != XMLStreamConstants.START_ELEMENT
              && xmlStreamReader.hasNext()) {
            xmlStreamReader.next();
          }

          elem = xmlMapper.readValue(xmlStreamReader, LdmlBcp47.class);
        } finally {
          xmlStreamReader.close();
        }

        if (version == null) {
          version = elem.getVersion();
        }

        for (Key key : elem.getKeys()) {
          // The extension attribute is set for all Extension T keys.
          // If the extension attribute is missing, we know this is an Extension U key.
          if (key.getExtension() == null) {
            key.setExtension("u");
          }

          keys.add(key);
        }

        attributes.addAll(elem.getAttributes());
      }

      combinedElem.setVersion(version);
      combinedElem.setKeys(keys);
      combinedElem.setAttributes(attributes);

      jsonMapper.writeValue(jsonGenerator, combinedElem);
    }
  }
}
