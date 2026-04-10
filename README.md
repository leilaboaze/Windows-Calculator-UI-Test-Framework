# Windows Calculator UI Test Framework

- Author: [Leila Boaze](https://github.com/leilaboaze)
---

## Table of Contents

- [Development Environment](#development-environment)
- [Architecture](#architecture)
- [Design Decisions](#design-decisions)
- [Problems Encountered](#problems-encountered)
- [Test Coverage](#test-coverage)
- [CI/CD Design](#cicd-design)
- [AI Integration](#ai-integration)
- [AI Usage Transparency](#ai-usage-transparency)
- [Improvements](#improvements)

---

## Development Environment

The first challenge of this project had nothing to do with test automation: my development machine is an Apple Silicon 
Mac, and the Windows UI Automation API only runs on Windows machines.

### Running Windows on Apple Silicon

The solution was to run Windows 11 inside a virtual machine using **UTM**, a free virtualisation tool for macOS that 
works natively on Apple Silicon. 

Microsoft provides official Windows 11 ARM evaluation images for developers at no cost, valid for 90 days and more than 
sufficient for this assessment. The VM was configured with 4 GB of RAM and a virtual display, which was necessary because the Windows UI Automation API requires an active graphical session.

Within the VM, the development environment was set up as follows:

- **Java 21** (Microsoft OpenJDK) installed via `winget`
- **Apache Maven 3.9**, installed manually, added to `PATH`
- **IntelliJ IDEA Community**;
- **Accessibility Insights for Windows** used to inspect the Calculator's UI Automation tree and discover the real Automation IDs and Control Types of every element
- Environment variable `ANTHROPIC_API_KEY` required only for AI features (natural language tests and failure analysis)

### Build and Run

```bash
# Clone the repository
git clone https://github.com/leilaboaze/Nitro-Assessment.git
cd nitro-assessment

# Build the project
mvn clean compile

# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=StandardCalculatorTest

# Run only AI-powered tests
mvn test -Dtest=NaturalLanguageTest
```

Test reports are generated at:
```
target/surefire-reports/
```

---

## Architecture

```
nitro-assessment/
├── src/
│   ├── main/java/com/nitro/
│   │   ├── driver/
│   │   │   └── AppDriver.java          # Launches and manages the Calculator process
│   │   ├── screens/
│   │   │   ├── BaseScreen.java         # Abstract base for all Screen Objects
│   │   │   ├── StandardScreen.java     # Screen Object for Standard mode
│   │   │   └── ScientificScreen.java   # Screen Object for Scientific mode
│   │   ├── utils/
│   │   │   ├── ScreenUtils.java        # Calculator mode switching logic
│   │   │   └── ScreenshotUtil.java     # Screenshot capture
│   │   └── ai/
│   │       ├── ClaudeClient.java       # Anthropic API HTTP client
│   │       ├── NaturalLanguageTestGenerator.java  # AI test generation
│   │       └── FailureAnalyzer.java    # AI failure analysis
│   └── test/java/com/nitro/
│       ├── BaseTest.java               # JUnit lifecycle: setUp / tearDown
│       ├── StandardCalculatorTest.java # 12 tests for Standard mode
│       ├── ScientificCalculatorTest.java # 2 tests for Scientific mode
│       └── NaturalLanguageTest.java    # 3 AI-generated test execution
├── .github/workflows/
│   └── ci.yml                          # GitHub Actions pipeline
├── pom.xml
└── README.md
```

### Component Overview

**`AppDriver`** is the single entry point for process management. It uses `UIAutomation.launchOrAttach("calc.exe")` so tests can recover from a previously open Calculator window without failing.

**`BaseScreen`** is the abstract base class holding the `Window` reference. It implements all common UI interactions: `clickButton()`, `clear()`, `typeKeys()`, `pressEnter()`, `pressEscape()`, and `getResult()`. This exposes a fluent API (`clickButton(...).clickButton(...)`) that reads like a test script and hides all UI Automation complexity. All Screen Objects extend it, ensuring every interaction goes through a consistent, validated window handle.

**`StandardScreen` / `ScientificScreen`** extend `BaseScreen` and add mode-specific behaviour: each constructor ensures the Calculator is in the correct mode (switching if necessary), and each exposes a method to switch to the other mode (`switchToScientific()` and `switchToStandard()` respectively). They inherit all UI interaction methods from `BaseScreen`.

**`ScreenUtils`** centralises mode switching logic. Rather than duplicating navigation code across screen classes, both `StandardScreen` and `ScientificScreen` delegate to `ScreenUtils.switchToStandard()` / `switchToScientific()`. This is the single place to add other calculator's mode (e.g.: Programmer, Graphing, etc.) switching logic.

**`ClaudeClient`** is a thin HTTP wrapper around the Anthropic Messages API, shared by both AI features to avoid duplication.

---

## Design Decisions

### Why JUnit 5 over TestNG?

JUnit 5 was chosen for two primary reasons, though familiarity with the framework was also a factor:

- **Modern annotations and readability.** Its `@DisplayName` annotation produces human-readable test names in CI reports without extra configuration, making test results accessible to non-technical stakeholders.
- **Maven integration.** JUnit 5 supports in Maven Surefire 3.x with no additional dependencies. By contrast, TestNG requires a separate runner plugin and often an XML suite configuration file.

The primary tradeoff is that TestNG's `@DataProvider` mechanism is more powerful for complex data-driven test scenarios. However, for this scope, JUnit 5's `@ParameterizedTest` would be more than sufficient and aligns better with a readable, maintainable test structure.

### Why `ui-automation` (mmarquee) over WinAppDriver?

The assessment explicitly suggested this library, which made it a natural starting point. More importantly, `ui-automation` wraps the native MS UIAutomation COM API directly via JNA, with no additional server process to manage. 
Other option would be WinAppDriver which requires a running WebDriver server on port 4723, which adds operational complexity in CI.

The tradeoff is that `ui-automation` is less actively maintained and its ARM64 JNA support required an explicit version override to `5.14.0`. 
In a production context, the WinAppDriver + Appium approach would be preferred for its ecosystem maturity and cross-platform potential.

### Implementation of the Screen Object pattern

Directly calling `window.getButton("Five")` in test classes creates a maintenance problem: when the Calculator UI changes, 
every test that touches that element needs updating. With Screen Objects, that change happens only in one place. 
It also makes tests easier to read (just like the specification)  `screen.clickButton("Five").clickButton("Plus").clickButton("Three").clickButton("Equals")`, which is valuable for non-technical stakeholders reviewing test coverage.

### Test independence design

Every test is completely independent and starts with a fresh, predictable state:

1. `@BeforeEach` calls `AppDriver.launch()` which uses `launchOrAttach`, and `@AfterEach` calls `AppDriver.close()` to terminate the Calculator. This ensures that one test's state cannot affect another. 
2. Each Screen Object constructor validates and enforces its mode. When `StandardScreen` is instantiated, it checks if the Calculator is already in Scientific mode and automatically switches to Standard. Similarly, `ScientificScreen` switches to Scientific if needed. This pattern ensures that regardless of what the previous test left the Calculator in, the current test starts in exactly the right mode.
3. Because each test opens, configures, and closes the Calculator independently, tests can be run in any order or in parallel without risk of interference.

### Display scaling in screenshot capture

The Windows UI Automation API returns **physical pixel coordinates**, while `java.awt.Robot.createScreenCapture` operates in **logical pixel coordinates**. These two coordinate systems diverge on displays with DPI scaling.

In this case, it was a 200% DPI-scaled display, the UI Automation API returns physical pixel coordinates that are exactly double the logical pixel coordinates that `Robot.createScreenCapture()` expects. 
The method `ScreenshotUtils.captureWindow()` detects the display scale factor and divides the physical coordinates by 2.0 before passing them to `Robot.createScreenCapture()`. This converts physical coordinates back to logical coordinates:

This ensures the screenshot is captured correctly regardless of the display's DPI scaling factor. **TODO:** The scale factor is currently hardcoded to `2.0` (tested in 200% scaling); in production, it should be dynamically retrieved via `GraphicsConfiguration.getDefaultTransform()` to support arbitrary scaling levels.


---

## Problems Encountered

This section documents the real problems hit during development and how each was resolved. These are presented honestly because understanding failure modes is part of understanding the framework.

### Problem 1 — JNA ARM64 native library missing

**Symptom:**
```
java.lang.UnsatisfiedLinkError: Native library
(com/sun/jna/win32-aarch64/jnidispatch.dll) not found
```

**Cause:** The `ui-automation` library declares a transitive dependency on JNA `5.6.0`, which was released before ARM64 Windows support was added. Since the development VM runs Windows 11 ARM (required for Apple Silicon via UTM), this native library was simply absent from the JAR.

**Fix:** Explicitly overriding both `jna` and `jna-platform` to version `5.14.0` in `pom.xml`. Maven's dependency resolution gives explicit declarations priority over transitive ones.

**Lesson:** When a framework depends on native libraries, always verify platform coverage before committing to it. ARM64 Windows is still underrepresented in older Java ecosystem dependencies.
 
---

### Problem 2 — Wrong ControlType for the display element

**Symptom:**
```
mmarquee.automation.ElementNotFoundException: Element not found
at Container.getTextBox(Container.java:897)
```

**Cause:** The initial assumption was that the Calculator display was a `TextBox`. Inspecting the real UI with Accessibility Insights revealed it is a `Text` element (ControlType `50020`), which is read-only and exposes its content through the `Name` property — prefixed as `"Display is 7"` rather than simply `"7"`.

**Fix:** Replaced `getTextBox(...)` with a traversal of `window.getChildren(true)`, filtering for elements whose `Name` starts with `"Display is "` and stripping the prefix. This approach handles all Calculator states — numbers, error messages, expressions — because the prefix is consistent regardless of what is displayed.

**Lesson:** UI Automation element properties cannot be assumed from the visual appearance of the app. Accessibility Insights is an essential tool and should be the first step before writing any element interaction code.
 
---

### Problem 3 — Button Automation IDs

**Symptom:** `ElementNotFoundException` for buttons even when the button name appeared correct.

**Cause:** The Windows Calculator uses full English words as Automation Names — `"Two"`, `"Plus"`, `"Multiply by"`, `"Divide by"` — rather than the symbols or digits shown on screen. This is not documented anywhere accessible without inspecting the live application.

**Fix:** Each button name was verified individually using Accessibility Insights. The complete verified list is embedded in the system prompt for the AI natural language test generator, which suffered from the same problem initially.
 
---

### Problem 4 — Screenshot coordinates on a 200% DPI display

**Symptom:** All screenshots captured for failure analysis were entirely black.

**Cause:** The Windows UI Automation API returns bounding rectangle coordinates in **physical pixels**. On a 200% DPI display, physical pixels are exactly double the logical pixels. `java.awt.Robot.createScreenCapture` operates in **logical pixels**. The Calculator's physical bounding rectangle (`x=1996, y=399, width=666, height=1078`) was completely outside the logical screen bounds (`1470×923`), so the capture returned a black image.

**Fix:** `ScreenshotUtil` now detects the display scale factor via `GraphicsConfiguration.getDefaultTransform().getScaleX()` and divides all coordinates before passing them to `createScreenCapture`. A clamping step ensures the resulting rectangle never exceeds the logical screen bounds regardless of window position.

**Lesson:** DPI scaling is an invisible layer that silently breaks coordinate-based operations in Windows automation. Any code that captures screen content or maps coordinates must account for it explicitly.
 
---

### Problem 5 — CI pipeline: Vagrant not viable on GitHub-hosted runners

**Symptom:**
```
VBoxManage: error: hv_vm_create() failed: 0xfae9400f HV_UNSUPPORTED
(VERR_NEM_INIT_FAILED)
```

**Cause:** The initial CI/CD design used Vagrant with VirtualBox on a macOS GitHub Actions runner to spin up an ephemeral Windows VM per run. GitHub's macOS runners do not support nested virtualisation — the runner itself runs inside a VM, and VirtualBox cannot create a VM inside it without hardware-level support that is not exposed.

**What was tried:**
- `macos-latest` runner (ARM64) — nested virtualisation explicitly unsupported per GitHub documentation
- `macos-13` runner (Intel) — same `HV_UNSUPPORTED` error at the VBoxManage level
- Installing Vagrant and VirtualBox via Homebrew in the pipeline confirmed working, but VM creation fails at the hypervisor level regardless of the installation method

**Resolution:** The pipeline was redesigned to use the `windows-latest` GitHub Actions runner directly. This runner is a Windows VM with an active display session, which is exactly what UI Automation tests require. It is less exotic than Vagrant but reliable and genuinely production-viable for teams without dedicated Windows CI infrastructure.

The Vagrant + self-hosted runner architecture remains the recommended design for teams that need full control over the Windows version or want to match specific customer environments. In that setup, a dedicated Windows machine or a cloud VM acts as the self-hosted runner, and Vagrant manages test environments on top of it, achieving the same "create, test, destroy" lifecycle without the nested virtualisation constraint.
 
---

### Problem 6 — AI response JSON wrapped in markdown

**Symptom:**
```
com.google.gson.JsonSyntaxException: MalformedJsonException
```

**Cause:** Claude's API response for the natural language test generator included markdown code fences (` ```json `) around the JSON output, despite the system prompt not requesting them. `JsonParser.parseString()` does not tolerate this wrapping.

**Fix:** Added a cleanup step before parsing that strips ` ```json ` and ` ``` ` from the response, and strengthened the system prompt to explicitly instruct the model to return raw JSON with no surrounding text or formatting.
 
---

## Test Coverage

### Techniques Applied

| Technique | Applied to                                                                              |
|-----------|-----------------------------------------------------------------------------------------|
| Equivalence Partitioning | One basic arithmetic representative test per operator class                             |
| Boundary Value Analysis | Boundary values: Zero result, negative result, very large number                        |
| State Transition Testing | From Standard to Scientific back to Standard. It verifies display resets on mode change |
| Error Guessing | Division by zero, square root of negative number                                        |
| Exploratory / UX | Keyboard input via `java.awt.Robot`, Escape and Enter keys behaviour                    |

### Test Cases

| ID | Test Name | Technique |
|----|-----------|-----------|
| TC01 | It should display 5 when for the expression 2 + 3 | EP |
| TC02 | It should display 6 when for the expression 10 - 4 | EP |
| TC03 | It should display 42 when for the expression 6 × 7 | EP |
| TC04 | It should display 5 when for the expression 15 ÷ 3 | EP |
| TC05 | It should display 0 when for the expression 3 - 3 | BVA |
| TC06 | It should display -7 when for the expression 3 - 10 | BVA |
| TC07 | It should display 9 when for the expression 2 + 3 + 4 | BVA |
| TC08 | It should display 1,000,000 when for the expression 999999 + 1 | BVA |
| TC09 | It should show 'Cannot divide by zero' error when input the expression 5 ÷ 0 | Error Guessing |
| TC10 | It should display 0 when clear is pressed after entering 99 | Error Guessing |
| TC11 | It should display 8 when for keyboard input '5+3=' | Exploratory/UX |
| TC12 | It should display 0 when Escape key is pressed after entering 99 | Exploratory/UX |
| TC13 | It should correctly switch between operations in Scientific, Standard and back to Scientific modes | State Transition |
| TC14 | It should return 2 when for the expression √4 | EP |
| TC16 | It should display 'Invalid input' error for square root of negative number inputs | Error Guessing |
| TC17 | It should generate an addition calculator test when given plain natural language text in Standard mode | AI Generation |
| TC18 | It should generate a division calculator test when given plain natural language text in Standard mode | AI Generation |
| TC19 | It should generate a square root calculator test when given plain natural language text in Scientific mode | AI Generation |

---

## CI/CD Design

### Pipeline Overview

```
GitHub Push / Pull Request
         │
         ▼
  GitHub Actions
  runs-on: windows-latest   ← Windows VM with active display session
         │
         ├── actions/setup-java@v4 (Microsoft OpenJDK 21)
         ├── Verify Calculator is installed (Get-AppxPackage)
         ├── mvn clean test --batch-mode
         │       │
         │       ├── Tests run against Calculator UI
         │       └── Screenshots saved on failure → target/screenshots/
         │
         ├── mikepenz/action-junit-report  ← publishes results in GitHub UI
         └── actions/upload-artifact       ← uploads screenshots on failure
```

### Handling the Screen Session Constraint

Desktop UI tests cannot run headless the Windows UI Automation API requires an active graphical session to find and interact with window elements. The `windows-latest` GitHub Actions runner provides exactly this: it runs as an interactive Windows session with a virtual display, so the Calculator window can open and be automated normally.

For a production setup with higher control over the environment, the preferred approach would be a **self-hosted Windows runner** on a dedicated machine or a cloud VM (Azure or AWS EC2 Windows). This avoids dependency on GitHub's hosted runner availability and allows pinning a specific Windows version to match the target customer environment.

An alternative explored during this project was **Vagrant + VirtualBox** on a macOS runner. This was ultimately not viable for GitHub-hosted runners because macOS runners do not support nested virtualisation, which VirtualBox requires. The constraint is documented in GitHub's own runner images repository. For teams running their own CI infrastructure, Vagrant remains a strong option for reproducible Windows environments.

### Test Result Reporting

JUnit XML reports are generated by Maven Surefire at `target/surefire-reports/` and published to the GitHub Actions summary via `mikepenz/action-junit-report`, making pass/fail visible directly in pull request checks.

---

## AI Integration

### Model and API

**Model:** `claude-sonnet-4-5`  
**API:** Anthropic Messages API (`POST /v1/messages`)  
**Authentication:** `ANTHROPIC_API_KEY` environment variable

## Feature 1 — Natural Language Test Generation

A test can be described in plain English and the framework will execute it against the real Calculator. The description is sent to Claude with a structured system prompt defining the expected JSON schema and the complete list of valid button names. Claude returns a structured response that the framework parses and executes via the existing Screen Object layer.

```
Input:  "verify that the square root of 9 equals 3 in Scientific mode"
 
Claude returns:
{
  "mode": "Scientific",
  "steps": ["Nine", "Square root"],
  "expectedResult": "3"
}
 
Framework: switches to Scientific mode, clicks Nine, clicks Square root,
           asserts display shows "3"
```

### Feature 2 — Intelligent Failure Analysis

When a test fails, `FailureAnalyzer` captures a screenshot of the Calculator window and sends it to LLM alongside the test name, description, and exception message. Claude returns a human-readable explanation of what the screenshot shows, the likely root cause, and a suggested next step.

```
=== AI Failure Analysis ===
## Analysis

**What the screenshot shows:**
The Calculator is in Scientific mode, displaying "√(4)" in the expression area and "2" as the result - which is mathematically correct.

**Root cause:**
This is a **test automation bug**, not a product defect. The Calculator correctly computed √4 = 2, but the test assertion is inverted: it expects the value to be `<3>` when the actual result is `<2>`. The error message "expected: <3> but was: <2>" confirms the test expected an incorrect answer (3).

**Issue type:**
Test/automation issue - the assertion logic is backwards or contains a copy-paste error.

**Suggested next step:**
Review the test code's assertion statement. The expected value should be `2`, not `3`. Fix the assertion from `assertEquals(3, result)` to `assertEquals(2, result)` or similar, depending on your test framework.
===========================
```

### Limitations Encountered

- **Markdown in JSON responses.** Claude occasionally wraps JSON output in Markdown code fences (` ```json `), causing parse failures. Mitigated by removing the code fences before parsing and by explicitly instructing the model in the system prompt to return raw JSON only.
- **Button name hallucination.** In early iterations, Claude occasionally produced button names that did not match the Calculator's actual Automation IDs (e.g. `"Add"` instead of `"Plus"`). Fixed by providing the complete list of valid button names in the system prompt.
- **DPI-aware screenshot capture.** On displays with 200% scaling, the UI Automation API returns physical pixel coordinates while `java.awt.Robot` operates in logical pixels. This mismatch caused captured screenshots to be black. Resolved by detecting the display scale factor and adjusting coordinates accordingly.
- **No support for complex expressions.** The natural language generator handles basic two-operand expressions reliably. Parenthesised or multi-step expressions (e.g. `"(2 + 3) × 4"`) require more sophisticated prompt engineering to produce correct step sequences.

---

## AI Usage Transparency

### Tools Used

| Tool | Used for                                                                                |
|------|-----------------------------------------------------------------------------------------|
| Claude (claude.ai) | Framework architecture, debugging, CI/CD design, code generation ,and README drafting   |
| Claude API (claude-sonnet-4-5) | Natural language test generation and failure analysis features built into the framework |

### What Required Correction

- **`AppDriver.launch()`**: the initial LLM suggestion used `automation.launch("calc.exe")` which fails if a Calculator window is already open. Corrected to `launchOrAttach` after observing the failure in practice.
- **`getResult()` implementation**: the LLM initially suggested using `window.getTextBox("CalculatorResults")`, but the display element is of ControlType `Text (50020)`, not `TextBox`. Discovered via Accessibility Insights and corrected to iterate `window.getChildren(true)` and match on `name.startsWith("Display is ")`. This happened to many other elements as well, and the correct Automation IDs were only discoverable through live inspection.
- **Mode switching**:At the beginning the calculator app would be launched in the default mode, which could be either Standard, Scientific or even Graphing depending on the last state. The LLM-suggested implementation of the Screen Object constructors did not account for this, leading to test failures when the mode was different from the expected. This was resolved by adding mode validation and switching logic to each constructor, ensuring a consistent starting state regardless of previous tests.
- **JNA version**: the initial `pom.xml` used JNA `5.6.0` (transitive from `ui-automation`), which does not include the `win32-aarch64` native library needed for Windows ARM. Required explicit override to JNA `5.14.0`.
- **UI Automation Library**: The library doesn't have a method to select a ListItem by name, so the initial suggestion of `window.getButton("Standard Calculator");` failed. The library is poorly documented and unmaintained, so I iterated through available methods until discovering the correct approach: get the control by class and name, then select it.
- **Screenshot coordinates** — the AI-generated `ScreenshotUtil` did not account for DPI scaling, producing black screenshots on 200% displays. I discovered this by running tests on the actual display and observing the failure, then researched the mismatch between physical and logical pixel coordinates. The fix required dividing the physical coordinates by the scale factor before passing to `createScreenCapture`.- **Screenshot coordinates** — the AI-generated `ScreenshotUtil` did not account for DPI scaling, producing black screenshots on 200% displays. Required dividing the physical coordinates by the scale factor before passing to `createScreenCapture`.

### Where AI Was Most Helpful

Setting up the overall framework skeleton quickly (e.g.: the Screen Object structure, Maven dependencies, and JUnit lifecycle) was significantly faster with AI assistance. Debugging the JNA ARM64 issue was also well-handled: the error message was specific enough that Claude identified the root cause and the fix immediately.

### Where AI Was Least Helpful

- Anything requiring live interaction with the actual application (e.g.: the correct ControlType of the display element, the exact Automation IDs of buttons, the navigation structure of the mode-switching menu) had to be discovered manually using Accessibility Insights. 
AI-generated element names were plausible but frequently incorrect, and trusting them without verification would have cost more time than inspecting the UI directly.
- Debbuging any unexpected behaviour that required understanding the real behaviour of the Calculator (e.g. the screenshot coordinate mismatch, mode switching) was not something the AI could assist with, since it had no access to the live application or its runtime state.

---

## Improvements

Given more time, the following improvements would be prioritised:

- Cover more edge cases and error states in the existing modes (e.g. overflow errors, invalid input formats);
- Add tests for the Failure Analysis feature, ensuring that screenshots are captured and sent to the model correctly, and that the model response quality is sufficient to guide debugging;
- Add tests for the natural language test generator that verify it can handle a wider variety of input phrasings and more complex expressions, and that it handles errors correctly when given invalid input.
- The arithmetic tests currently repeat the same structure with different values, so replacing them with `@ParameterizedTest` tests driven by a CSV or method source would reduce code duplication and make it trivial to add new cases.