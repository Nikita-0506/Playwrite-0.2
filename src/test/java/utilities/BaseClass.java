package utilities;

import java.io.IOException;
import java.util.Properties;

//@Author: neha.verma@inadev.com
//@Date: 3 Feb 2026
//@Desc: This class holds the wrapper methods to manage locator access for Playwright

public class BaseClass {
	private final Properties locators = new Properties();
	@SuppressWarnings("unused")
	private static int counter = 0;

	public BaseClass() {
		try {
			locators.load(getClass().getClassLoader().getResourceAsStream("repository/locators.properties"));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getLocator(String key) {
		return this.locators.get(key).toString();
	}

	public String createDynamicQueryXpath(String string, String toBeReplacedKey, String replacedValue) {
		return (string.replace(toBeReplacedKey, replacedValue));
	}

	public String createDynamicQueryXpath(String string, String toBeReplacedKey1, String replacedValue1,
			String toReplace2, String value2) {
		String temp = string.replace(toReplace2, value2);
		return (temp.replace(toBeReplacedKey1, replacedValue1));
	}

	/**
	 * Playwright uses different selector syntax:
	 * - XPath: starts with // or (
	 * - CSS: direct selector string
	 * - ID: #id or id=value
	 * - Text: text=value
	 * - Name: [name="value"]
	 * - Class: .classname
	 */
	public String toPlaywrightLocator(String locator) {
		String result = null;

		if (locator.startsWith("//") || locator.startsWith("(")) {
			// XPath selector
			result = locator;
		} else if (locator.startsWith("css=")) {
			// CSS selector
			result = locator.replace("css=", "");
		} else if (locator.startsWith("class=")) {
			// Class selector
			result = "." + locator.replace("class=", "");
		} else if (locator.startsWith("tag=")) {
			// Tag selector
			result = locator.replace("tag=", "");
		} else if (locator.startsWith("#")) {
			// Name selector - convert to attribute selector
			result = "[name=\"" + locator.replace("#", "") + "\"]";
		} else if (locator.startsWith("plt=")) {
			// Partial link text - convert to text selector
			result = "text=" + locator.replace("plt=", "");
		} else if (locator.startsWith("lt=")) {
			// Link text - convert to exact text selector
			result = "text=\"" + locator.replace("lt=", "") + "\"";
		} else if (locator.startsWith("name=")) {
			// Name attribute selector
			result = "[name=\"" + locator.replace("name=", "") + "\"]";
		} else {
			// Assume it's an ID
			result = "#" + locator;
		}

		return result;
	}
}
