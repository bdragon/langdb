package com.bryandragon.langdb.common.domain.cldr;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@JacksonXmlRootElement(localName = "ldmlBCP47")
public class LdmlBcp47 implements Serializable {
  @JacksonXmlProperty(localName = "version")
  @JsonIgnore()
  private Version version;

  @JacksonXmlElementWrapper(localName = "keyword")
  @JsonProperty("keys")
  private List<Key> keys;

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "attribute")
  @JsonProperty("attributes")
  private List<Attribute> attributes;

  public LdmlBcp47() {
    this.version = new Version();
    this.keys = Collections.emptyList();
    this.attributes = Collections.emptyList();
  }

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = version;
  }

  @JsonGetter("cldrVersion")
  public String getCldrVersion() {
    if (version != null) {
      return version.getCldrVersion();
    }
    return null;
  }

  @JsonSetter("cldrVersion")
  public void setCldrVersion(String cldrVersion) {
    if (version == null) {
      version = new Version();
    }
    version.setCldrVersion(cldrVersion);
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
