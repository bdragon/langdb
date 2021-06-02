package com.bryandragon.langdb.common.domain.cldr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@JacksonXmlRootElement(localName = "key")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Key implements Serializable {
  @JacksonXmlProperty(localName = "extension", isAttribute = true)
  @JsonProperty("extension")
  private String extension;

  @JacksonXmlProperty(localName = "name", isAttribute = true)
  @JsonProperty("name")
  private String name;

  @JacksonXmlProperty(localName = "description", isAttribute = true)
  @JsonProperty("description")
  private String description;

  @JacksonXmlProperty(localName = "deprecated", isAttribute = true)
  @JsonProperty("deprecated")
  private boolean deprecated = false;

  @JacksonXmlProperty(localName = "valueType", isAttribute = true)
  @JsonProperty("valueType")
  private String valueType;

  @JacksonXmlProperty(localName = "preferred", isAttribute = true)
  @JsonProperty("preferred")
  private String preferred;

  @JacksonXmlProperty(localName = "alias", isAttribute = true)
  @JsonProperty("alias")
  private String alias;

  @JacksonXmlProperty(localName = "since", isAttribute = true)
  @JsonProperty("since")
  private String since;

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "type")
  @JsonProperty("types")
  List<Type> types;

  public Key() {
    this.types = Collections.emptyList();
  }

  public String getExtension() {
    return extension;
  }

  public void setExtension(String extension) {
    this.extension = extension;
  }

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

  public String getValueType() {
    return valueType;
  }

  public void setValueType(String valueType) {
    this.valueType = valueType;
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

  public List<Type> getTypes() {
    return types;
  }

  public void setTypes(List<Type> types) {
    this.types = types;
  }
}
