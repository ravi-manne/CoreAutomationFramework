package core;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BrowserFactory {
    private static final Map<String, WebDriver> driverMap = new ConcurrentHashMap<>();
    private static AppiumDriverLocalService appiumService;
    private static UiAutomator2Options androidOptions;
    private static Capabilities caps;
    private static XCUITestOptions iosOptions;

    public static WebDriver getDriver(String browserType) {
        driverMap.computeIfAbsent(browserType, BrowserFactory::createDriver);
        return driverMap.get(browserType);
    }

    private static WebDriver createDriver(String browserType) {
        WebDriver driver = null;
        try {
            switch (browserType.toLowerCase()) {
                case "chrome":
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.addArguments("--use-fake-ui-for-media-stream", "--use-fake-device-for-media-stream");
                    chromeOptions.setPageLoadStrategy(org.openqa.selenium.PageLoadStrategy.NORMAL);
                    driver = new ChromeDriver(chromeOptions);
                    break;
                case "safari":
                    SafariOptions safariOptions = new SafariOptions();
                    safariOptions.setPageLoadStrategy(org.openqa.selenium.PageLoadStrategy.NORMAL);
                    driver = new SafariDriver(safariOptions);
                    break;
                case "firefox":
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    firefoxOptions.addPreference("browser.privatebrowsing.autostart", true);
                    firefoxOptions.addPreference("media.navigator.enabled", true);
                    firefoxOptions.addPreference("media.navigator.permission.disabled", true);
                    firefoxOptions.addPreference("media.navigator.streams.fake", true);
                    firefoxOptions.setPageLoadStrategy(org.openqa.selenium.PageLoadStrategy.NORMAL);
                    driver = new FirefoxDriver(firefoxOptions);
                    break;
                case "edge":
                    EdgeOptions edgeOptions = new EdgeOptions();
                    edgeOptions.setPageLoadStrategy(org.openqa.selenium.PageLoadStrategy.NORMAL);
                    driver = new EdgeDriver(edgeOptions);
                    break;
                case "android":
                    driver = createMobileDriver("android", getAndroidDevice());
                    break;
                case "ios":
                    driver = createMobileDriver("ios", getIosDevice());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported browser/platform: " + browserType);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Failed to set up browser", e);
        }
        caps = ((RemoteWebDriver) driver).getCapabilities();
        System.out.println("------------ Platform: " + caps.getBrowserName() + "\tVersion: " + caps.getBrowserVersion() + " -----------");
        return driver;
    }

    private static WebDriver createMobileDriver(String platform, String deviceName) throws MalformedURLException, URISyntaxException {
        WebDriver driver = null;
        if (platform.equalsIgnoreCase("android")) {
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("--use-fake-ui-for-media-stream", "--use-fake-device-for-media-stream");
            androidOptions = new UiAutomator2Options();
            androidOptions.noReset().withBrowserName("Chrome");
            androidOptions.setChromedriverExecutable("drivers/chrome/chromedriver");
            androidOptions.setDeviceName(deviceName);
            androidOptions.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
            driver = new AndroidDriver(new URI("http://127.0.0.1:4723").toURL(), androidOptions);

        } else if (platform.equalsIgnoreCase("ios")) {
            iosOptions = new XCUITestOptions();
            iosOptions.withBrowserName("Safari");
            iosOptions.setUdid(deviceName);
            driver = new IOSDriver(new URI("http://127.0.0.1:4723").toURL(), iosOptions);
        }
        return driver;
    }

    public static String getAndroidDevice() {
        return getPropertyValue("ANDROID-DEVICE");
    }

    public static String getIosDevice() {
        return getPropertyValue("IOS-DEVICE");
    }

    private static String getPropertyValue(String property) {
        String value = null;
        try {
            Properties properties = new Properties();
            FileInputStream file = new FileInputStream("src/test/resources/config.properties");
            properties.load(file);
            value = properties.getProperty(property).toLowerCase().trim();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return value;
    }

    public static void cleanupDriver(String browserType) {
        WebDriver driver = driverMap.remove(browserType);
        if (driver != null) {
            driver.quit();
        }
    }

    public static void cleanupAllDrivers() {
        driverMap.forEach((browserType, driver) -> {
            if (driver != null) {
                driver.quit();
            }
        });
        driverMap.clear();
    }

    // Method to start the Appium service
    public static AppiumDriverLocalService startAppiumService() {
        if (appiumService == null) {
            appiumService = new AppiumServiceBuilder()
                    .withTimeout(Duration.ofMinutes(60))
                    .withAppiumJS(new File("/usr/local/lib/node_modules/appium/build/lib/main.js"))
                    .withIPAddress("127.0.0.1")
                    .usingPort(4723)
                    .build();
        }
        return appiumService;
    }

    // Stop the Appium service if needed
    public static void stopAppiumService() {
        if (appiumService != null && appiumService.isRunning()) {
            appiumService.stop();
        }
    }

    public static void openBrowsers(String[] browserTypes, String[] urls) {
        if (browserTypes.length != urls.length) {
            throw new IllegalArgumentException("The number of browsers must match the number of URLs.");
        }

        ExecutorService executor = Executors.newFixedThreadPool(browserTypes.length);

        for (int i = 0; i < browserTypes.length; i++) {
            final String browserType = browserTypes[i];
            final String url = urls[i];

            executor.execute(() -> {
                WebDriver driver = null;
                try {
                    driver = getDriver(browserType); // Reuse existing getDriver method
                    driver.get(url);
                    System.out.println(browserType + " Driver Current URL: " + driver.getCurrentUrl());
                } catch (Exception e) {
                    System.err.println("Error in " + browserType + " thread: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait for all threads to complete
        }

        System.out.println("Browser Opened Succefully");
    }
}
