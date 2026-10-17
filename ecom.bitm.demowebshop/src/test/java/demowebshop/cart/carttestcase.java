
package demowebshop.cart;

import java.io.IOException;
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

    // Log in using credentials stored in Excel
    private void loginToApplication(String email, String password) {

        driver.get(properties.getProperty("url") + "login");

        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(email, password);

        Assert.assertTrue(
                loginPage.isLogoutLinkDisplayed(),
                "Login was unsuccessful.");
    }

    // Open the cart page
    private CartPage openCart() {
        driver.get(properties.getProperty("url") + "cart");
        return new CartPage(driver);
    }

    // Search for a product, configure it if required, and add it to the cart
    private void addProductToCart(String productName) {

        String searchUrl = properties.getProperty("url")
                + "search?q="
                + productName.trim().replace(" ", "+");

        driver.get(searchUrl);

        ProductPage productPage = new ProductPage(driver);
        productPage.clickProduct(productName.trim());

        WebDriverWait wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(
                        Long.parseLong(
                                properties.getProperty("explicitWait", "20")
                        )
                )
        );

        // Build your own computer requires an HDD selection
        if (productName.trim().equalsIgnoreCase(
                "Build your own computer")) {

            // Select the HDD option using its visible label
            By hddOption = By.xpath(
                    "//label[contains(normalize-space(), '320 GB')]"
            );

            wait.until(
                    ExpectedConditions.elementToBeClickable(hddOption)
            ).click();
        }

        // Click Add to Cart
        driver.findElement(
                By.cssSelector("input[id^='add-to-cart-button']")
        ).click();

        // Wait for the site's success or error notification
        wait.until(ExpectedConditions.or(
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
                    "Unable to add product: " + productName
            );
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

    // TC-CART-03: Verify that product quantity can be updated to two
    @Test(groups = "Functionality")
    public void verifyQuantityCanBeUpdated() throws IOException {

        int row = 2;

        loginToApplication(
                getTestData(row, "Email"),
                getTestData(row, "Password"));

        String productName = getTestData(row, "ProductName").trim();
        String quantity = getTestData(row, "Quantity");

        // Add the product using the existing helper method
        addProductToCart(productName);

        CartPage cartPage = openCart();

        Assert.assertTrue(
                cartPage.getCartItemCount() > 0,
                "Product was not added to the cart.");

        // Update the product quantity
        cartPage.updateQuantity(0, quantity);
        cartPage.clickUpdateCart();

        // Re-find the quantity field on every poll.
        // This handles the cart refresh and stale elements.
        WebDriverWait wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(
                        Long.parseLong(
                                properties.getProperty("explicitWait", "20")
                        )
                )
        );

        boolean quantityUpdated = wait.until(d -> {
            try {
                return quantity.equals(
                        d.findElement(
                                By.cssSelector(
                                        ".cart-item-row input.qty-input"))
                         .getAttribute("value")
                );
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


    // TC-CART-04: Verify that removing the product empties the cart



    // TC-CART-04: Verify that all products can be removed from the cart
    @Test(groups = "System")
    public void verifyCartAfterRemovingProducts() throws IOException {

        int row = 1;

        loginToApplication(
                getTestData(row, "Email"),
                getTestData(row, "Password"));

        String productName = getTestData(row, "ProductName").trim();

        // Add the product from Excel
        addProductToCart(productName);

        // Open the cart
        CartPage cartPage = openCart();

        Assert.assertTrue(
                cartPage.getCartItemCount() > 0,
                "The cart is empty before removing products.");

        // Select every product for removal
        int itemCount = cartPage.getCartItemCount();

        for (int i = 0; i < itemCount; i++) {
            cartPage.selectItemForRemoval(i);
        }

        // Apply the removal once
        cartPage.clickUpdateCart();

        // Verify that the cart is empty
        WebDriverWait wait = new WebDriverWait(
                driver,
                java.time.Duration.ofSeconds(
                        Long.parseLong(
                                properties.getProperty("explicitWait", "20"))));

        wait.until(ExpectedConditions.or(
                ExpectedConditions.numberOfElementsToBe(
                        By.cssSelector(".cart-item-row"), 0),
                ExpectedConditions.textToBePresentInElementLocated(
                        By.cssSelector(".order-summary-content"),
                        "Your Shopping Cart is empty!")));

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
