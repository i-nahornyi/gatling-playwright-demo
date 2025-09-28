package com.demo.simulation;

import com.microsoft.playwright.BrowserType;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.javaapi.core.*;

import static io.gatling.javaapi.core.CoreDsl.*;

@SuppressWarnings("unused")
public class SyntheticMonitoring extends Simulation {

    ProtocolBuilder browserProtocol = BrowserDsl
            .gatlingBrowser()
            .withLaunchOptions(new BrowserType.LaunchOptions().setHeadless(false))
            .enableUIMetrics()
            .buildProtocol();

    FeederBuilder<String> pageFeeder = csv("feeders/page.csv").circular();
    int linkCount  = pageFeeder.recordsCount();

    ScenarioBuilder mainScenario = scenario("test")
            .repeat(session -> linkCount).on(
                    feed(pageFeeder),
                    BrowserDsl.browserAction("#{name}").open("#{link}").withLoadValidations(),
                    BrowserDsl.browserCleanContext()
            );


    {
        setUp(mainScenario.injectOpen(atOnceUsers(1))).protocols(browserProtocol);
    }

}
