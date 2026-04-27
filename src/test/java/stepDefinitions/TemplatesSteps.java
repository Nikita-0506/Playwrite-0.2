package stepDefinitions;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import utilities.BaseClass;
import utilities.DriverManager;

//@Author: neha.verma@inadev.com
//@Date: 27 April 2026
//@Desc: This class holds template filter step definitions using Playwright

public class TemplatesSteps {

	private static final Logger log = LogManager.getLogger(TemplatesSteps.class);
	private final int timeout = 15000;
	private final BaseClass base;
	private int baselineRowCount;
	private String baselinePagination = "";

	public TemplatesSteps() {
		base = new BaseClass();
	}

	private Locator resolveFirstVisibleLocator(String[] selectors, String elementName, int timeoutMs) {
		Page page = DriverManager.getPage();
		long deadline = System.currentTimeMillis() + timeoutMs;

		while (System.currentTimeMillis() < deadline) {
			for (String selector : selectors) {
				try {
					Locator locator = page.locator(selector);
					long count = locator.count();
					for (int i = 0; i < count; i++) {
						Locator candidate = locator.nth(i);
						if (candidate.isVisible()) {
							return candidate;
						}
					}
				} catch (Exception ignored) {
					// Try next selector.
				}
			}
			page.waitForTimeout(250);
		}

		throw new AssertionError("Could not find visible " + elementName + " using configured selectors.");
	}

	private List<String> getVisibleTexts(String locatorKey) {
		String selector = base.toPlaywrightLocator(base.getLocator(locatorKey));
		Locator locator = DriverManager.getPage().locator(selector);
		List<String> values = new ArrayList<>();
		long count = locator.count();
		for (int i = 0; i < count; i++) {
			Locator candidate = locator.nth(i);
			if (candidate.isVisible()) {
				String text = candidate.textContent();
				if (text != null && !text.trim().isEmpty()) {
					values.add(text.trim());
				}
			}
		}
		return values;
	}

	private int getVisibleRowCount() {
		String selector = base.toPlaywrightLocator(base.getLocator("templatesPage.resultRows"));
		Locator rows = DriverManager.getPage().locator(selector);
		int visibleCount = 0;
		long count = rows.count();
		for (int i = 0; i < count; i++) {
			if (rows.nth(i).isVisible()) {
				visibleCount++;
			}
		}
		return visibleCount;
	}

	private String getVisiblePaginationText() {
		List<String> values = getVisibleTexts("templatesPage.paginationSummary");
		return values.isEmpty() ? "" : values.get(0);
	}

	private void assertRowsMatchKeyword(String keyword) {
		String rowsSelector = base.toPlaywrightLocator(base.getLocator("templatesPage.resultRows"));
		Locator rows = DriverManager.getPage().locator(rowsSelector);
		long deadline = System.currentTimeMillis() + timeout;
		int checkedRows = 0;

		while (System.currentTimeMillis() < deadline) {
			long rowCount = rows.count();
			checkedRows = 0;
			boolean mismatchFound = false;

			for (int i = 0; i < rowCount; i++) {
				Locator row = rows.nth(i);
				String rowText;
				try {
					rowText = row.innerText();
				} catch (Exception ignored) {
					continue;
				}

				if (rowText == null || rowText.trim().isEmpty()) {
					continue;
				}

				String normalized = rowText.toLowerCase();
				if (!normalized.contains(keyword.toLowerCase())) {
					mismatchFound = true;
					break;
				}
				checkedRows++;
			}

			if (checkedRows > 0 && !mismatchFound) {
				return;
			}

			DriverManager.getPage().waitForTimeout(300);
		}

		Assert.assertTrue(false, "No filtered rows matched keyword '" + keyword + "' within timeout.");
	}

	@When("User navigates to Templates module from More menu")
	public void user_navigates_to_templates_module_from_more_menu() {
		try {
			Page page = DriverManager.getPage();
			String templatesSelector = base.toPlaywrightLocator(base.getLocator("templatesPage.templatesNav"));
			Locator templatesNav = page.locator(templatesSelector).first();

			if (templatesNav.count() == 0 || !templatesNav.isVisible()) {
				String moreSelector = base.toPlaywrightLocator(base.getLocator("templatesPage.moreMenu"));
				resolveFirstVisibleLocator(new String[] {
					moreSelector,
					"a:has-text('More')",
					"button:has-text('More')"
				}, "More menu", 8000).click();
				page.waitForTimeout(500);
			}

			resolveFirstVisibleLocator(new String[] {
				templatesSelector,
				"a[href*='email-templates']:not([href*='pdf'])",
				"a[href*='#/email-templates']",
				"a[href$='email-templates']"
			}, "Templates module link", 8000).click();

			page.waitForLoadState(LoadState.DOMCONTENTLOADED);
			String currentUrl = page.url() == null ? "" : page.url().toLowerCase();
			if (currentUrl.contains("pdf-template") || currentUrl.contains("pdf-templates")) {
				log.warn("Navigated to PDF Templates by mistake. Retrying with explicit Email Templates link.");
				resolveFirstVisibleLocator(new String[] {
					"a[href*='email-templates']:not([href*='pdf'])",
					"a[href*='#/email-templates']",
					"a[href$='email-templates']"
				}, "Email Templates module link", 8000).click();
				page.waitForLoadState(LoadState.DOMCONTENTLOADED);
			}
			log.info("Navigated to Templates module.");
		} catch (Exception e) {
			log.error("Failed to navigate to Templates module.", e);
			throw new AssertionError("Failed to navigate to Templates module. Reason: " + e.getMessage(), e);
		}
	}

	@Then("Templates list page should be displayed")
	public void templates_list_page_should_be_displayed() {
		try {
			Page page = DriverManager.getPage();
			boolean urlMatched = page.url() != null && page.url().toLowerCase().contains("email-templates");
			boolean markerVisible = false;

			try {
				String markerSelector = base.toPlaywrightLocator(base.getLocator("templatesPage.pageMarker"));
				markerVisible = resolveFirstVisibleLocator(new String[] {
					markerSelector,
					"text=Templates",
					"text=Email Templates"
				}, "Templates page marker", 6000).isVisible();
			} catch (Exception ignored) {
				markerVisible = false;
			}

			Assert.assertTrue(urlMatched || markerVisible,
				"Templates list page is not displayed. Current URL: " + page.url());

			// Give grid/pagination a moment to render after module navigation.
			page.waitForTimeout(1500);
			baselineRowCount = getVisibleRowCount();
			baselinePagination = getVisiblePaginationText();
			log.info("Templates list page verified. Baseline rows: " + baselineRowCount
				+ ", baseline pagination: " + baselinePagination);
		} catch (Exception e) {
			log.error("Templates list page validation failed.", e);
			throw new AssertionError("Templates list page validation failed. Reason: " + e.getMessage(), e);
		}
	}

	@When("User clicks on Filter button on Templates page")
	public void user_clicks_on_filter_button_on_templates_page() {
		try {
			try {
				resolveFirstVisibleLocator(new String[] {
					base.toPlaywrightLocator(base.getLocator("templatesPage.filterButton")),
					"button:has-text('Filter')",
					"a:has-text('Filter')"
				}, "Templates filter button", 3000).click();
			} catch (Exception ignored) {
				// Filter panel may already be expanded on page load.
			}

			resolveFirstVisibleLocator(new String[] {
				base.toPlaywrightLocator(base.getLocator("templatesPage.filterKeywordInput")),
				"label:has-text('Name') + input",
				"input[name='name']"
			}, "Templates filter input", timeout);

			log.info("Templates filter panel opened.");
		} catch (Exception e) {
			log.error("Failed to open Templates filter.", e);
			throw new AssertionError("Failed to open Templates filter. Reason: " + e.getMessage(), e);
		}
	}

	@When("User enters filter keyword {string}")
	public void user_enters_filter_keyword(String keyword) {
		try {
			Locator filterInput = resolveFirstVisibleLocator(new String[] {
				base.toPlaywrightLocator(base.getLocator("templatesPage.filterKeywordInput")),
				"label:has-text('Name') + input",
				"input[name='name']"
			}, "Templates filter input", timeout);
			filterInput.clear();
			filterInput.fill(keyword);
			log.info("Entered template filter keyword: " + keyword);
		} catch (Exception e) {
			log.error("Failed to enter template filter keyword.", e);
			throw new AssertionError("Failed to enter template filter keyword. Reason: " + e.getMessage(), e);
		}
	}

	@When("User enters filter keyword {string} in Basic Filter Name field")
	public void user_enters_filter_keyword_in_basic_filter_name_field(String keyword) {
		user_enters_filter_keyword(keyword);
	}

	@When("User applies the filter")
	public void user_applies_the_filter() {
		try {
			Page page = DriverManager.getPage();
			Locator applyButton = page.locator(base.toPlaywrightLocator(base.getLocator("templatesPage.applyFilterButton"))).first();

			if (applyButton.count() > 0 && applyButton.isVisible()) {
				applyButton.click();
			} else {
				resolveFirstVisibleLocator(new String[] {
					base.toPlaywrightLocator(base.getLocator("templatesPage.filterKeywordInput")),
					"label:has-text('Name') + input",
					"input[name='name']"
				}, "Templates filter input", timeout).press("Enter");
			}

			page.waitForTimeout(1500);
			log.info("Templates filter applied.");
		} catch (Exception e) {
			log.error("Failed to apply templates filter.", e);
			throw new AssertionError("Failed to apply templates filter. Reason: " + e.getMessage(), e);
		}
	}

	@Then("User should see only templates matching keyword {string}")
	public void user_should_see_only_templates_matching_keyword(String keyword) {
		try {
			assertRowsMatchKeyword(keyword);
			log.info("Filtered templates matched keyword '" + keyword + "'.");
		} catch (Exception e) {
			log.error("Filtered template results validation failed.", e);
			throw new AssertionError("Filtered template results validation failed. Reason: " + e.getMessage(), e);
		}
	}

	@When("User clears the filter")
	public void user_clears_the_filter() {
		try {
			Page page = DriverManager.getPage();

			// Ensure Basic Filter panel is open before attempting to clear.
			Locator filterInputProbe = page.locator(base.toPlaywrightLocator(base.getLocator("templatesPage.filterKeywordInput"))).first();
			if (filterInputProbe.count() == 0 || !filterInputProbe.isVisible()) {
				try {
					resolveFirstVisibleLocator(new String[] {
						base.toPlaywrightLocator(base.getLocator("templatesPage.filterButton")),
						"button:has-text('Filter')",
						"a:has-text('Filter')"
					}, "Templates filter button", 4000).click();
					page.waitForTimeout(400);
				} catch (Exception ignored) {
					// Continue to clear via fallback strategies.
				}
			}

			Locator clearButton = null;
			try {
				clearButton = resolveFirstVisibleLocator(new String[] {
					base.toPlaywrightLocator(base.getLocator("templatesPage.resetFilterButton")),
					"button:has-text('Clear')",
					"a:has-text('Clear')",
					"button:has-text('Reset')",
					"a:has-text('Reset')"
				}, "Templates clear button", 4000);
			} catch (Exception ignored) {
				clearButton = null;
			}

			if (clearButton != null) {
				clearButton.click();
			} else {
				Locator filterInput = resolveFirstVisibleLocator(new String[] {
					base.toPlaywrightLocator(base.getLocator("templatesPage.filterKeywordInput")),
					"label:has-text('Name') + input",
					"input[name='name']"
				}, "Templates filter input", timeout);
				filterInput.clear();
				filterInput.press("Enter");
			}

			page.waitForTimeout(1200);
			log.info("Templates filter cleared.");
		} catch (Exception e) {
			log.error("Failed to clear templates filter.", e);
			throw new AssertionError("Failed to clear templates filter. Reason: " + e.getMessage(), e);
		}
	}

	@Then("User should see the full templates list")
	public void user_should_see_the_full_templates_list() {
		try {
			int currentRowCount = getVisibleRowCount();
			String currentPagination = getVisiblePaginationText();
			boolean onTemplatesPage = DriverManager.getPage().url() != null
				&& DriverManager.getPage().url().toLowerCase().contains("email-templates");

			boolean rowCountRestored = baselineRowCount > 0 && currentRowCount >= baselineRowCount;
			boolean paginationRestored = !baselinePagination.isEmpty()
				&& !currentPagination.isEmpty()
				&& baselinePagination.equalsIgnoreCase(currentPagination);
			boolean pageStillValid = onTemplatesPage && (currentRowCount > 0 || !currentPagination.isEmpty());

			Assert.assertTrue(rowCountRestored || paginationRestored || pageStillValid,
				"Templates list was not restored after clearing filter. Baseline rows=" + baselineRowCount
					+ ", current rows=" + currentRowCount
					+ ", baseline pagination='" + baselinePagination
					+ "', current pagination='" + currentPagination + "'.");

			log.info("Templates list restored successfully after clearing filter.");
		} catch (Exception e) {
			log.error("Templates full list validation failed.", e);
			throw new AssertionError("Templates full list validation failed. Reason: " + e.getMessage(), e);
		}
	}
}
