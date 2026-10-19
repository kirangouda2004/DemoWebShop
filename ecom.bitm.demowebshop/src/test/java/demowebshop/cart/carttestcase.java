package demowebshop.cart;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import ecom.bitm.demowebshop.baseClass.baseclass;
import ecom.bitm.demowebshop.objRepo.CartPage;
import ecom.bitm.demowebshop.objRepo.LoginPage;
import ecom.bitm.demowebshop.objRepo.ProductPage;
import ecom.bitm.demowebshop.utility.ExcelUtility;

public class carttestcase extends baseclass {

    private static final String EXCEL_PATH =
            System.getProperty("user.dir")
            + "/src/test/resources/demowebshop_testdata.xlsx";

    private static final String SHEET_NAME = "CartData";

    // Read test data from Excel
    private String getTestData(int row, String column)
            throws IOException {

        try (ExcelUtility excel =
                new ExcelUtility(EXCEL_PATH, SHEET_NAME)) {
            return excel.getCellData(row, column);
        }
    }

    // Get the application URL without a trailing slash
    private String getBaseUrl() {
        String url = properties.getProperty("url");

        if (url == null || url.isBlank()) {
            throw new IllegalStateException(
                    "Application URL is missing in CommonData.properties");
        }

        return url.replaceAll("/+$", "");
    }

    // Create an explicit wait using CommonData.properties
    private WebDriverWait getWait() {
        int waitTime = Integer.parseInt(
                properties.getProperty("explicitWait", "20"));

        return new WebDriverWait(driver, Duration.ofSeconds(waitTime));
    }

    // Log in using credentials stored in Excel
    private void loginToApplication(String email, String password) {

        driver.get(getBaseUrl() + "/login");

        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(email, password);

        Assert.assertTrue(
                loginPage.isLogoutLinkDisplayed(),
                "Login was unsuccessful.");
    }

    // Open the cart page
    private CartPage openCart() {
        driver.get(getBaseUrl() + "/cart");
        return new CartPage(driver);
    }

    // Search for a product and add it to the cart
    private void addProductToCart(String productName) {

        String encodedProductName = URLEncoder.encode(
                productName.trim(), StandardCharsets.UTF_8);

        String searchUrl = getBaseUrl()
                + "/search?q="
                + encodedProductName;

        driver.get(searchUrl);

        ProductPage productPage = new ProductPage(driver);
        productPage.clickProduct(productName.trim());

        // Build your own computer requires an HDD selection
        if (productName.trim().equalsIgnoreCase(
                "Build your own computer")) {

            By hddOption = By.xpath(
                    "//label[contains(normalize-space(), '320 GB')]");

            getWait().until(
                    ExpectedConditions.elementToBeClickable(hddOption)
            ).click();
        }

        // Click Add to Cart
        driver.findElement(
                By.cssSelector("input[id^='add-to-cart-button']")
        ).click();

        // Wait for the success or error notification
        getWait().until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".bar-notification.success")),
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".bar-notification.error"))
        ));

        if (!driver.findElements(
                By.cssSelector(".bar-notification.error")
        ).isEmpty()
                && driver.findElement(
                        By.cssSelector(".bar-notification.error")
                ).isDisplayed()) {

            Assert.fail(
                    "Unable to add product: " + productName);
        }
    }

    // TC-CART-01: Verify that the cart page opens
    @Test(groups = "Integration")
    public void verifyCartPageOpens() throws IOException {

        loginToApplication(
                getTestData(0, "Email"),
                getTestData(0, "Password"));

        CartPage cartPage = openCart();

        Assert.assertTrue(
                cartPage.getCurrentPageUrl().contains("cart"),
                "Cart page did not open.");
    }

    // TC-CART-02: Verify that the selected product appears in the cart
    @Test(groups = "Integration")
    public void verifyProductAppearsInCart() throws IOException {

        int row = 1;

        loginToApplication(
                getTestData(row, "Email"),
                getTestData(row, "Password"));

        String expectedProduct =
                getTestData(row, "ProductName").trim();

        addProductToCart(expectedProduct);

        CartPage cartPage = openCart();

        boolean productFound = cartPage.getCartProductNames()
                .stream()
                .map(String::trim)
                .anyMatch(name ->
                        name.equalsIgnoreCase(expectedProduct));

        Assert.assertTrue(
                productFound,
                "Product '" + expectedProduct
                + "' was not found in the cart. Actual cart contents: "
                + cartPage.getCartProductNames());
    }

    // TC-CART-03: Verify that product quantity can be updated
    @Test(groups = "Functionality")
    public void verifyQuantityCanBeUpdated() throws IOException {

        int row = 2;

        loginToApplication(
                getTestData(row, "Email"),
                getTestData(row, "Password"));

        String productName = getTestData(row, "ProductName").trim();
        String quantity = getTestData(row, "Quantity");

        addProductToCart(productName);

        CartPage cartPage = openCart();

        Assert.assertTrue(
                cartPage.getCartItemCount() > 0,
                "Product was not added to the cart.");

        cartPage.updateQuantity(0, quantity);
        cartPage.clickUpdateCart();

        // Check the quantity after the cart refresh
        boolean quantityUpdated = getWait().until(d -> {
            try {
                return quantity.equals(
                        d.findElement(
                                By.cssSelector(
                                        ".cart-item-row input.qty-input"))
                                .getAttribute("value"));
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                return false;
            } catch (org.openqa.selenium.NoSuchElementException e) {
                return false;
            }
        });

        Assert.assertTrue(
                quantityUpdated,
                "Product quantity was not updated to " + quantity + ".");
    }

    // TC-CART-04: Verify that all products can be removed from the cart
    @Test(groups = "System")
    public void verifyCartAfterRemovingProducts() throws IOException {

        int row = 1;

        loginToApplication(
                getTestData(row, "Email"),
                getTestData(row, "Password"));

        String productName = getTestData(row, "ProductName").trim();

        addProductToCart(productName);

        CartPage cartPage = openCart();

        Assert.assertTrue(
                cartPage.getCartItemCount() > 0,
                "The cart is empty before removing products.");

        int itemCount = cartPage.getCartItemCount();

        for (int i = 0; i < itemCount; i++) {
            cartPage.selectItemForRemoval(i);
        }

        cartPage.clickUpdateCart();

        getWait().until(ExpectedConditions.or(
                ExpectedConditions.numberOfElementsToBe(
                        By.cssSelector(".cart-item-row"), 0),
                ExpectedConditions.textToBePresentInElementLocated(
                        By.cssSelector(".order-summary-content"),
                        "Your Shopping Cart is empty!")
        ));

        Assert.assertEquals(
                cartPage.getCartItemCount(),
                0,
                "Products remain in the cart after removal.");
    }

    // TC-CART-05: Verify Continue Shopping navigation
    @Test(groups = "Smoke")
    public void verifyContinueShopping() throws IOException {

        int row = 1;

        loginToApplication(
                getTestData(row, "Email"),
                getTestData(row, "Password"));

        String productName =
                getTestData(row, "ProductName").trim();

        addProductToCart(productName);

        CartPage cartPage = openCart();

        Assert.assertTrue(
                cartPage.getCartItemCount() > 0,
                "Cart is empty; add a product before continuing shopping.");

        cartPage.clickContinueShopping();

        Assert.assertFalse(
                driver.getCurrentUrl().contains("/cart"),
                "Continue Shopping did not navigate away from the cart.");
    }
}
