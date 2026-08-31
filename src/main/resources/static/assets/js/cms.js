(() => {
    const imageName = /\.(png|jpe?g|gif|webp|avif|bmp|svg)$/i;

    const isImage = (file) => {
        if (!file) {
            return false;
        }
        if (file.type && file.type.startsWith("image/")) {
            return true;
        }
        return imageName.test(file.name || "");
    };

    document.querySelectorAll("[data-file-field]").forEach((field) => {
        const input = field.querySelector("input[type='file']");
        const preview = field.querySelector("[data-file-preview]");
        const caption = field.querySelector("[data-file-caption]");
        if (!input) {
            return;
        }

        const multiple = input.hasAttribute("multiple");
        let staged = [];
        let syncing = false;

        const fileKey = (file) => file.name + ":" + file.size + ":" + file.lastModified;

        const mergeFiles = (incoming) => {
            const images = incoming.filter(isImage);
            if (!multiple) {
                staged = images.slice(0, 1);
                return;
            }

            const seen = new Set(staged.map(fileKey));
            images.forEach((file) => {
                const key = fileKey(file);
                if (seen.has(key)) {
                    return;
                }
                seen.add(key);
                staged.push(file);
            });
        };

        const syncInput = () => {
            const transfer = new DataTransfer();
            staged.forEach((file) => transfer.items.add(file));
            syncing = true;
            input.files = transfer.files;
            syncing = false;
            render();
        };

        const render = () => {
            field.classList.toggle("has-files", staged.length > 0);

            if (caption) {
                if (staged.length === 0) {
                    caption.textContent = field.dataset.emptyCaption || "Choose images";
                } else if (multiple) {
                    caption.textContent = staged.length === 1
                        ? "1 image ready — click it to preview, or add more"
                        : staged.length + " images ready — click to preview, or add more";
                } else {
                    caption.textContent = "Click the image to preview, or choose a replacement";
                }
            }

            if (!preview) {
                return;
            }

            preview.querySelectorAll("img").forEach((image) => URL.revokeObjectURL(image.src));
            preview.replaceChildren();
            staged.forEach((file, index) => {
                const item = document.createElement("figure");
                item.className = "drop-preview__item";

                const image = document.createElement("img");
                image.alt = file.name;
                image.src = URL.createObjectURL(file);
                item.appendChild(image);

                image.title = "Click to preview";
                image.addEventListener("click", (event) => {
                    event.preventDefault();
                    event.stopPropagation();
                    field.dispatchEvent(new CustomEvent("cms:preview", {
                        bubbles: true,
                        detail: { src: image.src, alt: file.name }
                    }));
                });

                if (multiple) {
                    const remove = document.createElement("button");
                    remove.type = "button";
                    remove.className = "drop-preview__remove";
                    remove.setAttribute("aria-label", "Remove " + file.name);
                    remove.textContent = "×";
                    const stop = (event) => {
                        event.preventDefault();
                        event.stopPropagation();
                    };
                    remove.addEventListener("pointerdown", stop);
                    remove.addEventListener("click", (event) => {
                        stop(event);
                        staged.splice(index, 1);
                        syncInput();
                    });
                    item.appendChild(remove);
                }

                preview.appendChild(item);
            });
        };

        input.addEventListener("change", () => {
            if (syncing) {
                return;
            }
            mergeFiles(Array.from(input.files || []));
            syncInput();
        });

        field.addEventListener("dragover", (event) => {
            event.preventDefault();
            field.classList.add("is-dragover");
        });

        field.addEventListener("dragleave", (event) => {
            if (!field.contains(event.relatedTarget)) {
                field.classList.remove("is-dragover");
            }
        });

        field.addEventListener("drop", (event) => {
            event.preventDefault();
            field.classList.remove("is-dragover");
            const dropped = Array.from(event.dataTransfer?.files || []);
            if (!dropped.length) {
                return;
            }
            mergeFiles(dropped);
            syncInput();
        });
    });
})();

(() => {
    const account = document.querySelector(".desk-account");
    if (!account) {
        return;
    }

    document.addEventListener("click", (event) => {
        if (account.open && !account.contains(event.target)) {
            account.removeAttribute("open");
        }
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape" && account.open) {
            account.removeAttribute("open");
        }
    });
})();

(() => {
    const region = document.querySelector("[data-toast-region]");
    const toast = region?.querySelector("[data-toast]");
    if (!region || !toast) {
        return;
    }

    let hideTimer = 0;
    let hiding = false;
    let removed = false;

    const finish = () => {
        if (removed) {
            return;
        }
        removed = true;
        region.remove();
    };

    const hide = () => {
        if (hiding) {
            return;
        }
        hiding = true;
        window.clearTimeout(hideTimer);
        region.classList.add("is-hidden");
        region.addEventListener("transitionend", (event) => {
            if (event.target === region && event.propertyName === "transform") {
                finish();
            }
        });
        window.setTimeout(finish, 650);
    };

    const show = () => {
        region.getBoundingClientRect();
        region.classList.remove("is-hidden");
    };

    const arm = () => {
        window.clearTimeout(hideTimer);
        hideTimer = window.setTimeout(hide, 4200);
    };

    window.requestAnimationFrame(() => {
        window.requestAnimationFrame(() => {
            show();
            arm();
        });
    });

    toast.addEventListener("mouseenter", () => window.clearTimeout(hideTimer));
    toast.addEventListener("mouseleave", arm);
    toast.addEventListener("focusin", () => window.clearTimeout(hideTimer));
    toast.addEventListener("focusout", arm);
    toast.querySelector("[data-toast-dismiss]")?.addEventListener("click", hide);

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            hide();
        }
    });
})();

(() => {
    const normalizeSrc = (src) => {
        if (!src) {
            return "";
        }
        if (src.startsWith("blob:") || src.startsWith("data:")) {
            return src;
        }
        try {
            const url = new URL(src, window.location.href);
            return url.pathname + url.search;
        } catch {
            return src;
        }
    };

    const collectImages = (scope, preferredSrc) => {
        const root = scope || document;
        const nodes = Array.from(root.querySelectorAll(
            ".drop-preview img, .current-cover img, .media-card img"
        ));
        const images = [];
        const seen = new Set();

        nodes.forEach((node) => {
            if (node.closest(".cms-lightbox")) {
                return;
            }
            const src = node.currentSrc || node.getAttribute("src") || "";
            const key = normalizeSrc(src);
            if (!key || seen.has(key)) {
                return;
            }
            seen.add(key);
            images.push({
                src,
                alt: node.getAttribute("alt") || "Image preview"
            });
        });

        const preferredKey = normalizeSrc(preferredSrc);
        if (preferredKey && !seen.has(preferredKey)) {
            images.unshift({ src: preferredSrc, alt: "Image preview" });
        }

        return images;
    };

    const wrapIndex = (index, length) => ((index % length) + length) % length;

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

    const sync = () => {
        if (!lightbox.images.length) {
            return;
        }
        const current = lightbox.images[lightbox.index];
        lightbox.imgEl.src = current.src;
        lightbox.imgEl.alt = current.alt || `Image ${lightbox.index + 1} of ${lightbox.images.length}`;
        lightbox.countEl.textContent = `${lightbox.index + 1} / ${lightbox.images.length}`;
        const showNav = lightbox.images.length > 1;
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
        document.body.style.removeProperty("overflow");
    };

    const openLightbox = (images, index) => {
        if (!images.length) {
            return;
        }
        ensureLightbox();
        lightbox.images = images;
        lightbox.index = wrapIndex(index, images.length);
        sync();
        lightbox.isOpen = true;
        lightbox.root.classList.add("is-open");
        lightbox.root.setAttribute("aria-hidden", "false");
        document.body.style.overflow = "hidden";
        window.requestAnimationFrame(() => lightbox.closeEl?.focus());
    };

    const step = (delta) => {
        if (!lightbox.isOpen || lightbox.images.length < 2) {
            return;
        }
        lightbox.index = wrapIndex(lightbox.index + delta, lightbox.images.length);
        sync();
    };

    const ensureLightbox = () => {
        if (lightbox.root) {
            return;
        }

        const root = document.createElement("div");
        root.className = "cms-lightbox";
        root.setAttribute("aria-hidden", "true");

        const backdrop = document.createElement("div");
        backdrop.className = "cms-lightbox__backdrop";

        const dialog = document.createElement("div");
        dialog.className = "cms-lightbox__dialog";
        dialog.setAttribute("role", "dialog");
        dialog.setAttribute("aria-modal", "true");
        dialog.setAttribute("aria-label", "Image preview");

        const img = document.createElement("img");
        img.className = "cms-lightbox__img";
        img.alt = "";

        const prev = document.createElement("button");
        prev.type = "button";
        prev.className = "cms-lightbox__nav cms-lightbox__nav--prev";
        prev.setAttribute("aria-label", "Previous image");
        prev.innerHTML = '<span aria-hidden="true">‹</span>';

        const next = document.createElement("button");
        next.type = "button";
        next.className = "cms-lightbox__nav cms-lightbox__nav--next";
        next.setAttribute("aria-label", "Next image");
        next.innerHTML = '<span aria-hidden="true">›</span>';

        const count = document.createElement("div");
        count.className = "cms-lightbox__count";

        const close = document.createElement("button");
        close.type = "button";
        close.className = "cms-lightbox__close";
        close.setAttribute("aria-label", "Close image preview");
        close.innerHTML = '<span aria-hidden="true">×</span>';

        prev.addEventListener("click", () => step(-1));
        next.addEventListener("click", () => step(1));
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

    const openFrom = (target, preferredSrc) => {
        const scope = target.closest(".inspector, .composer__stage, .upload-field, form") || document;
        const images = collectImages(scope, preferredSrc);
        const preferredKey = normalizeSrc(preferredSrc);
        const index = Math.max(0, images.findIndex((image) => normalizeSrc(image.src) === preferredKey));
        openLightbox(images, index);
    };

    document.addEventListener("cms:preview", (event) => {
        const src = event.detail?.src;
        if (!src) {
            return;
        }
        openFrom(event.target, src);
    });

    document.addEventListener("click", (event) => {
        if (event.target.closest(".drop-preview__remove")) {
            return;
        }
        const trigger = event.target.closest(".media-preview-trigger, .current-cover img, .media-card img");
        if (!trigger) {
            return;
        }
        const src = trigger.getAttribute("data-preview-src")
            || trigger.querySelector?.("img")?.currentSrc
            || trigger.currentSrc
            || trigger.getAttribute("src");
        if (!src) {
            return;
        }
        event.preventDefault();
        openFrom(trigger, src);
    });

    document.addEventListener("keydown", (event) => {
        if (!lightbox.isOpen) {
            return;
        }
        if (event.key === "Escape") {
            event.preventDefault();
            closeLightbox();
        }
        if (event.key === "ArrowLeft") {
            step(-1);
        }
        if (event.key === "ArrowRight") {
            step(1);
        }
    });
})();
