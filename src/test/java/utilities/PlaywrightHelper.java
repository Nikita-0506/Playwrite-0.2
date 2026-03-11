package utilities;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Helper utility class to simplify Playwright operations in step definitions
 * This class provides convenient methods that combine common patterns
 * 
 * @Author: Migration Helper
 * @Date: February 2026
 */
public class PlaywrightHelper {
    
    private static final Logger log = LogManager.getLogger(PlaywrightHelper.class);
    private static final int DEFAULT_TIMEOUT = 30000; // 30 seconds in milliseconds
    private final BaseClass base;
    private final Page page;
    
    public PlaywrightHelper(BaseClass base) {
        this.base = base;
        this.page = DriverManager.getPage();
    }
    
    /**
     * Get a Locator from a properties key
     * @param key The key from locators.properties
     * @return Playwright Locator object
     */
    public Locator getLocator(String key) {
        String locatorString = base.getLocator(key);
        String playwrightLocator = base.toPlaywrightLocator(locatorString);
        return page.locator(playwrightLocator);
    }
    
    /**
     * Click on an element identified by a properties key
     * @param key The key from locators.properties
     */
    public void click(String key) {
        try {
            getLocator(key).click();
            log.info("Clicked on element: " + key);
        } catch (Exception e) {
            log.error("Failed to click on element: " + key, e);
            throw new AssertionError("Failed to click on element: " + key + ". Reason: " + e.getMessage(), e);
        }
    }
    
    /**
     * Fill text into an element identified by a properties key
     * @param key The key from locators.properties
     * @param text The text to fill
     */
    public void fill(String key, String text) {
        try {
            Locator locator = getLocator(key);
            locator.clear();
            locator.fill(text);
            log.info("Filled text into element: " + key);
        } catch (Exception e) {
            log.error("Failed to fill text into element: " + key, e);
            throw new AssertionError("Failed to fill text into element: " + key + ". Reason: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get text content from an element identified by a properties key
     * @param key The key from locators.properties
     * @return The text content of the element
     */
    public String getText(String key) {
        try {
            String text = getLocator(key).textContent();
            log.info("Retrieved text from element: " + key);
            return text;
        } catch (Exception e) {
            log.error("Failed to get text from element: " + key, e);
            throw new AssertionError("Failed to get text from element: " + key + ". Reason: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if an element is visible
     * @param key The key from locators.properties
     * @return true if visible, false otherwise
     */
    public boolean isVisible(String key) {
        try {
            return getLocator(key).isVisible();
        } catch (Exception e) {
            log.warn("Element not visible: " + key);
            return false;
        }
    }
    
    /**
     * Wait for an element to be visible
     * @param key The key from locators.properties
     * @param timeoutMillis Timeout in milliseconds
     * @return The Locator object
     */
    public Locator waitForVisible(String key, int timeoutMillis) {
        try {
            Locator locator = getLocator(key);
            locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(timeoutMillis));
            log.info("Element became visible: " + key);
            return locator;
        } catch (Exception e) {
            log.error("Element did not become visible: " + key, e);
            throw new AssertionError("Element did not become visible: " + key + ". Reason: " + e.getMessage(), e);
        }
    }
    
    /**
     * Wait for an element to be visible with default timeout
     * @param key The key from locators.properties
     * @return The Locator object
     */
    public Locator waitForVisible(String key) {
        return waitForVisible(key, DEFAULT_TIMEOUT);
    }
    
    /**
     * Select an option from a dropdown by visible text
     * @param key The key from locators.properties
     * @param optionText The visible text of the option
     */
    public void selectByText(String key, String optionText) {
        try {
            getLocator(key).selectOption(optionText);
            log.info("Selected option '" + optionText + "' from dropdown: " + key);
        } catch (Exception e) {
            log.error("Failed to select option from dropdown: " + key, e);
            throw new AssertionError("Failed to select option from dropdown: " + key + ". Reason: " + e.getMessage(), e);
        }
    }
    
    /**
     * Hover over an element
     * @param key The key from locators.properties
     */
    public void hover(String key) {
        try {
            getLocator(key).hover();
            log.info("Hovered over element: " + key);
        } catch (Exception e) {
            log.error("Failed to hover over element: " + key, e);
            throw new AssertionError("Failed to hover over element: " + key + ". Reason: " + e.getMessage(), e);
        }
    }
    
    /**
     * Scroll element into view
     * @param key The key from locators.properties
     */
    public void scrollIntoView(String key) {
        try {
            getLocator(key).scrollIntoViewIfNeeded();
            log.info("Scrolled element into view: " + key);
        } catch (Exception e) {
            log.error("Failed to scroll element into view: " + key, e);
            throw new AssertionError("Failed to scroll element into view: " + key + ". Reason: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check a checkbox or radio button
     * @param key The key from locators.properties
     */
    public void check(String key) {
        try {
            getLocator(key).check();
            log.info("Checked element: " + key);
        } catch (Exception e) {
            log.error("Failed to check element: " + key, e);
            throw new AssertionError("Failed to check element: " + key + ". Reason: " + e.getMessage(), e);
        }
    }
    
    /**
     * Uncheck a checkbox
     * @param key The key from locators.properties
     */
    public void uncheck(String key) {
        try {
            getLocator(key).uncheck();
            log.info("Unchecked element: " + key);
        } catch (Exception e) {
            log.error("Failed to uncheck element: " + key, e);
            throw new AssertionError("Failed to uncheck element: " + key + ". Reason: " + e.getMessage(), e);
        }
    }
    
    /**
     * Press a key on an element
     * @param key The key from locators.properties
     * @param keyToPress The key to press (e.g., "Enter", "Tab", "Escape")
     */
    public void pressKey(String key, String keyToPress) {
        try {
            getLocator(key).press(keyToPress);
            log.info("Pressed key '" + keyToPress + "' on element: " + key);
        } catch (Exception e) {
            log.error("Failed to press key on element: " + key, e);
            throw new AssertionError("Failed to press key on element: " + key + ". Reason: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get attribute value from an element
     * @param key The key from locators.properties
     * @param attributeName The name of the attribute
     * @return The attribute value
     */
    public String getAttribute(String key, String attributeName) {
        try {
            String value = getLocator(key).getAttribute(attributeName);
            log.info("Retrieved attribute '" + attributeName + "' from element: " + key);
            return value;
        } catch (Exception e) {
            log.error("Failed to get attribute from element: " + key, e);
            throw new AssertionError("Failed to get attribute from element: " + key + ". Reason: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if element is enabled
     * @param key The key from locators.properties
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled(String key) {
        try {
            return getLocator(key).isEnabled();
        } catch (Exception e) {
            log.warn("Failed to check if element is enabled: " + key);
            return false;
        }
    }
    
    /**
     * Get the count of elements matching the locator
     * @param key The key from locators.properties
     * @return The count of matching elements
     */
    public int getCount(String key) {
        try {
            return getLocator(key).count();
        } catch (Exception e) {
            log.error("Failed to get element count: " + key, e);
            return 0;
        }
    }
    
    /**
     * Click element with retry logic
     * @param key The key from locators.properties
     * @param maxRetries Maximum number of retry attempts
     */
    public void clickWithRetry(String key, int maxRetries) {
        int attempts = 0;
        while (attempts < maxRetries) {
            try {
                attempts++;
                getLocator(key).click();
                log.info("Clicked on element: " + key + " (attempt " + attempts + ")");
                return;
            } catch (Exception e) {
                if (attempts >= maxRetries) {
                    log.error("Failed to click on element after " + maxRetries + " attempts: " + key, e);
                    throw new AssertionError("Failed to click on element: " + key + ". Reason: " + e.getMessage(), e);
                }
                log.warn("Click failed, retrying... (attempt " + attempts + ")");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
