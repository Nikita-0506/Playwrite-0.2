package cucumberConfig;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import utilities.RetryFailure;

//@Author: neha.verma@inadev.co
//@Date: 3 Feb 2026
//@Desc: This class holds runner class configurations

//testing -----------hello
@CucumberOptions(features="src/test/java/features",glue ={"stepDefinitions","utilities"}
,monochrome=true, tags ="@SMOKE or @TEMPLATES or @OPPORTUNITIES",
plugin= {"json:target/cucumber-reports/Cucumber.json","html:target/cucumber-reports/cucumber.html","com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"})
public class TestNGTestRunner extends AbstractTestNGCucumberTests{

	@Override
	@DataProvider(parallel=false)
	public Object[][] scenarios()
	{
		return super.scenarios();
	}
	
	@Test(dataProvider = "scenarios", retryAnalyzer = RetryFailure.class)
    public void runScenario(io.cucumber.testng.PickleWrapper pickle, io.cucumber.testng.FeatureWrapper feature) {
        super.runScenario(pickle, feature);
    }	
}
