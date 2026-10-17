
package ecom.bitm.demowebshop.objRepo;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class HomePage {

    private WebDriver driver;

    // Register and login links
    @FindBy(linkText = "Register")
    private WebElement registerLink;

    @FindBy(linkText = "Log in")
    private WebElement loginLink;

    // Shopping cart and wishlist links
    @FindBy(css = "a.ico-cart")
    private WebElement shoppingCartLink;

    @FindBy(css = "a.ico-wishlist")
    private WebElement wishlistLink;

    // Product search elements
    @FindBy(id = "small-searchterms")
    private WebElement searchTextField;

    @FindBy(css = "input.search-box-button")
    private WebElement searchButton;

    // Main category links
    @FindBy(linkText = "Books")
    private WebElement booksLink;

    @FindBy(linkText = "Computers")
    private WebElement computersLink;

    @FindBy(linkText = "Electronics")
    private WebElement electronicsLink;

    @FindBy(linkText = "Apparel & Shoes")
    private WebElement apparelShoesLink;

    @FindBy(linkText = "Digital downloads")
    private WebElement digitalDownloadsLink;

    @FindBy(linkText = "Jewelry")
    private WebElement jewelryLink;

    @FindBy(linkText = "Gift Cards")
    private WebElement giftCardsLink;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    // Checks whether the home page is open
    public boolean isHomePageDisplayed() {
        return driver.getCurrentUrl()
                .contains("demowebshop.tricentis.com");
    }

    // Opens the registration page
    public void clickRegister() {
        registerLink.click();
    }

    // Opens the login page
    public void clickLogin() {
        loginLink.click();
    }

    // Opens the shopping cart
    public void clickShoppingCart() {
        shoppingCartLink.click();
    }

    // Opens the wishlist
    public void clickWishlist() {
        wishlistLink.click();
    }

    // Enters a product name in the search field
    public void enterSearchText(String productName) {
        searchTextField.clear();
        searchTextField.sendKeys(productName);
    }

    // Searches for a product
    public void clickSearch() {
        searchButton.click();
    }

    public void searchProduct(String productName) {
        enterSearchText(productName);
        clickSearch();
    }

    // Opens product categories
    public void openBooksCategory() {
        booksLink.click();
    }

    public void openComputersCategory() {
        computersLink.click();
    }

    public void openElectronicsCategory() {
        electronicsLink.click();
    }

    public void openApparelShoesCategory() {
        apparelShoesLink.click();
    }

    public void openDigitalDownloadsCategory() {
        digitalDownloadsLink.click();
    }

    public void openJewelryCategory() {
        jewelryLink.click();
    }

    public void openGiftCardsCategory() {
        giftCardsLink.click();
    }

    // Checks whether the search field is available
    public boolean isSearchFieldDisplayed() {
        return searchTextField.isDisplayed();
    }

    public boolean isRegisterLinkDisplayed() {
        return registerLink.isDisplayed();
    }

    public boolean isLoginLinkDisplayed() {
        return loginLink.isDisplayed();
    }
}
