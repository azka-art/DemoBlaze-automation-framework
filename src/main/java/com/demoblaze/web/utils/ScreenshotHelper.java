package com.demoblaze.web.utils;

import io.qameta.allure.Allure;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotHelper {
    
    public static void takeScreenshot(String testName, String status) {
        TakesScreenshot ts = (TakesScreenshot) DriverManager.getDriver();
        File source = ts.getScreenshotAs(OutputType.FILE);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = String.format("%s_%s_%s.png", testName, status, timestamp);
        
        try {
            File screenshotDir = new File("screenshots");
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }
            
            File destFile = new File(screenshotDir, fileName);
            FileUtils.copyFile(source, destFile);
            
            // Attach to Allure report
            if (destFile.exists()) {
                Allure.addAttachment(testName, new FileInputStream(destFile));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
