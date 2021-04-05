package com.bryandragon.langdb.common.domain.cldr;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.io.Serializable;

@JacksonXmlRootElement(localName = "version")
public class Version implements Serializable {
  @JacksonXmlProperty(localName = "number")
  private String number;

  @JacksonXmlProperty(localName = "cldrVersion")
  private String cldrVersion;

  public Version() {}

  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public String getCldrVersion() {
    return cldrVersion;
  }

  public void setCldrVersion(String cldrVersion) {
    this.cldrVersion = cldrVersion;
  }
}
