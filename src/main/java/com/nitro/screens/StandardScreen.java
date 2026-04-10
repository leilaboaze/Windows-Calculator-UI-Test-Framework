package com.nitro.screens;

import com.nitro.utils.ScreenUtils;
import mmarquee.automation.controls.Application;

public class StandardScreen extends BaseScreen {

    public StandardScreen(Application app) throws Exception {
        super(app);
        // Ensure we're in Standard mode, regardless of current mode (Programmer, Graphing, Scientific, etc.)
        try {
            if (window.getTextBox("Scientific Calculator mode") != null)
                ScreenUtils.switchToStandard(app);
        } catch (Exception e) {
            // Already in Standard mode or switch was unnecessary - continue
        }
    }

    public ScientificScreen switchToScientific(Application app) throws Exception {
        return ScreenUtils.switchToScientific(app);
    }
}