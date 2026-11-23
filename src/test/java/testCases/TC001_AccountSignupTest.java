package testCases;

import org.testng.Assert;
import org.testng.annotations.*;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import pageObject.AccountSignupDetailsPage;
import pageObject.AccountSignupPage;
import pageObject.HomePage;

import utilities.Log;
import utilities.ReportingListener;

import org.apache.logging.log4j.Logger;



public class TC001_AccountSignupTest extends BaseClass
{   
	  Logger logger = Log.getLogger();
	
	@Test (priority = 1, groups = {"Sanity","Master"})
    @Story("Verify Customer Login")   //Allure Report Annotation
    @Severity(SeverityLevel.CRITICAL) //Allure Report Annotation
    @Description("Verify that customer can login successfully") //Allure Report Annotation
	public void verify_AccountSignup()
	{   
    	logger.info("Test execution started");
    	
    	try
    	{
    	
    	//HomePage
		HomePage objhp = new HomePage(driver);
		objhp.clickSignupLogin();
		
		logger.info("Signup Login process started");
		Allure.step("signup in AutomationExercise had been started");
		//SignUp page
		AccountSignupPage objac = new  AccountSignupPage(driver);
		objac.setName("Jim Carry");
		
		//Random generated emailID method in "BaseClass" as "TC001_AccountSignupTest" is child class of "Baseclass" 
		//we can access base class methods(Public, Protected only) without object .Otherwise use parentname.method()
		objac.setEmail(randomString()+"@gmail.com"); 
		objac.clickSignup();
		
		logger.info("Signup filling details started");
		Allure.step("Signup filling details started");
		//SignUp detail page
		AccountSignupDetailsPage objasd = new AccountSignupDetailsPage(driver);
		objasd.enterAccInfo("Apple123", "10", "MAY", "2000", "JIM", "Carry", "14 freedom way ","India","MP", "Indore", "452005","1234567890");
		
		logger.info("Verify SignUp is successfull");
		Allure.step("Verify SignUp is successfull");
		//Confirm Account Creation
		String conmsg = objasd.getConfirmationmsg();
		
		Assert.assertEquals(conmsg,"ACCOUNT CREATED!");
		logger.info("Signup completed successfully");
		Allure.step("Signup completed successfully");	
		
    	}
    	catch (Exception e)
    	{
			logger.error("Test failed", e);
			Assert.fail("Account signup failed", e);
    	
    	
    	}
		
	}
    
   
    
    

}
