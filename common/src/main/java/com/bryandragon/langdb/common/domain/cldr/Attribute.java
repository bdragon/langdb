package com.bryandragon.langdb.common.domain.cldr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.io.Serializable;

@JacksonXmlRootElement(localName = "attribute")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Attribute implements Serializable {
  @JacksonXmlProperty(localName = "name", isAttribute = true)
  @JsonProperty("name")
  private String name;

  @JacksonXmlProperty(localName = "description", isAttribute = true)
  @JsonProperty("description")
  private String description;

  @JacksonXmlProperty(localName = "deprecated", isAttribute = true)
  @JsonProperty("deprecated")
  private boolean deprecated = false;

  @JacksonXmlProperty(localName = "preferred", isAttribute = true)
  @JsonProperty("preferred")
  private String preferred;

  @JacksonXmlProperty(localName = "since", isAttribute = true)
  @JsonProperty("since")
  private String since;

  public Attribute() {}

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

  public String getSince() {
    return since;
  }

  public void setSince(String since) {
    this.since = since;
  }
}
