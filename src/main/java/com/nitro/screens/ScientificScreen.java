package com.nitro.screens;

import com.nitro.utils.ScreenUtils;
import mmarquee.automation.controls.Application;

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

    public StandardScreen switchToStandard(Application app) throws Exception {
        return ScreenUtils.switchToStandard(app);
    }
}