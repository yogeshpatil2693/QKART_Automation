package QKART_TESTNG;

import QKART_TESTNG.pages.Checkout;
import QKART_TESTNG.pages.Home;
import QKART_TESTNG.pages.Login;
import QKART_TESTNG.pages.Register;
import QKART_TESTNG.pages.SearchResult;

import static org.testng.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import org.testng.annotations.Test;

@Listeners(ListenerClass.class)
public class QKART_Tests {

        static RemoteWebDriver driver;
        public static String lastGeneratedUserName;

        @BeforeSuite
        public static void createDriver() throws MalformedURLException {
                // Launch Browser using Zalenium
                final DesiredCapabilities capabilities = new DesiredCapabilities();
                capabilities.setBrowserName(BrowserType.CHROME);
                driver = new RemoteWebDriver(new URL("http://localhost:8082/wd/hub"), capabilities);
                System.out.println("createDriver()");
        }

        /*
         * Testcase01: Verify a new user can successfully register
         */
        @Test(description = "Verify registration happens correctly", priority = 1,
                        groups = {"Sanity_test"})
        @Parameters({"UserName","Password"})
        public void TestCase01( String UserName,String Password) throws InterruptedException {
                Boolean status;

                // Visit the Registration page and register a new user
                Register registration = new Register(driver);
                registration.navigateToRegisterPage();
                status = registration.registerUser(UserName, Password, true);
                Assert.assertTrue(status, "Registration failed");

                // Save the last generated username
                lastGeneratedUserName = registration.lastGeneratedUsername;

                // Visit the login page and login with the previuosly registered user
                Login login = new Login(driver);
                login.navigateToLoginPage();
                status = login.PerformLogin(lastGeneratedUserName, Password);
                // logStatus("Test Step", "User Perform Login: ", status ? "PASS" : "FAIL");
                Assert.assertTrue(status, "Login failed");

                // Visit the home page and log out the logged in user
                Home home = new Home(driver);
                status = home.PerformLogout();

        }

        @Test(description = "Verify re-registering an already registered user fails", priority = 2,
                        groups = {"Sanity_test"})
        public void TestCase02() throws InterruptedException {
                Boolean status;

                // Visit the Registration page and register a new user
                Register registration = new Register(driver);
                registration.navigateToRegisterPage();
                status = registration.registerUser("testUser", "abc@123", true);
                Assert.assertTrue(status, "User registration failed");

                // Save the last generated username
                lastGeneratedUserName = registration.lastGeneratedUsername;

                // Visit the Registration page and try to register using the previously
                // registered user's credentials
                registration.navigateToRegisterPage();
                status = registration.registerUser(lastGeneratedUserName, "abc@123", false);
                Assert.assertFalse(status, "Re-registration Successfull");
                
        }

        @Test(description = "Verify the functionality of search text box", priority = 3,
                        groups = {"Sanity_test"})
        public void TestCase03() throws InterruptedException {
               
                boolean status;

                // Visit the home page
                Home homePage = new Home(driver);
                homePage.navigateToHome();

                // Search for the "yonex" product
                status = homePage.searchForProduct("YONEX");
                Assert.assertTrue(status,"Yonex product search failed");

                // Fetch the search results
                List<WebElement> searchResults = homePage.getSearchResults();

                // Verify the search results are available
                status = searchResults.size() == 0;
                Assert.assertFalse(status, "There were no results for the given search string");;

                for (WebElement webElement : searchResults) {
                        // Create a SearchResult object from the parent element
                        SearchResult resultelement = new SearchResult(webElement);

                        // Verify that all results contain the searched text
                        String elementText = resultelement.getTitleofResult();
                        status = elementText.toUpperCase().contains("YONEX");
                        Assert.assertTrue(status, "Test results contains un-expected values");
                }

                Thread.sleep(3000);
                // Search for product
                status = homePage.searchForProduct("Gesundheit");
                Assert.assertTrue(status,"Invalid keyword returned result");

                // Verify no search results are found
                searchResults = homePage.getSearchResults();
                if (searchResults.size() == 0) {
                        status = homePage.isNoResultFound();
                        Assert.assertTrue(status,"Successfully validated that no products found message is displayed");

                }
        }

        @Test(description = "Verify the existence of size chart for certain items and validate contents of size chart",
                        priority = 4, groups = {"Regression_Test"})
        public void TestCase04() throws InterruptedException {
                boolean status = false;

                // Visit home page
                Home homePage = new Home(driver);
                homePage.navigateToHome();

                // Search for product and get card content element of search results
                status = homePage.searchForProduct("Running Shoes");
                Assert.assertTrue(status, "Running shoes product search failed");
                List<WebElement> searchResults = homePage.getSearchResults();

                // Create expected values
                List<String> expectedTableHeaders =
                                Arrays.asList("Size", "UK/INDIA", "EU", "HEEL TO TOE");
                List<List<String>> expectedTableBody =
                                Arrays.asList(Arrays.asList("6", "6", "40", "9.8"),
                                                Arrays.asList("7", "7", "41", "10.2"),
                                                Arrays.asList("8", "8", "42", "10.6"),
                                                Arrays.asList("9", "9", "43", "11"),
                                                Arrays.asList("10", "10", "44", "11.5"),
                                                Arrays.asList("11", "11", "45", "12.2"),
                                                Arrays.asList("12", "12", "46", "12.6"));

                // Verify size chart presence and content matching for each search result
                for (WebElement webElement : searchResults) {
                        SearchResult result = new SearchResult(webElement);

                        // Verify if the size chart exists for the search result
                        status = result.verifySizeChartExists();
                        Assert.assertTrue(result.verifySizeChartExists(),
                                        "Size chart link does not exist");

                        // Verify if size dropdown exists
                        status = result.verifyExistenceofSizeDropdown(driver);
                        Assert.assertTrue(status, "Existence of size dropdown failed");

                        // Open the size chart
                        if (result.openSizechart()) {

                                Assert.assertTrue(result.validateSizeChartContents(
                                                expectedTableHeaders, expectedTableBody, driver),
                                                "Failure while validating contents of Size Chart Link");

                                // Close the size chart moda
                                status = result.closeSizeChart(driver);
                                Assert.assertTrue(status,"Size chart does not close");
                         }
                }

        }

        @Test(description = "Verify that a new user can add multiple products in to the cart and Checkout",
                        priority = 5, groups = {"Sanity_test"})
        @Parameters({"ProductNameToSearchFor1", "ProductNameToSearchFor2", "AddressDetails"})
        public void TestCase05(String ProductNameToSearchFor1, String ProductNameToSearchFor2,
                        String AddressDetails) throws InterruptedException {
                Boolean status;

                // Go to the Register page
                Register registration = new Register(driver);
                registration.navigateToRegisterPage();

                // Register a new user
                status = registration.registerUser("testUser", "abc@123", true);
                Assert.assertTrue(status, "Registartion failed");
           
                // Save the username of the newly registered user
                lastGeneratedUserName = registration.lastGeneratedUsername;

                // Go to the login page
                Login login = new Login(driver);
                login.navigateToLoginPage();

                // Login with the newly registered user's credentials
                status = login.PerformLogin(lastGeneratedUserName, "abc@123");
                Assert.assertTrue(status, "Login failed");

                // Go to the home page
                Home homePage = new Home(driver);
                homePage.navigateToHome();

                // Find required products by searching and add them to the user's cart
                status = homePage.searchForProduct(ProductNameToSearchFor1);
                Assert.assertTrue(status, "Yonex product search failed");
                homePage.addProductToCart(ProductNameToSearchFor1);
                status = homePage.searchForProduct(ProductNameToSearchFor2);
                Assert.assertTrue(status, "Tan product search failed");
                homePage.addProductToCart(ProductNameToSearchFor2);

                // Click on the checkout button
                homePage.clickCheckout();

                // Add a new address on the Checkout page and select it
                Checkout checkoutPage = new Checkout(driver);
                checkoutPage.addNewAddress(AddressDetails);
                checkoutPage.selectAddress(AddressDetails);

                // Place the order
                checkoutPage.placeOrder();

                WebDriverWait wait = new WebDriverWait(driver, 30);
                wait.until(ExpectedConditions
                                .urlToBe("https://crio-qkart-frontend-qa.vercel.app/thanks"));

                // Check if placing order redirected to the Thansk page
                status = driver.getCurrentUrl().endsWith("/thanks");
                Assert.assertTrue(status, "URL failed ");

                // Go to the home page
                homePage.navigateToHome();

                // Log out the user
                homePage.PerformLogout();

        }

        @Test(description = "Verify that the contents of the cart can be edited", priority = 6,
                        groups = {"Regression_Test"})
        @Parameters({"Product1", "Product2"})
        public void TestCase06(String Product1, String Product2) throws InterruptedException {
                Boolean status;
                // logStatus("Start TestCase", "Test Case 6: Verify that cart can be edited", "DONE");
                Home homePage = new Home(driver);
                Register registration = new Register(driver);
                Login login = new Login(driver);

                registration.navigateToRegisterPage();
                status = registration.registerUser("testUser", "abc@123", true);
                Assert.assertTrue(status, "Registration failed");

                // }
                lastGeneratedUserName = registration.lastGeneratedUsername;

                login.navigateToLoginPage();
                status = login.PerformLogin(lastGeneratedUserName, "abc@123");
                Assert.assertTrue(status, "Login failed");

                homePage.navigateToHome();
                status = homePage.searchForProduct(Product1);
                Assert.assertTrue(status, "Xtend product search failed");
                homePage.addProductToCart(Product1);

                status = homePage.searchForProduct(Product2);
                Assert.assertTrue(status, "Yarine product search failed");
                homePage.addProductToCart(Product2);

                // update watch quantity to 2
                homePage.changeProductQuantityinCart(Product1, 2);

                // update table lamp quantity to 0
                homePage.changeProductQuantityinCart(Product2, 0);

                // update watch quantity again to 1
                homePage.changeProductQuantityinCart(Product1, 1);

                homePage.clickCheckout();

                Checkout checkoutPage = new Checkout(driver);
                checkoutPage.addNewAddress("Addr line 1 addr Line 2 addr line 3");
                checkoutPage.selectAddress("Addr line 1 addr Line 2 addr line 3");

                checkoutPage.placeOrder();

                try {
                        WebDriverWait wait = new WebDriverWait(driver, 30);
                        wait.until(ExpectedConditions.urlToBe(
                                        "https://crio-qkart-frontend-qa.vercel.app/thanks"));
                } catch (TimeoutException e) {
                        System.out.println("Error while placing order in: " + e.getMessage());

                }

                status = driver.getCurrentUrl().endsWith("/thanks");
                Assert.assertTrue(status,"URL does not end with thanks");

                homePage.navigateToHome();
                homePage.PerformLogout();
        }

        @Test(description = "Verify that insufficient balance error is thrown when the wallet balance is not enough",
                        priority = 7, groups = {"Sanity_test"})
        @Parameters({"ProductName", "Qty"})
        public void TestCase07(String ProductName, int Qty) throws InterruptedException {
                Boolean status;
                Register registration = new Register(driver);
                registration.navigateToRegisterPage();
                status = registration.registerUser("testUser", "abc@123", true);
                Assert.assertTrue(status, "Registration failed");
                lastGeneratedUserName = registration.lastGeneratedUsername;

                Login login = new Login(driver);
                login.navigateToLoginPage();
                status = login.PerformLogin(lastGeneratedUserName, "abc@123");
                Assert.assertTrue(status, "Login Failed");

                Home homePage = new Home(driver);
                homePage.navigateToHome();
                status = homePage.searchForProduct(ProductName);
                Assert.assertTrue(status,"search failed");
                homePage.addProductToCart(ProductName);

                homePage.changeProductQuantityinCart(ProductName, Qty);

                homePage.clickCheckout();

                Checkout checkoutPage = new Checkout(driver);
                checkoutPage.addNewAddress("Addr line 1 addr Line 2 addr line 3");
                checkoutPage.selectAddress("Addr line 1 addr Line 2 addr line 3");

                checkoutPage.placeOrder();
                Thread.sleep(3000);

                status = checkoutPage.verifyInsufficientBalanceMessage();
                Assert.assertTrue(status, "Unable to verify error message");

        }

        @Test(description = "Verify that a product added to a cart is available when a new tab is added",
                        priority = 8, groups = {"Regression_Test"})
        public void TestCase08() throws InterruptedException {
                Boolean status = false;

                Register registration = new Register(driver);
                registration.navigateToRegisterPage();
                status = registration.registerUser("testUser", "abc@123", true);
                Assert.assertTrue(status, "Registration failled");
    
                lastGeneratedUserName = registration.lastGeneratedUsername;

                Login login = new Login(driver);
                login.navigateToLoginPage();
                status = login.PerformLogin(lastGeneratedUserName, "abc@123");
                Assert.assertTrue(status, "Login failed");

                Home homePage = new Home(driver);
                homePage.navigateToHome();

                status = homePage.searchForProduct("YONEX");
                Assert.assertTrue(status, "Product search failed");
                homePage.addProductToCart("YONEX Smash Badminton Racquet");

                String currentURL = driver.getCurrentUrl();

                driver.findElement(By.linkText("Privacy policy")).click();
                Set<String> handles = driver.getWindowHandles();
                driver.switchTo().window(handles.toArray(new String[handles.size()])[1]);

                driver.get(currentURL);
                Thread.sleep(2000);

                List<String> expectedResult = Arrays.asList("YONEX Smash Badminton Racquet");
                status = homePage.verifyCartContents(expectedResult);
                Assert.assertTrue(status, "Cartcontents failed");

                driver.close();

                driver.switchTo().window(handles.toArray(new String[handles.size()])[0]);

        }

        @Test(description = "Verify that privacy policy and about us links are working fine",
                        priority = 9, groups = {"Regression_Test"})
        public void TestCase09() throws InterruptedException {
                Boolean status = false;

                Register registration = new Register(driver);
                registration.navigateToRegisterPage();
                status = registration.registerUser("testUser", "abc@123", true);
                Assert.assertTrue(status, "Registration failed");

                lastGeneratedUserName = registration.lastGeneratedUsername;

                Login login = new Login(driver);
                login.navigateToLoginPage();
                status = login.PerformLogin(lastGeneratedUserName, "abc@123");
                Assert.assertTrue(status, "Login failed");

                Home homePage = new Home(driver);
                homePage.navigateToHome();

                String basePageURL = driver.getCurrentUrl();

                driver.findElement(By.linkText("Privacy policy")).click();
                status = driver.getCurrentUrl().equals(basePageURL);
                Assert.assertTrue(status, "BasePageURL failed");

                Set<String> handles = driver.getWindowHandles();
                driver.switchTo().window(handles.toArray(new String[handles.size()])[1]);
                WebElement PrivacyPolicyHeading =
                                driver.findElement(By.xpath("//*[@id=\"root\"]/div/div[2]/h2"));
                status = PrivacyPolicyHeading.getText().equals("Privacy Policy");
                Assert.assertTrue(status, "Privacy policy page failed");
   
                driver.switchTo().window(handles.toArray(new String[handles.size()])[0]);
                driver.findElement(By.linkText("Terms of Service")).click();

                handles = driver.getWindowHandles();
                driver.switchTo().window(handles.toArray(new String[handles.size()])[2]);
                WebElement TOSHeading =
                                driver.findElement(By.xpath("//*[@id=\"root\"]/div/div[2]/h2"));
                status = TOSHeading.getText().equals("Terms of Service");
                Assert.assertTrue(status, "Terms of Service failed");
     
                driver.close();
                driver.switchTo().window(handles.toArray(new String[handles.size()])[1]).close();
                driver.switchTo().window(handles.toArray(new String[handles.size()])[0]);


        }

        @Test(description = "Verify that the contact us dialog works fine", priority = 10)
        public void TestCase10() throws InterruptedException {
       
                Home homePage = new Home(driver);
                homePage.navigateToHome();

                driver.findElement(By.xpath("//*[text()='Contact us']")).click();

                WebElement name = driver.findElement(By.xpath("//input[@placeholder='Name']"));
                name.sendKeys("crio user");
                WebElement email = driver.findElement(By.xpath("//input[@placeholder='Email']"));
                email.sendKeys("criouser@gmail.com");
                WebElement message =
                                driver.findElement(By.xpath("//input[@placeholder='Message']"));
                message.sendKeys("Testing the contact us page");

                WebElement contactUs = driver.findElement(By.xpath(
                                "/html/body/div[2]/div[3]/div/section/div/div/div/form/div/div/div[4]/div/button"));

                contactUs.click();


                WebDriverWait wait = new WebDriverWait(driver, 30);
                wait.until(ExpectedConditions.invisibilityOf(contactUs));

        }

        @Test(description = "Ensure that the Advertisement Links on the QKART page are clickable",
                        priority = 11, groups = {"Sanity_test", "Regression_Test"})
        public void TestCase11() throws InterruptedException {
                Boolean status = false;
     
                Register registration = new Register(driver);
                registration.navigateToRegisterPage();
                status = registration.registerUser("testUser", "abc@123", true);
                Assert.assertTrue(status, "Registration failed");
 
                lastGeneratedUserName = registration.lastGeneratedUsername;

                Login login = new Login(driver);
                login.navigateToLoginPage();
                status = login.PerformLogin(lastGeneratedUserName, "abc@123");
                Assert.assertTrue(status, "Login failed");
 
                Home homePage = new Home(driver);
                homePage.navigateToHome();

                status = homePage.searchForProduct("YONEX Smash Badminton Racquet");
                Assert.assertTrue(status, "Yonex badminton racquet product search failed");
                homePage.addProductToCart("YONEX Smash Badminton Racquet");
                homePage.changeProductQuantityinCart("YONEX Smash Badminton Racquet", 1);
                homePage.clickCheckout();

                Checkout checkoutPage = new Checkout(driver);
                checkoutPage.addNewAddress("Addr line 1  addr Line 2  addr line 3");
                checkoutPage.selectAddress("Addr line 1  addr Line 2  addr line 3");
                checkoutPage.placeOrder();
                Thread.sleep(3000);

                String currentURL = driver.getCurrentUrl();

                List<WebElement> Advertisements = driver.findElements(By.xpath("//iframe"));

                status = Advertisements.size() == 3;
                Assert.assertTrue(status, "Advertisement size failed");
       
                WebElement Advertisement1 = driver.findElement(
                                By.xpath("//*[@id=\"root\"]/div/div[2]/div/iframe[1]"));
                driver.switchTo().frame(Advertisement1);
                driver.findElement(By.xpath("//button[text()='Buy Now']")).click();
                driver.switchTo().parentFrame();

                status = !driver.getCurrentUrl().equals(currentURL);
                Assert.assertTrue(status,"Advertisement 1 is not clickable");
     

                driver.get(currentURL);
                Thread.sleep(3000);

                WebElement Advertisement2 = driver.findElement(
                                By.xpath("//*[@id=\"root\"]/div/div[2]/div/iframe[2]"));
                driver.switchTo().frame(Advertisement2);
                driver.findElement(By.xpath("//button[text()='Buy Now']")).click();
                driver.switchTo().parentFrame();

                status = !driver.getCurrentUrl().equals(currentURL);
                Assert.assertTrue(status,"advertisement 2 is not clickable");
        }

        @AfterSuite
        public static void quitDriver() {
                System.out.println("quit()");
                driver.quit();
        }

        public static void logStatus(String type, String message, String status) {

                System.out.println(String.format("%s |  %s  |  %s | %s",
                                String.valueOf(java.time.LocalDateTime.now()), type, message,
                                status));
        }

        public static void takeScreenshot(WebDriver driver, String screenshotType,String description) {
                try {
                        File theDir = new File("/screenshots");
                        if (!theDir.exists()) {
                                theDir.mkdirs();
                        }
                        String timestamp = String.valueOf(java.time.LocalDateTime.now());
                        String fileName = String.format("screenshot_%s_%s_%s.png", timestamp,
                                        screenshotType, description);
                        TakesScreenshot scrShot = ((TakesScreenshot) driver);
                        File SrcFile = scrShot.getScreenshotAs(OutputType.FILE);
                        File DestFile = new File("screenshots/" + fileName);
                        FileUtils.copyFile(SrcFile, DestFile);
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
}

