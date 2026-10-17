package ecom.bitm.demowebshop.baseClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class baseclass {

	protected WebDriver driver;
	protected Properties properties;

	// Read configuration and start the selected browser
	@BeforeMethod(alwaysRun = true)
	public void setUp(ITestContext context) throws IOException {

		properties = new Properties();

		String configPath = System.getProperty("user.dir") + "/src/main/resources/CommonData.properties";

		try (FileInputStream fis = new FileInputStream(configPath)) {
			properties.load(fis);
		}

		// Get the browser from TestNG XML or properties
		String browser = context.getCurrentXmlTest().getParameter("browser");

		if (browser == null || browser.isBlank()) {
			browser = properties.getProperty("browser", "chrome");
		}

		browser = browser.trim().toLowerCase();

		// Start the selected browser
		switch (browser) {
		case "chrome":
			ChromeOptions options = new ChromeOptions();
			options.setPageLoadStrategy(PageLoadStrategy.EAGER);
			driver = new ChromeDriver(options);
			break;

		case "firefox":
			driver = new FirefoxDriver();
			break;

		case "edge":
			driver = new EdgeDriver();
			break;

		default:
			throw new IllegalArgumentException("Unsupported browser: " + browser);
		}

		// Maximize the browser window
		driver.manage().window().maximize();

		// Configure implicit wait
		int implicitWait = Integer.parseInt(properties.getProperty("implicitWait", "10"));

		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));

		// Configure page load timeout
		int pageLoadTimeout = Integer.parseInt(properties.getProperty("pageLoadTimeout", "30"));

		driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));

		// Open the application
		String url = properties.getProperty("url");

		if (url == null || url.isBlank()) {
			throw new IllegalStateException("Application URL is missing in CommonData.properties");
		}

		try {
			driver.get(url);
		} catch (org.openqa.selenium.TimeoutException e) {
			if (!driver.getCurrentUrl().startsWith(url)) {
				throw e;
			}
		}
	}

	// Take a screenshot when a Negative test fails, then close the browser

	@AfterMethod(alwaysRun = true)
	public void tearDown(ITestResult result) {

		try {
			if (driver != null) {

				boolean isNegativeTest = false;

				for (String group : result.getMethod().getGroups()) {
					if (group.equalsIgnoreCase("Negative")) {
						isNegativeTest = true;
						break;
					}
				}

				if (isNegativeTest) {

					Path folder = Paths.get(System.getProperty("user.dir"), "Screenshots");

					Files.createDirectories(folder);

					String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());

					String testName = result.getMethod().getMethodName().replaceAll("[^a-zA-Z0-9_-]", "_");

					File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

					Path destination = folder.resolve(testName + "_" + timestamp + ".png");

					Files.copy(source.toPath(), destination);

					System.out.println("Screenshot saved: " + destination);
				}

			}
		} catch (Exception e) {
			System.out.println("Screenshot could not be saved: " + e.getMessage());
		} finally {
			if (driver != null) {
				driver.quit();
				driver = null;
			}
		}
	}
}