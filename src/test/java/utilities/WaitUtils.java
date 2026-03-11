package utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;

public class WaitUtils {
	private static final Logger log = LogManager.getLogger(WaitUtils.class);
	static int timeOutInSeconds = 30;
	
	/**
	 * Wait until an element is visible on the page.
	 */
	public static Locator waitForVisibility(Page page, String locator, int timeOutInSeconds) {
		try {
			Locator element = page.locator(locator);
			element.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.VISIBLE)
				.setTimeout(timeOutInSeconds * 1000.0));
			return element;
		} catch (Exception e) {
			log.info("Unexpected error while waiting for visibility of element: " + locator);
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Wait until an element is clickable (attached and visible)
	 */
	public static Locator waitForElementIsClickable(Page page, String locator, int timeOutInSeconds) {
		try {
			Locator element = page.locator(locator);
			element.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.VISIBLE)
				.setTimeout(timeOutInSeconds * 1000.0));
			return element;
		} catch (Exception e) {
			log.info("Unexpected error while waiting for clickable element: " + locator);
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Wait until a dialog/alert is present
	 * Note: Playwright handles dialogs differently - you need to register a dialog handler
	 */
	public static void waitForAlertIsPresent(Page page, int timeOutInSeconds) {
		try {
			// In Playwright, you need to set up a dialog handler before the action that triggers it
			page.onDialog(dialog -> {
				log.info("Dialog appeared: " + dialog.message());
				dialog.accept();
			});
		} catch (Exception e) {
			log.info("Unexpected error while waiting for dialog: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Wait until an element is present in the DOM.
	 */
	public static Locator waitForPresence(Page page, String locator, int timeOutInSeconds) {
		try {
			Locator element = page.locator(locator);
			element.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.ATTACHED)
				.setTimeout(timeOutInSeconds * 1000.0));
			return element;
		} catch (Exception e) {
			log.info("Unexpected error while waiting for element: " + locator, e);
			throw e;
		}
	}

	/**
	 * Wait for a frame to be available and switch to it.
	 */
	public static FrameLocator waitForFrameAndSwitchTo(Page page, String locator, int timeOutInSeconds) {
		try {
			// In Playwright, frames are accessed via frameLocator
			FrameLocator frameLocator = page.frameLocator(locator);
			// Wait for frame to be attached
			page.waitForSelector(locator, new Page.WaitForSelectorOptions()
				.setTimeout(timeOutInSeconds * 1000.0));
			return frameLocator;
		} catch (Exception e) {
			log.info("Unexpected error while waiting for frame: " + locator);
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Wait for an Element to be Present
	 */
	public static Locator waitForElementPresent(Page page, String locator, int timeOutInSeconds) {
		try {
			Locator element = page.locator(locator);
			element.waitFor(new Locator.WaitForOptions()
				.setState(WaitForSelectorState.ATTACHED)
				.setTimeout(timeOutInSeconds * 1000.0));
			return element;
		} catch (PlaywrightException e) {
			throw new AssertionError("Element not present within timeout: " + locator, e);
		}
	}
}
