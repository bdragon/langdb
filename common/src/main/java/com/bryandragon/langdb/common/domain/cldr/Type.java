package com.bryandragon.langdb.common.domain.cldr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.io.Serializable;

@JacksonXmlRootElement(localName = "type")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Type implements Serializable {
  @JacksonXmlProperty(localName = "name", isAttribute = true)
  private String name;

  @JacksonXmlProperty(localName = "description", isAttribute = true)
  private String description;

  @JacksonXmlProperty(localName = "deprecated", isAttribute = true)
  private boolean deprecated = false;

  @JacksonXmlProperty(localName = "preferred", isAttribute = true)
  private String preferred;

  @JacksonXmlProperty(localName = "alias", isAttribute = true)
  private String alias;

  @JacksonXmlProperty(localName = "since", isAttribute = true)
  private String since;

  public Type() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean getDeprecated() {
    return deprecated;
  }

  public void setDeprecated(boolean deprecated) {
    this.deprecated = deprecated;
  }

  public String getPreferred() {
    return preferred;
  }

  public void setPreferred(String preferred) {
    this.preferred = preferred;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public String getSince() {
    return since;
  }

  public void setSince(String since) {
    this.since = since;
  }
}
