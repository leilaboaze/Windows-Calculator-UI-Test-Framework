package com.nitro;

import com.nitro.ai.FailureAnalyzer;
import com.nitro.driver.AppDriver;
import com.nitro.screens.BaseScreen;
import mmarquee.automation.controls.Application;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class BaseTest {

    protected Application app;
    protected FailureAnalyzer failureAnalyzer;

    @BeforeEach
    public void setUp() throws Exception {
        app = AppDriver.launch();
        failureAnalyzer = new FailureAnalyzer();
    }

    @AfterEach
    public void tearDown() {
        AppDriver.close();
    }

    /**
     * Call this method in tests to use AI analysis on a failure.
     * Example of usage in the test:
     *
     *   try {
     *       assertEquals("5", screen.getResult());
     *   } catch (AssertionError e) {
     *       analyzeFailure("TC01:  2 + 3 = 5", "2+3 should equal 5", e, screen);
     *       throw e;
     *   }
     */
    protected void analyzeFailure(String testName, String description,
                                  Throwable error, BaseScreen screen) {
        try {
            failureAnalyzer.analyze(testName, description, error, screen.getWindow(), app);
        } catch (Exception ex) {
            System.err.println("[AI] Could not analyse failure: " + ex.getMessage());
        }
    }
}