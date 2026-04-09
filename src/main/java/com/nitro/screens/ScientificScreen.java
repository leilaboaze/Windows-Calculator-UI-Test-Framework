package com.nitro.screens;

import mmarquee.automation.controls.Application;
import mmarquee.automation.controls.Button;

public class ScientificScreen extends BaseScreen {

    public ScientificScreen(Application app) throws Exception {
        super(app);
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
        Button menuButton = window.getButton("Open Navigation");
        menuButton.click();
        Thread.sleep(500);
        Button standardButton = window.getButton("Standard Calculator");
        standardButton.click();
        Thread.sleep(1000);
        return new StandardScreen(app);
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