package com.nitro.ai;

import com.nitro.utils.ScreenshotUtil;
import mmarquee.automation.controls.Application;
import mmarquee.automation.controls.Window;

public class FailureAnalyzer {

    private final ClaudeClient claude;

    private static final String SYSTEM_PROMPT = """
        You are a QA expert analysing failures in automated UI tests
        for the Windows Calculator application.
        
        You will receive:
        1. A screenshot of the Calculator at the moment of failure
        2. The test name and what it was trying to verify
        3. The exception or error message
        
        Provide a concise, human-readable analysis (max 150 words) covering:
        - What the screenshot shows
        - The likely root cause of the failure
        - Whether this looks like a product bug or a test/automation issue
        - A suggested next step to investigate
        
        Be direct and specific.
        """;

    public FailureAnalyzer() {
        this.claude = new ClaudeClient();
    }

    /**
     * Analyzes a test failure with AI support.
     *
     * @param testName    name of the test that failed
     * @param description what the test was trying to verify
     * @param error       the exception or error message
     * @param window      Calculator window to capture screenshot
     * @return analysis in human-readable text
     */
    public String analyze(String testName, String description,
                          Throwable error, Window window, Application application) {
        try {
            byte[] screenshot = ScreenshotUtil.captureWindow(window);

            String userMessage = String.format("""
                Test name: %s
                Test description: %s
                Error: %s: %s
                
                Please analyse the screenshot and explain what went wrong.
                """,
                    testName,
                    description,
                    error.getClass().getSimpleName(),
                    error.getMessage()
            );

            String analysis = claude.sendMessageWithImage(
                    SYSTEM_PROMPT, userMessage, screenshot
            );

            System.out.println("\n=== AI Failure Analysis ===");
            System.out.println(analysis);
            System.out.println("===========================\n");

            return analysis;

        } catch (Exception e) {
            return "[AI analysis unavailable: " + e.getMessage() + "]";
        }
    }
}