package core;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ReusableLibrary {

    private WebDriver driver;
    private WebDriverWait wait;

    public ReusableLibrary(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    // General Utility Methods

    public WebElement waitForVisibility(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    public WebElement waitForClickability(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public void click(WebElement element) {
        waitForClickability(element).click();
    }

    public void enterText(WebElement element, String text) {
        waitForVisibility(element).clear();
        element.sendKeys(text);
    }

    public void selectDropdownByVisibleText(WebElement dropdown, String visibleText) {
        Select select = new Select(waitForVisibility(dropdown));
        select.selectByVisibleText(visibleText);
    }

    public void selectDropdownByIndex(WebElement dropdown, int index) {
        Select select = new Select(waitForVisibility(dropdown));
        select.selectByIndex(index);
    }

    public void selectDropdownByValue(WebElement dropdown, String value) {
        Select select = new Select(waitForVisibility(dropdown));
        select.selectByValue(value);
    }

    public List<WebElement> getAllDropdownOptions(WebElement dropdown) {
        Select select = new Select(waitForVisibility(dropdown));
        return select.getOptions();
    }

    public boolean isElementDisplayed(WebElement element) {
        try {
            return waitForVisibility(element).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public void scrollToBottom() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    public void acceptAlert() {
        driver.switchTo().alert().accept();
    }

    public void dismissAlert() {
        driver.switchTo().alert().dismiss();
    }

    public String getAlertText() {
        return driver.switchTo().alert().getText();
    }

    public void enterTextInAlert(String text) {
        driver.switchTo().alert().sendKeys(text);
    }

    public byte[] takeScreenshot() {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    public void hoverOverElement(WebElement element) {
        Actions actions = new Actions(driver);
        actions.moveToElement(waitForVisibility(element)).perform();
    }

    public void dragAndDrop(WebElement source, WebElement target) {
        Actions actions = new Actions(driver);
        actions.dragAndDrop(waitForVisibility(source), waitForVisibility(target)).perform();
    }

    public void switchToFrame(WebElement frameElement) {
        driver.switchTo().frame(waitForVisibility(frameElement));
    }

    public void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public String getCurrentURL() {
        return driver.getCurrentUrl();
    }

    public void navigateToURL(String url) {
        driver.navigate().to(url);
    }

    public void refreshPage() {
        driver.navigate().refresh();
    }

    public void closeBrowser() {
        driver.close();
    }

    public void quitDriver() {
        driver.quit();
    }

    // Dynamic Table Handling Methods

    public String getTableCellValue(By tableLocator, int rowIndex, int colIndex) {
        WebElement table = waitForVisibility(driver.findElement(tableLocator));
        WebElement cell = table.findElement(By.xpath(".//tr[" + rowIndex + "]/td[" + colIndex + "]"));
        return cell.getText();
    }

    public List<WebElement> getColumnValues(By tableLocator, int colIndex) {
        WebElement table = waitForVisibility(driver.findElement(tableLocator));
        return table.findElements(By.xpath(".//tr/td[" + colIndex + "]"));
    }

    public void clickTableCell(By tableLocator, int rowIndex, int colIndex) {
        WebElement table = waitForVisibility(driver.findElement(tableLocator));
        WebElement cell = table.findElement(By.xpath(".//tr[" + rowIndex + "]/td[" + colIndex + "]"));
        cell.click();
    }

    public int getRowIndexByCellValue(By tableLocator, int columnIndex, String cellValue) {
        WebElement table = waitForVisibility(driver.findElement(tableLocator));
        List<WebElement> rows = table.findElements(By.xpath(".//tr"));
        for (int i = 1; i <= rows.size(); i++) {
            WebElement cell = rows.get(i - 1).findElement(By.xpath(".//td[" + columnIndex + "]"));
            if (cell.getText().equalsIgnoreCase(cellValue)) {
                return i;
            }
        }
        return -1; // Not found
    }

    public List<WebElement> getAllRows(By tableLocator) {
        WebElement table = waitForVisibility(driver.findElement(tableLocator));
        return table.findElements(By.xpath(".//tr"));
    }

    public boolean isValuePresentInTable(By tableLocator, String value) {
        WebElement table = waitForVisibility(driver.findElement(tableLocator));
        List<WebElement> cells = table.findElements(By.xpath(".//td"));
        for (WebElement cell : cells) {
            if (cell.getText().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    public List<WebElement> getRowValues(By tableLocator, int rowIndex) {
        WebElement table = waitForVisibility(driver.findElement(tableLocator));
        return table.findElements(By.xpath(".//tr[" + rowIndex + "]/td"));
    }
}
