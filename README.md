# Selenium Grid + Java / Maven / TestNG — Docker Compose

This setup runs a Selenium Grid 4 Hub with Chrome, Firefox, and Edge browser nodes, plus a Maven test-runner container for your existing Java/TestNG project.

## Services

| Service | Purpose | Host access |
|---|---|---|
| `selenium-hub` | Grid UI, WebDriver endpoint, Event Bus | `http://localhost:4444` |
| `chrome` | Chrome browser node | Internal Docker network |
| `firefox` | Firefox browser node | Internal Docker network |
| `edge` | Edge browser node | Internal Docker network |
| `test-runner` | Runs Maven/TestNG tests | Compose profile `tests` |

All services share the `selenium-grid` bridge network. The test runner must use `http://selenium-hub:4444` (the Compose service name), not `localhost`, to reach the Hub from inside Docker.

## Assumptions

- Run these files from the root of your automation project.
- The project root contains `pom.xml`, `src/`, and normally `testng.xml`.
- Your Maven Surefire/TestNG configuration is already set up in `pom.xml`.
- Your framework creates a `RemoteWebDriver` using the `SELENIUM_REMOTE_URL` and `BROWSER` environment variables.
- Selenium Hub and browser-node images use the same version. The supplied default is `4.52.0`; change `SELENIUM_VERSION` in `.env` if your project uses another available matching Selenium Docker image version.

## 1. Put the files in your project

Copy `docker-compose.yaml`, `Dockerfile`, `.dockerignore`, and `.env.example` into the same directory as `pom.xml`.

Create your local `.env` file:

**Windows PowerShell**
```powershell
Copy-Item .env.example .env
```

**Windows CMD**
```cmd
copy .env.example .env
```

Review `.env` and adjust the version/browser settings if needed.

## 2. Start Selenium Grid

Start Hub and all three browser nodes:

```bash
docker compose up -d selenium-hub chrome firefox edge
```

Check container status:

```bash
docker compose ps
```

Open the Grid UI:

- `http://localhost:4444`
- `http://localhost:4444/ui`

Check Grid readiness:

**PowerShell**
```powershell
Invoke-RestMethod http://localhost:4444/status
```

**CMD**
```cmd
curl http://localhost:4444/status
```

The response should report `value.ready` as `true`. In the Grid UI, confirm that Chrome, Firefox, and Edge nodes have registered before launching tests.

## 3. Configure your Java framework for RemoteWebDriver

Your framework needs to read the remote URL and browser from environment variables. Example factory method:

```java
import java.net.URL;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class DriverFactory {
    public static WebDriver createDriver() throws Exception {
        String remoteUrl = System.getenv().getOrDefault(
            "SELENIUM_REMOTE_URL", "http://localhost:4444"
        );
        String browser = System.getenv().getOrDefault("BROWSER", "chrome");

        switch (browser.toLowerCase()) {
            case "firefox":
                return new RemoteWebDriver(new URL(remoteUrl), new FirefoxOptions());
            case "edge":
                return new RemoteWebDriver(new URL(remoteUrl), new EdgeOptions());
            case "chrome":
            default:
                ChromeOptions chromeOptions = new ChromeOptions();
                if (Boolean.parseBoolean(
                        System.getenv().getOrDefault("HEADLESS", "true"))) {
                    chromeOptions.addArguments("--headless=new");
                }
                chromeOptions.addArguments("--no-sandbox", "--disable-dev-shm-usage");
                return new RemoteWebDriver(new URL(remoteUrl), chromeOptions);
        }
    }
}
```

Use this factory from your existing TestNG `@BeforeMethod` / `@BeforeClass` setup, and call `driver.quit()` in the corresponding teardown. Do not instantiate `ChromeDriver`, `FirefoxDriver`, or `EdgeDriver` when running against Grid.

If you already have a `BaseClass`, adapt its existing driver initialization rather than adding a second driver lifecycle.

## 4. Run the TestNG/Maven suite inside Docker

The Compose runner defaults to `mvn clean test` and `SeleniumGrid_Docker.xml`. That suite selects the `Regression` group, so it currently runs `TC002_Login` on Chrome and Edge; the signup and data-driven tests are not in that group.

```bash
docker compose --profile tests run --rm test-runner
```

To select a different TestNG suite, set `TESTNG_SUITE`:

**PowerShell**
```powershell
$env:TESTNG_SUITE="crossbrowserparalleltesting.xml"
docker compose --profile tests run --rm test-runner
Remove-Item Env:TESTNG_SUITE
```

The browser capabilities for the default suite are defined in `SeleniumGrid_Docker.xml`. For a custom suite, set its `browser` parameters accordingly.

The Compose setup starts the required browser nodes and waits for them to become healthy before running Maven.

To select another browser in a custom suite without editing files:

**PowerShell**
```powershell
$env:BROWSER="firefox"
docker compose --profile tests run --rm test-runner
Remove-Item Env:BROWSER
```

**CMD**
```cmd
set BROWSER=firefox
docker compose --profile tests run --rm test-runner
```

For Edge, use `BROWSER=edge`.

To pass Maven arguments directly:

```bash
docker compose --profile tests run --rm -e MAVEN_ARGS="clean test -Dtest=LoginTest" test-runner
```

If your project uses an explicit TestNG suite file and Surefire is not already configured to pick it up, configure it in `pom.xml`, or pass the appropriate Surefire property, for example:

```bash
docker compose --profile tests run --rm -e MAVEN_ARGS="clean test -Dsurefire.suiteXmlFiles=testng.xml" test-runner
```

The exact suite property depends on your Maven Surefire/TestNG configuration.

## 5. Run tests from your Windows host instead

If you prefer running Maven from Eclipse/Windows while browsers execute in Docker:

- Keep the Hub and nodes running with `docker compose up -d selenium-hub chrome firefox edge`.
- Set the framework remote URL to `http://localhost:4444`.
- Run your TestNG suite from Eclipse or `mvn clean test` on Windows.

Do not use `http://selenium-hub:4444` from Windows; that DNS name is only available to containers on the Compose network.

## 6. Useful commands

```bash
# Start Grid
docker compose up -d selenium-hub chrome firefox edge

# View running services
docker compose ps

# Follow Hub logs
docker compose logs -f selenium-hub

# Follow Chrome node logs
docker compose logs -f chrome

# Follow all Grid logs
docker compose logs -f

# Stop and remove containers/network (keeps Maven cache volume)
docker compose down

# Also remove the Maven cache volume
docker compose down -v

# Rebuild the test-runner image
docker compose build --no-cache test-runner
```

## 7. Troubleshooting

### `SessionNotCreatedException` / empty or invalid response
1. Confirm `http://localhost:4444/status` returns JSON and `value.ready` is `true`.
2. Confirm browser nodes appear in `http://localhost:4444/ui`.
3. Check logs: `docker compose logs selenium-hub chrome firefox edge`.
4. Ensure all Selenium images use the same `SELENIUM_VERSION`.
5. From a container, use `http://selenium-hub:4444`; from Windows, use `http://localhost:4444`.

### Node does not register
- Check the Hub is healthy and all services are on the `selenium-grid` network.
- Confirm the Event Bus host and ports in the node environment are unchanged.
- Check that Docker has enough memory/CPU for the selected browser nodes.

### Browser exits or crashes
- Keep `shm_size: 2gb` for browser nodes.
- Start with one session per node. Increase only after confirming sufficient host resources.
- On Windows, ensure Docker Desktop is running with Linux containers enabled.

### Tests run before nodes are ready
The Compose dependencies wait for Hub health and node container startup, but `service_started` does not guarantee Grid registration. For reliable runs, verify the Grid UI/status before starting the suite, or add a Grid-node readiness wait in your test launcher.

## Notes

- The `test-runner` is behind the `tests` profile so `docker compose up -d` does not unexpectedly execute a test suite.
- Test reports written under the project workspace (for example `target/surefire-reports`) remain available on the host because the project directory is bind-mounted.
- Do not commit real credentials or secrets in `.env`.
