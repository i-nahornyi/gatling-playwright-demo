package com.demo.simulation;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.custom.browser.model.PageLoadValidator;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ProtocolBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static com.microsoft.playwright.options.WaitUntilState.NETWORKIDLE;
import static io.gatling.javaapi.core.CoreDsl.*;

@SuppressWarnings("unused")
public class SyntheticMonitoring extends Simulation {

    ProtocolBuilder browserProtocol = BrowserDsl
            .gatlingBrowser()
            .withLaunchOptions(new BrowserType.LaunchOptions().setHeadless(false))
            .withContextOptions(new Browser.NewContextOptions()
                    .setViewportSize(1920, 1080).setBypassCSP(true)
            )
            .enableUIMetrics()
            .buildProtocol();

    FeederBuilder<String> pageFeeder = csv("feeders/page.csv").circular();

    String customValidationScript = BrowserDsl.loadScript("scripts/customValidationScript.js");
    PageLoadValidator customPageLoadValidator = new PageLoadValidator(
            customValidationScript,
            null,
            new Page.WaitForFunctionOptions().setPollingInterval(200).setTimeout(30000)
    );

    ScenarioBuilder mainScenario = scenario("test")
            .repeat(10).on(
                    feed(pageFeeder),
                    BrowserDsl.browserAction("#{name}")
                            .open("#{link}")
                            .withNavigateOptions(new Page.NavigateOptions().setWaitUntil(NETWORKIDLE))
                            .withLoadValidations(customPageLoadValidator),
                    pause(1,3),
                    BrowserDsl.browserCleanContext()
            );


    {
        setUp(mainScenario.injectOpen(atOnceUsers(1))).protocols(browserProtocol);
    }

}
