package com.demo.scenario;

import com.demo.actions.EcomActions;
import com.microsoft.playwright.Page.NavigateOptions;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;

import static com.microsoft.playwright.options.WaitUntilState.NETWORKIDLE;
import static io.gatling.javaapi.core.CoreDsl.*;

public class FrontendJourney {

    public static ScenarioBuilder buildScenario(String scenarioName, ChainBuilder THINK_TIME, FeederBuilder<String> usersFeeder, FeederBuilder<String> productsFeeder) {
        return scenario(scenarioName)
                .exec(
                        group("UI - Browse").on(
                                BrowserDsl.browserAction("HomePage").open("/").withNavigateOptions(new NavigateOptions().setWaitUntil(NETWORKIDLE))
                        ),
                        THINK_TIME,
                        group("UI - Authenticate").on(
                                feed(usersFeeder),
                                BrowserDsl.browserAction("Login").executeFlow(EcomActions.login)
                        ),
                        // A failed login leaves the shopper logged out, so there's no point
                        // letting search/purchase run afterwards - same spirit as BackendJourney.
                        exitHereIfFailed(),
                        repeat("#{randomInt(1,5)}").on(
                                feed(productsFeeder),
                                THINK_TIME,
                                group("UI - Search").on(
                                        BrowserDsl.browserAction("Search").executeFlow(EcomActions.search)
                                ),
                                THINK_TIME,
                                group("UI - Purchase").on(
                                        BrowserDsl.browserAction("AddItem").executeFlow(EcomActions.addItemToCards)
                                ),
                                // Don't check out on top of an add-to-cart that never happened.
                                exitHereIfFailed()
                        ),
                        THINK_TIME,
                        group("UI - Purchase").on(
                                BrowserDsl.browserAction("Checkout").executeFlow(EcomActions.checkout)
                        ),
                        BrowserDsl.browserCleanContext(),
                        exec(Session::reset)
                );
    }
}
