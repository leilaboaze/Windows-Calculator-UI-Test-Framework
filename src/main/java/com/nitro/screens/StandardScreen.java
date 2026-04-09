package com.nitro.screens;

import com.nitro.utils.ScreenUtils;
import mmarquee.automation.controls.Application;
import mmarquee.automation.controls.Button;
import java.awt.Robot;
import java.awt.event.KeyEvent;

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

    public StandardScreen clickButton(String name) throws Exception {
        Button button = window.getButton(name);
        button.click();
        return this;
    }

    public void clear() throws Exception {
        clickButton("Clear");
    }

    public void typeKeys(String keys) throws Exception {
        window.focus();
        Robot robot = new Robot();
        Thread.sleep(200);

        for (char c : keys.toCharArray()) {
            int keyCode = charToKeyCode(c);
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
            Thread.sleep(50);
        }

        Thread.sleep(50);
    }
    public void pressEnter() throws Exception {
        window.focus();
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        Thread.sleep(100);
    }

    public void pressEscape() throws Exception {
        window.focus();
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_ESCAPE);
        robot.keyRelease(KeyEvent.VK_ESCAPE);
        Thread.sleep(100);
    }

    public ScientificScreen switchToScientific(Application app) throws Exception {
        return ScreenUtils.switchToScientific(app);
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

    private int charToKeyCode(char c) {
        return switch (c) {
            case '0' -> KeyEvent.VK_0;
            case '1' -> KeyEvent.VK_1;
            case '2' -> KeyEvent.VK_2;
            case '3' -> KeyEvent.VK_3;
            case '4' -> KeyEvent.VK_4;
            case '5' -> KeyEvent.VK_5;
            case '6' -> KeyEvent.VK_6;
            case '7' -> KeyEvent.VK_7;
            case '8' -> KeyEvent.VK_8;
            case '9' -> KeyEvent.VK_9;
            case '+' -> KeyEvent.VK_ADD;
            case '-' -> KeyEvent.VK_SUBTRACT;
            case '*' -> KeyEvent.VK_MULTIPLY;
            case '/' -> KeyEvent.VK_DIVIDE;
            case '=' -> KeyEvent.VK_EQUALS;
            default  -> throw new IllegalArgumentException("Key not supported " + c);
        };
    }
}