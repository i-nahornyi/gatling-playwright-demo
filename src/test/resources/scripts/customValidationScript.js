(function () {

    window.__gatling_playwright = window.__gatling_playwright || {};
    const state = window.__gatling_playwright;


    const container = document.querySelector("#products-section");
    if (!container) return false;

    const items = container.children;
    const count = items.length;
    if (count < 8 ) return false;

    return true;
})();