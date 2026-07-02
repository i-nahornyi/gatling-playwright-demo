package com.demo.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.GetByRoleOptions;
import com.microsoft.playwright.options.AriaRole;
import io.gatling.custom.browser.javaapi.model.BrowserSession;
import io.gatling.javaapi.core.Session;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class EcomActions {

    public static BiFunction<Page, BrowserSession,BrowserSession> addItemToCards = (page, browserSession) -> {
        List<Locator> itemsList;
        Session session = browserSession.getGatlingSession();
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
        browserSession.setActionStartTime();
        item.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Add to Cart")).click();

        itemCounter++;

        assertThat(page.locator("//div[contains(@class,'flex items-center justify-center') and contains(text(), '"+ itemCounter+"')]").first()).isVisible();
        browserSession.setActionEndTime();

        session = session.set("itemCounter",itemCounter);


        return browserSession.updateBrowserSession(session);
    };

    public static BiFunction<Page, BrowserSession,BrowserSession> login = (page, browserSession) -> {

        page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Login")).click();

        page.getByPlaceholder("Username").fill("admin");
        page.getByPlaceholder("Password").fill("gatling");
        page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Submit")).click();

        assertThat(page.locator("//div[contains(@class, 'grid')]/div[contains(@class, 'group')]").first()).isVisible();

        return browserSession;
    };

    public static BiFunction<Page, BrowserSession,BrowserSession> checkout = (page, browserSession) -> {


        Session session = browserSession.getGatlingSession();
        int itemCounter = session.getInt("itemCounter");

        Locator card = page.locator("//div[contains(@class,'flex items-center justify-center') and contains(text(), '"+ itemCounter+"')]");
        card.first().click();

        page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("Checkout")).click();

        assertThat(card).hasCount(0);

        return browserSession;
    };
}
