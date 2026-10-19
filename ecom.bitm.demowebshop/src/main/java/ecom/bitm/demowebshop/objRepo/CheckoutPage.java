
package ecom.bitm.demowebshop.objRepo;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

public class CheckoutPage {

    private WebDriver driver;

    // Checkout terms and conditions
    @FindBy(id = "termsofservice")
    private WebElement termsCheckbox;

    // Checkout button on the shopping cart page
    @FindBy(id = "checkout")
    private WebElement checkoutButton;

    // Billing address dropdown
    @FindBy(id = "billing-address-select")
    private WebElement billingAddressDropdown;

    // Billing address fields
    @FindBy(id = "BillingNewAddress_FirstName")
    private WebElement firstNameField;

    @FindBy(id = "BillingNewAddress_LastName")
    private WebElement lastNameField;

    @FindBy(id = "BillingNewAddress_Email")
    private WebElement emailField;

    @FindBy(id = "BillingNewAddress_CountryId")
    private WebElement countryDropdown;

    @FindBy(id = "BillingNewAddress_StateProvinceId")
    private WebElement stateDropdown;

    @FindBy(id = "BillingNewAddress_City")
    private WebElement cityField;

    @FindBy(id = "BillingNewAddress_Address1")
    private WebElement addressField;

    @FindBy(id = "BillingNewAddress_ZipPostalCode")
    private WebElement postalCodeField;

    @FindBy(id = "BillingNewAddress_PhoneNumber")
    private WebElement phoneField;

    // Continue buttons for checkout steps
    @FindBy(css = "input.button-1.new-address-next-step-button")
    private WebElement billingContinueButton;

    @FindBy(css = "input.button-1.payment-method-next-step-button")
    private WebElement paymentMethodContinueButton;

    @FindBy(css = "input.button-1.payment-info-next-step-button")
    private WebElement paymentInfoContinueButton;

    @FindBy(css = "input.button-1.confirm-order-next-step-button")
    private WebElement confirmOrderButton;

    // Shipping address and shipping method continue buttons
    private By shippingAddressContinueButton =
            By.cssSelector(
                    "input.button-1.shipping-address-next-step-button");

    private By shippingMethodContinueButton =
            By.cssSelector(
                    "input.button-1.shipping-method-next-step-button");

    // Order review section
    private By orderReviewSection =
            By.id("checkout-confirm-order-load");

    // Order completion information
    @FindBy(css = ".section.order-completed")
    private WebElement orderCompletedSection;

    @FindBy(css = ".order-completed .title")
    private WebElement orderCompletedMessage;

    // Validation messages
    @FindBy(css = ".field-validation-error")
    private List<WebElement> fieldValidationErrors;

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    // Accept terms and conditions when the checkbox is not selected.
    public void acceptTermsAndConditions() {
        if (!termsCheckbox.isSelected()) {
            termsCheckbox.click();
        }
    }

    // Begin checkout from the cart page.
    public void clickCheckout() {
        acceptTermsAndConditions();
        checkoutButton.click();
    }

    // Select an existing billing address or New Address.
    public void selectBillingAddress(String address) {
        new Select(billingAddressDropdown)
                .selectByVisibleText(address);
    }

    // Check whether the billing address dropdown is displayed.
    public boolean isBillingAddressDropdownDisplayed() {
        List<WebElement> elements =
                driver.findElements(By.id("billing-address-select"));

        return !elements.isEmpty() && elements.get(0).isDisplayed();
    }

    // Check whether all new billing address fields are displayed.
    public boolean areBillingFieldsDisplayed() {
        By[] billingFields = {
            By.id("BillingNewAddress_FirstName"),
            By.id("BillingNewAddress_LastName"),
            By.id("BillingNewAddress_Email"),
            By.id("BillingNewAddress_CountryId"),
            By.id("BillingNewAddress_StateProvinceId"),
            By.id("BillingNewAddress_City"),
            By.id("BillingNewAddress_Address1"),
            By.id("BillingNewAddress_ZipPostalCode"),
            By.id("BillingNewAddress_PhoneNumber")
        };

        for (By field : billingFields) {
            List<WebElement> elements = driver.findElements(field);

            if (elements.isEmpty() || !elements.get(0).isDisplayed()) {
                return false;
            }
        }

        return true;
    }

    // Enter billing address details.
    public void enterBillingAddress(
            String firstName,
            String lastName,
            String email,
            String country,
            String state,
            String city,
            String address,
            String postalCode,
            String phone) {

        firstNameField.clear();
        firstNameField.sendKeys(firstName);

        lastNameField.clear();
        lastNameField.sendKeys(lastName);

        emailField.clear();
        emailField.sendKeys(email);

        new Select(countryDropdown).selectByVisibleText(country);

        // Select a state only when a value is provided.
        if (state != null && !state.isBlank()) {
            new Select(stateDropdown).selectByVisibleText(state);
        }

        cityField.clear();
        cityField.sendKeys(city);

        addressField.clear();
        addressField.sendKeys(address);

        postalCodeField.clear();
        postalCodeField.sendKeys(postalCode);

        phoneField.clear();
        phoneField.sendKeys(phone);
    }

    // Continue after entering billing details.
    public void clickBillingContinue() {
        billingContinueButton.click();
    }

    // Check whether billing validation messages are displayed.
    public boolean areBillingValidationErrorsDisplayed() {
        List<WebElement> errors = driver.findElements(
                By.cssSelector(
                        ".field-validation-error, .validation-summary-errors"));

        for (WebElement error : errors) {
            if (error.isDisplayed()
                    && !error.getText().trim().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    // Get the number of visible field validation errors.
    public int getValidationErrorCount() {
        int count = 0;

        for (WebElement error : fieldValidationErrors) {
            if (error.isDisplayed()
                    && !error.getText().trim().isEmpty()) {
                count++;
            }
        }

        return count;
    }

    // Continue after selecting the shipping address.
    public void clickShippingAddressContinue() {
        driver.findElement(shippingAddressContinueButton).click();
    }

    // Select the Ground shipping option.
    public void selectGroundShipping() {
        driver.findElement(By.xpath(
                "//label[contains(normalize-space(.), 'Ground')]"))
                .click();
    }

    // Continue after selecting a shipping method.
    public void clickShippingMethodContinue() {
        driver.findElement(shippingMethodContinueButton).click();
    }

    // Select Cash On Delivery when available.
    public void selectCashOnDelivery() {
        driver.findElement(By.xpath(
                "//label[contains(normalize-space(.), 'Cash On Delivery')]"))
                .click();
    }

    // Continue through the payment method step.
    public void clickPaymentMethodContinue() {
        paymentMethodContinueButton.click();
    }

    // Continue through the payment information step.
    public void clickPaymentInfoContinue() {
        paymentInfoContinueButton.click();
    }

    // Check whether the order review section is displayed.
    public boolean isOrderReviewDisplayed() {
        List<WebElement> elements =
                driver.findElements(orderReviewSection);

        return !elements.isEmpty() && elements.get(0).isDisplayed();
    }

    // Check the expected product name on the order review page.
    public boolean isProductPresentOnOrderReview(String productName) {
        return driver.findElement(By.tagName("body"))
                .getText().contains(productName);
    }

    // Check whether the order review displays a total.
    public boolean isOrderTotalDisplayed() {
        String pageText = driver.findElement(By.tagName("body"))
                .getText();

        return pageText.contains("Total:");
    }

    // Confirm the order.
    public void clickConfirmOrder() {
        confirmOrderButton.click();
    }

    // Check whether the order completion section is displayed.
    public boolean isOrderCompletedDisplayed() {
        List<WebElement> sections = driver.findElements(
                By.cssSelector(".section.order-completed"));

        for (WebElement section : sections) {
            if (section.isDisplayed()) {
                return true;
            }
        }

        return false;
    }

    // Read the order number from the actual confirmation page text.
    public String getOrderNumber() {
        String pageText = driver.findElement(By.tagName("body"))
                .getText();

        Pattern pattern = Pattern.compile(
                "Order\\s+number\\s*:\\s*(\\S+)",
                Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(pageText);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        throw new AssertionError(
                "Order number was not found on the order confirmation page.");
    }

    // Get the order completion message.
    public String getOrderCompletedMessage() {
        return orderCompletedMessage.getText();
    }

    // Get the current page URL.
    public String getCurrentPageUrl() {
        return driver.getCurrentUrl();
    }
}
