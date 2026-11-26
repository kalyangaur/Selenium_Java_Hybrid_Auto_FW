package pageObject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.FindBy;

import java.time.Duration;

public class LoginPage extends BasePage
{

	public LoginPage(WebDriver driver) 
	{
		super(driver);
		// TODO Auto-generated constructor stub
	}

	
	//pagefactory elements identification
	//Login Page Elements 
	@FindBy(xpath="//input[@data-qa='login-email']")	
	WebElement txtEmailID;
	   
	@FindBy(xpath="//input[@placeholder='Password']")	
	WebElement txtPassword;
	   
	@FindBy(xpath="//button[normalize-space()='Login']")	
	WebElement btnLogin;
	
	@FindBy(xpath="//li[10]//a[1]")
	WebElement lnkLoggedinAsMsg;
	   
	@FindBy(xpath="//a[normalize-space()='Logout']")
	WebElement lnkLogout;
	 
	//Login Page Methods
	public void setEmail(String emailID)
	{
		waitForVisibility(txtEmailID).sendKeys(emailID);
	}
			
	public void setPassword(String password)
	{
		waitForVisibility(txtPassword).sendKeys(password);
	}
	
	public void clickLogin()
	{
		waitForClickable(btnLogin).click();
	}	
	
	public void clickLogout()
	{
		waitForClickable(lnkLogout).click();
	}	

	public boolean logoutdisplayed()
	{  
		try
		{
		   waitForVisibility(lnkLogout, Duration.ofSeconds(5));
		   return true;
		}
		catch(TimeoutException e)
		{
		   return false;
			
		}
	}

}
	
	
	
	

