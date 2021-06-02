package com.bryandragon.langdb.extract.sources;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.locators.RelativeLocator;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.List;

public class Iso3166_1 {
  private static final String URL = "https://www.iso.org/obp/ui/#search/code/";
  private final WebDriver driver;

  public Iso3166_1(WebDriver driver) {
    this.driver = driver;
  }

  public void extractTo(OutputStream out) throws IOException {
    System.out.println("Getting ISO 3166-1 country codes...");

    this.driver.get(URL);
    System.out.printf("Page title: %s\n", this.driver.getTitle());

    WebElement label = this.driver.findElement(By.xpath("//*[text()=\"Results per page:\"]"));
    WebElement select = this.driver.findElement(RelativeLocator.with(By.tagName("select")).near(label));
    Select perPage = new Select(select);

    List<WebElement> tables = this.driver.findElements(By.cssSelector("table[role=grid]"));
    System.out.printf("Found %d TABLE candidates; assuming first contains data.\n", tables.size());
    WebElement table = tables.get(0);

    By rowsSelector = By.cssSelector("tbody tr[role=row]");
    int initialRowCount = table.findElements(rowsSelector).size();

    System.out.printf("Initial row count: %d\n", initialRowCount);
    System.out.println("Showing all rows...");
    perPage.selectByVisibleText("300"); // Largest option is currently 300.

    FluentWait<WebElement> wait = new FluentWait<>(table)
        .withTimeout(Duration.ofSeconds(10))
        .pollingEvery(Duration.ofMillis(250));
    wait.until(t -> t.findElements(rowsSelector).size() > initialRowCount);

    List<WebElement> ths = table.findElements(By.cssSelector("thead tr[role=rowheader] th[role=columnheader]"));
    String[] fields = ths.stream().map(WebElement::getText).toArray(String[]::new);

    JsonFactory jsonFactory = new JsonFactory();
    try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(out)) {
      jsonGenerator.writeStartArray();

      List<WebElement> trs = table.findElements(rowsSelector);
      for (WebElement tr : trs) {
        jsonGenerator.writeStartObject();

        List<WebElement> tds = tr.findElements(By.cssSelector("td[role=gridcell]"));
        String[] values = tds.stream().map(WebElement::getText).toArray(String[]::new);

        for (int i = 0; i < fields.length; i++) {
          jsonGenerator.writeFieldName(fields[i]);
          jsonGenerator.writeString(values[i]);
        }

        jsonGenerator.writeEndObject();
      }

      jsonGenerator.writeEndArray();
      System.out.println("Done.");
    }
  }
}
