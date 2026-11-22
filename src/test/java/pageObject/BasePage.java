package pageObject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

//parent of all pageObjectClasses

public class BasePage {
	
	protected final WebDriver driver;
	private final WebDriverWait wait;
	
	public BasePage(WebDriver driver)
	{
		this.driver = driver;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		PageFactory.initElements(driver, this);
	}

	protected WebElement waitForVisibility(WebElement element)
	{
		return wait.until(ExpectedConditions.visibilityOf(element));
	}

	protected WebElement waitForVisibility(WebElement element, Duration timeout)
	{
		return new WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOf(element));
	}

	protected WebElement waitForClickable(WebElement element)
	{
		return wait.until(ExpectedConditions.elementToBeClickable(element));
	}
}
