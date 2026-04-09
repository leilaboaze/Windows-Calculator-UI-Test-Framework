package com.nitro;

import com.nitro.screens.ScientificScreen;
import com.nitro.screens.StandardScreen;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
public class ScientificCalculatorTest extends BaseTest {

    private ScientificScreen screen;
    @BeforeEach
    public void initScreen() throws Exception {
        screen = new ScientificScreen(app);
        screen.clear();
    }
    @Test
    @DisplayName("TC13: It should correctly switch between operations in Scientific, Standard and back to Scientific modes")
    public void testModeSwitching() throws Exception {

        // Operation in Scientific mode
        screen.clickButton("Five")
                .clickButton("Square");

        // Switch to Standard
        StandardScreen standard = screen.switchToStandard(app);
        assertNotNull(standard, "Should be able to open Standard mode");
        //assertEquals("", screen.getResult(), "Should clear display output after switching modes");
        //standard.clear();

        // Perform operation in Standard mode
        standard.clickButton("Five").clickButton("Plus").clickButton("Three").clickButton("Equals");
        assertEquals("8", screen.getResult(), "5 + 3 should equal 8 in Standard mode");

        // Switch back to Scientific
        ScientificScreen backToScientific = standard.switchToScientific(app);
        assertNotNull(backToScientific, "Should be able to return to Scientific mode");
        //assertEquals("", screen.getResult(), "Should clear display output after switching modes");

        // Verify that we're back in Scientific mode with a clean state
        String result = backToScientific.getResult();
        assertNotNull(result, "Display should not be null after mode switch");
    }

    @Test
    @DisplayName("TC15: It should return 2 when for the expression √4")
    public void testSquareRoot() throws Exception {

        screen.clickButton("Four")
                .clickButton("Square root");

        assertEquals("2", screen.getResult(), "√4 should be 2");
    }


    @Test
    @DisplayName("TC16: It should display 'Invalid input' error for square root of negative number inputs")
    public void testSquareRootOfNegative() throws Exception {
        screen.clickButton("Left parenthesis")
                .clickButton("Four")
                .clickButton("Minus")
                .clickButton("Five")
                .clickButton("Right parenthesis")
                .clickButton("Square root");

        String result = screen.getResult();
        assertTrue(result.contains("Invalid input") || result.contains("Not a number"),
                "Square root of negative number should show error, but showed: " + result);
    }
}