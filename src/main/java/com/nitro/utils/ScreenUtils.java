package com.nitro.utils;

import com.nitro.screens.ScientificScreen;
import com.nitro.screens.StandardScreen;
import mmarquee.automation.AutomationException;
import mmarquee.automation.ControlType;
import mmarquee.automation.controls.*;

/**
 * Utility class for switching between different calculator screens.
 * This class provides static methods to switch to Standard or Scientific mode
 * regardless of the current calculator mode (Programmer, Graphing, etc.).
 */
public class ScreenUtils {
    public static ScientificScreen switchToScientific(Application app) throws Exception {
        Window window = app.getWindow("Calculator");
        // Only open navigation if we're in Standard mode
        try {

            window.getButton("Open Navigation").click();
            Thread.sleep(500);
            clickListItem(window, "Scientific Calculator");
            //Button scientificButton = window.getButton("Scientific Calculator");
            //scientificButton.click();
            Thread.sleep(1000);
            window.getButton("Close Navigation").click();


        } catch (Exception e) {
            // Already in Scientific mode or navigation not needed - continue
        }
        return new ScientificScreen(app);
    }

    public static StandardScreen switchToStandard(Application app) throws Exception {
        Window window = app.getWindow("Calculator");
        // Only close navigation if we're in Standard mode
        try {

            window.getButton("Open Navigation").click();
            Thread.sleep(500);
            clickListItem(window, "Standard Calculator");
            //Button standardButton = window.getButton("Standard Calculator");
            //standardButton.click();
            Thread.sleep(1000);
            window.getButton("Close Navigation").click();

        } catch (Exception e) {
            // Navigation already closed or textbox not found - continue
        }
        return new StandardScreen(app);
    }

    private static void clickListItem(Window window, String itemName) throws AutomationException {
        window.getControlByControlType("itemName", ControlType.ListItem);
        for (var child : window.getChildren(true)) {
            try {
                String name = child.getName();
                if (name != null && name.contains(itemName)) {
                    Button bt = new Button(
                            new ElementBuilder(child.getAutomation()));
                    bt.click();
                    break;  // Now valid - break in traditional for loop
                }
            } catch (Exception e) {
                // Ignore exceptions while searching for the button
            }
        }
    }

}

