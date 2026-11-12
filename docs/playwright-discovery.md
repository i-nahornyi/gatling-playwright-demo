# Scripting new UI flows with playwright-cli

Before adding a new `EcomPage` locator or `EcomActions` flow, drive the page you're targeting
first and confirm what actually happens - don't guess selectors or assume request ordering.

## 1. Open a browser against the target page

```shell
playwright-cli open https://your-site.example/
```

If `playwright-cli` isn't on your PATH but Node is installed:

```shell
npx @playwright/cli cli open https://your-site.example/
```

## 2. Snapshot the page to get element refs

```shell
playwright-cli snapshot
```

This prints an accessibility-tree view of the page with a `ref` (e.g. `e16`) next to every
interactive element. Use those refs to drive the page:

```shell
playwright-cli click e26
playwright-cli fill e16 "some text"
playwright-cli press Enter
```

## 3. Confirm what network calls an action actually triggers

Don't assume a click or a keystroke calls the API you expect - check:

```shell
playwright-cli requests
playwright-cli request 12   # full detail (status, duration, headers) for request #12
```

Use this to find out whether an input fires a request on every keystroke or only on submit,
whether two calls are genuinely sequential or just happen to be fired together with no real
delay between them, and generally what a given UI action costs on the wire before you decide
where a `pause`/think-time belongs in the journey you're writing.

## 4. Fall back to raw DOM queries when the accessibility snapshot hides something

The accessibility snapshot only lists elements a screen reader would see - an element that's
hidden (e.g. behind a responsive breakpoint) won't show up there at all, so a locator that
looks unique in the snapshot can still throw Playwright's strict-mode "resolved to N elements"
error at runtime. When that happens, query the DOM directly to see what's actually there:

```shell
playwright-cli --raw eval "() => [...document.querySelectorAll('your-selector')].map(el => ({visible: el.offsetParent !== null, cls: el.className}))"
```

## 5. Translate the confirmed flow into code

Once the real selector and the real request sequence are confirmed:

- add the locator/action to `EcomPage` (`src/test/java/com/demo/actions/EcomPage.java`)
- orchestrate Gatling session state and timing around it in `EcomActions`
  (`src/test/java/com/demo/actions/EcomActions.java`)
- wire it into `FrontendJourney` - and the matching `BackendJourney` step, if there's an API
  equivalent - in `src/test/java/com/demo/scenario/`

Then close the browser:

```shell
playwright-cli close
```
