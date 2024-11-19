### Core Automation Testing Framework Guide

* * *

#### **Overview**

This document provides a detailed guide for using the automated testing framework. It includes:

* Core framework components and their functionality.
* Steps to configure the framework for subsequent projects.
* Feature file, TestRunner, TestHooks, and Gradle setup.
* Detailed instructions for executing tests.

* * *

### **Core Framework Components**

#### **BrowserFactory**

Manages WebDriver instances for various browsers and platforms:

* **Web Browsers**: Chrome, Firefox, Safari, Edge.
* **Mobile Platforms**: Android and iOS (via Appium).
* **Features**:
    * Thread-safe WebDriver management.
    * Dynamic property loading for configurations.
    * Parallel browser launching for multi-browser testing.

**Key Methods**:

* **`getDriver(String browser)`**: Initializes and returns a `WebDriver` instance for the specified browser.
    
    ```java
    WebDriver chromeDriver = BrowserFactory.getDriver("chrome");
    WebDriver firefoxDriver = BrowserFactory.getDriver("firefox");
    ```
    
* **`cleanupDriver(String browser)`**: Closes the specified WebDriver instance.
    
    ```java
    BrowserFactory.cleanupDriver("chrome");
    ```
    
* **`cleanupAllDrivers()`**: Closes all active WebDriver instances.
    

* * *

#### **ExtentManager**

Generates detailed test reports using ExtentReports:

* **Features**:
    * Per-thread test logging.
    * Customizable metadata (e.g., Platform, Cluster, Base URL).
    * HTML reporting with timestamps.

**Key Methods**:

* **Initialize reports**:
    
    ```java
    ExtentReports extent = ExtentManager.getInstance("Platform", "ClusterName", "https://example.com");
    ```
    
* **Create a new test**:
    
    ```java
    ExtentManager.createTest("Login Test", "Validates login functionality");
    ```
    
* **Finalize and write the report**:
    
    ```java
    ExtentManager.flush();
    ```
    

* * *

### **Setup Instructions**

#### **Gradle Configuration**

1. Locate or create the `gradle.properties` file in the following locations:
    
    * **Mac/Linux**: `/Users/<your-username>/.gradle/gradle.properties`
    * **Windows**: `C:\Users\<your-username>\.gradle\gradle.properties`
2. Add the following credentials:
    
    ```properties
    liveswitchQAUsername=liveswitch
    liveswitchQAPassword=PERSONAL_ACCESS_TOKEN
    ```
    
    Replace:
    
    * `liveswitchQAUsername` with your Azure DevOps username.
    * `PERSONAL_ACCESS_TOKEN` with your Azure DevOps PAT (Personal Access Token).

* * *

### **Feature Files**

Define test scenarios and examples in `.feature` files. Example:


### **TestRunner**

The `TestRunner` class integrates TestNG with Cucumber for executing feature files and generating reports.

```java
package runners;

import core.ExtentManager;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;

@CucumberOptions(
        features = "src/test/resources/features", // Path to feature files
        glue = "steps",                           // Step definitions package
        tags = "@SmokeTest",                      // Tags to filter scenarios
        plugin = {
                "pretty",                         // Console output
                "html:target/cucumber-reports/cucumber.html", // HTML report
                "json:target/cucumber-reports/cucumber.json", // JSON report
                "junit:target/cucumber-reports/cucumber.xml"  // JUnit XML report
        }
)
public class TestRunner extends AbstractTestNGCucumberTests {
    @BeforeClass
    public void beforeClass(@Optional String pt) {
        ExtentManager.extent = ExtentManager.getInstance(
            System.getProperty("BROWSER"),
            System.getProperty("CLUSTER"), 
            ""
        );
    }
}
```

* * *

### **TestHooks**

The `TestHooks` class initializes and cleans up resources for each test scenario.

```java
package steps;

import core.BrowserFactory;
import core.ExtentManager;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class TestHooks {
    public static AppiumDriverLocalService service;

    @Before
    public void setup(Scenario scenario) {
        if (System.getProperty("BROWSER").equals("android")) {
            service = BrowserFactory.startAppiumService();
            service.start();
        }
        ExtentManager.createTest(scenario.getName(), "");
    }

    @After
    public void teardown() {
        if (service != null) {
            service.stop();
        }
        ExtentManager.flush();
    }
}
```

* * *

### **Execution**

Execute the tests using the following command:

```bash
gradle clean test -DsuiteFile=resources/testng.xml -DBROWSER=firefox -DCLUSTER=https://uberchat-rc-apsouth1.liveswitch.io/
```

**Parameters**:

* `-DsuiteFile`: Specifies the TestNG suite file to execute.
* `-DBROWSER`: Specifies the browser for test execution.
* `-DCLUSTER`: Specifies the cluster (base URL) for the application under test.

* * *

### **Best Practices**

* Define scenarios in `.feature` files using Cucumber Gherkin syntax.
* Use `BrowserFactory` and `ExtentManager` for browser and reporting management.
* Add the `TestRunner` class with appropriate `@CucumberOptions`.
* Run the Gradle command with required parameters.
* Store settings (e.g., browser type, cluster URLs) in a `config.properties` file.
* Clean up WebDriver instances using `cleanupDriver()` or `cleanupAllDrivers()`.

* * *

### **Reports**

The `ExtentManager` generates detailed HTML reports including:

* Test names and descriptions.
* Step-by-step logs.
* Pass/fail status for each step.

Reports are saved at:

```bash
target/cucumber-reports/cucumber.html
```

* * *

This document consolidates all the required steps to effectively use the core framework in subsequent projects. Let me know if you need further additions or modifications!
