package stepDefinitions;

import pageObjects.CommonPage;
import utilities.BaseClass;
import utilities.DriverManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Then;

//@Author: neha.verma@inadev.com
//@Date: 12 July 2025
//@Desc: This class holds the common step definitions using Playwright

public class CommonSteps {

	BaseClass base;
	CommonPage cp = null;
	private static final Logger log = LogManager.getLogger(CommonSteps.class);


	public CommonSteps(BaseClass base) {
		this.base = base;
		cp = new CommonPage(base);
		
	}

	@Then("Heading is displayed with property {string}")
	public void heading_is_displayed_with_property(String textBoxName) {
		String loc = cp.getTextBoxLocator(textBoxName);
		String playwrightLoc = base.toPlaywrightLocator(loc);
		DriverManager.getPage().locator(playwrightLoc).isVisible();
        log.info("Verified heading with property: " + textBoxName + " successfully");

	}


	@After
	public void AfterScenario(Scenario s) {
		byte[] screenshot = DriverManager.getPage().screenshot();
		s.attach(screenshot, "image/png", "screenshot");

	}
}
