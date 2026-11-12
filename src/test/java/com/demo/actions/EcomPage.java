package com.demo.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Page object for the ecomm.gatling.io storefront: owns every locator and UI
 * interaction so {@code EcomActions} only has to orchestrate Gatling session
 * state and timing around these calls.
 */
public class EcomPage {

    private final Page page;

    public EcomPage(Page page) {
        this.page = page;
    }

    public List<Locator> productCards() {
        return page.locator("//div[contains(@class, 'grid')]/div[contains(@class, 'group')]").all();
    }

    public void hoverProduct(Locator productCard) {
        productCard.hover();
    }

    public void clickAddToCart(Locator productCard) {
        productCard.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Add to Cart")).click();
    }

    public Locator cartBadge(int itemCount) {
        return page.locator("//div[contains(@class,'flex items-center justify-center') and contains(text(), '" + itemCount + "')]");
    }

    public void assertCartCount(int itemCount) {
        assertThat(cartBadge(itemCount).first()).isVisible();
    }

    public void openLoginForm() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login")).click();
    }

    public void submitLogin(String username, String password) {
        page.getByPlaceholder("Username").fill(username);
        page.getByPlaceholder("Password").fill(password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit")).click();
    }

    public void assertCatalogVisible() {
        assertThat(page.locator("//div[contains(@class, 'grid')]/div[contains(@class, 'group')]").first()).isVisible();
    }
    private Locator searchBox() {
        return page.getByPlaceholder("Search products...").first();
    }

    public void fillSearch(String productName) {
        searchBox().fill(productName);
    }

    public void submitSearch() {
        searchBox().press("Enter");
    }

    public void assertSearchResult(String productName) {
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(productName)).first()).isVisible();
    }

    public void openCart(int itemCount) {
        cartBadge(itemCount).first().click();
    }

    public void clickCheckout() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Checkout")).click();
    }

    public void assertCartCleared(int itemCount) {
        assertThat(cartBadge(itemCount)).hasCount(0);
    }
}
