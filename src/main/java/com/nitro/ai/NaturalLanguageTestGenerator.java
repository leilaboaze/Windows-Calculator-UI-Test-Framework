package com.nitro.ai;

import com.google.gson.*;
import com.nitro.screens.StandardScreen;
import com.nitro.screens.ScientificScreen;
import mmarquee.automation.controls.Application;

public class NaturalLanguageTestGenerator {

    private final ClaudeClient claude;
    private final Application app;

    private static final String SYSTEM_PROMPT = """
                 You are a test automation assistant for the Windows Calculator app.
                \s
                 Given a plain-English test description, respond ONLY with a raw JSON object.
                 Do NOT use markdown formatting, code fences, or backticks.
                 Do NOT include any explanation or text outside the JSON.
                 Your entire response must be valid JSON and nothing else.
                \s
                 Schema:
                 {
                   "mode": "Standard" or "Scientific",
                   "steps": ["ButtonName1", "ButtonName2", ...],
                   "expectedResult": "the expected display value"
                 }
                \s
                 Valid button names for Standard mode:
                 Numbers: "Zero","One","Two","Three","Four","Five","Six","Seven","Eight","Nine"
                 Operators: "Plus","Minus","Multiply by","Divide by","Equals"
                 Other: "Clear","Positive negative","Decimal separator"
                \s
                 Valid button names for Scientific mode (in addition to above):
                 "Square root","Square","Sine","Cosine","Tangent","Log","Natural log",
                 "Left parenthesis","Right parenthesis"
                \s
                 Example input: "verify that 5 + 3 equals 8 in Standard mode"
                 Example output:
                 {"mode":"Standard","steps":["Five","Plus","Three","Equals"],"expectedResult":"8"}
            \s""";

    public NaturalLanguageTestGenerator(Application app) {
        this.claude = new ClaudeClient();
        this.app = app;
    }

    /**
     * Executes a test from a natural language description.
     *
     * @param description ex: "verify that 10 divided by 2 equals 5 in Standard mode"
     * @return actual calculator result
     * @throws AssertionError if the result does not match the expected
     */
    public String runTest(String description) throws Exception {
        System.out.println("[AI] Parsing test description: " + description);

        //Ask Claude to interpret the description
        String jsonResponse = claude.sendTextMessage(SYSTEM_PROMPT, description);
        System.out.println("[AI] Claude response: " + jsonResponse);

        String cleanJson = jsonResponse
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();

        System.out.println("[AI] Cleaned JSON: " + cleanJson);
        JsonObject plan = JsonParser.parseString(cleanJson).getAsJsonObject();

        String mode = plan.get("mode").getAsString();
        String expectedResult = plan.get("expectedResult").getAsString();
        JsonArray steps = plan.getAsJsonArray("steps");

        // Execute the steps in the calculator
        String actualResult;
        if ("Scientific".equalsIgnoreCase(mode)) {
            ScientificScreen screen = new ScientificScreen(app);
            screen.clear();
            for (JsonElement step : steps) {
                screen.clickButton(step.getAsString());
            }
            actualResult = screen.getResult();
        } else {
            StandardScreen screen = new StandardScreen(app);
            screen.clear();
            for (JsonElement step : steps) {
                screen.clickButton(step.getAsString());
            }
            actualResult = screen.getResult();
        }

        // Verify the result
        System.out.println("[AI] Expected: " + expectedResult
                + " | Actual: " + actualResult);

        if (!expectedResult.equals(actualResult)) {
            throw new AssertionError(
                    "AI-generated test FAILED.\n"
                            + "Description: " + description + "\n"
                            + "Expected: " + expectedResult + "\n"
                            + "Actual:   " + actualResult
            );
        }

        return actualResult;
    }
}