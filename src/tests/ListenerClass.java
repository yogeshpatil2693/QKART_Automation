package QKART_TESTNG;

import org.testng.ITestListener;
import org.testng.ITestContext;
import org.testng.ITestResult;

public class ListenerClass implements ITestListener{
    
    @Override
    public void onStart(ITestContext context) {
        System.out.println("Test Execution started");
    }

    @Override
    public void onTestStart(ITestResult result) {
        QKART_Tests.takeScreenshot(QKART_Tests.driver, "Test start", result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        QKART_Tests.takeScreenshot(QKART_Tests.driver, "Test success", result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        QKART_Tests.takeScreenshot(QKART_Tests.driver, "Test case failed", result.getName());
    }


}