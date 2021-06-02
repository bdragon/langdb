package com.bryandragon.langdb.extract.sources;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class Iso639_2Changes {
  private static final String URL = "https://www.loc.gov/standards/iso639-2/php/code_changes.php";
  private final WebDriver driver;

  public Iso639_2Changes(WebDriver driver) {
    this.driver = driver;
  }

  public void extractTo(OutputStream out) throws IOException {
    System.out.println("Getting ISO 639-2 change list...");

    this.driver.get(URL);
    System.out.printf("Page title: %s\n", this.driver.getTitle());

    WebElement knownTh = this.driver.findElement(By.xpath("//th[text()=\"ISO 639-1 Code\"]"));
    WebElement table = knownTh.findElement(By.xpath(".//ancestor::table[1]"));

    List<WebElement> ths = table.findElements(By.xpath(".//tr[1]//th"));
    String[] fields = ths.stream().map(WebElement::getText).toArray(String[]::new);

    JsonFactory jsonFactory = new JsonFactory();
    try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(out)) {
      jsonGenerator.writeStartArray();

      List<WebElement> trs = table.findElements(By.xpath(".//tr[position()>1]"));
      for (WebElement tr : trs) {
        List<WebElement> tds = tr.findElements(By.cssSelector("td"));
        String[] values = tds.stream().map(WebElement::getText).toArray(String[]::new);

        jsonGenerator.writeStartObject();
        for (int i = 0; i < fields.length; i++) {
          String value = values[i];
          value = value.isBlank() ? "" : value;
          jsonGenerator.writeStringField(fields[i], value);
        }
        jsonGenerator.writeEndObject();
      }

      jsonGenerator.writeEndArray();
      System.out.println("Done.");
    }
  }
}
