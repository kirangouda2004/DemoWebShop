
package demowebshop.login;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ecom.bitm.demowebshop.baseClass.baseclass;
import ecom.bitm.demowebshop.objRepo.LoginPage;
import ecom.bitm.demowebshop.objRepo.RegisterPage;
import ecom.bitm.demowebshop.utility.ExcelUtility;

public class signup_logintestcase extends baseclass {

    private static final String EXCEL_PATH =
            System.getProperty("user.dir")
            + "/src/test/resources/demowebshop_testdata.xlsx";

    private static final String SHEET_NAME = "SignupLoginData";

    // Open a page using the URL configured in CommonData.properties
    private void openPage(String page) {
        String baseUrl = properties.getProperty("url");

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "Application URL is missing in CommonData.properties");
        }

        driver.get(baseUrl.replaceAll("/+$", "") + "/" + page);
    }

    // Read matching test data from Excel
    private static List<Map<String, String>> readTestData(
            String testCaseID) throws IOException {

        ExcelUtility excel = new ExcelUtility(EXCEL_PATH, SHEET_NAME);

        try {
            return excel.getRowsByTestCaseID(testCaseID);
        } finally {
            excel.closeWorkbook();
        }
    }

    // Convert Excel rows into TestNG data provider format
    private static Object[][] convertToTestData(
            List<Map<String, String>> rows) {

        Object[][] data = new Object[rows.size()][1];

        for (int i = 0; i < rows.size(); i++) {
            data[i][0] = rows.get(i);
        }

        return data;
    }

    @DataProvider(name = "registrationData")
    public Object[][] registrationData() throws IOException {
        return convertToTestData(readTestData("TC_ID02"));
    }

    @DataProvider(name = "blankRegistrationData")
    public Object[][] blankRegistrationData() throws IOException {
        return convertToTestData(readTestData("TC_ID03"));
    }

    @DataProvider(name = "invalidEmailData")
    public Object[][] invalidEmailData() throws IOException {
        return convertToTestData(readTestData("TC_ID04"));
    }

    @DataProvider(name = "existingEmailData")
    public Object[][] existingEmailData() throws IOException {
        return convertToTestData(readTestData("TC_ID05"));
    }

    @DataProvider(name = "validLoginData")
    public Object[][] validLoginData() throws IOException {
        return convertToTestData(readTestData("TC_ID07"));
    }

    @DataProvider(name = "invalidLoginData")
    public Object[][] invalidLoginData() throws IOException {
        return convertToTestData(readTestData("TC_ID08"));
    }

    // TC_ID01: Verify registration page opens
    @Test(groups = {"Smoke"})
    public void TC_ID01_verifyRegistrationPageOpens() {

        openPage("register");

        Assert.assertTrue(
                driver.getCurrentUrl().contains("/register"),
                "Registration page did not open.");
    }

    // TC_ID02: Register with valid details
    @Test(
        dataProvider = "registrationData",
        groups = {"Smoke", "Functional"}
    )
    public void TC_ID02_registerWithValidDetails(
            Map<String, String> data) {

        RegisterPage registerPage = new RegisterPage(driver);

        openPage("register");

        // Generate a different email address for each execution
        String uniqueEmail = "kiran"
                + System.currentTimeMillis()
                + "@example.com";

        registerPage.enterRegistrationDetails(
                data.get("Gender"),
                data.get("FirstName"),
                data.get("LastName"),
                uniqueEmail,
                data.get("Password"),
                data.get("ConfirmPassword")
        );

        registerPage.clickRegister();

        Assert.assertTrue(
                driver.getPageSource().contains("Your registration completed"),
                "Registration was not successful.");
    }

    // TC_ID03: Register with blank required fields
    @Test(
        dataProvider = "blankRegistrationData",
        groups = {"Functional", "Negative"}
    )
    public void TC_ID03_registerWithBlankFields(
            Map<String, String> data) {

        RegisterPage registerPage = new RegisterPage(driver);

        openPage("register");
        registerPage.clickRegister();

        boolean validationDisplayed = !driver.findElements(
                By.cssSelector(
                        ".field-validation-error, .validation-summary-errors"))
                .isEmpty();

        Assert.assertTrue(
                validationDisplayed,
                "Expected registration validation messages were not displayed.");
    }

    // TC_ID04: Register with an invalid email format
    @Test(
        dataProvider = "invalidEmailData",
        groups = {"Functional", "Negative"}
    )
    public void TC_ID04_registerWithInvalidEmail(
            Map<String, String> data) {

        RegisterPage registerPage = new RegisterPage(driver);

        openPage("register");

        registerPage.enterRegistrationDetails(
                data.get("Gender"),
                data.get("FirstName"),
                data.get("LastName"),
                data.get("Email"),
                data.get("Password"),
                data.get("ConfirmPassword")
        );

        registerPage.clickRegister();

        boolean validationDisplayed = !driver.findElements(
                By.cssSelector(
                        ".field-validation-error, .validation-summary-errors"))
                .isEmpty();

        boolean stillOnRegistrationPage =
                driver.getCurrentUrl().contains("/register");

        Assert.assertTrue(
                validationDisplayed || stillOnRegistrationPage,
                "Invalid email was unexpectedly accepted.");
    }

    // TC_ID05: Register with an already registered email
    @Test(
        dataProvider = "existingEmailData",
        groups = {"Functional", "Negative"}
    )
    public void TC_ID05_registerWithExistingEmail(
            Map<String, String> data) {

        RegisterPage registerPage = new RegisterPage(driver);

        String existingEmail = data.get("Email");

        Assert.assertTrue(
                existingEmail != null
                && !existingEmail.trim().isEmpty()
                && !existingEmail.startsWith("REPLACE_WITH"),
                "Enter an already registered email in the Excel sheet.");

        openPage("register");

        registerPage.enterRegistrationDetails(
                data.get("Gender"),
                data.get("FirstName"),
                data.get("LastName"),
                existingEmail,
                data.get("Password"),
                data.get("ConfirmPassword")
        );

        registerPage.clickRegister();

        String pageSource = driver.getPageSource().toLowerCase();

        Assert.assertTrue(
                pageSource.contains("already exists"),
                "Expected duplicate-email validation was not displayed.");
    }

    // TC_ID06: Verify login page opens
    @Test(groups = {"Smoke"})
    public void TC_ID06_verifyLoginPageOpens() {

        openPage("login");

        LoginPage loginPage = new LoginPage(driver);

        Assert.assertTrue(
                loginPage.isLoginButtonDisplayed(),
                "Login page did not open correctly.");

        Assert.assertTrue(
                loginPage.isEmailFieldDisplayed(),
                "Email field is not displayed.");
    }

    // TC_ID07: Login with valid credentials
    @Test(
        dataProvider = "validLoginData",
        groups = {"Smoke", "Functional"}
    )
    public void TC_ID07_loginWithValidCredentials(
            Map<String, String> data) {

        LoginPage loginPage = new LoginPage(driver);

        String email = data.get("Email");
        String password = data.get("Password");

        Assert.assertTrue(
                email != null && !email.startsWith("REPLACE_WITH")
                && !email.trim().isEmpty(),
                "Enter a registered email in the Excel sheet.");

        Assert.assertTrue(
                password != null && !password.startsWith("REPLACE_WITH")
                && !password.trim().isEmpty(),
                "Enter the correct account password in the Excel sheet.");

        openPage("login");

        loginPage.login(email, password);

        Assert.assertTrue(
                loginPage.isLogoutLinkDisplayed(),
                "Login failed. Check the registered email and password.");
    }

    // TC_ID08: Invalid and blank login credentials
    @Test(
        dataProvider = "invalidLoginData",
        groups = {"Functional", "Negative"}
    )
    public void TC_ID08_loginWithInvalidOrBlankCredentials(
            Map<String, String> data) {

        LoginPage loginPage = new LoginPage(driver);

        openPage("login");

        loginPage.login(
                data.get("Email"),
                data.get("Password")
        );

        Assert.assertFalse(
                loginPage.isLogoutLinkDisplayed(),
                "Login should not succeed with invalid or blank credentials.");

        boolean validationDisplayed = !driver.findElements(
                By.cssSelector(
                        ".validation-summary-errors, .field-validation-error"))
                .isEmpty();

        Assert.assertTrue(
                validationDisplayed,
                "Expected login validation message was not displayed.");
    }
}
