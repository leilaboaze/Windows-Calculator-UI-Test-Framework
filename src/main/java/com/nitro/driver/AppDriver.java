package com.nitro.driver;

import mmarquee.automation.AutomationException;
import mmarquee.automation.UIAutomation;
import mmarquee.automation.controls.Application;

public class AppDriver {

    private static Application application;
    private static UIAutomation automation;

    public static Application launch() throws Exception {
        automation = UIAutomation.getInstance();
        application = automation.launchOrAttach("calc.exe");
        Thread.sleep(2000);
        return application;
    }

    public static void close() {
        if (application != null) {
            application.close("Calculator");
        }
    }
}