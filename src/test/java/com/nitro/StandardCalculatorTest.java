package com.nitro;

import com.nitro.screens.StandardScreen;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
public class StandardCalculatorTest extends BaseTest {

    private StandardScreen screen;

    @BeforeEach
    public void initScreen() throws Exception {
        screen = new StandardScreen(app);
        screen.clear();
    }

    @Test
    @DisplayName("TC01: It should display 5 when for the expression 2 + 3")
    public void testAddition() throws Exception {
        screen.clickButton("Two")
                .clickButton("Plus")
                .clickButton("Three")
                .clickButton("Equals");

        assertEquals("5", screen.getResult(), "2 + 3 should result in 5");
    }

    @Test
    @DisplayName("TC02: It should display 6 when for the expression 10 - 4")
    public void testSubtraction() throws Exception {
        screen.clickButton("One")
                .clickButton("Zero")
                .clickButton("Minus")
                .clickButton("Four")
                .clickButton("Equals");

        assertEquals("6", screen.getResult(), "10 - 4 should result in 6");
    }

    @Test
    @DisplayName("TC03: It should display 42 when for the expression 6 × 7")
    public void testMultiplication() throws Exception {
        screen.clickButton("Six")
                .clickButton("Multiply by")
                .clickButton("Seven")
                .clickButton("Equals");

        assertEquals("42", screen.getResult(), "6 × 7 should result in 42");
    }

    @Test
    @DisplayName("TC04: It should display 5 when for the expression 15 ÷ 3")
    public void testDivision() throws Exception {
        screen.clickButton("One")
                .clickButton("Five")
                .clickButton("Divide by")
                .clickButton("Three")
                .clickButton("Equals");

        assertEquals("5", screen.getResult(), "15 ÷ 3 should result in 5");
    }


    @Test
    @DisplayName("TC05: It should display 0 when for the expression 3 - 3")
    public void testResultIsZero() throws Exception {
        screen.clickButton("Three")
                .clickButton("Minus")
                .clickButton("Three")
                .clickButton("Equals");

        assertEquals("0", screen.getResult(), "3 - 3 should result in 0");
    }

    @Test
    @DisplayName("TC06: It should display -7 when for the expression 3 - 10")
    public void testNegativeResult() throws Exception {
        screen.clickButton("Three")
                .clickButton("Minus")
                .clickButton("One")
                .clickButton("Zero")
                .clickButton("Equals");

        assertEquals("-7", screen.getResult(), "3 - 10 should result in -7");
    }

    @Test
    @DisplayName("TC07: It should display 9 when for the expression 2 + 3 + 4")
    public void testChainedOperations() throws Exception {
        screen.clickButton("Two")
                .clickButton("Plus")
                .clickButton("Three")
                .clickButton("Plus")
                .clickButton("Four")
                .clickButton("Equals");

        assertEquals("9", screen.getResult(), "2 + 3 + 4 should result in 9");
    }

    @Test
    @DisplayName("TC08: It should display 1,000,000 when for the expression 999999 + 1")
    public void testLargeNumber() throws Exception {
        screen.clickButton("Nine").clickButton("Nine").clickButton("Nine")
                .clickButton("Nine").clickButton("Nine").clickButton("Nine")
                .clickButton("Plus")
                .clickButton("One")
                .clickButton("Equals");

        assertEquals("1,000,000", screen.getResult(), "999999 + 1 should result in 1,000,000");
    }


    @Test
    @DisplayName("TC09: It should show 'Cannot divide by zero' error when input the expression 5 ÷ 0")
    public void testDivisionByZero() throws Exception {
        screen.clickButton("Five")
                .clickButton("Divide by")
                .clickButton("Zero")
                .clickButton("Equals");

        String result = screen.getResult();
        assertTrue(result.contains("Cannot divide by zero"),
                "Division by zero should show error message, but showed: " + result);
    }



    @Test
    @DisplayName("TC10: It should display 0 when clear is pressed after entering 99")
    public void testClearResetsDisplay() throws Exception {
        screen.clickButton("Nine")
                .clickButton("Nine")
                .clickButton("Clear");

        assertEquals("0", screen.getResult(), "After Clear, display should show 0");
    }

    @Test
    @DisplayName("TC12: It should display 8 when for keyboard input '5+3='")
    public void testKeyboardInput() throws Exception {
        screen.typeKeys("5+3");
        screen.pressEnter();

        assertEquals("8", screen.getResult(), "Keyboard input '5+3=' should result in 8");
    }

    @Test
    @DisplayName("TC13: It should display 0 when Escape key is pressed after entering 99")
    public void testEscapeKeyClearsDisplay() throws Exception {
        screen.typeKeys("99");
        screen.pressEscape();

        assertEquals("0", screen.getResult(), "Escape key should clear the display");
    }
}