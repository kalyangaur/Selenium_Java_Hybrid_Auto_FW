package testCases;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import pageObject.HomePage;
import pageObject.LoginPage;
import utilities.DataProviders;
import utilities.Log;
import utilities.ReportingListener;

/* Valid data - login success - test passed - logout
  * Valid data - login failed - test failed
  * 
  *  Invalid data - login success - test failed - logout
  *  Invalid data - login failed - test passed
  * */	

public class TC003_LoginDDT extends Old_BaseClass
 {
	 Logger logger = Log.getLogger();
	 
	 @Test(dataProvider = "LoginData", dataProviderClass = DataProviders.class, priority = 1, groups = {"DataDriven","Master"}) //need second parameter as dataprovider in diff. class
	 public void verify_loginDDT(String email, String pwd, String expectedresult) throws IOException 
	 {
	    try
	     { 
		   logger.info("Test execution TC003_LoginDDT started");
		   boolean lklogoutPresent = false;
		   
	       //HomePage
	       HomePage objhp = new HomePage(driver);
	       objhp.clickSignupLogin();
	       logger.info("Login process started");
	
  	       LoginPage objlp = new LoginPage(driver);
  	       objlp.setEmail(email);
  	       objlp.setPassword(pwd);
	       objlp.clickLogin();
	       
	       lklogoutPresent = objlp.logoutdisplayed();
           
	       //if expected login data is valid
	       if(expectedresult.equalsIgnoreCase("Valid"))
	       {
	    	   if(lklogoutPresent == true)
	    	   {   
	    		   //logout
	    	       objlp.clickLogout();
	    		   
	    		   Assert.assertTrue(true);
	    		   logger.info("Login Successfull with Valid Data");
	    	   }
	    	   else
	    	   { 
	    	       Assert.assertTrue(false);
	    	       logger.info("Login unSuccessfull with Valid Data");
	    	   }
	       }
	       //if expected login data is Invalid
	       if(expectedresult.equalsIgnoreCase("Invalid"))
	       {
	    	   if(lklogoutPresent == true)
	    	   {   
	    		   //logout
	    	       objlp.clickLogout();
	    		   
	    		   Assert.assertTrue(false);
	    		   logger.info("Login Successfull but data is Invalid");
	    	   }
	    	   else
	    	   { 
	    	       Assert.assertTrue(true);
	    	       logger.info("Login unSuccessfull as data is Invalid");
	    	   }
	       }
	      }
         catch(Exception e)
          {
	        Assert.fail();
          }
	      
	    logger.info("Execution of TC003_LoginDDT test case is completed");
	       
      }
	 
}
