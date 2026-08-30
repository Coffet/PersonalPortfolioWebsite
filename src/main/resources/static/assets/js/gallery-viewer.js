(() => {
    const collectImages = (root) =>
        Array.from(root.querySelectorAll("img")).map((image) => ({
            src: image.getAttribute("src") || "",
            alt: image.getAttribute("alt") || ""
        })).filter((image) => image.src);

    const lightbox = {
        root: null,
        imgEl: null,
        prevEl: null,
        nextEl: null,
        countEl: null,
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
        lightbox.imgEl.src = current.src;
        lightbox.imgEl.alt = current.alt || `Image ${lightbox.index + 1} of ${length}`;
        lightbox.countEl.textContent = `${lightbox.index + 1} / ${length}`;
        const showNav = length > 1;
        lightbox.prevEl.hidden = !showNav;
        lightbox.nextEl.hidden = !showNav;
        lightbox.countEl.hidden = !showNav;
    };

    const closeLightbox = () => {
        if (!lightbox.isOpen) {
            return;
        }
        lightbox.isOpen = false;
        lightbox.root.classList.remove("is-open");
        lightbox.root.setAttribute("aria-hidden", "true");
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
        window.requestAnimationFrame(() => lightbox.root.querySelector(".work-lightbox__close")?.focus());
    };

    const stepLightbox = (delta) => {
        if (!lightbox.isOpen || lightbox.images.length < 2) {
            return;
        }
        lightbox.index = wrapIndex(lightbox.index + delta, lightbox.images.length);
        syncLightbox();
    };

    const ensureLightbox = () => {
        if (lightbox.root) {
            return;
        }

        const root = document.createElement("div");
        root.className = "work-lightbox";
        root.setAttribute("aria-hidden", "true");

        const backdrop = document.createElement("div");
        backdrop.className = "work-lightbox__backdrop";

        const dialog = document.createElement("div");
        dialog.className = "work-lightbox__dialog";
        dialog.setAttribute("role", "dialog");
        dialog.setAttribute("aria-modal", "true");
        dialog.setAttribute("aria-label", "Image viewer");

        const img = document.createElement("img");
        img.className = "work-lightbox__img";
        img.decoding = "async";
        img.alt = "";

        const prev = document.createElement("button");
        prev.type = "button";
        prev.className = "work-lightbox__nav work-lightbox__nav--prev";
        prev.setAttribute("aria-label", "Previous image");
        prev.innerHTML = '<span aria-hidden="true">‹</span>';

        const next = document.createElement("button");
        next.type = "button";
        next.className = "work-lightbox__nav work-lightbox__nav--next";
        next.setAttribute("aria-label", "Next image");
        next.innerHTML = '<span aria-hidden="true">›</span>';

        const count = document.createElement("div");
        count.className = "work-lightbox__count";

        const close = document.createElement("button");
        close.type = "button";
        close.className = "work-lightbox__close";
        close.setAttribute("aria-label", "Close image viewer");
        close.innerHTML = '<span aria-hidden="true">×</span>';

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
    };

    const enhanceStage = (stage) => {
        const images = collectImages(stage);
        if (!images.length) {
            return;
        }

        stage.replaceChildren();
        stage.classList.add("has-image");

        const img = document.createElement("img");
        img.className = "work-modal__media-img";
        img.decoding = "async";
        img.alt = images[0].alt;
        img.src = images[0].src;
        img.style.cursor = "zoom-in";

        const prev = document.createElement("button");
        prev.type = "button";
        prev.className = "work-modal__nav work-modal__nav--prev";
        prev.setAttribute("aria-label", "Previous image");
        prev.innerHTML = '<span aria-hidden="true">‹</span>';

        const next = document.createElement("button");
        next.type = "button";
        next.className = "work-modal__nav work-modal__nav--next";
        next.setAttribute("aria-label", "Next image");
        next.innerHTML = '<span aria-hidden="true">›</span>';

        const count = document.createElement("div");
        count.className = "work-modal__count";

        let index = 0;

        const sync = () => {
            const current = images[index];
            img.src = current.src;
            img.alt = current.alt || `Image ${index + 1} of ${images.length}`;
            count.textContent = `${index + 1} / ${images.length}`;
            const showNav = images.length > 1;
            prev.hidden = !showNav;
            next.hidden = !showNav;
            count.hidden = !showNav;
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
        img.addEventListener("click", () => openLightbox(images, index));

        stage.append(img, prev, next, count);
        sync();
    };

    document.querySelectorAll("[data-gallery-stage]").forEach(enhanceStage);

    document.querySelectorAll("[data-gallery-viewer]").forEach((viewer) => {
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
