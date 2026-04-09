package com.nitro;
import com.nitro.driver.AppDriver;
import mmarquee.automation.controls.Application;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class BaseTest {

    protected Application app;

    @BeforeEach
    public void setUp() throws Exception {
        app = AppDriver.launch();
    }

    @AfterEach
    public void tearDown() {
        AppDriver.close();
    }
}