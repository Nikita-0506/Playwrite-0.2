package pageObjects;

import utilities.BaseClass;

//@Author: neha.verma@inadev.com
//@Date: 11 April 2026
//@Desc: This class holds all the dashboard page methods and locators

public class DashboardPage {

	BaseClass base;

	public DashboardPage(BaseClass base) {
		this.base = base;
	}

	// Method to get sidebar menu locator
	public String getSidebarLocator() {
		return base.getLocator("dashboardPage.sidebar");
	}

	// Method to get header locator
	public String getHeaderLocator() {
		return base.getLocator("dashboardPage.header");
	}

	// Method to get user profile icon locator
	public String getUserProfileIconLocator() {
		return base.getLocator("dashboardPage.user profile icon");
	}

	// Method to get dashboard welcome message locator
	public String getWelcomeMessageLocator() {
		return base.getLocator("dashboardPage.welcomeMessage");
	}

	// Method to get user profile menu locator
	public String getUserProfileMenuLocator() {
		return base.getLocator("dashboardPage.userProfileMenu");
	}

	// Method to get logout button locator
	public String getLogoutButtonLocator() {
		return base.getLocator("dashboardPage.logoutButton");
	}

	// Method to get logout message locator
	public String getLogoutMessageLocator() {
		return base.getLocator("dashboardPage.logoutMessage");
	}

	// Method to get dashboard header locator
	public String getDashboardHeaderLocator() {
		return base.getLocator("dashboardPage.dashboardHeader");
	}
}
