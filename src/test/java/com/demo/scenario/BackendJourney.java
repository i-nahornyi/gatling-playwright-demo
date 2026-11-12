package com.demo.scenario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.demo.endpoints.EcomApiEndpoints.*;
import static io.gatling.javaapi.core.CoreDsl.*;

public class BackendJourney {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static ScenarioBuilder buildScenario(String scenarioName, ChainBuilder THINK_TIME, FeederBuilder<String> usersFeeder, FeederBuilder<String> productsFeeder) {
        return scenario(scenarioName)
                .exec(
                        group("API - Browse").on(
                                homepage,
                                session,
                                products
                        ),
                        THINK_TIME,
                        group("API - Authenticate").on(
                                feed(usersFeeder),
                                login
                        ),
                        // A failed login leaves accessToken empty, so there's no point letting
                        // search/purchase run with it - same spirit as FrontendJourney never
                        // checking out when its UI AddItem never actually set itemCounter.
                        exitHereIfFailed(),
                        repeat("#{randomInt(1,5)}").on(
                                feed(productsFeeder),
                                THINK_TIME,
                                group("API - Search").on(
                                        search
                                ),
                                THINK_TIME,
                                group("API - Purchase").on(
                                        exec(session -> {
                                            try {
                                                List<Map<String, Object>> catalog = mapper.readValue(session.getString(PRODUCTS), new TypeReference<>() {});
                                                Map<String, Object> picked = catalog.get(ThreadLocalRandom.current().nextInt(catalog.size()));
                                                return session.set(CART_ITEMS, mapper.writeValueAsString(List.of(picked)));
                                            } catch (Exception e) {
                                                throw new RuntimeException(e);
                                            }
                                        }),
                                        cart,
                                        // Don't check out on top of a cart add that never happened.
                                        exitHereIfFailed()
                                )
                        ),
                        THINK_TIME,
                        group("API - Purchase").on(
                                checkout
                        )
                );
    }
}
