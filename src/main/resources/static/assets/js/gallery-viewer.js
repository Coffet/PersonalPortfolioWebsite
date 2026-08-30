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

    document.querySelectorAll("[data-gallery-viewer]").forEach((viewer) => {
        viewer.addEventListener("pointerenter", () => prefetchAll(collectImages(viewer)), { once: true });
        viewer.addEventListener("click", (event) => {
            event.preventDefault();
            const images = collectImages(viewer);
            if (!images.length) {
                return;
            }
            const clicked = event.target.closest("img");
            const index = clicked ? Math.max(0, Array.from(viewer.querySelectorAll("img")).indexOf(clicked)) : 0;
            openLightbox(images, index);
        });
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
