package com.demo.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.gatling.custom.browser.javaapi.model.BrowserSession;
import io.gatling.javaapi.core.Session;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

public class EcomActions {

    public static BiFunction<Page, BrowserSession,BrowserSession> addItemToCards = (page, browserSession) -> {
        EcomPage storefront = new EcomPage(page);

        Session session = browserSession.getGatlingSession();
        int itemCounter = session.contains("itemCounter") ? session.getInt("itemCounter") : 0;

        // Re-queried every call rather than cached: each iteration searches a different
        // product first, so the grid it points at has already changed.
        List<Locator> itemsList = storefront.productCards();
        Locator item = itemsList.get(ThreadLocalRandom.current().nextInt(itemsList.size()));

        storefront.hoverProduct(item);
        browserSession.setActionStartTime();
        storefront.clickAddToCart(item);

        itemCounter++;

        storefront.assertCartCount(itemCounter);
        browserSession.setActionEndTime();

        session = session.set("itemCounter",itemCounter);

        return browserSession.updateBrowserSession(session);
    };

    public static BiFunction<Page, BrowserSession,BrowserSession> login = (page, browserSession) -> {
        EcomPage storefront = new EcomPage(page);

        Session session = browserSession.getGatlingSession();

        storefront.openLoginForm();
        storefront.submitLogin(session.getString("username"), session.getString("password"));

        storefront.assertCatalogVisible();

        return browserSession;
    };

    public static BiFunction<Page, BrowserSession,BrowserSession> search = (page, browserSession) -> {
        EcomPage storefront = new EcomPage(page);

        Session session = browserSession.getGatlingSession();
        String productName = session.getString("productName");

        storefront.fillSearch(productName);

        browserSession.setActionStartTime();
        storefront.submitSearch();

        storefront.assertSearchResult(productName);
        browserSession.setActionEndTime();

        return browserSession;
    };

    public static BiFunction<Page, BrowserSession,BrowserSession> checkout = (page, browserSession) -> {
        EcomPage storefront = new EcomPage(page);

        Session session = browserSession.getGatlingSession();
        int itemCounter = session.getInt("itemCounter");

        storefront.openCart(itemCounter);
        storefront.clickCheckout();

        storefront.assertCartCleared(itemCounter);

        return browserSession;
    };
}
