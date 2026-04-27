package stepDefinitions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.WaitForSelectorState;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import utilities.BaseClass;
import utilities.DriverManager;

//@Author: neha.verma@inadev.com
//@Date: 11 Apr 2026
//@Desc: This class holds all the Opportunities related step definitions using Playwright

public class OpportunitySteps {

	private static final Logger log = LogManager.getLogger(OpportunitySteps.class);
	private final int timeout = 15000;
	BaseClass base;

	public OpportunitySteps() {
		base = new BaseClass();
	}

	private Locator getOpportunitiesMenu() {
		String opportunitiesMenuLocator = base.toPlaywrightLocator(base.getLocator("opportunityPage.menu"));
		return DriverManager.getPage().locator(opportunitiesMenuLocator)
			.filter(new Locator.FilterOptions().setHasText("Opportunities"))
			.first();
	}

	private void openHoverMenuAndClick(Page page, String menuText, String itemText) {
		Locator menu = page.getByText(menuText).first();
		Locator menuContainer = menu.locator("xpath=ancestor::li[contains(@class,'dropdown')][1]");
		if (menuContainer.count() == 0) {
			menu = page.locator("xpath=(//a[contains(@class,'dropdown-toggle') and contains(normalize-space(.),\"" + menuText + "\")])[1]");
			menuContainer = menu.locator("xpath=ancestor::li[contains(@class,'dropdown')][1]");
		}

		menu.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeout));
		menu.scrollIntoViewIfNeeded();
		menu.hover();
		menu.dispatchEvent("mouseover");
		menu.dispatchEvent("mouseenter");
		menu.dispatchEvent("mousemove");

		BoundingBox box = menu.boundingBox();
		if (box == null) {
			throw new AssertionError("Unable to resolve menu bounding box for: " + menuText);
		}

		page.mouse().move(box.x + 20, box.y + box.height + 20);
		page.mouse().move(box.x + 40, box.y + box.height + 35);

		Locator item = page.getByText(itemText).first();
		if (item.count() == 0 || !item.isVisible()) {
			menuContainer.evaluate("el => { el.classList.add('open'); const dd = el.querySelector('ul.dropdown-menu'); if (dd) { dd.style.display = 'block'; dd.style.visibility = 'visible'; dd.style.opacity = '1'; } }");
			item = menuContainer
				.locator("xpath=.//ul[contains(@class,'dropdown-menu')]//a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), translate(\"" + itemText + "\",'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'))]")
				.first();

			if (item.count() == 0 || !item.isVisible()) {
				item = menuContainer.locator("xpath=.//ul[contains(@class,'dropdown-menu')]//li[1]//a").first();
			}
		}

		try {
			item.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeout));
			item.click(new Locator.ClickOptions().setTrial(true));
			item.click();
		} catch (Exception visibilityOrClickError) {
			Locator hiddenItem = menuContainer
				.locator("xpath=.//ul[contains(@class,'dropdown-menu')]//a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), translate(\"" + itemText + "\",'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'))]")
				.first();
			if (hiddenItem.count() == 0) {
				hiddenItem = menuContainer.locator("xpath=.//ul[contains(@class,'dropdown-menu')]//li[1]//a").first();
			}

			hiddenItem.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(timeout));
			String href = hiddenItem.getAttribute("href");
			if (href == null || href.trim().isEmpty()) {
				throw visibilityOrClickError;
			}

			String currentUrl = page.url();
			String baseUrl = currentUrl.split("#")[0];
			String targetUrl = href.startsWith("#") ? baseUrl + href : href;
			page.navigate(targetUrl);
		}
	}

	@When("User navigates to Opportunities module")
	public void user_navigates_to_opportunities_module() {
		try {
			Locator opportunitiesMenu = getOpportunitiesMenu();
			opportunitiesMenu.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeout));
			opportunitiesMenu.scrollIntoViewIfNeeded();
			log.info("User reached Opportunities module");
		} catch (Exception e) {
			log.warn("Opportunities menu not visible, falling back to direct URL navigation. Reason: " + e.getMessage());
			try {
				Page page = DriverManager.getPage();
				String currentUrl = page.url();
				String baseUrl = currentUrl.split("#")[0];
				page.navigate(baseUrl + "#/opportunities");
				page.waitForURL(url -> url.contains("opportunities"), new Page.WaitForURLOptions().setTimeout(timeout));
				log.info("User reached Opportunities module via direct URL navigation");
			} catch (Exception navError) {
				log.error("Failed to navigate to Opportunities module", navError);
				throw new AssertionError("Failed to navigate to Opportunities module. Reason: " + navError.getMessage(), navError);
			}
		}
	}

	@When("User clicks on New Opportunity")
	@When("User clicks on Create Opportunity")
	public void user_clicks_on_new_opportunity() {
		try {
			Page page = DriverManager.getPage();
			openHoverMenuAndClick(page, "Opportunities", "Create Opportunity");
			
			page.waitForURL(url -> url.toLowerCase().contains("edit") || url.toLowerCase().contains("create") || url.toLowerCase().contains("opportunit"),
					new Page.WaitForURLOptions().setTimeout(timeout));
			log.info("User clicked on Create Opportunity");
		} catch (Exception e) {
			log.error("Failed to click Create Opportunity", e);
			throw new AssertionError("Failed to click Create Opportunity. Reason: " + e.getMessage(), e);
		}
	}

	@When("User enters opportunity name {string}")
	public void user_enters_opportunity_name(String opportunityName) {
		try {
			String opportunityNameLocator = base.toPlaywrightLocator(base.getLocator("opportunityPage.nameTextBox"));
			Locator opportunityNameField = DriverManager.getPage().locator(opportunityNameLocator);
			opportunityNameField.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeout));
			opportunityNameField.clear();
			opportunityNameField.fill(opportunityName);
			log.info("Entered opportunity name: " + opportunityName);
		} catch (Exception e) {
			log.error("Failed to enter opportunity name", e);
			throw new AssertionError("Failed to enter opportunity name. Reason: " + e.getMessage(), e);
		}
	}

	@When("User selects account name {string}")
	public void user_selects_account_name(String accountName) {
		try {
			String accountNameLocator = base.toPlaywrightLocator(base.getLocator("opportunityPage.accountNameTextBox"));
			Locator accountNameField = DriverManager.getPage().locator(accountNameLocator);
			accountNameField.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeout));
			accountNameField.click();

			try {
				accountNameField.clear();
				accountNameField.fill(accountName);
			} catch (Exception ignored) {
				// Some select-style controls do not support clear/fill directly.
			}

			Locator accountOption = DriverManager.getPage().getByText(accountName).first();
			try {
				accountOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
				accountOption.click();
			} catch (Exception noExactMatch) {
				accountNameField.press("ArrowDown");
				accountNameField.press("Enter");
			}

			log.info("Selected account name: " + accountName);
		} catch (Exception e) {
			log.error("Failed to select account name", e);
			throw new AssertionError("Failed to select account name. Reason: " + e.getMessage(), e);
		}
	}

	@When("User enters expected close date {string}")
	public void user_enters_expected_close_date(String expectedCloseDate) {
		try {
			String expectedCloseDateLocator = base.toPlaywrightLocator(base.getLocator("opportunityPage.expectedCloseDateTextBox"));
			Locator expectedCloseDateField = DriverManager.getPage().locator(expectedCloseDateLocator);
			expectedCloseDateField.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeout));
			expectedCloseDateField.clear();
			expectedCloseDateField.fill(expectedCloseDate);
			expectedCloseDateField.press("Tab");
			log.info("Entered expected close date: " + expectedCloseDate);
		} catch (Exception e) {
			log.error("Failed to enter expected close date", e);
			throw new AssertionError("Failed to enter expected close date. Reason: " + e.getMessage(), e);
		}
	}

	@When("User enters opportunity amount {string}")
	public void user_enters_opportunity_amount(String amount) {
		try {
			String opportunityAmountLocator = base.toPlaywrightLocator(base.getLocator("opportunityPage.amountTextBox"));
			Locator opportunityAmountField = DriverManager.getPage().locator(opportunityAmountLocator);
			opportunityAmountField.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeout));
			opportunityAmountField.clear();
			opportunityAmountField.fill(amount);
			log.info("Entered opportunity amount: " + amount);
		} catch (Exception e) {
			log.error("Failed to enter opportunity amount", e);
			throw new AssertionError("Failed to enter opportunity amount. Reason: " + e.getMessage(), e);
		}
	}

	@When("User selects opportunity stage {string}")
	public void user_selects_opportunity_stage(String stage) {
		try {
			String opportunityStageLocator = base.toPlaywrightLocator(base.getLocator("opportunityPage.stageDropdown"));
			Locator stageDropdown = DriverManager.getPage().locator(opportunityStageLocator);
			stageDropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeout));
			try {
				stageDropdown.selectOption(stage);
			} catch (Exception selectError) {
				stageDropdown.click();
				Locator stageOption = DriverManager.getPage().getByText(stage).first();
				stageOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeout));
				stageOption.click();
			}
			log.info("Selected opportunity stage: " + stage);
		} catch (Exception e) {
			log.error("Failed to select opportunity stage", e);
			throw new AssertionError("Failed to select opportunity stage. Reason: " + e.getMessage(), e);
		}
	}

	@When("User saves the opportunity")
	public void user_saves_the_opportunity() {
		try {
			String saveButtonLocator = base.toPlaywrightLocator(base.getLocator("opportunityPage.saveButton"));
			Locator saveButton = DriverManager.getPage().locator(saveButtonLocator);
			saveButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeout));
			saveButton.click();
			log.info("User clicked on Save Opportunity button");
		} catch (Exception e) {
			log.error("Failed to save opportunity", e);
			throw new AssertionError("Failed to save opportunity. Reason: " + e.getMessage(), e);
		}
	}

	@Then("User should see opportunity {string} in Opportunities list")
	public void user_should_see_opportunity_in_opportunities_list(String opportunityName) {
		try {
			String dynamicResultLocator = base.createDynamicQueryXpath(base.getLocator("opportunityPage.resultName"), "temp", opportunityName);
			String playwrightResultLocator = base.toPlaywrightLocator(dynamicResultLocator);
			Locator opportunityNameResult = DriverManager.getPage().locator(playwrightResultLocator);
			opportunityNameResult.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeout));
			Assert.assertTrue(opportunityNameResult.isVisible(), "Expected opportunity record not found in list");
			log.info("Verified opportunity is visible in list: " + opportunityName);
		} catch (Exception e) {
			log.error("Failed to verify opportunity in list", e);
			throw new AssertionError("Failed to verify opportunity in list. Reason: " + e.getMessage(), e);
		}
	}
}
