package testCases;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

import org.apache.commons.lang3.RandomStringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.testng.annotations.*;

import io.github.bonigarcia.wdm.WebDriverManager;

import org.apache.commons.io.FileUtils;

public class BaseClass 
{
	public WebDriver driver;
	public Properties p;
    
	//Parallel testing driver thread creation
	public static ThreadLocal<WebDriver> tdriver = new ThreadLocal<WebDriver>();
		
	public WebDriver initialize_driver() 
	{ 
	  WebDriverManager.chromedriver().setup();
	  driver = new ChromeDriver();
	  driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
	  driver.manage().window().maximize();
	  driver.manage().timeouts().implicitlyWait(Duration.ZERO);
	  tdriver.set(driver);
	  return driver;
	}
		
	public static synchronized WebDriver getDriver()
	{
		return tdriver.get();
	}

	   @BeforeSuite(alwaysRun = true)
	   public void cleanAllureResults() throws IOException
	    {
	       File allureResults = new File("allure-results");
	         if (allureResults.exists())
	          {
	             FileUtils.deleteDirectory(allureResults);
	           }
	    }
	   
	    @BeforeClass(alwaysRun = true)
	    @Parameters({"browser"})
		public void setup(@Optional("chrome") String br) throws IOException
		{     
	    	//Read Properties file in resource folder to get data
	    	p = new Properties();
	    	try (FileReader objfilereader = new FileReader("./src/test/resources/config.properties")) 
	    	{
	    		p.load(objfilereader);
	    	}
	    	
	    	switch(br.toLowerCase())
	    	 {  
	    	   case "chrome":
	    		   driver = new ChromeDriver();
	    		   break;
	    	   case "edge" :
	    		   driver = new  EdgeDriver();
	    		   break;
	    	   default :
	    		   System.out.println("Invalid Browser Name");
	    		   return;
	    	}
	    	
			tdriver.set(driver);
			driver.manage().deleteAllCookies();
			driver.manage().timeouts().implicitlyWait(Duration.ZERO);
			
			driver.get(p.getProperty("appURL")); //Reading URL from properties file
			driver.manage().window().maximize();
			
		}
		
	    public void quitDriver() 
	    {
	        WebDriver currentDriver = getDriver();
	        if (currentDriver != null) {
	            try {
	                currentDriver.quit();
	            } finally {
	                tdriver.remove();
	                driver = null;
	            }
	        }
	    }

	    @AfterClass(alwaysRun = true)
		public void tearDown()
		{
	    	quitDriver();
		}
	    
	    public String randomString()
	    {
	    	String generatedstring = RandomStringUtils.insecure().nextAlphanumeric(10);	
	    	return generatedstring;
	    }

}
