package com.demo.simulation;

import com.demo.actions.EcomActions;
import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Page.NavigateOptions;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.javaapi.core.*;

import static com.microsoft.playwright.options.WaitUntilState.NETWORKIDLE;
import static io.gatling.javaapi.core.CoreDsl.*;

@SuppressWarnings("unused")
public class DemoSimulation extends Simulation {


    ProtocolBuilder browserProtocol = BrowserDsl
            .gatlingBrowser()
            .withLaunchOptions(new LaunchOptions().setHeadless(false))
            .withContextOptions(new NewContextOptions()
                    .setViewportSize(1920, 1080)
                    .setBaseURL("https://ecomm.gatling.io")
            )
            .buildProtocol();


    private final ChainBuilder THINK_TIME = pause(3,5);



    ScenarioBuilder mainScenario = scenario("test")
            .repeat(3).on(
                    BrowserDsl.browserAction("HomePage").open("/",new NavigateOptions().setWaitUntil(NETWORKIDLE)),
                    THINK_TIME,
                    BrowserDsl.browserAction("Login").executeFlow(EcomActions.login),
                    repeat("#{randomInt(2,5)}").on(
                            THINK_TIME,
                            BrowserDsl.browserAction("AddItem").executeFlow(EcomActions.addItemToCards)
                    ),
                    doIf(session -> session.contains("itemCounter")).then(
                            THINK_TIME,
                            BrowserDsl.browserAction("Checkout").executeFlow(EcomActions.checkout),
                            THINK_TIME
                    ),
                    BrowserDsl.browserCleanContext(),
                    exec(Session::reset)
            );


    {
        setUp(mainScenario.injectOpen(OpenInjectionStep.atOnceUsers(1))).protocols(browserProtocol);
    }


}
