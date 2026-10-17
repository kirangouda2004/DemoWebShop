package ecom.bitm.demowebshop.objRepo;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginPage {

    private WebDriver driver;

    // Email input field
    @FindBy(id = "Email")
    private WebElement emailTextField;

    // Password input field
    @FindBy(id = "Password")
    private WebElement passwordTextField;

    // Remember me checkbox
    @FindBy(id = "RememberMe")
    private WebElement rememberMeCheckbox;

    // Login button
    @FindBy(css = "input.login-button")
    private WebElement loginButton;

    // Login validation error message
    @FindBy(css = ".validation-summary-errors")
    private WebElement errorMessage;

    // Forgot password link
    @FindBy(linkText = "Forgot password?")
    private WebElement forgotPasswordLink;

    // My Account link shown after successful login
    @FindBy(css = "a.account")
    private WebElement accountLink;

    // Logout link shown after successful login
    @FindBy(linkText = "Log out")
    private WebElement logoutLink;

    // Initialize the page elements
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    // Enter the registered email address
    public void enterEmail(String email) {
        emailTextField.clear();
        emailTextField.sendKeys(email);
    }

    // Enter the account password
    public void enterPassword(String password) {
        passwordTextField.clear();
        passwordTextField.sendKeys(password);
    }

    // Click the Remember me checkbox if it is not selected
    public void selectRememberMe() {
        if (!rememberMeCheckbox.isSelected()) {
            rememberMeCheckbox.click();
        }
    }

    // Unselect Remember me if it is selected
    public void unselectRememberMe() {
        if (rememberMeCheckbox.isSelected()) {
            rememberMeCheckbox.click();
        }
    }

    // Submit the login form
    public void clickLogin() {
        loginButton.click();
    }

    // Enter credentials and submit the login form
    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLogin();
    }

    // Get the displayed login error message
    public String getErrorMessage() {
        return errorMessage.getText();
    }

    // Check whether the login button is displayed
    public boolean isLoginButtonDisplayed() {
        return loginButton.isDisplayed();
    }

    // Check whether the email field is displayed
    public boolean isEmailFieldDisplayed() {
        return emailTextField.isDisplayed();
    }

    // Check whether the password field is displayed
    public boolean isPasswordFieldDisplayed() {
        return passwordTextField.isDisplayed();
    }

    // Check whether the password field masks its contents
    public String getPasswordFieldType() {
        return passwordTextField.getAttribute("type");
    }

    // Open the Forgot password page
    public void clickForgotPassword() {
        forgotPasswordLink.click();
    }

    // Check whether the customer account link is displayed
    public boolean isAccountLinkDisplayed() {
        return !driver.findElements(
                org.openqa.selenium.By.cssSelector("a.account")).isEmpty();
    }

    // Check whether the Logout link is displayed
    public boolean isLogoutLinkDisplayed() {
        return !driver.findElements(
                org.openqa.selenium.By.linkText("Log out")).isEmpty();
    }

    // Get the account email displayed after login
    public String getLoggedInEmail() {
        return accountLink.getText();
    }

    // Click Logout after successful login
    public void clickLogout() {
        logoutLink.click();
    }
}
