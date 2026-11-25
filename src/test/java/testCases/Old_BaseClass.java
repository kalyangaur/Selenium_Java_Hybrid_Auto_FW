package testCases;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.time.Duration;
import java.util.Properties;

import org.apache.commons.lang3.RandomStringUtils;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.*;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;

import org.testng.annotations.Listeners;
import utilities.ReportingListener;

@Listeners({ReportingListener.class}) //Adding listener to the base class to generate Allure report. 
//This listener will be applied to all the test classes that extend this base class.	
//This base class is used to initialize the driver and read the properties file. All the test classes will extend this base class to get the driver and properties file.
public class Old_BaseClass 
{
	public WebDriver driver;
	public Properties p;
    
	//Parallel testing driver thread creation
	//ThreadLocal is a class that provides thread-local variables. Each thread accessing such a variable (via its get or set method) has its own, independently initialized copy of the variable. ThreadLocal instances are typically private static fields in classes that wish to associate state with a thread.
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
	    @Parameters({"os", "browser"})
		public void setup(String os, @Optional("chrome") String br) throws IOException
		{     
	    	//Read Properties file in resource folder to get data
	    	p = new Properties();
	    	try (FileReader objfilereader = new FileReader("./src/test/resources/config.properties")) 
	    	{
	    		p.load(objfilereader);
	    	}
	    	
	    	//Remote machine execution - using Selenium Grid
	    	//Select OS
	    	if(p.getProperty("execution_env").equalsIgnoreCase("remote")) //Reading execution environment from properties file
	    	{
               DesiredCapabilities objcapabilities = new  DesiredCapabilities(); //Creating object of DesiredCapabilities class
               
               if("windows".equalsIgnoreCase(os)) 
                {
            	   objcapabilities.setPlatform(Platform.WIN11);
	    	    }
               else if("linux".equalsIgnoreCase(os)) 
               {
               	objcapabilities.setPlatform(Platform.LINUX);
                 }
               else if("mac".equalsIgnoreCase(os)) 
                {
                	objcapabilities.setPlatform(Platform.MAC);
                  }
               else
                {
                    System.out.println("No Matching OS");
                }
	    	
                //Select Browser
	    	    switch(br.toLowerCase())
	    	    {
	    	    case "chrome" :
	    	    	objcapabilities.setBrowserName("chrome");
	    	    	break;
	    	    case "edge":
	    	    	objcapabilities.setBrowserName("MicrosoftEdge");
	    	    	break;
	    	    default: 
	    	    	System.out.println("No Matching browser");
	    	    }
                
				String remoteUrl = System.getenv().getOrDefault("SELENIUM_REMOTE_URL", "http://localhost:4444");
				URL gridURL = URI.create(remoteUrl).toURL();
				driver = new RemoteWebDriver(gridURL, objcapabilities);
	    	}
	    	
	    	    //Local Machine Execution
		    	if(p.getProperty("execution_env").equalsIgnoreCase("local"))
		    	 {
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
