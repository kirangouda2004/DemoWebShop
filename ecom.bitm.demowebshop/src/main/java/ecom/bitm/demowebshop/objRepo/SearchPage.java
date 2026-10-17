package ecom.bitm.demowebshop.objRepo;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import org.openqa.selenium.support.PageFactory;

public class SearchPage {

    private WebDriver driver;

    // Search field
    @FindBy(id = "small-searchterms")
    private WebElement searchTextField;

    // Search button
    @FindBy(css = "input.search-box-button")
    private WebElement searchButton;

    // Search result products
    @FindBy(css = ".product-item")
    private List<WebElement> searchResults;

    // Message displayed when no products are found
    @FindBy(css = ".no-result")
    private WebElement noResultMessage;

    // Constructor
    public SearchPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    // Enter product name
    public void enterSearchText(String productName) {
        searchTextField.clear();
        searchTextField.sendKeys(productName);
    }

    // Click search button
    public void clickSearch() {
        searchButton.click();
    }

    // Search for a product
    public void searchProduct(String productName) {
        enterSearchText(productName);
        clickSearch();
    }

    // Return the number of products displayed
    public int getSearchResultCount() {
        return searchResults.size();
    }

    // Check whether search results are displayed
    public boolean areSearchResultsDisplayed() {
        return !searchResults.isEmpty();
    }

    // Check whether the no-results message is displayed
    public boolean isNoResultMessageDisplayed() {
        return !driver.findElements(
                org.openqa.selenium.By.cssSelector(".no-result"))
                .isEmpty();
    }

    // Get the no-results message
    public String getNoResultMessage() {
        return noResultMessage.getText();
    }

    // Get the current page URL
    public String getCurrentPageUrl() {
        return driver.getCurrentUrl();
    }

    // Check whether the search page is open
    public boolean isSearchPageDisplayed() {
        return driver.getCurrentUrl().contains("search");
    }
}