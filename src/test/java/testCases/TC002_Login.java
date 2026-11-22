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
import utilities.Log;
import utilities.ReportingListener;

@Listeners({ReportingListener.class})
public class TC002_Login  extends BaseClass
{
	 Logger logger = Log.getLogger();
	
	 @Test(priority = 1, groups = {"Regression","Master"})
	 public void verify_login() throws IOException 
	 {
		 logger.info("Test execution TC002_Login started");
		 
		 try
		  {  
		    //Read Properties file in resource folder to get data
	        FileReader objfilereader = new FileReader("./src//test//resources//config.properties");
	        p = new Properties();
	        p.load(objfilereader);
		 
		 
		    //HomePage
		    HomePage objhp = new HomePage(driver);
		    objhp.clickSignupLogin();
		    logger.info("Login process started");
		
	  	    LoginPage objlp = new LoginPage(driver);
		    String streMailId = p.getProperty("email");
		    String strpassword = p.getProperty("password");
		
		    objlp.setEmail(streMailId);
		    objlp.setPassword(strpassword);
		    objlp.clickLogin();
		    boolean lo = objlp.logoutdisplayed();
        
	        Assert.assertTrue(lo, "Login failed");
		    logger.info("Login Successfull");
	      }
	    catch(Exception e)
	     {
		   Assert.fail();
	      }
       }
}