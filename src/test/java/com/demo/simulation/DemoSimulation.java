package com.demo.simulation;

import com.demo.scenario.BackendJourney;
import com.demo.scenario.FrontendJourney;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static com.demo.endpoints.EcomApiEndpoints.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Mixes two protocols in a single load test: plain HTTP calls against the ecommerce
 * backend API (api-ecomm.gatling.io) and real browser journeys driven through
 * Playwright against the storefront UI (ecomm.gatling.io), both injected together via
 * {@code setUp(...)} thanks to gatling-playwright-plugin exposing its browser protocol
 * as a regular Gatling {@link io.gatling.javaapi.core.ProtocolBuilder}.
 */
@SuppressWarnings("unused")
public class DemoSimulation extends Simulation {

    int backendUsers = Integer.parseInt(System.getProperty("backendUsers", "10"));
    int frontendUsers = Integer.parseInt(System.getProperty("frontendUsers", "2"));
    int TEST_DURATION = Integer.parseInt(System.getProperty("durationSec", "360"));

    HttpProtocolBuilder httpProtocol = withAuthenticationHeader(http
            .baseUrl("https://api-ecomm.gatling.io")
            .acceptHeader("application/json")
            .userAgentHeader("Mozilla/5.0 gatling")
    );

    ProtocolBuilder browserProtocol = BrowserDsl
            .gatlingBrowser()
            .withLaunchOptions(new BrowserType.LaunchOptions().setHeadless(false))
            .withContextOptions(new Browser.NewContextOptions()
                    .setViewportSize(1920, 1080)
                    .setBaseURL("https://ecomm.gatling.io")
                    .setUserAgent("Mozilla/5.0 gatling-playwright-browser")
            )
            .withDefaultTimeout(60000)
            .enableUIMetrics();


    FeederBuilder<String> usersFeeder = csv("feeders/users.csv").circular();
    FeederBuilder<String> productsFeeder = csv("feeders/products.csv").circular();

    private final ChainBuilder THINK_TIME = pause(1, 5);


    ScenarioBuilder backendScenario = BackendJourney.buildScenario("Backend scenario", THINK_TIME, usersFeeder, productsFeeder);
    ScenarioBuilder frontendScenario = FrontendJourney.buildScenario("Frontend scenario", THINK_TIME, usersFeeder, productsFeeder);

    {
        setUp(
                backendScenario.injectClosed(constantConcurrentUsers(backendUsers).during(TEST_DURATION)),
                frontendScenario.injectClosed(constantConcurrentUsers(frontendUsers).during(TEST_DURATION))
        ).protocols(httpProtocol, browserProtocol);
    }

}
