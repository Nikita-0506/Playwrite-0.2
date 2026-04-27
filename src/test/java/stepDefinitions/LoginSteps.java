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
	private final int timeout = 15000; // 15 seconds in milliseconds
	private final int navTimeout = Integer.parseInt(System.getProperty("navTimeoutMs",
		ConfigReader.get("navTimeoutMs") != null ? ConfigReader.get("navTimeoutMs") : "45000"));
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

	private Locator resolveVisibleLocator(String[] selectors, String elementName) {
		Page page = DriverManager.getPage();
		for (String selector : selectors) {
			try {
				Locator candidate = page.locator(selector).first();
				candidate.waitFor(new Locator.WaitForOptions()
					.setState(WaitForSelectorState.VISIBLE)
					.setTimeout(3000));
				return candidate;
			} catch (Exception ignored) {
				// Try next selector.
			}
		}

		for (Frame frame : page.frames()) {
			for (String selector : selectors) {
				try {
					Locator candidate = frame.locator(selector).first();
					candidate.waitFor(new Locator.WaitForOptions()
						.setState(WaitForSelectorState.VISIBLE)
						.setTimeout(2000));
					log.info("Resolved " + elementName + " inside frame: " + frame.url());
					return candidate;
				} catch (Exception ignored) {
					// Try next selector/frame.
				}
			}
		}

		throw new AssertionError("Could not find visible " + elementName + " on page or frames. URL: " + page.url());
	}

	@Given("I am on InsureCRM page")
	public void i_am_on_suitecrm_page() {
		PlaywrightException lastError = null;
		try {
			Page page = DriverManager.getPage();
			for (int attempt = 1; attempt <= 2; attempt++) {
				try {
					page.navigate(url, new Page.NavigateOptions()
						.setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
						.setTimeout(navTimeout));

					String usernameLocator = base.toPlaywrightLocator(base.getLocator("loginPage.userNameTextBox"));
					page.locator(usernameLocator).first().waitFor(new Locator.WaitForOptions()
						.setState(WaitForSelectorState.VISIBLE)
						.setTimeout(navTimeout));

					log.info("Successfully opened login page on attempt " + attempt);
					return;
				} catch (PlaywrightException e) {
					lastError = e;
					log.warn("Attempt " + attempt + " to open login page failed: " + e.getMessage());

					if (attempt < 2) {
						try {
							if (page != null && !page.isClosed()) {
								page.close();
							}
						} catch (Exception ignored) {
							// Ignore close failures and proceed with a fresh page.
						}
						page = DriverManager.getContext().newPage();
					}
				}
			}

			String reason = lastError != null ? lastError.getMessage() : "Unknown navigation error";
			throw new AssertionError("Failed to navigate to URL after retry: " + url + ". Reason: " + reason, lastError);
		} catch (AssertionError e) {
			throw e;
		} catch (Exception e) {
			throw new AssertionError("Failed to navigate to URL: " + url + ". Reason: " + e.getMessage(), e);
		}
	}

	@When("User enters {string}")
	public void user_enters(String username) {
		try {
			String usernameLocator = base.toPlaywrightLocator(base.getLocator("loginPage.userNameTextBox"));
			Locator usernameField = resolveVisibleLocator(new String[] {
				usernameLocator,
				"input[name='username']",
				"input[name='user_name']",
				"#username",
				"#user_name"
			}, "username field");
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
			Locator passwordField = resolveVisibleLocator(new String[] {
				passwordLocator,
				"input[name='password']",
				"input[name='user_password']",
				"#password",
				"#user_password",
				"input[type='password']"
			}, "password field");
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
			String currentUrlBeforeClick = DriverManager.getPage().url();
			if (currentUrlBeforeClick != null && !currentUrlBeforeClick.toLowerCase().contains("login")) {
				log.info("Login click skipped: user already redirected to authenticated page: " + currentUrlBeforeClick);
				return;
			}

			String loginBtnLocator = base.toPlaywrightLocator(base.getLocator("loginPage.loginButton"));
			try {
				Locator loginButton = resolveVisibleLocator(new String[] {
					loginBtnLocator,
					"button[type='submit']",
					"button:has-text('Login')",
					"button:has-text('Sign in')",
					"input[type='submit']"
				}, "login button");
				loginButton.click();
			} catch (AssertionError noButtonFound) {
				String passwordLocator = base.toPlaywrightLocator(base.getLocator("loginPage.passwordTextBox"));
				Locator passwordField = resolveVisibleLocator(new String[] {
					passwordLocator,
					"input[name='password']",
					"input[name='user_password']",
					"#password",
					"#user_password",
					"input[type='password']"
				}, "password field for submit fallback");
				passwordField.press("Enter");
				log.warn("Login button not visible; submitted login using Enter key on password field.");
			}

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
				new Page.WaitForURLOptions().setTimeout(navTimeout));
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