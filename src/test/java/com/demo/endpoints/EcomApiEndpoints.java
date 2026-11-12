package com.demo.endpoints;

import io.gatling.javaapi.http.HttpProtocolBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

import java.util.Optional;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

@SuppressWarnings("unused")
public class EcomApiEndpoints {

    public static final String SESSION_ID = "sessionId";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String PRODUCTS = "products";
    public static final String CART_ITEMS = "cartItems";

    // /checkout requires the bearer token handed back by login; /session, /products and
    // /cart don't check it. Forwarding it unconditionally is harmless either way.
    public static HttpProtocolBuilder withAuthenticationHeader(HttpProtocolBuilder protocolBuilder) {
        return protocolBuilder.header(
                "Authorization",
                session -> Optional.ofNullable(session.getString(ACCESS_TOKEN)).orElse(""));
    }

    // Plain HTTP fetch of the storefront's HTML shell, same as a browser's initial
    // navigation before its JS kicks in and starts calling /session and /products.
    public static final HttpRequestActionBuilder homepage =
            http("API_HomePage")
                    .get("https://ecomm.gatling.io/")
                    .check(status().is(200));

    public static final HttpRequestActionBuilder session =
            http("API_Session")
                    .get("/session")
                    .check(status().is(200))
                    .check(jmesPath("sessionId").saveAs(SESSION_ID));

    public static final HttpRequestActionBuilder products =
            http("API_Products")
                    .get("/products")
                    .queryParam("page", 0)
                    .check(status().is(200))
                    .check(jmesPath("products").saveAs(PRODUCTS));

    public static final HttpRequestActionBuilder login =
            http("API_Login")
                    .post("/login")
                    .asFormUrlEncoded()
                    .formParam("username", "#{username}")
                    .formParam("password", "#{password}")
                    .check(status().is(200))
                    .check(jmesPath("accessToken").saveAs(ACCESS_TOKEN));

    public static final HttpRequestActionBuilder search =
            http("API_Search")
                    .get("/products")
                    .queryParam("search", "#{productName}")
                    .check(status().is(200))
                    .check(jmesPath("products").saveAs(PRODUCTS));

    public static final HttpRequestActionBuilder cart =
            http("API_AddToCart")
                    .post("/cart")
                    .asJson()
                    .body(ElFileBody("bodies/cart.json"))
                    .check(status().is(200));

    public static final HttpRequestActionBuilder checkout =
            http("API_Checkout")
                    .post("/checkout")
                    .asJson()
                    .body(ElFileBody("bodies/cart.json"))
                    .check(status().is(200));
}
