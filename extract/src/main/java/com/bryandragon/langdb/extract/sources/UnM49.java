package com.bryandragon.langdb.extract.sources;

import com.bryandragon.langdb.common.format.CsvReader;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;

public class UnM49 {
  private static final String URL = "https://unstats.un.org/unsd/methodology/m49/overview/";
  private final WebDriver driver;
  private final Path downloadsDir;

  public UnM49(WebDriver driver, Path downloadsDir) {
    this.driver = driver;
    this.downloadsDir = downloadsDir;
  }

  public void extractTo(OutputStream out) throws IOException {
    System.out.println("Getting UN M49 codes...");

    driver.get(URL);

    String title = driver.getTitle();
    System.out.printf("Page title: %s\n", title);

    // The data table [1] is configured with three (client side-generated) download options:
    // Copy, Excel, and CSV.
    //
    // The CSV export does not quote fields and, since some fields contain commas,
    // the downloaded CSV file is not parsable without some ugly special handling.
    //
    // The Copy option copies the data in TSV format to the system clipboard (presumably),
    // but accessing the clipboard contents via java.awt did not seem to work.
    //
    // Ultimately, we build our own CSV export configuration, which quotes fields and
    // does not add an unnecessary BOM to UTF-8-encoded fields.
    //
    // [1] See: https://datatables.net/reference/api/

    if (driver instanceof JavascriptExecutor) {
      ((JavascriptExecutor) driver).executeScript(
          "$('#downloadTableEN').DataTable().button().add(0, {" +
          "  extend: 'csv'," +
          "  charset: 'utf-8'," +
          "  bom: false," +
          "  fieldBoundary: '\"'," +
          "  extension: '.csv'," +
          "  filename: 'un-m49'," +
          "  text: 'Parsable CSV'," +
          "});"
      );
    } else {
      throw new RuntimeException("Driver does not support JavaScript execution");
    }

    WebElement button = driver.findElement(By.linkText("Parsable CSV"));
    button.click();

    // Poll for the downloaded file for up to 10s.
    long initialFileCount = countFiles(downloadsDir);
    FluentWait<Path> wait = new FluentWait<>(downloadsDir)
        .withTimeout(Duration.ofSeconds(10))
        .pollingEvery(Duration.ofMillis(250));
    wait.until(dir -> countFiles(dir) > initialFileCount);

    File downloadedFile = downloadsDir.resolve("un-m49.csv").toFile();

    JsonFactory jsonFactory = new JsonFactory();

    try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(out)) {
      CsvReader csvReader =
          new CsvReader(new BufferedReader(new FileReader(downloadedFile)), ',');
      Iterator<Map<String, String>> it = csvReader.iterator();

      jsonGenerator.writeStartArray();

      while (it.hasNext()) {
        Map<String, String> item = it.next();

        jsonGenerator.writeStartObject();

        for (Map.Entry<String, String> entry : item.entrySet()) {
          jsonGenerator.writeStringField(entry.getKey(), entry.getValue());
        }

        jsonGenerator.writeEndObject();
      }

      jsonGenerator.writeEndArray();
      downloadedFile.delete();

      System.out.println("Done.");
    }
  }

  private static long countFiles(Path dir) {
    try {
      return Files.list(dir).count();
    } catch (IOException e) {
      e.printStackTrace(System.err);
    }
    return 0;
  }
}
