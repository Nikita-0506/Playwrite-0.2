package utilities;

import com.microsoft.playwright.*;

//@Author: neha.verma@inadev.com
//@Date: 12 July 2025
//@Desc: This class holds browser driver handling methods using Playwright

public class DriverManager {
	// ThreadLocal instances for thread-safe Playwright objects in parallel runs
	private static ThreadLocal<Playwright> playwright = new ThreadLocal<>();
	private static ThreadLocal<Browser> browser = new ThreadLocal<>();
	private static ThreadLocal<BrowserContext> context = new ThreadLocal<>();
	private static ThreadLocal<Page> page = new ThreadLocal<>();

	// Initialize Playwright and browser for current thread
	public static void initDriver() {
		String browserType = ConfigReader.get("browser");
		boolean isCi = System.getenv("JENKINS_URL") != null || System.getenv("CI") != null;
		boolean headless = isCi;

		// Create Playwright instance
		playwright.set(Playwright.create());

		// Launch browser based on configuration
		BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
			.setHeadless(headless)
			.setArgs(java.util.Arrays.asList("--disable-dev-shm-usage", "--no-sandbox"));

		if (browserType.equalsIgnoreCase("chrome") || browserType.equalsIgnoreCase("chromium")) {
			browser.set(playwright.get().chromium().launch(launchOptions));
		} else if (browserType.equalsIgnoreCase("firefox")) {
			browser.set(playwright.get().firefox().launch(launchOptions));
		} else if (browserType.equalsIgnoreCase("edge")) {
			browser.set(playwright.get().chromium().launch(launchOptions.setChannel("msedge")));
		} else if (browserType.equalsIgnoreCase("webkit") || browserType.equalsIgnoreCase("safari")) {
			browser.set(playwright.get().webkit().launch(launchOptions));
		} else {
			throw new RuntimeException("Unsupported browser: " + browserType);
		}

		// Create browser context with deterministic desktop viewport for CI stability.
		Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
			.setViewportSize(1920, 1080);
		
		context.set(browser.get().newContext(contextOptions));
		
		// Create new page
		page.set(context.get().newPage());
	}

	// Get Playwright instance for current thread
	public static Playwright getPlaywright() {
		return playwright.get();
	}

	// Get browser for current thread
	public static Browser getBrowser() {
		return browser.get();
	}

	// Get browser context for current thread
	public static BrowserContext getContext() {
		return context.get();
	}

	// Get page for current thread
	public static Page getPage() {
		return page.get();
	}

	// Quit driver for current thread and remove it from ThreadLocal
	public static void quitDriver() {
		if (page.get() != null) {
			page.get().close();
			page.remove();
		}
		if (context.get() != null) {
			context.get().close();
			context.remove();
		}
		if (browser.get() != null) {
			browser.get().close();
			browser.remove();
		}
		if (playwright.get() != null) {
			playwright.get().close();
			playwright.remove();
		}
	}
}
