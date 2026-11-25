package testCases;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import utilities.ReportingListener;

/**
 * Creation date: 2024-06-10 by Claude AI
 * Base class for all test classes. Creates the WebDriver (local or Selenium Grid),
 * reads config.properties and cleans Allure results before the suite.
 *
 * Headless mode (needed when Jenkins runs as a Windows service / SYSTEM, which has no desktop):
 *   mvn clean test -Dheadless=true        (or set headless=true in config.properties)
 */
@Listeners({ReportingListener.class}) // Applied to every test class that extends this base class (Allure reporting)
public class BaseClass
{
    public WebDriver driver;
    public Properties p;

    // One WebDriver per thread, so parallel execution does not share browsers.
    // Tests / listeners should prefer getDriver() over the 'driver' field when running in parallel.
    public static ThreadLocal<WebDriver> tdriver = new ThreadLocal<>();

    // Temp browser profile used only in headless local runs, deleted again in quitDriver().
    private static final ThreadLocal<Path> profileDir = new ThreadLocal<>();

    public static WebDriver getDriver()
    {
        // ThreadLocal.get() is already thread-safe; 'synchronized' would only serialise all threads.
        return tdriver.get();
    }

    /**
     * Legacy helper kept so existing callers still compile. It now reuses the same
     * option handling as setup(). If nothing calls it, delete it.
     */
    
    @BeforeSuite(alwaysRun = true)
    public void cleanAllureResults() throws IOException
    {
        Path allureResults = Path.of("allure-results");
        if (Files.exists(allureResults))
        {
            FileUtils.deleteDirectory(allureResults.toFile());
        }
    }

    @BeforeClass(alwaysRun = true)
    @Parameters({"os", "browser"})
    public void setup(@Optional("windows") String os, @Optional("chrome") String br) throws IOException
    {
        p = loadConfig();

        String env = p.getProperty("execution_env", "local").trim();
        boolean headless = isHeadless();

        if (env.equalsIgnoreCase("remote"))
        {
            driver = createRemoteDriver(os, br, headless);   // Selenium Grid
        }
        else if (env.equalsIgnoreCase("local"))
        {
            driver = createLocalDriver(br, headless);         // Browser on this machine
        }
        else
        {
            throw new IllegalArgumentException("execution_env must be 'local' or 'remote', but was: " + env);
        }

        // Register the driver immediately so tearDown() can always quit it, even if a later step fails.
        tdriver.set(driver);

        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.get(requireProperty("appURL"));

        if (!headless)
        {
            driver.manage().window().maximize();   // No effect in headless; --window-size is used there
        }
    }

    public void quitDriver()
    {
        WebDriver currentDriver = getDriver();
        try
        {
            if (currentDriver != null)
            {
                currentDriver.quit();
            }
        }
        finally
        {
            tdriver.remove();
            driver = null;

            Path dir = profileDir.get();
            if (dir != null)
            {
                FileUtils.deleteQuietly(dir.toFile());
                profileDir.remove();
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
        return RandomStringUtils.insecure().nextAlphanumeric(10);
    }

    // ------------------------------------------------------------------
    // Driver creation helpers
    // ------------------------------------------------------------------

    private WebDriver createLocalDriver(String br, boolean headless)
    {
        ChromiumOptions<?> options = newOptions(br);

        if (headless)
        {
            addHeadlessArgs(options);
            // Own profile per session: avoids profile locks in parallel runs and the missing
            // user profile of the SYSTEM account (a cause of "DevToolsActivePort file doesn't exist").
            options.addArguments("--user-data-dir=" + createProfileDir());
        }

        // Selenium Manager (built into Selenium 4.6+) downloads the matching driver, so
        // WebDriverManager is no longer needed here.
        if (options instanceof EdgeOptions)
        {
            return new EdgeDriver((EdgeOptions) options);
        }
        return new ChromeDriver((ChromeOptions) options);
    }

    private WebDriver createRemoteDriver(String os, String br, boolean headless) throws IOException
    {
        ChromiumOptions<?> options = newOptions(br);

        if (headless)
        {
            addHeadlessArgs(options);   // No --user-data-dir: a local temp path would not exist on the grid node
        }

        Platform platform = resolvePlatform(os);
        if (platform != null)
        {
            options.setCapability(CapabilityType.PLATFORM_NAME, platform);
        }

        String remoteUrl = System.getenv().getOrDefault("SELENIUM_REMOTE_URL", "http://localhost:4444");
        return new RemoteWebDriver(URI.create(remoteUrl).toURL(), options);
    }

    private ChromiumOptions<?> newOptions(String br)
    {
        switch (br.toLowerCase())
        {
            case "chrome":
                return new ChromeOptions();
            case "edge":
                return new EdgeOptions();
            default:
                // Fail loudly instead of continuing with a null driver (which caused NPEs later)
                throw new IllegalArgumentException("Unsupported browser: '" + br + "' (use chrome or edge)");
        }
    }

    private Platform resolvePlatform(String os)
    {
        if ("windows".equalsIgnoreCase(os)) return Platform.WIN11;
        if ("linux".equalsIgnoreCase(os))   return Platform.LINUX;
        if ("mac".equalsIgnoreCase(os))     return Platform.MAC;

        System.out.println("No matching OS for '" + os + "', platform not set");
        return null;
    }

    private void addHeadlessArgs(ChromiumOptions<?> options)
    {
        options.addArguments(
                "--headless=new",
                "--window-size=1920,1080",
                "--disable-gpu",
                "--no-sandbox"   // Needed when running as SYSTEM / in containers; only used in headless (CI) runs
        );
    }

    private String createProfileDir()
    {
        try
        {
            Path dir = Files.createTempDirectory("browser-profile-");
            profileDir.set(dir);
            return dir.toString();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException("Could not create temporary browser profile", e);
        }
    }

    // ------------------------------------------------------------------
    // Configuration helpers
    // ------------------------------------------------------------------

    /** Reads config.properties from the classpath (src/test/resources), independent of the working directory. */
    private Properties loadConfig() throws IOException
    {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties"))
        {
            if (in == null)
            {
                throw new IOException("config.properties not found on the classpath (expected in src/test/resources)");
            }
            props.load(in);
        }
        return props;
    }

    private String requireProperty(String key)
    {
        String value = p.getProperty(key);
        if (value == null || value.isBlank())
        {
            throw new IllegalStateException("Missing required property '" + key + "' in config.properties");
        }
        return value.trim();
    }

    /** -Dheadless=true on the Maven command line wins over config.properties; default is false. */
    private boolean isHeadless()
    {
        String fromConfig = (p != null) ? p.getProperty("headless", "false") : "false";
        return Boolean.parseBoolean(System.getProperty("headless", fromConfig).trim());
    }
}
