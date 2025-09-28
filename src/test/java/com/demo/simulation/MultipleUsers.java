package com.demo.simulation;

import com.microsoft.playwright.BrowserType;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.javaapi.core.*;

import java.util.ArrayList;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.pause;
import static io.gatling.javaapi.core.CoreDsl.scenario;

@SuppressWarnings("unused")
public class MultipleUsers extends Simulation {
    ProtocolBuilder browserProtocol = BrowserDsl
            .gatlingBrowser()
            .withLaunchOptions(new BrowserType.LaunchOptions().setHeadless(false))
            .enableUIMetrics()
            .buildProtocol();
    int vUserCount = Integer.parseInt(System.getProperty("vUsers","3"));

    ChainBuilder scenarioBuilder = ChainBuilder.EMPTY.repeat(5).on(
            BrowserDsl.browserAction("home").open("https://gatling.io/").withLoadValidations(),
            pause(1, 5),
            BrowserDsl.browserAction("docks").open("https://docs.gatling.io/").withLoadValidations(),
            pause(1, 5),
            BrowserDsl.browserCleanContext()
    );

    {
        List<PopulationBuilder> simulationList = new ArrayList<>();
        for (int i = 0; i < vUserCount; i++) {
            simulationList.add(scenario("scenario_"+i).exec(scenarioBuilder).injectOpen(OpenInjectionStep.atOnceUsers(1)));
        }

        setUp(simulationList).protocols(browserProtocol);
    }
}
