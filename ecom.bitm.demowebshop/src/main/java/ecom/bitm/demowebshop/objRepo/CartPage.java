
package ecom.bitm.demowebshop.objRepo;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CartPage {

    private WebDriver driver;

    // Shopping cart table rows
    @FindBy(css = ".cart-item-row")
    private List<WebElement> cartItems;

    // Product names in the cart
    @FindBy(css = ".cart-item-row .product a")
    private List<WebElement> productNames;

    // Quantity fields
    @FindBy(css = ".cart-item-row input.qty-input")
    private List<WebElement> quantityFields;

    // Remove item checkboxes
    @FindBy(css = ".cart-item-row input[name='removefromcart']")
    private List<WebElement> removeCheckboxes;

    // Update shopping cart button
    @FindBy(name = "updatecart")
    private WebElement updateCartButton;

    // Continue shopping button
    @FindBy(name = "continueshopping")
    private WebElement continueShoppingButton;

    // Checkout button
    @FindBy(id = "checkout")
    private WebElement checkoutButton;

    // Terms and conditions checkbox
    @FindBy(id = "termsofservice")
    private WebElement termsCheckbox;

    // Empty cart message
    @FindBy(css = ".order-summary-content")
    private WebElement cartMessage;

    public CartPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    // Get the number of products in the cart
    public int getCartItemCount() {
        return cartItems.size();
    }

    // Check whether the cart contains products
    public boolean isCartNotEmpty() {
        return !cartItems.isEmpty();
    }

    // Get product names from the cart
    public List<String> getCartProductNames() {
        return productNames.stream()
                .map(WebElement::getText)
                .toList();
    }

    // Update the quantity of a product
    public void updateQuantity(int index, String quantity) {
        quantityFields.get(index).clear();
        quantityFields.get(index).sendKeys(quantity);
    }

    // Select a product for removal
    public void selectItemForRemoval(int index) {

        WebDriverWait wait = new WebDriverWait(
                driver, Duration.ofSeconds(10));

        List<WebElement> checkboxes = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                        By.cssSelector(
                                ".cart-item-row input[name='removefromcart']"
                        )
                )
        );

        if (index < 0 || index >= checkboxes.size()) {
            throw new IndexOutOfBoundsException(
                    "Invalid cart item index: " + index);
        }

        WebElement checkbox = checkboxes.get(index);

        if (!checkbox.isSelected()) {
            wait.until(
                    ExpectedConditions.elementToBeClickable(checkbox)
            ).click();
        }

        if (!checkbox.isSelected()) {
            throw new IllegalStateException(
                    "The product removal checkbox was not selected.");
        }
    }

    // Submit the cart update
    public void clickUpdateCart() {

        WebDriverWait wait = new WebDriverWait(
                driver, Duration.ofSeconds(10));

        wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.name("updatecart")
                )
        ).click();
    }

    // Remove an item by its position
    public void removeItem(int index) {
        selectItemForRemoval(index);
        clickUpdateCart();
    }

    // Continue shopping
    public void clickContinueShopping() {

        WebDriverWait wait = new WebDriverWait(
                driver, Duration.ofSeconds(10));

        wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.name("continueshopping")
                )
        ).click();
    }

    // Accept terms and conditions if not already selected
    public void acceptTermsAndConditions() {
        if (!termsCheckbox.isSelected()) {
            termsCheckbox.click();
        }
    }

    // Proceed to checkout
    public void clickCheckout() {
        acceptTermsAndConditions();
        checkoutButton.click();
    }

    // Get the cart message or empty-cart information
    public String getCartMessage() {
        return cartMessage.getText();
    }

    // Get the current page URL
    public String getCurrentPageUrl() {
        return driver.getCurrentUrl();
    }
}
