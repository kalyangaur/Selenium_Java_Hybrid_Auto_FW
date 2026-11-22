package pageObject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AccountSignupDetailsPage extends BasePage
{
	public AccountSignupDetailsPage(WebDriver driver)
	{
		super(driver);
	}
	
	
@FindBy(xpath="//input[@id='id_gender1']")
WebElement rdbtTilte;	

@FindBy(xpath="//input[@id='password']")
WebElement txtPassword;
	
@FindBy(xpath="//select[@id='days']")
WebElement ddDate;	

@FindBy(xpath="//select[@id='months']")
WebElement ddMonth;	

@FindBy(xpath="//select[@id='years']")
WebElement ddYear;	

@FindBy(xpath="//input[@id='first_name']")
WebElement txtAddressFirstName;

@FindBy(xpath="//input[@id='last_name']")
WebElement txtAddressLastName;

@FindBy(xpath="//input[@id='address1']")
WebElement txtAddress;

@FindBy(xpath="//select[@id='country']")
WebElement ddCountry;

@FindBy(xpath="//input[@id='state']")
WebElement txtState;

@FindBy(xpath="//input[@id='city']")
WebElement txtCity;

@FindBy(xpath="//input[@id='zipcode']")
WebElement txtZipCode;

@FindBy(xpath="//input[@id='mobile_number']")
WebElement txtMobileNo;

@FindBy(xpath="//button[normalize-space()='Create Account']")
WebElement btnCreateAccount;

@FindBy(xpath = "//b[normalize-space()='Account Created!']")
WebElement btnAccountCreated;

public void enterAccInfo(String password, String date, String month, String year, String addfn, String addln, String address, String country, String state, String city, String zip, String mobileno )
{
waitForClickable(rdbtTilte).click();
waitForVisibility(txtPassword).sendKeys(password);
waitForVisibility(ddDate).sendKeys(date);
waitForVisibility(ddMonth).sendKeys(month);
waitForVisibility(ddYear).sendKeys(year);
waitForVisibility(txtAddressFirstName).sendKeys(addfn);
waitForVisibility(txtAddressLastName).sendKeys(addln);
waitForVisibility(txtAddress).sendKeys(address);
waitForVisibility(ddCountry).sendKeys(country);
waitForVisibility(txtState).sendKeys(state);
waitForVisibility(txtCity).sendKeys(city);
waitForVisibility(txtZipCode).sendKeys(zip);
waitForVisibility(txtMobileNo).sendKeys(mobileno);
waitForClickable(btnCreateAccount).click();
}

//only capture the message in pageObject class and validation will happen in testCases
public String getConfirmationmsg()
{  
	try 
	 { 
		return waitForVisibility(btnAccountCreated).getText();
	 } 
	catch (Exception e) 
	 {
		return (e.getMessage());
	 }
}
   



}
