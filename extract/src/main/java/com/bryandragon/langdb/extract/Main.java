package com.bryandragon.langdb.extract;

import com.bryandragon.langdb.extract.sources.Iso3166_1;
import com.bryandragon.langdb.extract.sources.Iso639_2Changes;
import com.bryandragon.langdb.extract.sources.UnM49;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class Main {
  public static void main(String[] args) {
    Path dataDir = Path.of(System.getProperty("dataDir"));
    Path downloadsDir = Path.of(System.getProperty("downloadsDir"));

    Map<String, Object> prefs = new HashMap<>();
    prefs.put("download.default_directory", downloadsDir);
    prefs.put("download.prompt_for_download", false);
    prefs.put("profile.default_content_settings.popups", 0);

    ChromeOptions options = new ChromeOptions();
    options.setExperimentalOption("prefs", prefs);
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--no-sandbox");
    options.addArguments("--remote-debugging-port=9222");
    options.addArguments("--window-size=1920,1080");
    options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
    options.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT, true);
    options.setHeadless(true);

    WebDriver driver = new ChromeDriver(options);
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));

    try {
      (new Iso639_2Changes(driver))
          .extractTo(fileOutputStream(dataDir.resolve("json/iso-639-2-changes.json")));

      (new Iso3166_1(driver)).extractTo(fileOutputStream(dataDir.resolve("json/iso-3166-1.json")));

      (new UnM49(driver, downloadsDir))
          .extractTo(fileOutputStream(dataDir.resolve("json/un-m49.json")));

      System.exit(0);
    } catch (IOException | RuntimeException e) {
      e.printStackTrace(System.err);
      System.exit(1);
    } finally {
      driver.quit();
    }
  }

  /** Returns an output stream for the file located at {@code path}, creating it if necessary. */
  private static BufferedOutputStream fileOutputStream(Path path) throws IOException {
    File file = path.toFile();
    if (!file.exists()) {
      file.createNewFile();
    }
    return new BufferedOutputStream(new FileOutputStream(file));
  }
}
