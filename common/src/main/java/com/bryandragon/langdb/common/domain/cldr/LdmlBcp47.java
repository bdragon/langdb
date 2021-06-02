package com.bryandragon.langdb.common.domain.cldr;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@JacksonXmlRootElement(localName = "ldmlBCP47")
public class LdmlBcp47 implements Serializable {
  @JacksonXmlProperty(localName = "version")
  private String version;

  @JacksonXmlElementWrapper(localName = "keyword")
  private List<Key> keys;

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "attribute")
  private List<Attribute> attributes;

  public LdmlBcp47() {
    this.keys = Collections.emptyList();
    this.attributes = Collections.emptyList();
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public List<Key> getKeys() {
    return keys;
  }

  public void setKeys(List<Key> keys) {
    this.keys = keys;
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }
}
