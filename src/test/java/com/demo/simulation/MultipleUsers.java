package com.demo.simulation;

import com.microsoft.playwright.BrowserType;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.javaapi.core.ProtocolBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.*;

@SuppressWarnings("unused")
public class MultipleUsers extends Simulation {
    ProtocolBuilder browserProtocol = BrowserDsl
            .gatlingBrowser()
            .withLaunchOptions(new BrowserType.LaunchOptions().setHeadless(false))
            .enableUIMetrics()
            .buildProtocol();
    int vUserCount = Integer.parseInt(System.getProperty("vUsers","2"));

    ScenarioBuilder scenarioBuilder = scenario("scenario_").repeat(5).on(
            BrowserDsl.browserAction("home").open("https://gatling.io/").withLoadValidations(),
            pause(1, 5),
            BrowserDsl.browserAction("docks").open("https://docs.gatling.io/").withLoadValidations(),
            pause(1, 5),
            BrowserDsl.browserCleanContext()
    );

    {
        setUp(scenarioBuilder.injectOpen(atOnceUsers(vUserCount))).protocols(browserProtocol);
    }
}
