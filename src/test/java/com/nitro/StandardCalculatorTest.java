package com.nitro;

import com.nitro.screens.StandardScreen;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StandardCalculatorTest extends BaseTest {

    @Test
    public void testAddition() throws Exception {
        StandardScreen screen = new StandardScreen(app);
        screen.clear()
                .clickButton("Two")
                .clickButton("Plus")
                .clickButton("Three")
                .clickButton("Equals");

        assertEquals("5", screen.getResult());
    }

    @Test
    public void testDivisionByZero() throws Exception {
        StandardScreen screen = new StandardScreen(app);
        screen.clear()
                .clickButton("Five")
                .clickButton("Divide by")
                .clickButton("Zero")
                .clickButton("Equals");

        assertTrue(screen.getResult().contains("Cannot divide by zero"));
    }
}