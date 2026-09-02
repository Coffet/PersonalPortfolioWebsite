(() => {
    const collectImages = (root) =>
        Array.from(root.querySelectorAll("img")).map((image) => ({
            src: image.currentSrc || image.getAttribute("src") || "",
            alt: image.getAttribute("alt") || ""
        })).filter((image) => image.src);

    const warmed = new Set();

    const prefetch = (src) => {
        if (!src || warmed.has(src)) {
            return;
        }
        warmed.add(src);
        const image = new Image();
        image.src = src;
    };

    const prefetchAll = (images) => {
        images.forEach((image) => prefetch(image.src));
    };

    const lightbox = {
        root: null,
        imgEl: null,
        prevEl: null,
        nextEl: null,
        countEl: null,
        closeEl: null,
        images: [],
        index: 0,
        isOpen: false
    };

    const wrapIndex = (index, length) => ((index % length) + length) % length;

    const syncLightbox = () => {
        if (!lightbox.images.length) {
            return;
        }

        const length = lightbox.images.length;
        const current = lightbox.images[lightbox.index];
        if (lightbox.imgEl.getAttribute("src") !== current.src) {
            lightbox.imgEl.src = current.src;
        }
        lightbox.imgEl.alt = current.alt || `Image ${lightbox.index + 1} of ${length}`;
        lightbox.countEl.textContent = `${lightbox.index + 1} / ${length}`;
        const showNav = length > 1;
        lightbox.prevEl.hidden = !showNav;
        lightbox.nextEl.hidden = !showNav;
        lightbox.countEl.hidden = !showNav;
        if (showNav) {
            prefetch(lightbox.images[wrapIndex(lightbox.index + 1, length)].src);
            prefetch(lightbox.images[wrapIndex(lightbox.index - 1, length)].src);
        }
    };

    const closeLightbox = () => {
        if (!lightbox.isOpen) {
            return;
        }
        lightbox.isOpen = false;
        lightbox.root.classList.remove("is-open");
        lightbox.root.setAttribute("aria-hidden", "true");
        document.body.style.removeProperty("overflow");
    };

    const openLightbox = (images, index) => {
        if (!images.length) {
            return;
        }
        ensureLightbox();
        lightbox.images = images;
        lightbox.index = wrapIndex(index, images.length);
        syncLightbox();
        lightbox.isOpen = true;
        lightbox.root.classList.add("is-open");
        lightbox.root.setAttribute("aria-hidden", "false");
        document.body.style.overflow = "hidden";
        window.requestAnimationFrame(() => lightbox.closeEl?.focus());
    };

    const stepLightbox = (delta) => {
        if (!lightbox.isOpen || lightbox.images.length < 2) {
            return;
        }
        lightbox.index = wrapIndex(lightbox.index + delta, lightbox.images.length);
        syncLightbox();
    };

    const makeNav = (direction, label) => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = `gallery-viewer__nav gallery-viewer__nav--${direction}`;
        button.setAttribute("aria-label", label);
        button.innerHTML = `<span aria-hidden="true">${direction === "prev" ? "‹" : "›"}</span>`;
        return button;
    };

    const ensureLightbox = () => {
        if (lightbox.root) {
            return;
        }

        const root = document.createElement("div");
        root.className = "gallery-viewer";
        root.setAttribute("aria-hidden", "true");

        const backdrop = document.createElement("div");
        backdrop.className = "gallery-viewer__backdrop";

        const dialog = document.createElement("div");
        dialog.className = "gallery-viewer__dialog";
        dialog.setAttribute("role", "dialog");
        dialog.setAttribute("aria-modal", "true");
        dialog.setAttribute("aria-label", "Image viewer");

        const img = document.createElement("img");
        img.className = "gallery-viewer__img";
        img.fetchPriority = "high";
        img.alt = "";

        const prev = makeNav("prev", "Previous image");
        const next = makeNav("next", "Next image");

        const count = document.createElement("div");
        count.className = "gallery-viewer__count";

        const close = document.createElement("button");
        close.type = "button";
        close.className = "gallery-viewer__close";
        close.setAttribute("aria-label", "Close image viewer");
        close.innerHTML = '<svg viewBox="0 0 16 16" aria-hidden="true"><path d="M4 4L12 12M12 4L4 12"/></svg>';

        prev.addEventListener("click", () => stepLightbox(-1));
        next.addEventListener("click", () => stepLightbox(1));
        close.addEventListener("click", closeLightbox);
        backdrop.addEventListener("click", closeLightbox);

        dialog.append(img, prev, next, count, close);
        root.append(backdrop, dialog);
        document.body.appendChild(root);

        lightbox.root = root;
        lightbox.imgEl = img;
        lightbox.prevEl = prev;
        lightbox.nextEl = next;
        lightbox.countEl = count;
        lightbox.closeEl = close;
    };

    const enhanceStage = (stage) => {
        const frames = Array.from(stage.querySelectorAll("img"));
        const images = collectImages(stage);
        if (!images.length) {
            return;
        }

        frames.forEach((img, frameIndex) => {
            img.classList.add("gallery-stage__img");
            img.hidden = frameIndex !== 0;
        });

        const prev = makeNav("prev", "Previous image");
        const next = makeNav("next", "Next image");

        const count = document.createElement("div");
        count.className = "gallery-stage__count";

        let index = 0;

        const sync = () => {
            frames.forEach((img, frameIndex) => {
                img.hidden = frameIndex !== index;
            });
            count.textContent = `${index + 1} / ${images.length}`;
            const showNav = images.length > 1;
            prev.hidden = !showNav;
            next.hidden = !showNav;
            count.hidden = !showNav;
            if (showNav) {
                prefetch(images[wrapIndex(index + 1, images.length)].src);
            }
        };

        prev.addEventListener("click", (event) => {
            event.stopPropagation();
            index = wrapIndex(index - 1, images.length);
            sync();
        });
        next.addEventListener("click", (event) => {
            event.stopPropagation();
            index = wrapIndex(index + 1, images.length);
            sync();
        });
        frames.forEach((img) => {
            img.addEventListener("click", () => openLightbox(images, index));
        });

        stage.append(prev, next, count);
        sync();
        prefetchAll(images);
    };

    document.querySelectorAll("[data-gallery-stage]").forEach(enhanceStage);

    const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)");
    const panItems = [];
    const PAN_PERIOD = reduceMotion.matches ? 72000 : 56000;
    const HOVER_IDLE_MS = 1400;
    let panTicker = 0;

    const clamp01 = (value) => Math.min(1, Math.max(0, value));

    const visibleImage = (frame) =>
        frame.querySelector("img:not([hidden])") || frame.querySelector("img");

    const applyPan = (frame, image, x, y) => {
        const overflowX = Math.max(0, image.offsetWidth - frame.clientWidth);
        const overflowY = Math.max(0, image.offsetHeight - frame.clientHeight);
        image.style.setProperty("--pan-x", `${(-overflowX * x)}px`);
        image.style.setProperty("--pan-y", `${(-overflowY * y)}px`);
    };

    // Lissajous path: starts at center (t = 0), then drifts smoothly.
    const autoPanTarget = (elapsed) => {
        const t = (elapsed / PAN_PERIOD) * Math.PI * 2;
        return [
            clamp01(0.5 + (0.5 * Math.sin(t))),
            clamp01(0.5 + (0.5 * Math.sin(t * 0.5)))
        ];
    };

    const HOME_PAN = 0.5;
    const RETURN_SPEED = 0.1;

    const tickPan = (now) => {
        if (!document.hidden) {
            panItems.forEach((item) => {
                if (!item.active) {
                    return;
                }
                if (item.startAt == null) {
                    item.startAt = now + (item.stagger || 0);
                    item.x = HOME_PAN;
                    item.y = HOME_PAN;
                }
                if (item.hovering) {
                    applyPan(item.frame, item.image, item.x, item.y);
                    return;
                }
                if (item.returning) {
                    item.x += (HOME_PAN - item.x) * RETURN_SPEED;
                    item.y += (HOME_PAN - item.y) * RETURN_SPEED;
                    if (Math.abs(item.x - HOME_PAN) < 0.002 && Math.abs(item.y - HOME_PAN) < 0.002) {
                        item.x = HOME_PAN;
                        item.y = HOME_PAN;
                        item.returning = false;
                        item.startAt = now + 700;
                        item.restUntil = now + 700;
                    }
                } else if (now >= (item.restUntil || 0)) {
                    const [targetX, targetY] = autoPanTarget(Math.max(0, now - item.startAt));
                    item.x = targetX;
                    item.y = targetY;
                }
                applyPan(item.frame, item.image, item.x, item.y);
            });
        }
        panTicker = window.requestAnimationFrame(tickPan);
    };

    const startPanTicker = () => {
        if (!panTicker && panItems.length) {
            panTicker = window.requestAnimationFrame(tickPan);
        }
    };

    const enhanceImagePan = (frame, index) => {
        const image = visibleImage(frame);
        if (!image) {
            return;
        }

        const item = {
            frame,
            image,
            startAt: null,
            stagger: index * 450,
            hovering: false,
            returning: false,
            restUntil: 0,
            active: false,
            idleTimer: 0,
            x: HOME_PAN,
            y: HOME_PAN
        };
        panItems.push(item);

        const clearHoverIdle = () => {
            if (item.idleTimer) {
                window.clearTimeout(item.idleTimer);
                item.idleTimer = 0;
            }
        };

        const releaseHoverForAutoPan = () => {
            item.hovering = false;
            item.returning = false;
            item.idleTimer = 0;
        };

        const setHoverPan = (event) => {
            const rect = frame.getBoundingClientRect();
            if (rect.width <= 0 || rect.height <= 0) {
                return;
            }
            item.hovering = true;
            item.returning = false;
            item.x = clamp01((event.clientX - rect.left) / rect.width);
            item.y = clamp01((event.clientY - rect.top) / rect.height);
            applyPan(frame, image, item.x, item.y);
            clearHoverIdle();
            item.idleTimer = window.setTimeout(releaseHoverForAutoPan, HOVER_IDLE_MS);
        };

        frame.addEventListener("pointermove", setHoverPan);
        frame.addEventListener("pointerleave", () => {
            clearHoverIdle();
            item.hovering = false;
            item.returning = true;
        });

        const observer = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                item.active = entry.isIntersecting;
            });
        }, { threshold: 0.05 });
        observer.observe(frame);

        const readyPan = () => {
            item.active = true;
            item.x = HOME_PAN;
            item.y = HOME_PAN;
            applyPan(frame, image, HOME_PAN, HOME_PAN);
            startPanTicker();
        };

        if (image.complete && image.naturalWidth > 0) {
            readyPan();
        } else {
            image.addEventListener("load", readyPan, { once: true });
        }
    };

    document.querySelectorAll("[data-gallery-viewer]").forEach((viewer, index) => {
        viewer.addEventListener("pointerenter", () => prefetchAll(collectImages(viewer)), { once: true });
        viewer.addEventListener("click", (event) => {
            event.preventDefault();
            const images = collectImages(viewer);
            if (!images.length) {
                return;
            }
            const clicked = event.target.closest("img");
            const imageIndex = clicked ? Math.max(0, Array.from(viewer.querySelectorAll("img")).indexOf(clicked)) : 0;
            openLightbox(images, imageIndex);
        });
        enhanceImagePan(viewer, index);
    });

    startPanTicker();

    document.addEventListener("visibilitychange", () => {
        if (document.hidden && panTicker) {
            window.cancelAnimationFrame(panTicker);
            panTicker = 0;
            return;
        }
        startPanTicker();
    });

    document.addEventListener("keydown", (event) => {
        if (!lightbox.isOpen) {
            return;
        }
        if (event.key === "Escape") {
            closeLightbox();
        }
        if (event.key === "ArrowLeft") {
            stepLightbox(-1);
        }
        if (event.key === "ArrowRight") {
            stepLightbox(1);
        }
    });
})();
