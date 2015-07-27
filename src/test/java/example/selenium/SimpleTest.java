package example.selenium;
 
import java.util.concurrent.TimeUnit;
 
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 
public class SimpleTest {
 
    WebDriver driver = new HtmlUnitDriver(true);
 
    String baseUrl = "http://localhost:7070";
 
    @Before
    public void setUp() throws Exception {
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }
 
    @Test
    public void testRootPage() throws Exception {
        driver.get(baseUrl + "/");
    }

    @Test
    public void testJsp() throws Exception {
        driver.get(baseUrl + "/hello.jsp");

        assertEquals("Hello!", driver.findElement(By.id("find_me")).getText());
        assertEquals("Maestro Sample Maven Web Application JSP Page", driver.getTitle());
    }
 
    @After
    public void tearDown() throws Exception {
        driver.quit();
    }
}
