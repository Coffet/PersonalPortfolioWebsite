(() => {
    const reduced = window.matchMedia("(prefers-reduced-motion: reduce)");

    document.querySelectorAll(".status-link--cycle").forEach((link) => {
        if (!reduced.matches) {
            return;
        }

        link.classList.add("is-js");
        const hold = 3800;
        const tick = () => {
            link.classList.toggle("is-here");
            window.setTimeout(tick, hold);
        };
        window.setTimeout(tick, hold);
    });
})();
