package com.nitro;

import com.nitro.ai.NaturalLanguageTestGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class NaturalLanguageTest extends BaseTest {

    private NaturalLanguageTestGenerator runner;

    @BeforeEach
    public void initRunner() {
        runner = new NaturalLanguageTestGenerator(app);
    }

    @Test
    @DisplayName("TC17: It should generate an addition calculator test when given plain natural language text in Standard mode")
    public void testAdditionInNaturalLanguage() throws Exception {
        runner.runTest("verify that 5 plus 3 equals 8 in Standard mode");
    }

    @Test
    @DisplayName("TC18: It should generate a division calculator test when given plain natural language text in Standard mode")
    public void testDivisionInNaturalLanguage() throws Exception {
        runner.runTest("verify that 15 divided by 3 equals 5 in Standard mode");
    }

    @Test
    @DisplayName("TC19: It should generate a square root calculator test when given plain natural language text in Scientific mode")
    public void testSquareRootInNaturalLanguage() throws Exception {
        runner.runTest("verify that the square root of 9 equals 3 in Scientific mode");
    }
}