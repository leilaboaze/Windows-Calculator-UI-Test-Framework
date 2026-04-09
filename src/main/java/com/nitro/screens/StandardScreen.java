package com.nitro.screens;

import mmarquee.automation.controls.Application;
import mmarquee.automation.controls.Button;

public class StandardScreen extends BaseScreen {

    public StandardScreen(Application app) throws Exception {
        super(app);
    }

    public StandardScreen clickButton(String name) throws Exception {
        Button button = window.getButton(name);
        button.click();
        return this;
    }

    public StandardScreen clear() throws Exception {
        return clickButton("Clear");
    }

    public String getResult() throws Exception {
        var children = window.getChildren(true);
        for (var child : children) {
            String name = child.getName();
            if (name != null && name.startsWith("Display is ")) {
                return name.replace("Display is ", "").trim();
            }
        }
        throw new Exception("Calculator display not found");
    }
}