package org.bordylek.web;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Function;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.bordylek.service.model.*;
import org.bordylek.service.repository.CommunityRepository;
import org.bordylek.service.repository.UserRepository;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.junit.Assert.*;

@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(locations = {"/uat-context.xml"})
public class UISteps {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private InMemoryUserDetailsManager userDetailsManager;

    @Autowired
    private MetricRegistry metricRegistry;

    private SharedDriver driver;
    private WebDriverWait wait;
    private EmbeddedHttpServer server;
    private User user;
    private Community community;

    public static final int PORT = 8080;
    public static final String URL = "http://localhost:" + PORT;
    public static final int INITIAL_TIMEOUT = 300;

    @Before
	public void before() throws Exception {
        server = new EmbeddedHttpServer();
        server.start(PORT, webApplicationContext);
		driver = new SharedDriver();
		driver.get(URL + "/rest");
		wait = new WebDriverWait(driver, INITIAL_TIMEOUT);
	}

	@After
	public void afterScenario() throws Exception {
        server.stop();

	}

    @Given("^community \"(Prague)\" exists$")
    public void theCommunityExists(String communityName) throws Throwable {
        if (user == null) throw new IllegalStateException("no user defined");
        community = new Community(communityName);
        community.setLocation(new Point(14.0, 50.0));
        community.setCreatedBy(user);
        communityRepository.save(community);
    }

    @Given("^(new|verified) user \"([^\"]*)\" with email ([\\w@\\.]+) exists$")
    public void theUserExists(String type, String name, String email) throws Throwable {
        user = new User();
        user.setEmail(email);
        user.setRegId("1");
        user.setName(name);
        user.setCreateDate(new Date());
        user.setStatus(UserStatus.valueOf(type.toUpperCase()));
        userRepository.save(user);
    }

    @Given("^lives in \"(Prague, Czech Republic)\"$")
    public void livingIn(String locationName) throws Throwable {
        if (user == null) throw new IllegalStateException("no user defined");
        user.setLocation(new Location(locationName, 50.0, 14.0));
        userRepository.save(user);
    }

    @Given("^is member of community \"([^\"]*)\"$")
    public void memberOfCommunity(String communityName) throws Throwable {
        if (user == null) throw new IllegalStateException("No user defined");
        Community comm  = communityRepository.findByTitle(communityName);
        user.getCommunities().add(new CommunityRef(comm.getId(), comm.getTitle()));
        userRepository.save(user);
    }

    @Given("^the ([/\\w-]+) page is shown$")
    public void pageShown(String uri) throws InterruptedException {
        driver.get(URL + "/" + ("index".equals(uri) ? "" : "#/" + uri));
        wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath("//body"));
            }
        });
    }

    @Then("^([\\w-]+) is shown$")
    public void shown(final String entityId)  {
        wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                WebElement element = driver.findElement(By.id(entityId));
                return element.isDisplayed() ? element : null;
            }
        });
    }

    @Then("^([\\w-]+) is shown with value (\\w+)$")
    public void shownValue(final String entityId, String value)  {
        final WebElement element = wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                WebElement element = driver.findElement(By.id(entityId));
                return element.isDisplayed() && element.getText().length() > 0 ? element : null;
            }
        });

        assertEquals(value, element.getText());
    }

    @Then("^field ([\\w-]+) is shown with value (\\w+)$")
    public void fieldShownValue(final String id, String value)  {
        final WebElement element = wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                WebElement element = driver.findElement(By.id(id));
                return element.isDisplayed() && element.getAttribute("value").length() > 0 ? element : null;
            }
        });

        assertEquals(value, element.getAttribute("value"));
    }

    @Then("^field ([\\w-]+) is shown with value \"([^\"]*)\"$")
    public void fieldShownValueQuotes(final String id, String value)  {
        final WebElement element = wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                WebElement element = driver.findElement(By.id(id));
                return element.isDisplayed() && element.getAttribute("value").length() > 0 ? element : null;
            }
        });

        assertEquals(value, element.getAttribute("value"));
    }

    @Then("^([\\w-]+) is shown with value \"([^\"]*)\"$")
    public void shownValueQuotes(final String entityId, String value)  {
        final WebElement element = wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                WebElement element = driver.findElement(By.id(entityId));
                return element.isDisplayed() && element.getText().length() > 0 ? element : null;
            }
        });

        assertEquals(value, element.getText());
    }

    @Then("^xpath (.+) is shown$")
    public void xpathIsShown(final String xpath) throws Throwable {
        wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                WebElement element = driver.findElement(By.xpath(xpath));
                return element.isDisplayed() && element.getText().length() > 0 ? element : null;
            }
        });
    }

    @Then("^xpath (.+) is shown with value \"([^\"]*)\"$")
    public void xpathIsShownWithValue(final String xpath, final String value) throws Throwable {
        final WebElement element = wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                WebElement element = driver.findElement(By.xpath(xpath));
                return element.isDisplayed() && element.getText().length() > 0 ? element : null;
            }
        });

        assertEquals(value, element.getText());
    }

    @Then("^([\\w-]+) is shown with value containing (\\w+)$")
    public void shownValueContaining(final String entityId, String value)  {
        final WebElement element = wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                WebElement element = driver.findElement(By.id(entityId));
                return element.isDisplayed() && element.getText().length() > 0 ? element : null;
            }
        });

        assertTrue(element.getText() + " does not contain " + value, element.getText().contains(value));
    }

    @And("^([\\w-]+) is shown with value containing \"([^\"]*)\"$")
    public void shownValueContainingQuotes(final String entityId, String value) throws Throwable {
        final WebElement element = wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                WebElement element = driver.findElement(By.id(entityId));
                return element.isDisplayed() && element.getText().length() > 0 ? element : null;
            }
        });

        assertTrue(element.getText() + " does not contain " + value, element.getText().contains(value));
    }

    @Then("^([\\w-]+) is shown empty$")
    public void shownEmpty(final String elementId)  {
        final WebElement element = driver.findElement(By.id(elementId));
        assertEquals("", element.getText());
    }

    @Then("^([\\w-]+) is not shown$")
    public void idNotShown(final String elementId)  {
        try {
            WebElement element = driver.findElement(By.id(elementId));
            if (element != null) assertFalse(element.isDisplayed());
        } catch (NoSuchElementException ignored) {
        }
    }

    @Then("^xpath (.+) is not shown$")
    public void xpathNotShown(final String xpath)  {
        try {
            WebElement element = driver.findElement(By.xpath(xpath));
            if (element != null) assertFalse(element.isDisplayed());
        } catch (NoSuchElementException ignored) {
        }
    }

    @When("^([\\w-]+) is (disabled|enabled)")
    public void disabled(final String elementId, final String state) throws InterruptedException {
        wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                WebElement element = driver.findElement(By.id(elementId));
                boolean stateCond = !"disabled".equals(state) || "true".equals(element.getAttribute("disabled"));
                return element.isDisplayed() && stateCond ? element : null;
            }
        });
    }

    @And("^(\\w+) key is pressed$")
    public void keyPressed(Keys key) throws Throwable {
        driver.switchTo().activeElement().sendKeys(key);
        Thread.sleep(500);
    }

    @When("^id (.+) is clicked$")
    public void clickId(final String elementId) throws InterruptedException {
        wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                WebElement element = driver.findElement(By.id(elementId));
                return element.isEnabled() ? element : null;
            }
        }).click();
        Thread.sleep(500);
    }

    @Then("^xpath (.+) is clicked$")
    public void xpathIsClicked(final String xpath) throws Throwable {
        wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                WebElement element = driver.findElement(By.xpath(xpath));
                return element.isDisplayed() && element.getText().length() > 0 ? element : null;
            }
        }).click();
    }

    @When("^(.+) input field value is \"([^\"]*)\"$")
    public void fieldValueIs(final String elementId, String value) throws Throwable {
        final WebElement field = wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                WebElement element = driver.findElement(By.id(elementId));
                return element.isDisplayed() ? element : null;
            }
        });
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        field.sendKeys(Keys.DELETE);
        field.sendKeys(value);
    }

}
