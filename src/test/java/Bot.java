
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.logging.Level;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.mashape.unirest.http.exceptions.UnirestException;

import io.github.bonigarcia.wdm.WebDriverManager;

public class Bot {

	WebDriver driver;
	WebDriverWait wait;
	static String accessToken = "";

	@BeforeSuite
	public void setUp() {

		LoggingPreferences preferences = new LoggingPreferences();
		preferences.enable(LogType.PERFORMANCE, Level.ALL);
		preferences.enable(LogType.BROWSER, Level.ALL);

		ChromeOptions option = new ChromeOptions();
		option.setCapability(CapabilityType.LOGGING_PREFS, preferences);
		option.setCapability("goog:loggingPrefs", preferences);
		option.addArguments();

		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver(option);
		driver.manage().window().maximize();
		wait = new WebDriverWait(driver, 30);

		driver.navigate().to("http://54.80.137.197:5000/");
	}

	@Test
	public void verifyHeaderContent() throws InterruptedException, AWTException, UnirestException, IOException {
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@href = '/intro']")));
		driver.findElement(By.xpath("//a[@href = '/intro']")).click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("start")));
		driver.findElement(By.id("start")).click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[text() = 'Proceed']")));
		for (WebElement element : driver.findElements(By.xpath("//button[text() = 'Proceed']"))) {
			element.click();
			boolean flag = false;
			LogEntries logs = driver.manage().logs().get(LogType.PERFORMANCE);
			for (LogEntry entry : logs) {
				if (entry.toString().contains("\"url\":\"http://54.80.137.197:5000/r/c1\"")) {
					flag = true;
				}
			}
			if (flag)
				break;
		}

		driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL, Keys.END);

		driver.switchTo().frame("aVideoPlayer");

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button.ytp-large-play-button")));
		driver.findElement(By.cssSelector("button.ytp-large-play-button")).click();

		Thread.sleep(9000);

		driver.findElement(By.cssSelector("body")).sendKeys(Keys.TAB);
		driver.findElement(By.cssSelector("body")).sendKeys(Keys.TAB);
		driver.findElement(By.cssSelector("body")).sendKeys(Keys.chord(Keys.TAB, Keys.ENTER));

		driver.switchTo().parentFrame();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("aVideoSubmit")));
		driver.findElement(By.id("aVideoSubmit")).click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("td.green")));
		WebElement exit = null;
		for (WebElement cell : driver.findElements(By.cssSelector("table#maze tr td")))
			if (cell.getAttribute("class").contains("green"))
				exit = cell;

		while (exit.getAttribute("class").contains("green"))
			driver.findElements(By.cssSelector("a.light-blue")).get(new Random().nextInt(4)).click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("crystalMazeFormSubmit")));
		driver.findElement(By.id("crystalMazeFormSubmit")).click();

		Thread.sleep(10000);

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("map")));
		Thread.sleep(5000);
		driver.findElement(By.cssSelector("body")).sendKeys(Keys.TAB);
		Thread.sleep(2000);

		Robot robot = new Robot();
		Thread.sleep(2000);
		robot.keyPress(KeyEvent.VK_I);
		Thread.sleep(2000);
		robot.keyRelease(KeyEvent.VK_I);

		WebElement circle = driver.findElement(By.xpath("//div[@id='map']//*[name() = 'svg']//*[name()='circle']"));
		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.xpath("//div[@id='map']//*[name() = 'svg']//*[name()='circle']")));

		JavascriptExecutor js = (JavascriptExecutor) driver;

		js.executeScript("arguments[0].setAttribute('cx', '365.5')", circle);
		js.executeScript("arguments[0].setAttribute('cy', '100.5')", circle);
		Thread.sleep(2000);

		driver.findElement(By.id("mapsChallengeSubmit")).click();

		boolean flag = false;
		String captcha = "";

		LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
		for (LogEntry entry : logs) {
			if (flag)
				captcha = entry.getMessage().substring(47, entry.getMessage().length() - 1);
			System.out.println(captcha);
			if (entry.getMessage().contains("here"))
				flag = true;
		}

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("notABotCaptchaResponse")));
		driver.findElement(By.id("notABotCaptchaResponse")).sendKeys(captcha);
		driver.findElement(By.id("notABotCaptchaSubmit")).click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("socketGateMessage")));
		handleSocketConnection(driver.findElement(By.cssSelector("div.yellow")).getText().trim());
		driver.findElement(By.id("socketGateMessage")).sendKeys(accessToken);
		driver.findElement(By.cssSelector("form#socketGate button")).click();

	}

	public static void handleSocketConnection(String message) throws IOException {

		try {
			final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(
					new URI("ws://54.80.137.197:5001"));

			clientEndPoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
				public void handleMessage(String message) {
					accessToken = message;
					System.out.println(message);
				}
			});

			clientEndPoint.sendMessage(message);

			Thread.sleep(5000);

		} catch (InterruptedException ex) {
			System.err.println("InterruptedException exception: " + ex.getMessage());
		} catch (URISyntaxException ex) {
			System.err.println("URISyntaxException exception: " + ex.getMessage());
		}

	}
}
