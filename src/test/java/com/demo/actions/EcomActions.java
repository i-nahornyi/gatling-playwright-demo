package com.demo.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.gatling.custom.browser.model.BrowserSession;
import io.gatling.javaapi.core.Session;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class EcomActions {

    public static BiFunction<Page, BrowserSession,BrowserSession> addItemToCards = (page, browserSession) -> {
        List<Locator> itemsList;
        Session session = browserSession.getJavaSession();
        int itemCounter = 0;

        if (session.contains("itemsList")){
            itemsList = session.getList("itemsList");
            itemCounter = session.getInt("itemCounter");
        }
        else {
            itemsList = page.locator("//div[contains(@class, 'grid')]/div[contains(@class, 'group')]").all();
            session = session.set("itemsList",itemsList);
        }

        Locator item = itemsList.get(ThreadLocalRandom.current().nextInt(itemsList.size()));

        item.hover();
        browserSession.setActionStartTime(System.currentTimeMillis());
        item.getByRole(AriaRole.BUTTON).click();

        itemCounter++;

        assertThat(page.locator("//div[contains(@class,'flex items-center justify-center') and contains(text(), '"+ itemCounter+"')]").first()).isVisible();
        browserSession.setActionEndTime(System.currentTimeMillis());

        session = session.set("itemCounter",itemCounter);


        return browserSession.updateBrowserSession(session);
    };

    public static BiFunction<Page, BrowserSession,BrowserSession> login = (page, browserSession) -> {

        page.locator("//button[contains(text(), 'Login')]").click();

        page.locator("//input[@placeholder='Username']").fill("admin");
        page.locator("//input[@placeholder='Password']").fill("gatling");
        page.locator("//label[contains(text(), 'Submit')]").click();

        assertThat(page.locator("//div[contains(@class, 'grid')]/div[contains(@class, 'group')]").first()).isVisible();

        return browserSession;
    };

    public static BiFunction<Page, BrowserSession,BrowserSession> checkout = (page, browserSession) -> {


        Session session = browserSession.getJavaSession();
        int itemCounter = session.getInt("itemCounter");

        Locator card = page.locator("//div[contains(@class,'flex items-center justify-center') and contains(text(), '"+ itemCounter+"')]");
        card.first().click();


        page.locator("//label[contains(text(), 'Checkout')]").click();

        assertThat(card).hasCount(0);

        return browserSession;
    };
}
