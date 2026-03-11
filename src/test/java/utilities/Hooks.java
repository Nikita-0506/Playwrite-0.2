package utilities;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

//@Author: neha.verma@inadev.com
//@Date: 12 July 2025
//@Desc: This class holds all the lifecycle functions for Playwright


public class Hooks {

    @Before
    public void setup() {
        DriverManager.initDriver();
    }

    @After(order = 0)
    public void tearDown() {
        DriverManager.quitDriver();
    }
    
    @After(order = 1)
    public void takeScreenshotIfFailed(Scenario scenario) {
        
        if (DriverManager.getPage() != null && scenario.isFailed()) {
            // Take screenshot using Playwright
            byte[] screenshot = DriverManager.getPage().screenshot();
            scenario.attach(screenshot, "image/png", "failure-screenshot");
        }
    }
    
    @Parameters("browser")
    @BeforeClass
    public void beforeSuite(@Optional("chrome") String browser) {
        System.setProperty("browser", browser);
    }    
 
}
