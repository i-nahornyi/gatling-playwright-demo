# gatling-playwright-demo

Showcase of [`gatling-playwright-plugin`](https://github.com/i-nahornyi/gatling-playwright-plugin):
it exposes a real Playwright browser session as a regular Gatling `ProtocolBuilder`, so one
`setUp(...)` can inject classic HTTP virtual users and real-browser virtual users side by side,
in the same simulation, against the same ecommerce demo site.

> [!WARNING]
> **Don't use the browser protocol to generate load.** Each browser virtual user is a real
> Chromium process - it doesn't scale to thousands of users the way an HTTP request does, and
> trying to use it that way will just exhaust your own machine/CI runner, not the system under
> test.
>
> Use it instead as a small, constant number of **real-user probes** that keep clicking through
> the same journey a human would, while your actual load comes from plain HTTP virtual users
> hitting the backend. That turns "how much load can we throw at this" into "how does backend
> load affect what a real user actually experiences" - page load times, click responsiveness,
> UI metrics (FCP/LCP/TTFB/CLS via `.enableUIMetrics()`) - which a pure HTTP test can't tell you
> at all.

### Build
```shell
docker build -t gatling-playwright . --no-cache
```

### Run
```shell
docker run --rm -v gatling_result:/gatling_test/target/gatling \
                      gatling-playwright \
                      com.demo.simulation.DemoSimulation
```

### Simulations in this repo

- **`DemoSimulation`** - the main example. Heavy HTTP load (`backendUsers`, default 10) against
  the API, plus a couple of real browsers (`frontendUsers`, default 2) running the exact same
  journey through the UI, so you can see how the former affects the latter.
- **`BrowserOnlySimulation`** - just the browser journey, no backend load pressure. Useful as a
  starting point for a pure UI functional/regression scenario.
- **`SyntheticMonitoring`** - no injected load at all: one browser repeatedly opening a list of
  pages (`feeders/page.csv`) with a custom page-load validation script, the classic "is the site
  up and fast" synthetic monitoring check.

### Adding your own flow

Every locator and flow in this repo was discovered by driving the live site with
`playwright-cli` first - snapshotting it, clicking around, checking what requests an action
actually fires - rather than guessing selectors. See
[`docs/playwright-discovery.md`](docs/playwright-discovery.md) for that walkthrough before
adding your own.

### Load model

`DemoSimulation` injects two scenarios built from the same two reusable journeys
(`BackendJourney`, plain HTTP; `FrontendJourney`, a real Playwright browser), both
walking the same four stages - Browse, Authenticate, Search, Purchase. Note the
imbalance in the injection profiles below: lots of backend HTTP users, few browser
ones - that imbalance is the whole point (see the warning above).

```mermaid
flowchart TD
    M1(["DemoSimulation.setUp()"])
    M1 -->|"injectClosed\nconstantConcurrentUsers(backendUsers=10)\nduring(durationSec=360)"| B0
    M1 -->|"injectClosed\nconstantConcurrentUsers(frontendUsers=2)\nduring(durationSec=360)"| F0

    subgraph Backend["BackendJourney (HTTP)"]
        direction TB
        B0["API - Browse\nHomePage -> Session -> Products"] --> B1["think time"]
        B1 --> B2["API - Authenticate\nfeed(usersFeeder) -> Login"]
        B2 --> BX["exitHereIfFailed()"]
        BX --> B3{{"repeat 1-5x"}}
        B3 --> B4["feed(productsFeeder) -> think"]
        B4 --> B5["API - Search\nSearch (saves matching products)"]
        B5 --> B6["think time"]
        B6 --> B7["API - Purchase\npick product from search results -> Add to Cart"]
        B7 --> BY["exitHereIfFailed()"]
        BY --> B3
        B3 -->|"loop done"| B8["think time"]
        B8 --> B9["API - Purchase\nCheckout"]
    end

    subgraph Frontend["FrontendJourney (Playwright browser)"]
        direction TB
        F0["UI - Browse\nopen HomePage"] --> F1["think time"]
        F1 --> F2["UI - Authenticate\nfeed(usersFeeder) -> Login"]
        F2 --> FX["exitHereIfFailed()"]
        FX --> F3{{"repeat 1-5x"}}
        F3 --> F4["feed(productsFeeder) -> think"]
        F4 --> F5["UI - Search\nSearch"]
        F5 --> F6["think time"]
        F6 --> F7["UI - Purchase\nAddItem"]
        F7 --> FY["exitHereIfFailed()"]
        FY --> F3
        F3 -->|"loop done"| F8["think time"]
        F8 --> F9["UI - Purchase\nCheckout"]
        F9 --> F10["browserCleanContext + Session reset"]
    end
```