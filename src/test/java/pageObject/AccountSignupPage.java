package pageObject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AccountSignupPage extends BasePage
{

	public AccountSignupPage(WebDriver driver) 
	{
		super(driver);
	}

	 
	//SignUp elements
	@FindBy(xpath="//input[@placeholder='Name']")
	WebElement txtName;

	@FindBy(xpath="//input[@data-qa='signup-email']")
	WebElement txtEmail;

	@FindBy(xpath="//button[normalize-space()='Signup']")
	WebElement btnSignup;

	public void setName(String fname)
	{
		waitForVisibility(txtName).sendKeys(fname);
	}
			
	public void setEmail(String email)
	{
		waitForVisibility(txtEmail).sendKeys(email);
	}
	
	public void clickSignup()
	{
		waitForClickable(btnSignup).click();
		
		//so12
		//btnSignup.submit();

		//so13
		//Actions act=new Actions(driver);
		//act.moveToElement(btnSignup).click().perform();

		//so14
		//JavascriptExecutor js=(JavascriptExecutor)driver;
		//js.executeScript("arguments[0].click();", btnSignup);

		//Sol 5
		//btnContinue.sendKeys(Keys.RETURN);

		//So16
		//WebDriverWait mywait = new WebDriverWait(driver, Duration.ofSeconds(10));
		//mywait.until(ExpectedConditions.elementToBeClickable(btnSignup)).click();
		
	}	

}
