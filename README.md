**Core Automation Framework User Guide**
========================================

This guide provides a detailed overview of the Core Automation Framework, covering its features, setup instructions, and steps to reuse the framework in subsequent projects. It includes:

* **Core Framework Overview**
* **Setup and Configuration**
* **Feature Files**
* **TestRunner Configuration**
* **TestHooks**
* **Test Execution**
* **Reporting**
* **Best Practices**

* * *

**1. Core Framework Overview**
------------------------------

### **Key Features**

1. **Browser Management:**
    
    * Supports desktop browsers: Chrome, Firefox, Safari, Edge.
    * Mobile platforms: Android and iOS (via Appium).
    * Thread-safe WebDriver management.
    * Parallel browser execution.
2. **Reporting:**
    
    * Generates HTML reports using ExtentReports.
    * Logs step-by-step test execution.
    * Provides metadata (Platform, Cluster, Base URL).
3. **Configuration:**
    
    * Centralized `config.properties` file to manage reusable test configurations.

* * *

**2. Setup and Configuration**
------------------------------

### **2.1 Gradle Configuration**

1. **Add Dependency**: Add the core framework dependency in your `build.gradle` file:
    
    ```groovy
    dependencies {
        implementation('com.ls:LSATFW:1.0-SNAPSHOT_0.2')  // Replace with the correct version
    }
    ```
    
2. **Repository Authentication**:
    * Update `gradle.properties`:
        
        ```makefile
        liveswitchQAUsername=liveswitch
        liveswitchQAPassword=PERSONAL_ACCESS_TOKEN
        ```
        
    * Ensure `gradle.properties` is located in:
        * macOS/Linux: `/Users/<your-username>/.gradle/gradle.properties`
        * Windows: `C:\Users\<your-username>\.gradle\gradle.properties`
    * Add this file to `.gitignore` to prevent exposure of sensitive information.

* * *

### **2.2 Configuration File**

The framework uses a centralized `config.properties` file for managing environment-specific and reusable configurations.

#### **Example `config.properties` File:**

```properties
# General configuration
BROWSER=chrome
CLUSTER=https://example-cluster-url.com

# Mobile configuration
ANDROID-DEVICE=emulator-5554
IOS-DEVICE=your-ios-udid

# Test-specific properties
TEST_ENVIRONMENT=staging
REPORT_PATH=AutomationReports/
```

#### **Purpose of Each Property:**

* **`BROWSER`**: Specifies the browser type (`chrome`, `firefox`, `android`, etc.).
* **`CLUSTER`**: Specifies the cluster URL for the application under test.
* **`ANDROID-DEVICE`** and **`IOS-DEVICE`**: Device names or UDID for mobile testing.
* **`TEST_ENVIRONMENT`**: The environment where tests will run (e.g., staging, production).
* **`REPORT_PATH`**: The folder where execution reports will be saved.

#### **Usage in the Framework**:

The `config.properties` file values can be dynamically accessed in the code. For example:

```java
public static String getAndroidDevice() {
    return getPropertyValue("ANDROID-DEVICE");
}

private static String getPropertyValue(String property) {
    String value = null;
    try {
        Properties properties = new Properties();
        FileInputStream file = new FileInputStream("src/test/resources/config.properties");
        properties.load(file);
        value = properties.getProperty(property).trim();
    } catch (IOException ex) {
        System.out.println(ex.getMessage());
    }
    return value;
}
```

* * *

**3. Feature Files**
--------------------

Define scenarios using Gherkin syntax. Example:

```gherkin
Feature: Validate Join and Drop Functionality
  @SmokeTest
  Scenario Outline: Validate chat message <PrimaryMode> - <SecondaryMode>
    Given I launch application
    When I join the video chat with the following details:
      | Name           | Channel   | Mode            |
      | Primary User   | channel01 | <PrimaryMode>   |
      | Secondary User | channel01 | <SecondaryMode> |
    And I validate Chat Messages for "Primary User"
      | Send Message    |           |
      | Receive Message |           |
    And I close the application
    Examples:
      | PrimaryMode | SecondaryMode |
      | SFU         | SFU           |
      | SFU         | MCU           |
      | MCU         | SFU           |
      | MCU         | MCU           |
```

* * *

**4. TestRunner Configuration**
-------------------------------

The TestRunner integrates TestNG with Cucumber for executing feature files and generating reports.

```java
package runners;

import core.ExtentManager;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;

@CucumberOptions(
    features = "src/test/resources/features", // Path to feature files
    glue = "steps",                           // Step definition package
    tags = "@SmokeTest",                      // Filters scenarios
    plugin = {
        "pretty",
        "html:target/cucumber-reports/cucumber.html",
        "json:target/cucumber-reports/cucumber.json",
        "junit:target/cucumber-reports/cucumber.xml"
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

**5. TestHooks**
----------------

TestHooks handle scenario-specific setup and teardown, including Appium service initialization.

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
        if (service != null)
            service.stop();
        ExtentManager.flush();
    }
}
```

* * *

**6. Test Execution**
---------------------

### **Command**

Run the tests using the following Gradle command:

```bash
gradle clean test -DsuiteFile=resources/testng.xml -DBROWSER=firefox -DCLUSTER=https://uberchat-rc-apsouth1.liveswitch.io/
```

### **Parameters**

* `-DsuiteFile`: Specifies the TestNG suite file.
* `-DBROWSER`: Specifies the browser (e.g., `chrome`, `firefox`, `android`).
* `-DCLUSTER`: Specifies the cluster (application URL).

* * *

**7. Reporting**
----------------

The framework uses ExtentReports for HTML-based reporting.

### **Features of Reports**:

* Detailed logs for each test step (pass/fail).
* Metadata (Platform, Cluster, Browser).
* Comprehensive status summary.

### **Location of Reports**:

Execution reports are available in the folder:  
**`AutomationReports`**

**Report Files Include:**

* HTML Report: `ExtentReport-<Platform>.html`
* Cucumber Reports: Generated in `target/cucumber-reports/`

Example for accessing the main report:

```plaintext
AutomationReports/ExtentReport-Windows.html
```
