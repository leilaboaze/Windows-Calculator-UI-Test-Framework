package com.nitro.screens;

import com.nitro.utils.ScreenUtils;
import mmarquee.automation.controls.Application;
import mmarquee.automation.controls.Button;

public class ScientificScreen extends BaseScreen {

    public ScientificScreen(Application app) throws Exception {
        super(app);
        // Ensure we're in Scientific mode, regardless of current mode (Programmer, Graphing, Standard, etc.)
        try {
            if (window.getTextBox("Standard Calculator mode") != null)
                ScreenUtils.switchToScientific(app);
        } catch (Exception e) {
            // Already in Scientific mode or switch was unnecessary - continue
        }
    }

    public ScientificScreen clickButton(String name) throws Exception {
        Button button = window.getButton(name);
        button.click();
        return this;
    }

    public ScientificScreen clear() throws Exception {
        return clickButton("Clear");
    }

    public StandardScreen switchToStandard(Application app) throws Exception {
        return ScreenUtils.switchToStandard(app);
    }

    public String getResult() throws Exception {
        var children = window.getChildren(true);
        for (var child : children) {
            String name = child.getName();
            if (name != null && name.startsWith("Display is ")) {
                return name.replace("Display is ", "").trim();
            }
        }
        throw new Exception("Scientific display not found");
    }
}