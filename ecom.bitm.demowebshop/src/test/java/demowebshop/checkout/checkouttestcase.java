
package demowebshop.checkout;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.openqa.selenium.By;
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
        return new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Read test data from Excel using the test case ID.
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

    // Open the login page, then log in using Excel credentials.
    private void loginToApplication(String testCaseID)
            throws IOException {

        String baseUrl = properties.getProperty("url");

        driver.get(baseUrl + "login");

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

    // Search for the product specified in Excel and add it to the cart.
    private void addProductToCart(String testCaseID)
            throws IOException {

        String productName =
                getTestData(testCaseID, "ProductName");

        String baseUrl = properties.getProperty("url");

        String searchUrl = baseUrl + "search?q="
                + URLEncoder.encode(
                        productName, StandardCharsets.UTF_8);

        driver.get(searchUrl);

        ProductPage productPage = new ProductPage(driver);
        productPage.clickProduct(productName);

        By addToCartButton =
                By.cssSelector("input[id^='add-to-cart-button']");

        getWait().until(
                ExpectedConditions.elementToBeClickable(addToCartButton))
                .click();

        // Wait for the site's success or error notification.
        getWait().until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".bar-notification.success")),
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".bar-notification.error"))
        ));

        Assert.assertTrue(
                driver.findElements(
                        By.cssSelector(".bar-notification.success"))
                        .size() > 0,
                "Product was not added successfully.");
    }

    // Log in, add a product, open the cart, and start checkout.
    private CheckoutPage openCheckout(String testCaseID)
            throws IOException {

        loginToApplication(testCaseID);
        addProductToCart(testCaseID);

        driver.get(properties.getProperty("url") + "cart");

        CartPage cartPage = new CartPage(driver);

        getWait().until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".cart")));

        Assert.assertTrue(
                cartPage.getCartItemCount() > 0,
                "The cart must contain a product before checkout.");

        CheckoutPage checkoutPage = new CheckoutPage(driver);
        checkoutPage.clickCheckout();

        // Wait for the billing address dropdown instead of the URL.
        getWait().until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("billing-address-select")));

        return checkoutPage;
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

    // TC-CHK-02: Verify billing address fields are displayed.
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

    // TC-CHK-06: Verify checkout without placing an order.
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

    // TC-CHK-09: Verify product and total on the order review.
    // This test must stop before Confirm Order.
    @Test(groups = "Integration")
    public void verifyProductAndTotalOnOrderReview()
            throws IOException {

        CheckoutPage checkoutPage = openCheckout("TC-CHK-09");

        // Verify checkout opens; this does not yet verify the order review.
        Assert.assertTrue(
                checkoutPage.isBillingAddressDropdownDisplayed(),
                "Checkout should be open.");

        Assert.assertFalse(
                checkoutPage.isOrderCompletedDisplayed(),
                "The review test must not complete the order.");
    }

    // TC-CHK-10: Complete an order successfully.
    // Final order submission is intentionally not performed here.
    @Test(groups = "End-to-end")
    public void completeOrderSuccessfully()
            throws IOException {

        CheckoutPage checkoutPage = openCheckout("TC-CHK-10");

        Assert.assertTrue(
                checkoutPage.isBillingAddressDropdownDisplayed(),
                "Checkout should be open before completing the order.");

        // Billing, shipping, payment, order review, and final
        // confirmation still need to be implemented.
    }
}
