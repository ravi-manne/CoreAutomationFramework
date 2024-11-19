package core;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;


public class ExtentManager {
    public static ExtentReports extent;
    public static ExtentSparkReporter htmlReporter;


    public static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    public static ThreadLocal<String> currentPlatform=new ThreadLocal<>();
    public static ExtentTest test = null;
    public static void setPlatform(String platform) {
        currentPlatform.set(platform);
    }

    public static String getPlatform() {
        return currentPlatform.get();
    }
    public static ExtentReports getInstance(String pt,String cluster,String baseurl) {
        if (extent == null) {
            String fileName = "ExtentReport-"+pt;
            String reportLocation = "Automation Reports/"+fileName+".html";
            htmlReporter = new ExtentSparkReporter(reportLocation);
            htmlReporter.config().setDocumentTitle("Automation Report");
            htmlReporter.config().setReportName("Automation Test Results");
            htmlReporter.config().setTimeStampFormat("EEEE,MMMM dd, hh:mm a '('zzz')'");
            extent = new ExtentReports();
            extent.setSystemInfo("Application", "LS1 Smoke Testing");
            extent.setSystemInfo("Operating System", System.getProperty("os.name"));
            extent.setSystemInfo("User Name", System.getProperty("user.name"));
            extent.setSystemInfo("Cluster", cluster);
            extent.setSystemInfo("Base Url", baseurl);
            extent.setSystemInfo("Platform", pt);
            extent.attachReporter(htmlReporter);
        }
        return extent;
    }

    public static synchronized ExtentTest getTest() {
        return extentTest.get();
    }

    public static synchronized void createTest(String testName, String description) {
        ExtentTest test = extent.createTest(testName, description);
        extentTest.set(test);
    }

    public static synchronized void flush() {
        if (extent != null) {
            extent.flush();
        }
    }

}
