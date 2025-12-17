package com.acme;

import java.util.Base64;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ScreenshotService {

    public String captureHomepageBase64(String url) {
        byte[] screenshot = captureHomepage(url);
        return Base64.getEncoder()
                .encodeToString(screenshot);
    }

    public byte[] captureHomepage(String url) {
        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.webkit().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(true));

            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            page.navigate(url);
            return page.screenshot();
        }
    }
}