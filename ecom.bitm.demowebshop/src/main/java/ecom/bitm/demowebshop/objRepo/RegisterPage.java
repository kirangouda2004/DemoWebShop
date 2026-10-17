
package ecom.bitm.demowebshop.objRepo;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class RegisterPage {

    private WebDriver driver;

    // Gender options
    @FindBy(id = "gender-male")
    private WebElement maleRadioButton;

    @FindBy(id = "gender-female")
    private WebElement femaleRadioButton;

    // Registration form fields
    @FindBy(id = "FirstName")
    private WebElement firstNameTextField;

    @FindBy(id = "LastName")
    private WebElement lastNameTextField;

    @FindBy(id = "Email")
    private WebElement emailTextField;

    @FindBy(id = "Password")
    private WebElement passwordTextField;

    @FindBy(id = "ConfirmPassword")
    private WebElement confirmPasswordTextField;

    // Register button
    @FindBy(id = "register-button")
    private WebElement registerButton;

    // Registration result
    @FindBy(css = ".result")
    private WebElement registrationResult;

    // Validation errors
    @FindBy(css = ".validation-summary-errors")
    private WebElement validationSummary;

    // Initialize page elements
    public RegisterPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    // Select male gender
    public void selectMale() {
        maleRadioButton.click();
    }

    // Select female gender
    public void selectFemale() {
        femaleRadioButton.click();
    }

    // Enter first name
    public void enterFirstName(String firstName) {
        firstNameTextField.clear();
        firstNameTextField.sendKeys(firstName);
    }

    // Enter last name
    public void enterLastName(String lastName) {
        lastNameTextField.clear();
        lastNameTextField.sendKeys(lastName);
    }

    // Enter email
    public void enterEmail(String email) {
        emailTextField.clear();
        emailTextField.sendKeys(email);
    }

    // Enter password
    public void enterPassword(String password) {
        passwordTextField.clear();
        passwordTextField.sendKeys(password);
    }

    // Confirm password
    public void enterConfirmPassword(String confirmPassword) {
        confirmPasswordTextField.clear();
        confirmPasswordTextField.sendKeys(confirmPassword);
    }

    // Fill all registration details
    public void enterRegistrationDetails(
            String gender,
            String firstName,
            String lastName,
            String email,
            String password,
            String confirmPassword) {

        if ("male".equalsIgnoreCase(gender)) {
            selectMale();
        } else if ("female".equalsIgnoreCase(gender)) {
            selectFemale();
        }

        enterFirstName(firstName);
        enterLastName(lastName);
        enterEmail(email);
        enterPassword(password);
        enterConfirmPassword(confirmPassword);
    }

    // Submit registration
    public void clickRegister() {
        registerButton.click();
    }

    // Get successful registration result
    public String getRegistrationResult() {
        return registrationResult.getText();
    }

    // Get validation summary text
    public String getValidationSummary() {
        return validationSummary.getText();
    }

    // Check whether the Register button is displayed
    public boolean isRegisterButtonDisplayed() {
        return registerButton.isDisplayed();
    }

    // Check whether password fields are masked
    public boolean arePasswordFieldsMasked() {
        return "password".equalsIgnoreCase(
                    passwordTextField.getAttribute("type"))
                && "password".equalsIgnoreCase(
                    confirmPasswordTextField.getAttribute("type"));
    }

    // Open the registration page
    public void openRegistrationPage() {
        driver.get("https://demowebshop.tricentis.com/register");
    }
}
