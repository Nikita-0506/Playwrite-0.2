package pageObjects;

import utilities.BaseClass;

//@Author: neha.verma@inadev.com
//@Date: 3 Feb 2026
//@Desc: This class holds all the common methods that can be used across pages

public class CommonPage {

	BaseClass base;

	public CommonPage(BaseClass base) {
		this.base = base;
	}

	public String getTextBoxLocator(String text) {
		return base.createDynamicQueryXpath(base.getLocator("commonPage.commonTextBox"), "temp", text);
	}
	
	public String getLinkLocator(String text) {
		return base.createDynamicQueryXpath(base.getLocator("quickRepairPage.profileLinks"), "temp", text);
		
	}

	
}
