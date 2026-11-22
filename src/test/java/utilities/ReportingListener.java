package utilities;

import java.util.List;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import testCases.BaseClass;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.Parameter;

public class ReportingListener implements ITestListener 
{

    private static String getTestMethodName(ITestResult iTestResult) 
	{
	   return iTestResult.getMethod().getConstructorOrMethod().getName();
	}
    
    //Allure attachment annotation, attach image to report 
     @Attachment(value = "Failure Screenshot", type = "image/png")
	 public byte[] saveFailureScreenShot(WebDriver driver) 
	 {
	   return ((TakesScreenshot)driver).getScreenshotAs(OutputType.BYTES);
	 }

	 @Attachment(value = "{0}", type = "text/plain")
	 public static String saveTextLog(String message)
	 {
	   return message;
	  }

	@Override
	public void onStart(ITestContext testContext) {

	        // Application information
	        Allure.label("application", "AutomationExercise");
	        Allure.label("module", "Admin");
	        Allure.label("subModule", "Customers");

	        // User information
	        Allure.label("user", System.getProperty("user.name"));

	        // Environment information
	        Allure.label("environment", "QA");

	        // Operating System
	        String os = testContext.getCurrentXmlTest().getParameter("os");

	        if (os != null) {
	            Allure.label("os", os);
	        }

	        // Browser information
	        String browser = testContext.getCurrentXmlTest().getParameter("browser");

	        if (browser != null) {
	            Allure.label("browser", browser);
	        }

	        // Included TestNG Groups
	        List<String> includedGroups = testContext.getCurrentXmlTest().getIncludedGroups();
	        if (!includedGroups.isEmpty()) 
	        {
	            Allure.label("groups", includedGroups.toString());
	        }

	        System.out.println("Allure Reporting Started");
	    }
	 
    @Override
    public void onTestStart(ITestResult result)
    {
    	System.out.println("Automation execution started"+getTestMethodName(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) 
    {
    	System.out.println("Test Completed : "+getTestMethodName(result));
    }

    @Override
    public void onTestFailure(ITestResult result)
    {
    	System.out.println("Test Failed : "+getTestMethodName(result));
		 
		// Object testClass = result.getInstance();
		 WebDriver driver = BaseClass.getDriver();
		 
		 //Allure Screenshot and saveTestLog
		 if (driver != null)
		  {
			 System.out.println("Screenshot Capture for the test case: "+getTestMethodName(result));
			 saveFailureScreenShot(driver);
		  }
		  else
		  {
			 System.err.println("Unable to capture failure screenshot: WebDriver is not available for "
					 + getTestMethodName(result));
		  }
		  saveTextLog(getTestMethodName(result));
    }

    @Override
	 public void onTestSkipped(ITestResult result) 
	  {
		 System.out.println("Test Failed : "+getTestMethodName(result));
			
	   }
	 
	 @Override
	 public void onFinish(ITestContext iTestContext) 
	  {
	     System.out.println("onFinish  : "+iTestContext.getName());
	   }
}
