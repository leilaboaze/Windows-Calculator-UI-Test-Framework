package com.nitro.utils;

import mmarquee.automation.controls.Window;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ScreenshotUtils {

    public static byte[] captureWindow(Window window) throws Exception {

        var rect = window.getBoundingRectangle().toRectangle();
        //TODO: This should dynamically retrieve computer's display scale. Was tested in a 200% scaling environment.
        double scale = 2.0;
        Rectangle screenRect = new Rectangle(
                (int) (rect.x/scale),
                (int) (rect.y/scale),
                (int) (rect.width/scale),
                (int) (rect.height/scale)
        );
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(screenRect);
        String dir = "target/screenshots";
        Files.createDirectories(Paths.get(dir));
        String filename = dir + "/screenshot_" + System.currentTimeMillis() + ".png";
        File imageFile = new File(filename);
        ImageIO.write(image, "PNG", imageFile);

        if (imageFile.exists()) {
            System.out.println("Screenshot saved: " + imageFile.getAbsolutePath());
        } else {
            System.err.println("Failed to save screenshot.");
        }

        return Files.readAllBytes(imageFile.toPath());
    }
}