package com.nitro.screens;

import mmarquee.automation.UIAutomation;
import mmarquee.automation.controls.Application;
import mmarquee.automation.controls.Button;
import mmarquee.automation.controls.Window;
import java.awt.Robot;
import java.awt.event.KeyEvent;

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

    public BaseScreen clickButton(String name) throws Exception {
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

    protected int charToKeyCode(char c) {
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
