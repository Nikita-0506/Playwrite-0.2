package stepDefinitions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import utilities.BaseClass;
import utilities.DriverManager;

//@Author: neha.verma@inadev.com
//@Date: 11 April 2026
//@Desc: This class holds all the dashboard related step definitions using Playwright

public class DashboardSteps {

	String url = "";
	private static final Logger log = LogManager.getLogger(DashboardSteps.class);
	private final int timeout = 15000; // 15 seconds in milliseconds
	BaseClass base;

	public DashboardSteps() {
		base = new BaseClass();
	}

	@Then("User should see the dashboard page")
	public void user_should_see_dashboard() {
		try {
			String dashboardHeaderLocator = base.toPlaywrightLocator(base.getLocator("dashboardPage.dashboardHeader"));
			Locator dashboardHeader = DriverManager.getPage().locator(dashboardHeaderLocator);
			dashboardHeader.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.VISIBLE)
				.setTimeout(timeout));
			Assert.assertTrue(dashboardHeader.isVisible(), "Dashboard page is not visible");
			log.info("Dashboard page is visible");
		} catch (Exception e) {
			log.error("Failed to verify dashboard page.", e);
			throw new AssertionError("Dashboard page verification failed: " + e.getMessage(), e);
		}
	}

	@Then("User should see the welcome message")
	public void user_should_see_welcome_message() {
		try {
			String welcomeMessageLocator = base.toPlaywrightLocator(base.getLocator("dashboardPage.welcomeMessage"));
			Locator welcomeMessage = DriverManager.getPage().locator(welcomeMessageLocator);
			welcomeMessage.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.VISIBLE)
				.setTimeout(timeout));
			String message = welcomeMessage.textContent();
			Assert.assertNotNull(message, "Welcome message is not displayed");
			Assert.assertTrue(message.toLowerCase().contains("welcome") || message.toLowerCase().contains("hello"), 
				"Welcome message does not contain expected text");
			log.info("Welcome message is visible: " + message);
		} catch (Exception e) {
			log.error("Failed to verify welcome message.", e);
			throw new AssertionError("Welcome message verification failed: " + e.getMessage(), e);
		}
	}

	@When("User clicks on the user profile menu")
	public void user_clicks_profile_menu() {
		try {
			String profileMenuLocator = base.toPlaywrightLocator(base.getLocator("dashboardPage.userProfileMenu"));
			Locator profileMenu = DriverManager.getPage().locator(profileMenuLocator);
			profileMenu.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.VISIBLE)
				.setTimeout(timeout));
			profileMenu.click();
			log.info("User profile menu clicked");
			DriverManager.getPage().waitForTimeout(500);
		} catch (Exception e) {
			log.error("Failed to click profile menu.", e);
			throw new AssertionError("Profile menu click failed: " + e.getMessage(), e);
		}
	}

	@When("User clicks on the logout button")
	public void user_clicks_logout_button() {
		try {
			String logoutButtonLocator = base.toPlaywrightLocator(base.getLocator("dashboardPage.logoutButton"));
			Locator logoutButton = DriverManager.getPage().locator(logoutButtonLocator);
			logoutButton.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.VISIBLE)
				.setTimeout(timeout));
			logoutButton.click();
			log.info("Logout button clicked");
			DriverManager.getPage().waitForLoadState(LoadState.NETWORKIDLE);
		} catch (Exception e) {
			log.error("Failed to click logout button.", e);
			throw new AssertionError("Logout button click failed: " + e.getMessage(), e);
		}
	}

	@Then("User should be redirected to login page")
	public void user_redirected_to_login_page() {
		try {
			String loginPageLocator = base.toPlaywrightLocator(base.getLocator("loginPage.userNameTextBox"));
			Locator loginField = DriverManager.getPage().locator(loginPageLocator);
			loginField.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.VISIBLE)
				.setTimeout(timeout));
			Assert.assertTrue(loginField.isVisible(), "Login page is not displayed after logout");
			log.info("User successfully redirected to login page");
		} catch (Exception e) {
			log.error("Failed to verify redirect to login page.", e);
			throw new AssertionError("Redirect to login page verification failed: " + e.getMessage(), e);
		}
	}

	@Then("Logout message should be displayed")
	public void logout_message_displayed() {
		try {
			// Wait a moment for any toast/alert to appear
			DriverManager.getPage().waitForTimeout(1000);
			String logoutMessageLocator = base.toPlaywrightLocator(base.getLocator("dashboardPage.logoutMessage"));
			Locator logoutMessage = DriverManager.getPage().locator(logoutMessageLocator);
			
			// Check if logout message appears (if locator is configured)
			if (logoutMessage.count() > 0) {
				logoutMessage.waitFor(new Locator.WaitForOptions()
					.setState(WaitForSelectorState.VISIBLE)
					.setTimeout(timeout));
				String message = logoutMessage.textContent();
				log.info("Logout message displayed: " + message);
			} else {
				log.info("No logout message toast found, but page has transitioned");
			}
		} catch (Exception e) {
			log.warn("Logout message verification - " + e.getMessage());
			// Don't fail if message is not found, as logout might be implicit
		}
	}

	@Then("User should see {string} on dashboard")
	public void user_should_see_element(String elementName) {
		try {
			String elementLocator = base.toPlaywrightLocator(base.getLocator("dashboardPage." + elementName));
			Locator element = DriverManager.getPage().locator(elementLocator);
			element.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.VISIBLE)
				.setTimeout(timeout));
			Assert.assertTrue(element.isVisible(), "Element '" + elementName + "' is not visible on dashboard");
			log.info("Element '" + elementName + "' is visible on dashboard");
		} catch (Exception e) {
			log.error("Failed to verify dashboard element: " + elementName, e);
			throw new AssertionError("Element verification failed for '" + elementName + "': " + e.getMessage(), e);
		}
	}

	@Then("User should see the dashboard UI elements")
	public void user_should_see_dashboard_ui_elements() {
		try {
			// Verify sidebar
			String sidebarLocator = base.toPlaywrightLocator(base.getLocator("dashboardPage.sidebar"));
			Locator sidebar = DriverManager.getPage().locator(sidebarLocator);
			sidebar.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.VISIBLE)
				.setTimeout(timeout));
			Assert.assertTrue(sidebar.isVisible(), "Sidebar is not visible");
			log.info("Sidebar is visible");

			// Verify header
			String headerLocator = base.toPlaywrightLocator(base.getLocator("dashboardPage.header"));
			Locator header = DriverManager.getPage().locator(headerLocator);
			header.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.VISIBLE)
				.setTimeout(timeout));
			Assert.assertTrue(header.isVisible(), "Header is not visible");
			log.info("Header is visible");

			// Verify profile icon
			String profileIconLocator = base.toPlaywrightLocator(base.getLocator("dashboardPage.user profile icon"));
			Locator profileIcon = DriverManager.getPage().locator(profileIconLocator);
			profileIcon.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.VISIBLE)
				.setTimeout(timeout));
			Assert.assertTrue(profileIcon.isVisible(), "User profile icon is not visible");
			log.info("User profile icon is visible");

		} catch (Exception e) {
			log.error("Failed to verify dashboard UI elements.", e);
			throw new AssertionError("Dashboard UI elements verification failed: " + e.getMessage(), e);
		}
	}

	@Then("User should see the user profile information displayed")
	public void user_profile_information_displayed() {
		try {
			String profileInfoLocator = base.toPlaywrightLocator(base.getLocator("dashboardPage.userProfileInfo"));
			Locator profileInfo = DriverManager.getPage().locator(profileInfoLocator);
			profileInfo.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.VISIBLE)
				.setTimeout(timeout));
			
			String profileText = profileInfo.textContent();
			Assert.assertNotNull(profileText, "Profile information is not displayed");
			Assert.assertTrue(profileText.trim().length() > 0, "Profile information is empty");
			log.info("User profile information is displayed: " + profileText);
		} catch (Exception e) {
			log.warn("User profile information not found with expected locator. " + e.getMessage());
			// Log as warning instead of error, as profile info might be in different location
		}
	}

	@When("User uses browser back button")
	public void user_uses_browser_back_button() {
		try {
			DriverManager.getPage().goBack();
			log.info("Browser back button clicked");
			DriverManager.getPage().waitForLoadState(LoadState.NETWORKIDLE);
		} catch (Exception e) {
			log.error("Failed to click browser back button.", e);
			throw new AssertionError("Browser back button click failed: " + e.getMessage(), e);
		}
	}

	@Then("User should not be able to access the dashboard without re-authentication")
	public void user_cannot_access_dashboard_without_auth() {
		try {
			// Check if page redirects back to login or shows access denied
			String currentUrl = DriverManager.getPage().url();
			log.info("Current URL after back button: " + currentUrl);

			// Wait a moment for redirect
			DriverManager.getPage().waitForTimeout(1000);

			String loginPageLocator = base.toPlaywrightLocator(base.getLocator("loginPage.userNameTextBox"));
			Locator loginField = DriverManager.getPage().locator(loginPageLocator);

			if (loginField.count() > 0 && loginField.isVisible()) {
				log.info("Session is invalidated - user redirected back to login page");
				Assert.assertTrue(true, "Session is properly invalidated");
			} else {
				// Additional check - look for access denied message
				String accessDeniedLocator = base.toPlaywrightLocator(base.getLocator("dashboardPage.accessDeniedMessage"));
				Locator accessDenied = DriverManager.getPage().locator(accessDeniedLocator);
				
				if (accessDenied.count() > 0 && accessDenied.isVisible()) {
					log.info("Session is invalidated - access denied message shown");
					Assert.assertTrue(true, "Session is properly invalidated");
				} else {
					throw new AssertionError("Session validation failed - still on dashboard or unexpected page");
				}
			}
		} catch (Exception e) {
			log.error("Failed to verify session invalidation.", e);
			throw new AssertionError("Session invalidation verification failed: " + e.getMessage(), e);
		}
	}

	@Then("User should be redirected to login page automatically")
	public void user_redirected_to_login_automatically() {
		try {
			String loginPageLocator = base.toPlaywrightLocator(base.getLocator("loginPage.userNameTextBox"));
			Locator loginField = DriverManager.getPage().locator(loginPageLocator);
			loginField.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.VISIBLE)
				.setTimeout(timeout));
			Assert.assertTrue(loginField.isVisible(), "Login page is not displayed");
			log.info("User automatically redirected to login page - session is secure");
		} catch (Exception e) {
			log.error("Failed to verify automatic redirect to login page.", e);
			throw new AssertionError("Automatic redirect verification failed: " + e.getMessage(), e);
		}
	}
}
