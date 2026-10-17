package ecom.bitm.demowebshop.objRepo;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

public class ProductPage {

    private WebDriver driver;

    // Product names displayed on the page
    @FindBy(css = ".product-item h2 a")
    private List<WebElement> productNames;

    // Product prices
    @FindBy(css = ".product-item .prices")
    private List<WebElement> productPrices;

    // Add to cart buttons
    @FindBy(css = ".product-item input[value='Add to cart']")
    private List<WebElement> addToCartButtons;

    // Product sorting dropdown
    @FindBy(id = "products-orderby")
    private WebElement sortDropdown;

    // Number of products dropdown
    @FindBy(id = "products-pagesize")
    private WebElement pageSizeDropdown;

    // View mode dropdown, when available
    @FindBy(id = "products-viewmode")
    private WebElement viewModeDropdown;

    // Constructor
    public ProductPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    // Get the number of products displayed
    public int getProductCount() {
        return productNames.size();
    }

    // Check whether products are displayed
    public boolean areProductsDisplayed() {
        return !productNames.isEmpty();
    }

    // Get all displayed product names
    public List<String> getProductNames() {
        return productNames.stream()
                .map(WebElement::getText)
                .toList();
    }

    // Get the name of a product by its position
    public String getProductName(int index) {
        return productNames.get(index).getText();
    }

    // Get the price of a product by its position
    public String getProductPrice(int index) {
        return productPrices.get(index).getText();
    }

    // Open a product using its name
    public void clickProduct(String productName) {
        for (WebElement product : productNames) {
            if (product.getText().trim()
                    .equalsIgnoreCase(productName.trim())) {
                product.click();
                return;
            }
        }

        throw new IllegalArgumentException(
                "Product not found: " + productName);
    }

    // Add a product to the cart using its position
    public void addProductToCart(int index) {
        addToCartButtons.get(index).click();
    }

    // Sort products using the visible dropdown option
    public void sortProductsByVisibleText(String option) {
        new Select(sortDropdown).selectByVisibleText(option);
    }

    // Change the number of products shown per page
    public void selectPageSize(String pageSize) {
        new Select(pageSizeDropdown).selectByVisibleText(pageSize);
    }

    // Change the product view mode when available
    public void selectViewMode(String viewMode) {
        new Select(viewModeDropdown).selectByVisibleText(viewMode);
    }

    // Get the current page URL
    public String getCurrentPageUrl() {
        return driver.getCurrentUrl();
    }
}