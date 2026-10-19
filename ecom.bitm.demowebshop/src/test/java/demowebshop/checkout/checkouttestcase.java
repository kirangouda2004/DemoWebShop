
package demowebshop.checkout;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import ecom.bitm.demowebshop.baseClass.baseclass;
import ecom.bitm.demowebshop.objRepo.LoginPage;
import ecom.bitm.demowebshop.objRepo.ProductPage;
import ecom.bitm.demowebshop.objRepo.CartPage;
import ecom.bitm.demowebshop.objRepo.CheckoutPage;
import ecom.bitm.demowebshop.utility.ExcelUtility;

public class checkouttestcase extends baseclass {

    private static final String EXCEL_PATH =
            System.getProperty("user.dir")
                    + "/src/test/resources/demowebshop_testdata.xlsx";

    private static final String SHEET_NAME = "CheckoutData";

    private WebDriverWait getWait() {
        int waitSeconds = Integer.parseInt(
                properties.getProperty("explicitWait", "20"));

        return new WebDriverWait(driver, Duration.ofSeconds(waitSeconds));
    }

    private String getBaseUrl() {
        String baseUrl = properties.getProperty("url");

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "Application URL is missing in CommonData.properties");
        }

        return baseUrl.replaceAll("/+$", "") + "/";
    }

    // Read test data from Excel using the TestCaseID.
    private String getTestData(String testCaseID, String columnName)
            throws IOException {

        try (ExcelUtility excel =
                     new ExcelUtility(EXCEL_PATH, SHEET_NAME)) {

            for (int row = 0; row < excel.getRowCount(); row++) {
                String currentID =
                        excel.getCellData(row, "TestCaseID").trim();

                if (currentID.equalsIgnoreCase(testCaseID)) {
                    return excel.getCellData(row, columnName).trim();
                }
            }
        }

        throw new IllegalArgumentException(
                "Test case not found in Excel: " + testCaseID);
    }

    // Log in using credentials from Excel.
    private void loginToApplication(String testCaseID)
            throws IOException {

        driver.get(getBaseUrl() + "login");

        getWait().until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("Email")));

        LoginPage loginPage = new LoginPage(driver);

        loginPage.login(
                getTestData(testCaseID, "Email"),
                getTestData(testCaseID, "Password"));

        getWait().until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("a.account")));
    }

    // Search for a product and add it to the cart.
    private void addProductToCart(String testCaseID)
            throws IOException {

        String productName = getTestData(testCaseID, "ProductName");

        String searchUrl = getBaseUrl() + "search?q="
                + URLEncoder.encode(productName, StandardCharsets.UTF_8);

        driver.get(searchUrl);

        ProductPage productPage = new ProductPage(driver);
        productPage.clickProduct(productName);

        By addToCartButton =
                By.cssSelector("input[id^='add-to-cart-button']");

        getWait().until(
                ExpectedConditions.elementToBeClickable(addToCartButton))
                .click();

        getWait().until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".bar-notification.success")),
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".bar-notification.error"))
        ));

        boolean addedSuccessfully = driver.findElements(
                        By.cssSelector(".bar-notification.success"))
                .stream()
                .anyMatch(WebElement::isDisplayed);

        Assert.assertTrue(
                addedSuccessfully,
                "Product was not added successfully.");
    }

    // Open checkout after adding a product.
    private CheckoutPage openCheckout(String testCaseID)
            throws IOException {

        loginToApplication(testCaseID);
        addProductToCart(testCaseID);

        driver.get(getBaseUrl() + "cart");

        CartPage cartPage = new CartPage(driver);

        getWait().until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".cart")));

        Assert.assertTrue(
                cartPage.getCartItemCount() > 0,
                "The cart must contain a product before checkout.");

        CheckoutPage checkoutPage = new CheckoutPage(driver);
        checkoutPage.clickCheckout();

        getWait().until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("billing-address-select")));

        return checkoutPage;
    }

    // Enter a new billing address using Excel data.
    private void enterNewBillingAddress(
            String testCaseID, CheckoutPage checkoutPage)
            throws IOException {

        checkoutPage.selectBillingAddress("New Address");

        getWait().until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("BillingNewAddress_FirstName")));

        checkoutPage.enterBillingAddress(
                getTestData(testCaseID, "FirstName"),
                getTestData(testCaseID, "LastName"),
                getTestData(testCaseID, "Email"),
                getTestData(testCaseID, "Country"),
                getTestData(testCaseID, "State"),
                getTestData(testCaseID, "City"),
                getTestData(testCaseID, "Address"),
                getTestData(testCaseID, "PostalCode"),
                getTestData(testCaseID, "Phone"));
    }

    // Find and click the visible, enabled checkout button.
    private void clickCheckoutStep(By locator) {

        WebElement button = getWait().until(webDriver -> {

            List<WebElement> elements = webDriver.findElements(locator);

            for (WebElement element : elements) {
                if (element.isDisplayed() && element.isEnabled()) {
                    return element;
                }
            }

            return null;
        });

        button.click();
    }

    // Wait for a checkout step to finish loading before continuing.
    private void waitForVisibleElement(By locator) {
        getWait().until(webDriver ->
                webDriver.findElements(locator).stream()
                        .anyMatch(element ->
                                element.isDisplayed() && element.isEnabled()));
    }

    // Proceed through all checkout steps until order review is ready.
    private void proceedToOrderReview(
            String testCaseID, CheckoutPage checkoutPage)
            throws IOException {

        String addressType =
                getTestData(testCaseID, "AddressType");

        // Step 1: Billing address.
        if (addressType.equalsIgnoreCase("New Address")) {
            enterNewBillingAddress(testCaseID, checkoutPage);
        }

        checkoutPage.clickBillingContinue();

        // Step 2: Shipping address.
        By shippingAddressContinue = By.cssSelector(
                "input.button-1.shipping-address-next-step-button");

        clickCheckoutStep(shippingAddressContinue);

        // Step 3: Select Ground shipping.
        By groundShipping = By.xpath(
                "//label[contains(normalize-space(.), 'Ground')]");

        getWait().until(
                ExpectedConditions.elementToBeClickable(groundShipping));

        checkoutPage.selectGroundShipping();

        clickCheckoutStep(By.cssSelector(
                "input.button-1.shipping-method-next-step-button"));

        // Step 4: Select Cash On Delivery.
        By cashOnDelivery = By.xpath(
                "//label[contains(normalize-space(.), 'Cash On Delivery')]");

        getWait().until(
                ExpectedConditions.elementToBeClickable(cashOnDelivery));

        checkoutPage.selectCashOnDelivery();

        clickCheckoutStep(By.cssSelector(
                "input.button-1.payment-method-next-step-button"));

        // Step 5: Continue through payment information.
        clickCheckoutStep(By.cssSelector(
                "input.button-1.payment-info-next-step-button"));

        // Step 6: Wait for the actual Confirm Order button.
        // The checkout steps load dynamically, so the button is used
        // to determine when the order-review step is ready.
        By confirmOrderButton = By.cssSelector(
                "input.button-1.confirm-order-next-step-button");

        waitForVisibleElement(confirmOrderButton);
    }

    // Place the order and verify the confirmation.
    private void confirmAndVerifyOrder(CheckoutPage checkoutPage) {

        By confirmOrderButton = By.cssSelector(
                "input.button-1.confirm-order-next-step-button");

        clickCheckoutStep(confirmOrderButton);

        getWait().until(
                webDriver -> checkoutPage.isOrderCompletedDisplayed());

        Assert.assertTrue(
                checkoutPage.isOrderCompletedDisplayed(),
                "Order confirmation was not displayed.");

        Assert.assertFalse(
                checkoutPage.getOrderNumber().trim().isEmpty(),
                "Order number was not displayed.");

        Assert.assertTrue(
                checkoutPage.getOrderCompletedMessage()
                        .toLowerCase().contains("success"),
                "Order success message was not displayed.");
    }

    // TC-CHK-01: Checkout starts from a non-empty cart.
    @Test(groups = "Integration")
    public void verifyCheckoutStartsFromNonEmptyCart()
            throws IOException {

        CheckoutPage checkoutPage = openCheckout("TC-CHK-01");

        Assert.assertTrue(
                checkoutPage.isBillingAddressDropdownDisplayed(),
                "Checkout should open from the non-empty cart.");
    }

    // TC-CHK-02: Verify billing address fields.
    @Test(groups = "Functionality")
    public void verifyBillingAddressFieldsDisplayed()
            throws IOException {

        CheckoutPage checkoutPage = openCheckout("TC-CHK-02");

        checkoutPage.selectBillingAddress("New Address");

        Assert.assertTrue(
                checkoutPage.areBillingFieldsDisplayed(),
                "Billing address fields should be displayed.");
    }

    // TC-CHK-03: Required billing fields cannot be skipped.
    @Test(groups = "Negative")
    public void verifyRequiredBillingFieldsCannotBeSkipped()
            throws IOException {

        CheckoutPage checkoutPage = openCheckout("TC-CHK-03");

        checkoutPage.selectBillingAddress("New Address");
        checkoutPage.clickBillingContinue();

        Assert.assertTrue(
                checkoutPage.areBillingValidationErrorsDisplayed(),
                "Validation should appear when required billing fields are empty.");
    }

    // TC-CHK-06: Verify checkout opens without placing an order.
    @Test(groups = "System")
    public void verifyCheckoutFlowWithoutPlacingOrder()
            throws IOException {

        CheckoutPage checkoutPage = openCheckout("TC-CHK-06");

        Assert.assertTrue(
                checkoutPage.isBillingAddressDropdownDisplayed(),
                "Checkout should remain in the checkout flow.");

        Assert.assertFalse(
                checkoutPage.isOrderCompletedDisplayed(),
                "This test must not place the final order.");
    }

    // TC-CHK-09: Use the saved billing address and place the order.
    @Test(groups = "Integration")
    public void verifyProductAndTotalOnOrderReview()
            throws IOException {

        CheckoutPage checkoutPage = openCheckout("TC-CHK-09");

        proceedToOrderReview("TC-CHK-09", checkoutPage);

        String expectedProduct =
                getTestData("TC-CHK-09", "ProductName");

        Assert.assertTrue(
                checkoutPage.isProductPresentOnOrderReview(expectedProduct),
                "Expected product is missing from order review.");

        Assert.assertTrue(
                checkoutPage.isOrderTotalDisplayed(),
                "Order total is missing from order review.");

        confirmAndVerifyOrder(checkoutPage);
    }

    // TC-CHK-10: Enter a new billing address and place the order.
    @Test(groups = "System")
    public void completeOrderSuccessfully()
            throws IOException {

        CheckoutPage checkoutPage = openCheckout("TC-CHK-10");

        proceedToOrderReview("TC-CHK-10", checkoutPage);

        String expectedProduct =
                getTestData("TC-CHK-10", "ProductName");

        Assert.assertTrue(
                checkoutPage.isProductPresentOnOrderReview(expectedProduct),
                "Expected product is missing from order review.");

        Assert.assertTrue(
                checkoutPage.isOrderTotalDisplayed(),
                "Order total is missing from order review.");

        confirmAndVerifyOrder(checkoutPage);
    }
}
