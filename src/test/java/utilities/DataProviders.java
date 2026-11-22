package utilities;

import java.io.IOException;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import jdk.jshell.execution.Util;


public class DataProviders {
	
	@DataProvider(name ="LoginData")
	public String[][] getData() throws IOException
	{
		String path = ".\\testData\\LoginData.xlsx";
		
		ExcelUtility xlutil = new ExcelUtility(path);
		
		int totalrows = xlutil.getRowCount("Credentials");
		int totalcols = xlutil.getCellCount("Credentials", 1);
		
		String logindata[][] = new String[totalrows][totalcols];
		
		//Read data from excel and ignore 1sr row as its Header of table
		for(int i=1;i<=totalrows;i++)
		{ 
			for(int j=0;j<totalcols;j++)
			{
				logindata[i-1][j] = xlutil.getCellData("Credentials", i, j);
			}
		}
      return logindata; // Returning two dimension array
	}
}
