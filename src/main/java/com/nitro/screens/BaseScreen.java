package com.nitro.screens;

import mmarquee.automation.UIAutomation;
import mmarquee.automation.controls.Application;
import mmarquee.automation.controls.Window;

public abstract class BaseScreen {

    protected Window window;
    protected UIAutomation automation;

    public BaseScreen(Application app) throws Exception {
        this.automation = UIAutomation.getInstance();
        this.window = app.getWindow("Calculator");
    }

    public Window getWindow() {
        return window;
    }

    public UIAutomation getAutomation() {
        return automation;
    }
}
