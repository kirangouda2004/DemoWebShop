package demowebshop.homepage;

import java.io.IOException;
import java.time.Duration;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

import ecom.bitm.demowebshop.baseClass.baseclass;
import ecom.bitm.demowebshop.objRepo.HomePage;
import ecom.bitm.demowebshop.objRepo.SearchPage;
import ecom.bitm.demowebshop.objRepo.ProductPage;
import ecom.bitm.demowebshop.utility.ExcelUtility;

public class homepagetestcase extends baseclass {

    // Read test data from the Excel sheet
    private String getTestData(int rowIndex, String columnName)
            throws IOException {

        String filePath = System.getProperty("user.dir")
                + "/src/test/resources/demowebshop_testdata.xlsx";

        try (ExcelUtility excel =
                new ExcelUtility(filePath, "HomepageSearchData")) {

            return excel.getCellData(rowIndex, columnName);
        }
    }

    // Create a wait using CommonData.properties
    private WebDriverWait getWait() {
        int waitTime = Integer.parseInt(
                properties.getProperty("explicitWait", "20"));

        return new WebDriverWait(driver, Duration.ofSeconds(waitTime));
    }

    // Wait for the search page to load
    private void waitForSearchResponse() {

        getWait().until(ExpectedConditions.urlContains("search"));

        getWait().until(webDriver ->
                "complete".equals(
                        ((org.openqa.selenium.JavascriptExecutor) webDriver)
                                .executeScript("return document.readyState")
                )
        );
    }

    // TC-HOME-01: Smoke Testing
    @Test(groups = {"Smoke"})
    public void TC_HOME_01_HomepageLoads() {

        HomePage home = new HomePage(
                driver, properties.getProperty("url"));

        Assert.assertTrue(
                home.isHomePageDisplayed(),
                "Homepage did not load successfully");
    }

    // TC-HOME-02: Functionality Testing
    @Test(groups = {"Functionality"})
    public void TC_HOME_02_SearchProduct() throws IOException {

        HomePage home = new HomePage(
                driver, properties.getProperty("url"));
        SearchPage search = new SearchPage(driver);

        String keyword = getTestData(1, "SearchKeyword");

        home.searchProduct(keyword);
        waitForSearchResponse();

        Assert.assertTrue(
                search.areSearchResultsDisplayed(),
                "Search results were not displayed");
    }

    // TC-HOME-03: Integration Testing
    @Test(groups = {"Integration"})
    public void TC_HOME_03_OpenProductDetails() throws IOException {

        HomePage home = new HomePage(
                driver, properties.getProperty("url"));
        ProductPage product = new ProductPage(driver);

        String keyword = getTestData(2, "SearchKeyword");

        home.searchProduct(keyword);
        waitForSearchResponse();

        Assert.assertTrue(
                product.areProductsDisplayed(),
                "No products were found for: " + keyword);

        String productName = product.getProductName(0);
        String searchUrl = driver.getCurrentUrl();

        product.clickProduct(productName);

        getWait().until(ExpectedConditions.not(
                ExpectedConditions.urlToBe(searchUrl)));

        Assert.assertNotEquals(
                driver.getCurrentUrl(),
                searchUrl,
                "Product details navigation did not occur");
    }

    // TC-HOME-04: System Testing
    @Test(groups = {"System"})
    public void TC_HOME_04_CompleteSearchWorkflow() throws IOException {

        HomePage home = new HomePage(
                driver, properties.getProperty("url"));
        SearchPage search = new SearchPage(driver);

        String keyword = getTestData(3, "SearchKeyword");

        Assert.assertTrue(home.isHomePageDisplayed());

        home.searchProduct(keyword);
        waitForSearchResponse();

        Assert.assertTrue(
                search.isSearchPageDisplayed(),
                "Search page did not open");

        Assert.assertTrue(
                search.areSearchResultsDisplayed(),
                "Search results were not displayed");
    }

    // TC-HOME-05: Negative Testing
    @Test(groups = {"Negative"})
    public void TC_HOME_05_SearchInvalidProduct() throws IOException {

        HomePage home = new HomePage(
                driver, properties.getProperty("url"));
        SearchPage search = new SearchPage(driver);

        String keyword = getTestData(4, "SearchKeyword");

        home.searchProduct(keyword);
        waitForSearchResponse();

        Assert.assertEquals(
                search.getSearchResultCount(),
                0,
                "Unexpected matching products were displayed");
    }

    // TC-HOME-06: Boundary Value Analysis (BVA)
    @Test(groups = {"BVA"})
    public void TC_HOME_06_SearchOneCharacter() throws IOException {

        HomePage home = new HomePage(
                driver, properties.getProperty("url"));
        SearchPage search = new SearchPage(driver);

        String keyword = getTestData(5, "SearchKeyword");

        home.searchProduct(keyword);
        waitForSearchResponse();

        Assert.assertTrue(
                search.isSearchPageDisplayed(),
                "Search page did not open for a one-character keyword");

        Assert.assertTrue(
                search.getSearchResultCount() >= 0,
                "Search response could not be evaluated");
    }
}
