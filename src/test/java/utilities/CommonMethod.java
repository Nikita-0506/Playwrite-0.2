package utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.microsoft.playwright.*;

public class CommonMethod {

	public final static int timeout = 80;
	public static BaseClass base = new BaseClass();
	private static final Logger log = LogManager.getLogger(CommonMethod.class);

	public CommonMethod() {
		base = new BaseClass();
	}


	
	// Reset focus on page
	public static void resetFocusOnPage() {
		try {
			// In Playwright, we can click on the body element to reset focus
			Locator body = WaitUtils.waitForPresence(DriverManager.getPage(), "body", timeout);
			body.click(new Locator.ClickOptions().setPosition(0, 0));
			log.info("Reset focus on the page body.");
		} catch (PlaywrightException te) {
			log.error("Body element not found within timeout.", te);
			throw new AssertionError("Body element not found within timeout.", te);

		} catch (Exception e) {
			log.error("Failed to reset focus on the page body.", e);
			throw new AssertionError("Failed to reset focus on the page body. Reason: " + e.getMessage(), e);
		}
	}
}
