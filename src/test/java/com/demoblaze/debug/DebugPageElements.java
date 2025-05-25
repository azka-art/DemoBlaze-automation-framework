package com.demoblaze.debug;

import com.demoblaze.web.utils.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.List;

public class DebugPageElements {
    public static void main(String[] args) {
        try {
            WebDriver driver = DriverManager.getDriver();
            driver.get("https://www.demoblaze.com");
            
            Thread.sleep(3000);
            
            System.out.println("Page title: " + driver.getTitle());
            System.out.println("Current URL: " + driver.getCurrentUrl());
            
            // Find all links
            List<WebElement> links = driver.findElements(By.tagName("a"));
            System.out.println("\nFound " + links.size() + " links:");
            
            for (int i = 0; i < Math.min(links.size(), 10); i++) {
                WebElement link = links.get(i);
                try {
                    String text = link.getText();
                    String id = link.getAttribute("id");
                    String href = link.getAttribute("href");
                    System.out.println((i+1) + ". Text: '" + text + "' | ID: '" + id + "' | Href: '" + href + "'");
                } catch (Exception e) {
                    System.out.println((i+1) + ". Error reading link: " + e.getMessage());
                }
            }
            
            driver.quit();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}