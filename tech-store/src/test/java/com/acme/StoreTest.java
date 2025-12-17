package com.acme;

import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;

import io.quarkiverse.playwright.InjectPlaywright;
import io.quarkiverse.playwright.WithPlaywright;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithPlaywright
//@WithPlaywright(debug=true)
//@WithPlaywright(browser = FIREFOX)
public class StoreTest {

    @InjectPlaywright
    BrowserContext context;

    @TestHTTPResource("/")
    URL index;
    
    @Test
    void testProductSearch() {
        final Page page = context.newPage();

        Response response = page.navigate(index.toString());
        Assertions.assertEquals("OK", response.statusText());

        page.waitForLoadState();

        page.getByPlaceholder("Search products...")
                .fill("Laptop");

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Find")).click();

        Locator result = page.locator(".item");

        Assertions.assertTrue(result.isVisible());
        Assertions.assertEquals(
                "Gaming Laptop X1",
                result.textContent());
    }
}