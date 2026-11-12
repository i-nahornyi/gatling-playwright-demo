(function () {

    // Ready once the product grid has rendered a full page of results (this
    // storefront shows 8 products per page). Only valid for pages that have a
    // #products-section - pointing this at a page without one (e.g. extending
    // feeders/page.csv) will time out here on every iteration.
    const container = document.querySelector("#products-section");
    if (!container || container.children.length < 8) return false;

    // Scroll to the bottom like a real visitor would, then confirm the footer
    // and its images actually finished loading - catches pages that render
    // fine above the fold but break, or never finish loading images, further
    // down the page.
    window.scrollTo(0, document.body.scrollHeight);

    const footer = document.querySelector("footer");
    if (!footer) return false;

    for (const img of footer.querySelectorAll("img")) {
        if (!img.complete || img.naturalWidth === 0) return false;
    }

    return true;
})();
