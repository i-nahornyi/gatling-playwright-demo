package com.demo.simulation;

import com.demo.scenario.FrontendJourney;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.javaapi.core.*;

import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.pause;

@SuppressWarnings("unused")
public class BrowserOnlySimulation extends Simulation {

    int vUsers = Integer.parseInt(System.getProperty("vUsers", "2"));
    FeederBuilder<String> usersFeeder = csv("feeders/users.csv").circular();
    FeederBuilder<String> productsFeeder = csv("feeders/products.csv").circular();

    ProtocolBuilder browserProtocol = BrowserDsl
                .gatlingBrowser()
                .withLaunchOptions(new BrowserType.LaunchOptions().setHeadless(false))
                .withContextOptions(new Browser.NewContextOptions()
                        .setViewportSize(1920, 1080)
                        .setBaseURL("https://ecomm.gatling.io")
                )
                .withDefaultTimeout(60000)
                .enableUIMetrics();

    private final ChainBuilder THINK_TIME = pause(1, 3);

    ScenarioBuilder frontendScenario = FrontendJourney.buildScenario("FrontEnd scenario", THINK_TIME, usersFeeder, productsFeeder);

    {
        setUp(frontendScenario.injectOpen(OpenInjectionStep.atOnceUsers(vUsers))).protocols(browserProtocol);
    }

}
