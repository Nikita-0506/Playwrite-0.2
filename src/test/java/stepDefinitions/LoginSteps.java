package stepDefinitions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import utilities.BaseClass;
import utilities.ConfigReader;
import utilities.DriverManager;

//@Author: neha.verma@inadev.com
//@Date: 12 July 2025
//@Desc: This class holds all the login related step definitions using Playwright

public class LoginSteps {

	String url = "";
	private static final Logger log = LogManager.getLogger(LoginSteps.class);
	private final int timeout = 30000; // 30 seconds — increased for slower CI/Jenkins environments
	BaseClass base;

	public LoginSteps() {
		if (url == null || url.trim().isEmpty()) {
			// Get environment from config file (can be overridden by system property)
			String environment = System.getProperty("env");
			if (environment == null || environment.trim().isEmpty()) {
				environment = ConfigReader.get("env");
			}
			log.info("Environment selected: " + environment);
			
			if (environment != null && environment.equalsIgnoreCase("QA")) {
				url = ConfigReader.get("qaURL");
				log.info("Using QA URL: " + url);
			} else if (environment != null && environment.equalsIgnoreCase("dev")){
				url = ConfigReader.get("devURL");
				log.info("Using Dev URL: " + url);
			} else {
				url = ConfigReader.get("prodURL");
				log.info("Using Prod URL: " + url);
			}
		}
		log.info("URL: " + url);
		base = new BaseClass();
	}

	@Given("I am on InsureCRM page")
	public void i_am_on_suitecrm_page() {
		try {
			DriverManager.getPage().navigate(url);
			// Wait for page to load
			DriverManager.getPage().waitForLoadState(LoadState.NETWORKIDLE);
		} catch (PlaywrightException e) {
			// Attach error message to report
			log.info("Failed to open URL: " + url);
			log.info("Exception message: " + e.getMessage());

			// Fail the step explicitly with a meaningful message
			throw new AssertionError("Failed to navigate to URL: " + url + ". Reason: " + e.getMessage(), e);
		}
	}

	@When("User enters {string}")
	public void user_enters(String username) {
		try {
			String usernameLocator = base.toPlaywrightLocator(base.getLocator("loginPage.userNameTextBox"));
			Locator usernameField = DriverManager.getPage().locator(usernameLocator);
			usernameField.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.VISIBLE)
				.setTimeout(timeout));
			usernameField.clear();
			usernameField.fill(username);
			log.info("Entered username: " + username);
		} catch (Exception e) {
			log.error("Failed to enter username.", e);
			throw new AssertionError("Failed to enter username. Reason: " + e.getMessage(), e);
		}
	}

	@When("User enters password {string}")
	public void user_enters_password(String password) {
		try {
			String passwordLocator = base.toPlaywrightLocator(base.getLocator("loginPage.passwordTextBox"));
			Locator passwordField = DriverManager.getPage().locator(passwordLocator);
			passwordField.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.VISIBLE)
				.setTimeout(timeout));
			passwordField.clear();
			passwordField.fill(password);
			log.info("Entered password.");
		} catch (Exception e) {
			log.error("Failed to enter password.", e);
			throw new AssertionError("Failed to enter password. Reason: " + e.getMessage(), e);
		}
	}

	@When("User clicks on the login button")
	public void user_clicks_on_the_login_button() {
		try {
			String loginBtnLocator = base.toPlaywrightLocator(base.getLocator("loginPage.loginButton"));
			Locator loginButton = DriverManager.getPage().locator(loginBtnLocator);
			loginButton.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.VISIBLE)
				.setTimeout(timeout));
			loginButton.click();

			log.info("User clicked on login button");
		} catch (Exception e) {
			log.error("Login failed or post-login page did not load.", e);
			throw new AssertionError("Login failed or post-login page did not load. Reason: " + e.getMessage(), e);
		}
	}

	@Then("User is logged in")
	public void user_is_logged_in() {
		try {
			// Wait for URL to change or a post-login element to appear
			DriverManager.getPage().waitForURL(url -> !url.contains("#/Login"), 
				new Page.WaitForURLOptions().setTimeout(timeout));
			log.info("User logged in and navigated to home page.");
		} catch (Exception e) {
			log.error("Login failed or post-login page did not load.", e);
			throw new AssertionError("Login failed or post-login page did not load. Reason: " + e.getMessage(), e);
		}
	}

	@Then("User should see an error message")
	public void user_should_see_an_error_message() {
		try {
			String errorLocator = base.toPlaywrightLocator(base.getLocator("loginPage.errorLabel"));
			Locator errorElement = DriverManager.getPage().locator(errorLocator);
			errorElement.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.VISIBLE)
				.setTimeout(timeout));
			String errorMessage = errorElement.textContent();
			Assert.assertTrue(!errorMessage.isEmpty(), "Expected error message but none displayed");
			log.info("Error message displayed: " + errorMessage);
		} catch (Exception e) {
			log.error("Error message not found when expected", e);
			throw new AssertionError("Error message validation failed. Reason: " + e.getMessage(), e);
		}
	}

	@Then("User should see an alert")
	public void user_should_see_an_alert() {
		try {
			String alertLocator = base.toPlaywrightLocator(base.getLocator("loginPage.alertLabel"));
			Locator alertElement = DriverManager.getPage().locator(alertLocator);
			alertElement.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.VISIBLE)
				.setTimeout(timeout));
			String alertMessage = alertElement.textContent();
			Assert.assertTrue(!alertMessage.isEmpty(), "Expected alert but none displayed");
			log.info("Alert displayed: " + alertMessage);
		} catch (Exception e) {
			log.error("Alert not found when expected", e);
			throw new AssertionError("Alert validation failed. Reason: " + e.getMessage(), e);
		}
	}

}