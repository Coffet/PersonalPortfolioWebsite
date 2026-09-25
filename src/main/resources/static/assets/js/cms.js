/* =========================================================================
   cms.js — Coft desk behaviour
   1. file staging + drag & drop
   2. menus, rail, mobile nav
   3. toast
   4. lightbox (top-layer dialog)
   5. content preview modal (public-site look, cards + detail)
   6. confirm modal
   7. list search + status filters
   8. command palette (Ctrl/Cmd + K)
   9. WYSIWYG editor for blog notes
  10. small niceties (autogrow, counters, dirty guard)

   Everything is progressive: if a page lacks the markup, the block is inert.
   No inline scripts anywhere (the site CSP forbids them).
   ========================================================================= */
(() => {
    "use strict";

    const ctxMeta = document.querySelector('meta[name="cms-ctx"]');
    const CTX = ctxMeta ? ctxMeta.getAttribute("content") || "" : "";

    const on = (target, type, handler, options) => target && target.addEventListener(type, handler, options);
    const qs = (selector, scope) => (scope || document).querySelector(selector);
    const qsa = (selector, scope) => Array.from((scope || document).querySelectorAll(selector));

    const escapeHtml = (value) =>
        String(value == null ? "" : value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");

    const withCtx = (path) => {
        if (!path) {
            return "";
        }
        if (/^(https?:|data:|blob:)/.test(path)) {
            return path;
        }
        return CTX + (path.startsWith("/") ? path : "/" + path);
    };

    const dialogSupported = typeof HTMLDialogElement === "function";
    let bodyLockCount = 0;

    const lockBody = () => {
        bodyLockCount += 1;
        document.body.classList.add("is-locked");
    };

    const unlockBody = () => {
        bodyLockCount = Math.max(0, bodyLockCount - 1);
        if (bodyLockCount === 0) {
            document.body.classList.remove("is-locked");
        }
    };

    const openDialog = (dialog) => {
        if (!dialog) {
            return;
        }
        if (dialog.open) {
            return;
        }
        if (dialogSupported && typeof dialog.showModal === "function") {
            dialog.showModal();
            lockBody();
        } else {
            dialog.setAttribute("open", "");
            lockBody();
        }
    };

    const closeDialog = (dialog) => {
        if (!dialog || !dialog.open) {
            return;
        }
        if (dialogSupported && typeof dialog.close === "function") {
            dialog.close();
        } else {
            dialog.removeAttribute("open");
        }
        unlockBody();
    };

    /* =====================================================================
       1. file staging + drag & drop
       ===================================================================== */
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

        qsa("[data-file-field]").forEach((field) => {
            const input = qs("input[type='file']", field);
            const preview = qs("[data-file-preview]", field);
            const caption = qs("[data-file-caption]", field);
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
                try {
                    const transfer = new DataTransfer();
                    staged.forEach((file) => transfer.items.add(file));
                    syncing = true;
                    input.files = transfer.files;
                    syncing = false;
                } catch (error) {
                    syncing = false;
                }
                render();
                document.dispatchEvent(new CustomEvent("cms:files", { detail: { field, input } }));
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

                qsa("img", preview).forEach((image) => URL.revokeObjectURL(image.src));
                preview.replaceChildren();

                staged.forEach((file, index) => {
                    const item = document.createElement("figure");
                    item.className = "drop-preview__item";

                    const trigger = document.createElement("button");
                    trigger.type = "button";
                    trigger.className = "media-preview-trigger";
                    trigger.setAttribute("aria-label", "Preview " + file.name);

                    const image = document.createElement("img");
                    image.alt = file.name;
                    image.src = URL.createObjectURL(file);
                    trigger.appendChild(image);
                    item.appendChild(trigger);

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

            on(input, "change", () => {
                if (syncing) {
                    return;
                }
                mergeFiles(Array.from(input.files || []));
                syncInput();
            });

            on(field, "dragover", (event) => {
                event.preventDefault();
                field.classList.add("is-dragover");
            });

            on(field, "dragleave", (event) => {
                if (!field.contains(event.relatedTarget)) {
                    field.classList.remove("is-dragover");
                }
            });

            on(field, "drop", (event) => {
                event.preventDefault();
                field.classList.remove("is-dragover");
                const dropped = Array.from((event.dataTransfer && event.dataTransfer.files) || []);
                if (!dropped.length) {
                    return;
                }
                mergeFiles(dropped);
                syncInput();
            });
        });
    })();

    /* =====================================================================
       2. menus, rail collapse, mobile nav
       ===================================================================== */
    (() => {
        const app = qs(".app");
        const storeKey = "cms:rail";

        if (app && window.matchMedia("(min-width: 768px)").matches) {
            try {
                if (window.localStorage.getItem(storeKey) === "collapsed") {
                    app.classList.add("is-collapsed");
                }
            } catch (error) {
                /* storage can be unavailable; ignore */
            }
        }

        const railToggle = qs("[data-rail-toggle]");

        const syncToggle = () => {
            if (!railToggle || !app) {
                return;
            }
            const mini = window.matchMedia("(max-width: 767px)").matches
                ? app.classList.contains("is-nav-open")
                : !app.classList.contains("is-collapsed");
            railToggle.setAttribute("aria-expanded", String(mini));
        };

        on(railToggle, "click", () => {
            if (!app) {
                return;
            }
            if (window.matchMedia("(max-width: 767px)").matches) {
                app.classList.toggle("is-nav-open");
            } else {
                app.classList.toggle("is-collapsed");
                try {
                    window.localStorage.setItem(storeKey, app.classList.contains("is-collapsed") ? "collapsed" : "full");
                } catch (error) {
                    /* ignore */
                }
            }
            syncToggle();
        });

        on(qs(".rail-scrim"), "click", () => {
            app && app.classList.remove("is-nav-open");
            syncToggle();
        });

        const closeMenus = (except) => {
            qsa(".menu[open]").forEach((menu) => {
                if (menu !== except) {
                    menu.removeAttribute("open");
                }
            });
        };

        document.addEventListener("click", (event) => {
            const menu = event.target.closest(".menu");
            closeMenus(menu);
            if (app && app.classList.contains("is-nav-open") && !event.target.closest(".rail") && !event.target.closest("[data-rail-toggle]")) {
                app.classList.remove("is-nav-open");
                syncToggle();
            }
        });

        document.addEventListener("keydown", (event) => {
            if (event.key === "Escape") {
                closeMenus();
                if (app && app.classList.contains("is-nav-open")) {
                    app.classList.remove("is-nav-open");
                    syncToggle();
                }
            }
        });

        syncToggle();
    })();

    /* =====================================================================
       3. toast
       ===================================================================== */
    (() => {
        const region = qs("[data-toast-region]");
        const toast = qs("[data-toast]", region);
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
            on(region, "transitionend", (event) => {
                if (event.target === region && event.propertyName === "transform") {
                    finish();
                }
            });
            window.setTimeout(finish, 700);
        };

        const arm = () => {
            window.clearTimeout(hideTimer);
            hideTimer = window.setTimeout(hide, 4600);
        };

        window.requestAnimationFrame(() => {
            window.requestAnimationFrame(() => {
                region.classList.remove("is-hidden");
                arm();
            });
        });

        on(toast, "mouseenter", () => window.clearTimeout(hideTimer));
        on(toast, "mouseleave", arm);
        on(toast, "focusin", () => window.clearTimeout(hideTimer));
        on(toast, "focusout", arm);
        on(qs("[data-toast-dismiss]", toast), "click", hide);
        document.addEventListener("keydown", (event) => {
            if (event.key === "Escape") {
                hide();
            }
        });
    })();

    /* =====================================================================
       4. lightbox (dialog based so it sits above other dialogs)
       ===================================================================== */
    (() => {
        const normalizeSrc = (src) => {
            if (!src) {
                return "";
            }
            if (/^(blob:|data:)/.test(src)) {
                return src;
            }
            try {
                const url = new URL(src, window.location.href);
                return url.pathname + url.search;
            } catch (error) {
                return src;
            }
        };

        const IMAGE_SELECTOR = [
            ".drop-preview img",
            ".current-cover img",
            ".media-card img",
            ".entry__thumb img",
            ".cms-preview .gallery-stage__img:not([hidden])",
            ".cms-preview .detail-hero img",
            ".cms-preview .blog-card__media img",
            ".cms-preview .gallery-tile__frame img"
        ].join(",");

        const collectImages = (scope, preferredSrc) => {
            const root = scope || document;
            const images = [];
            const seen = new Set();

            qsa(IMAGE_SELECTOR, root).forEach((node) => {
                const src = node.currentSrc || node.getAttribute("src") || "";
                const key = normalizeSrc(src);
                if (!key || seen.has(key)) {
                    return;
                }
                seen.add(key);
                images.push({ src, alt: node.getAttribute("alt") || "Image preview" });
            });

            const preferredKey = normalizeSrc(preferredSrc);
            if (preferredKey && !seen.has(preferredKey)) {
                images.unshift({ src: preferredSrc, alt: "Image preview" });
            }

            return images;
        };

        const wrapIndex = (index, length) => ((index % length) + length) % length;

        let root = null;
        let imgEl = null;
        let prevEl = null;
        let nextEl = null;
        let countEl = null;
        let images = [];
        let index = 0;

        const sync = () => {
            if (!images.length) {
                return;
            }
            const current = images[index];
            imgEl.src = current.src;
            imgEl.alt = current.alt || "Image " + (index + 1) + " of " + images.length;
            countEl.textContent = (index + 1) + " / " + images.length;
            const many = images.length > 1;
            prevEl.hidden = !many;
            nextEl.hidden = !many;
            countEl.hidden = !many;
        };

        const ensure = () => {
            if (root) {
                return;
            }

            root = document.createElement("dialog");
            root.className = "cms-lightbox";
            root.setAttribute("aria-label", "Image preview");

            const dialog = document.createElement("div");
            dialog.className = "cms-lightbox__dialog";

            imgEl = document.createElement("img");
            imgEl.className = "cms-lightbox__img";
            imgEl.alt = "";

            prevEl = document.createElement("button");
            prevEl.type = "button";
            prevEl.className = "cms-lightbox__nav cms-lightbox__nav--prev";
            prevEl.setAttribute("aria-label", "Previous image");
            prevEl.innerHTML = '<span aria-hidden="true">‹</span>';

            nextEl = document.createElement("button");
            nextEl.type = "button";
            nextEl.className = "cms-lightbox__nav cms-lightbox__nav--next";
            nextEl.setAttribute("aria-label", "Next image");
            nextEl.innerHTML = '<span aria-hidden="true">›</span>';

            countEl = document.createElement("div");
            countEl.className = "cms-lightbox__count";

            const close = document.createElement("button");
            close.type = "button";
            close.className = "cms-lightbox__close";
            close.setAttribute("aria-label", "Close image preview");
            close.innerHTML = '<span aria-hidden="true">×</span>';

            on(prevEl, "click", () => step(-1));
            on(nextEl, "click", () => step(1));
            on(close, "click", () => closeDialog(root));
            on(root, "click", (event) => {
                if (event.target === root || event.target === dialog) {
                    closeDialog(root);
                }
            });
            on(root, "close", () => {
                imgEl.removeAttribute("src");
                unlockBody();
            });

            dialog.append(imgEl, prevEl, nextEl, countEl, close);
            root.appendChild(dialog);
            document.body.appendChild(root);
        };

        const step = (delta) => {
            if (!root || root.open === false || images.length < 2) {
                return;
            }
            index = wrapIndex(index + delta, images.length);
            sync();
        };

        const openAt = (list, start) => {
            if (!list.length) {
                return;
            }
            ensure();
            images = list;
            index = wrapIndex(start, list.length);
            sync();
            if (root.open) {
                imgEl.focus();
                return;
            }
            openDialog(root);
            window.requestAnimationFrame(() => closeButtonFocus());
        };

        const closeButtonFocus = () => {
            const close = qs(".cms-lightbox__close", root);
            if (close && root.open) {
                close.focus();
            }
        };

        const openFrom = (target, preferredSrc) => {
            const scope = target.closest(".cms-preview, .inspector, .composer__stage, .upload-field, form") || document;
            const list = collectImages(scope, preferredSrc);
            const key = normalizeSrc(preferredSrc);
            const start = Math.max(0, list.findIndex((image) => normalizeSrc(image.src) === key));
            openAt(list, start);
        };

        document.addEventListener("click", (event) => {
            if (event.target.closest(".drop-preview__remove")) {
                return;
            }
            const trigger = event.target.closest(
                ".media-preview-trigger, .current-cover img, .media-card img, .entry__thumb img"
            );
            if (!trigger) {
                return;
            }
            const src = trigger.getAttribute("data-preview-src")
                || (trigger.querySelector && trigger.querySelector("img") && trigger.querySelector("img").currentSrc)
                || trigger.currentSrc
                || trigger.getAttribute("src");
            if (!src) {
                return;
            }
            event.preventDefault();
            openFrom(trigger, src);
        });

        document.addEventListener("keydown", (event) => {
            if (!root || !root.open) {
                return;
            }
            if (event.key === "Escape") {
                event.preventDefault();
                closeDialog(root);
            }
            if (event.key === "ArrowLeft") {
                step(-1);
            }
            if (event.key === "ArrowRight") {
                step(1);
            }
        });

        // exposed for the preview module
        window.__cmsLightbox = { openAt, collectImages, normalizeSrc };
    })();

    /* =====================================================================
       5. content preview modal
       ===================================================================== */
    (() => {
        const dialog = qs("#cms-preview");
        const mount = qs("[data-preview-mount]", dialog);
        const frame = qs("[data-preview-frame]", dialog);
        const titleEl = qs("[data-preview-title]", dialog);
        const subEl = qs("[data-preview-sub]", dialog);
        const linkEl = qs("[data-preview-link]", dialog);
        const tabsWrap = qs("[data-preview-tabs]", dialog);
        if (!dialog || !mount) {
            return;
        }

        let lastTrigger = null;

        const setDevice = (device) => {
            if (!frame) {
                return;
            }
            frame.setAttribute("data-device", device);
            qsa("[data-device-set]", dialog).forEach((button) => {
                button.setAttribute("aria-pressed", String(button.getAttribute("data-device-set") === device));
            });
        };

        const setView = (view) => {
            const available = qsa("[data-pv-view='" + view + "']", mount);
            if (!available.length && view !== "detail") {
                return;
            }
            qsa("[data-pv-view]", mount).forEach((node) => {
                node.hidden = node.getAttribute("data-pv-view") !== view;
            });
            qsa("[data-view-set]", dialog).forEach((button) => {
                button.setAttribute("aria-selected", String(button.getAttribute("data-view-set") === view));
            });
        };

        const wireStage = (scope) => {
            qsa(".gallery-stage", scope).forEach((stage) => {
                const slides = qsa(".gallery-stage__img", stage);
                const count = qs(".gallery-stage__count", stage);
                let current = 0;

                const show = (next) => {
                    if (!slides.length) {
                        return;
                    }
                    current = ((next % slides.length) + slides.length) % slides.length;
                    slides.forEach((slide, i) => {
                        slide.hidden = i !== current;
                    });
                    if (count) {
                        count.textContent = (current + 1) + " / " + slides.length;
                        count.hidden = slides.length < 2;
                    }
                    const many = slides.length > 1;
                    qsa(".gallery-stage__nav", stage).forEach((button) => {
                        button.hidden = !many;
                    });
                };

                on(qs(".gallery-stage__nav--prev", stage), "click", (event) => {
                    event.preventDefault();
                    show(current - 1);
                });
                on(qs(".gallery-stage__nav--next", stage), "click", (event) => {
                    event.preventDefault();
                    show(current + 1);
                });
                on(stage, "click", (event) => {
                    const image = event.target.closest(".gallery-stage__img");
                    if (!image) {
                        return;
                    }
                    event.preventDefault();
                    // Hand the whole set to the lightbox (like the public viewer),
                    // starting on the slide that is currently visible.
                    window.__cmsLightbox.openAt(
                        slides.map((slide) => ({ src: slide.currentSrc || slide.src, alt: slide.alt })),
                        Math.max(0, slides.indexOf(image))
                    );
                });

                show(0);
            });

            qsa("[data-pv-media]", scope).forEach((node) => {
                const src = node.getAttribute("data-pv-media");
                if (!/^(blob:|data:)/.test(src) && !/^(https?:)/.test(src) && src.charAt(0) !== "/") {
                    node.src = withCtx(src);
                }
            });
        };

        const openTemplate = (trigger) => {
            const key = trigger.getAttribute("data-preview-open");
            const template = qs('template[data-preview-key="' + key + '"]');
            if (!template) {
                return false;
            }
            const content = template.content.cloneNode(true);

            const page = document.createElement("main");
            page.className = "page-shell";
            const wrap = document.createElement("div");
            wrap.className = "wrap";
            const stack = document.createElement("div");
            stack.className = "detail-layout";

            while (content.firstElementChild) {
                stack.appendChild(content.firstElementChild);
            }
            wrap.appendChild(stack);
            page.appendChild(wrap);

            mount.replaceChildren(page);
            wireStage(mount);

            if (titleEl) {
                titleEl.textContent = trigger.getAttribute("data-preview-name") || "Preview";
            }
            if (subEl) {
                subEl.textContent = trigger.getAttribute("data-preview-state") || "";
            }

            const live = trigger.getAttribute("data-preview-live");
            if (linkEl) {
                if (live) {
                    linkEl.href = withCtx(live);
                    linkEl.hidden = false;
                } else {
                    linkEl.removeAttribute("href");
                    linkEl.hidden = true;
                }
            }

            const hasCard = Boolean(qs("[data-pv-view='card']", mount));
            if (tabsWrap) {
                tabsWrap.hidden = !hasCard;
            }
            setView(hasCard ? "detail" : "detail");
            setDevice("desktop");
            return true;
        };

        // ------------------------------------------------------------------
        // live preview from composer fields
        // ------------------------------------------------------------------
        const readValue = (form, name) => {
            const field = qs('[name="' + name + '"]', form);
            if (!field) {
                return "";
            }
            if (field.type === "checkbox") {
                return field.checked;
            }
            return field.value || "";
        };

        const fieldImages = (field) => {
            const nodes = qsa(".drop-preview__item img", field);
            return nodes.map((image) => image.currentSrc || image.src);
        };

        const existingImages = (form) => qsa(".current-cover img, .media-strip .media-card img", form)
            .map((image) => image.currentSrc || image.src);

        const stagedOrExisting = (form, fieldName) => {
            const field = qs('[data-file-field] input[name="' + fieldName + '"]', form);
            const holder = field ? field.closest("[data-file-field]") : null;
            const staged = holder ? fieldImages(holder) : [];
            if (staged.length) {
                return staged;
            }
            return existingImages(form);
        };

        const metaRow = (parts) =>
            '<div class="detail-meta">' + parts.filter(Boolean).map((part) => "<span>" + part + "</span>").join("") + "</div>";

        const stageMarkup = (sources, alt) => {
            if (!sources.length) {
                return "";
            }
            const imgs = sources.map((src, i) =>
                '<img class="gallery-stage__img" src="' + escapeHtml(src) + '" alt="' + escapeHtml(alt) + '"' + (i ? " hidden" : "") + ">"
            ).join("");
            const nav = sources.length > 1
                ? '<button type="button" class="gallery-stage__nav gallery-stage__nav--prev" aria-label="Previous image"><span aria-hidden="true">‹</span></button>' +
                  '<button type="button" class="gallery-stage__nav gallery-stage__nav--next" aria-label="Next image"><span aria-hidden="true">›</span></button>' +
                  '<div class="gallery-stage__count">1 / ' + sources.length + "</div>"
                : "";
            return '<div class="gallery-stage">' + imgs + nav + "</div>";
        };

        const buildBlog = (form) => {
            const title = readValue(form, "title");
            const excerpt = readValue(form, "excerpt");
            const body = readValue(form, "body");
            const date = readValue(form, "published") ? "Draft saved as live" : "Draft";
            const cover = stagedOrExisting(form, "coverImageFile");

            const detail =
                '<div class="pv-detail">' +
                    '<section class="page-intro">' +
                        metaRow(["Blog", escapeHtml(date)]) +
                        '<h1 class="detail-title">' + (escapeHtml(title) || "Untitled note") + "</h1>" +
                        (excerpt ? '<p class="detail-copy">' + escapeHtml(excerpt) + "</p>" : "") +
                    "</section>" +
                    (cover.length
                        ? '<div class="detail-hero"><img src="' + escapeHtml(cover[0]) + '" alt="' + escapeHtml(title) + '"></div>'
                        : "") +
                    '<section class="detail-richtext">' + (body || "<p>Write the note and it appears here exactly as readers will see it.</p>") + "</section>" +
                "</div>";

            const card =
                '<div class="pv-card" data-pv-view="card" hidden>' +
                    '<div class="blog-grid">' +
                        '<article class="blog-card">' +
                            (cover.length ? '<div class="blog-card__media"><img src="' + escapeHtml(cover[0]) + '" alt=""></div>' : "") +
                            '<div class="blog-card__meta"><span>Blog</span><span>' + escapeHtml(date) + "</span></div>" +
                            '<h2 class="blog-card__title">' + (escapeHtml(title) || "Untitled note") + "</h2>" +
                            '<p class="blog-card__text">' + (escapeHtml(excerpt) || "Add a short line for the blog list.") + "</p>" +
                        "</article>" +
                    "</div>" +
                "</div>";

            return detail + card;
        };

        const buildGallery = (form) => {
            const title = readValue(form, "title");
            const intro = readValue(form, "introText");
            const body = readValue(form, "body");
            const category = readValue(form, "category");
            const images = stagedOrExisting(form, "mediaFiles");
            const tags = String(category || "").split(/[,/|]/).map((tag) => tag.trim()).filter(Boolean);

            const detail =
                '<div class="pv-detail">' +
                    '<section class="page-intro">' +
                        '<div class="detail-meta">' +
                            tags.map((tag) => '<span class="gallery-card__tag">' + escapeHtml(tag) + "</span>").join("") +
                            "<span>Published</span>" +
                        "</div>" +
                        '<h1 class="detail-title">' + (escapeHtml(title) || "Untitled entry") + "</h1>" +
                        (intro ? '<p class="detail-copy">' + escapeHtml(intro) + "</p>" : "") +
                    "</section>" +
                    (body ? '<section class="detail-richtext"><p>' + escapeHtml(body) + "</p></section>" : "") +
                    stageMarkup(images, title) +
                "</div>";

            const first = images[0];
            const card =
                '<div class="pv-card" data-pv-view="card" hidden>' +
                    '<div class="gallery-mosaic">' +
                        '<article class="gallery-tile">' +
                            (first
                                ? '<button type="button" class="gallery-tile__frame"><img src="' + escapeHtml(first) + '" alt="' + escapeHtml(title) + '"></button>'
                                : "") +
                            '<div class="gallery-tile__body">' +
                                '<h2 class="gallery-tile__title">' + (escapeHtml(title) || "Untitled entry") + "</h2>" +
                                (intro ? '<p class="gallery-tile__text">' + escapeHtml(intro) + "</p>" : "") +
                            "</div>" +
                        "</article>" +
                    "</div>" +
                "</div>";

            return detail + card;
        };

        const buildProject = (form) => {
            const title = readValue(form, "title");
            const summary = readValue(form, "summary");
            const narrative = readValue(form, "narrative");
            const year = readValue(form, "yearLabel");
            const role = readValue(form, "role");
            const tools = readValue(form, "tools");
            const link = readValue(form, "externalLink");
            const linkLabel = readValue(form, "linkLabel") || "Visit project";
            const images = stagedOrExisting(form, "galleryFiles");

            const story = narrative
                ? '<section class="work-case__story"><p>' + escapeHtml(narrative) + "</p></section>"
                : "";
            const facts =
                '<aside class="work-case__facts">' +
                    (role ? "<div><h2>Role</h2><p>" + escapeHtml(role) + "</p></div>" : "") +
                    (tools ? "<div><h2>Tools</h2><p>" + escapeHtml(tools) + "</p></div>" : "") +
                    (link
                        ? '<div><h2>Visit</h2><a class="work-case__link" href="' + escapeHtml(link) + '" rel="noopener noreferrer">' + escapeHtml(linkLabel) + "</a></div>"
                        : "") +
                "</aside>";

            return (
                '<div class="pv-detail">' +
                    '<article class="work-case">' +
                        '<header class="work-case__intro">' +
                            '<p class="work-case__kicker">' + escapeHtml(year || "Year") + "</p>" +
                            '<h1 class="work-case__title">' + (escapeHtml(title) || "Untitled project") + "</h1>" +
                            (summary ? '<p class="work-case__lede">' + escapeHtml(summary) + "</p>" : "") +
                        "</header>" +
                        '<div class="work-case__stage">' + stageMarkup(images, title) + "</div>" +
                        (story || facts
                            ? '<div class="work-case__layout' + (story ? "" : " work-case__layout--solo") + '">' + story + facts + "</div>"
                            : "") +
                    "</article>" +
                "</div>"
            );
        };

        const openLive = (trigger) => {
            // The trigger may sit outside the <form> (top bar) or inside it (rail).
            const form = trigger.closest("form") || qs("form.composer");
            const kind = trigger.getAttribute("data-preview-live");
            if (!form || !kind) {
                return false;
            }

            let markup = "";
            if (kind === "blog") {
                markup = buildBlog(form);
            } else if (kind === "gallery") {
                markup = buildGallery(form);
            } else if (kind === "project") {
                markup = buildProject(form);
            }

            const page = document.createElement("main");
            page.className = "page-shell";
            const wrap = document.createElement("div");
            wrap.className = "wrap";
            const stack = document.createElement("div");
            stack.className = "detail-layout";
            stack.innerHTML = markup;
            wrap.appendChild(stack);
            page.appendChild(wrap);
            mount.replaceChildren(page);
            wireStage(mount);

            const title = qs('[name="title"]', form);
            if (titleEl) {
                titleEl.textContent = (title && title.value) || "Untitled";
            }
            if (subEl) {
                subEl.textContent = trigger.getAttribute("data-preview-state") || "Unsaved preview";
            }
            if (linkEl) {
                linkEl.hidden = true;
                linkEl.removeAttribute("href");
            }

            const hasCard = Boolean(qs("[data-pv-view='card']", mount));
            if (tabsWrap) {
                tabsWrap.hidden = !hasCard;
            }
            setView("detail");
            setDevice("desktop");
            return true;
        };

        document.addEventListener("click", (event) => {
            const trigger = event.target.closest("[data-preview-open], [data-preview-live]");
            if (!trigger) {
                return;
            }
            event.preventDefault();
            lastTrigger = trigger;
            const opened = trigger.hasAttribute("data-preview-open")
                ? openTemplate(trigger)
                : openLive(trigger);
            if (opened) {
                openDialog(dialog);
            }
        });

        qsa("[data-device-set]", dialog).forEach((button) => {
            on(button, "click", () => setDevice(button.getAttribute("data-device-set")));
        });

        qsa("[data-view-set]", dialog).forEach((button) => {
            on(button, "click", () => setView(button.getAttribute("data-view-set")));
        });

        on(dialog, "close", () => {
            mount.replaceChildren();
            unlockBody();
            if (lastTrigger && document.contains(lastTrigger)) {
                lastTrigger.focus();
            }
        });

        qsa("[data-preview-close]", dialog).forEach((button) => {
            on(button, "click", () => closeDialog(dialog));
        });

        // Clicking the dimmed area outside the panel closes the preview.
        on(dialog, "click", (event) => {
            if (event.target === dialog) {
                closeDialog(dialog);
            }
        });

        // keep an open live preview in sync while typing
        let syncTimer = 0;
        document.addEventListener("input", (event) => {
            if (!dialog.open || !mount.querySelector("[data-pv-view]")) {
                return;
            }
            const form = event.target.closest("form.composer");
            if (!form) {
                return;
            }
            window.clearTimeout(syncTimer);
            syncTimer = window.setTimeout(() => {
                const trigger = qs("[data-preview-live]", form);
                if (trigger) {
                    openLive(trigger);
                }
            }, 220);
        });
    })();

    /* =====================================================================
       6. confirm modal (replaces native confirm())
       ===================================================================== */
    (() => {
        const dialog = qs("#cms-confirm");
        const textEl = qs("[data-confirm-text]", dialog);
        const titleEl = qs("[data-confirm-title]", dialog);
        const okButton = qs("[data-confirm-ok]", dialog);
        let pendingForm = null;

        if (!dialog) {
            return;
        }

        document.addEventListener("submit", (event) => {
            const form = event.target;
            if (!(form instanceof HTMLFormElement) || !form.hasAttribute("data-confirm")) {
                return;
            }
            if (form.dataset.confirmed === "yes") {
                delete form.dataset.confirmed;
                return;
            }
            event.preventDefault();
            pendingForm = form;
            if (textEl) {
                textEl.textContent = form.getAttribute("data-confirm") || "This cannot be undone.";
            }
            if (titleEl) {
                titleEl.textContent = form.getAttribute("data-confirm-title") || "Delete this item?";
            }
            if (okButton) {
                okButton.textContent = form.getAttribute("data-confirm-ok") || "Delete";
            }
            openDialog(dialog);
        });

        on(okButton, "click", () => {
            const form = pendingForm;
            pendingForm = null;
            closeDialog(dialog);
            if (form) {
                form.dataset.confirmed = "yes";
                if (typeof form.requestSubmit === "function") {
                    form.requestSubmit();
                } else {
                    form.submit();
                }
            }
        });

        qsa("[data-confirm-cancel]", dialog).forEach((button) => {
            on(button, "click", () => {
                pendingForm = null;
                closeDialog(dialog);
            });
        });

        on(dialog, "close", () => {
            pendingForm = null;
            unlockBody();
        });

        on(dialog, "click", (event) => {
            if (event.target === dialog) {
                pendingForm = null;
                closeDialog(dialog);
            }
        });
    })();

    /* =====================================================================
       7. list search + status filters
       ===================================================================== */
    (() => {
        qsa("[data-filter-scope]").forEach((scope) => {
            const rows = qsa("[data-row]", scope);
            const input = qs("[data-filter-search]", scope);
            const buttons = qsa("[data-filter-status]", scope);
            const count = qs("[data-filter-count]", scope);
            const empty = qs("[data-filter-empty]", scope);
            let status = "all";

            const apply = () => {
                const needle = (input && input.value ? input.value : "").trim().toLowerCase();
                let shown = 0;

                rows.forEach((row) => {
                    const haystack = (row.getAttribute("data-search") || "").toLowerCase();
                    const matchesText = !needle || haystack.indexOf(needle) !== -1;
                    const rowStatus = row.getAttribute("data-status") || "";
                    const matchesStatus = status === "all" || rowStatus === status;
                    const visible = matchesText && matchesStatus;
                    row.classList.toggle("is-hidden", !visible);
                    if (visible) {
                        shown += 1;
                    }
                });

                if (count) {
                    count.textContent = shown + (shown === 1 ? " item" : " items");
                }
                if (empty) {
                    empty.hidden = shown !== 0 || rows.length === 0;
                }
            };

            on(input, "input", apply);
            buttons.forEach((button) => {
                on(button, "click", () => {
                    status = button.getAttribute("data-filter-status");
                    buttons.forEach((other) => other.setAttribute("aria-pressed", String(other === button)));
                    apply();
                });
            });
            apply();
        });
    })();

    /* =====================================================================
       8. command palette
       ===================================================================== */
    (() => {
        const dialog = qs("#cms-palette");
        const input = qs("[data-palette-input]", dialog);
        const list = qs("[data-palette-list]", dialog);
        const empty = qs("[data-palette-empty]", dialog);
        if (!dialog || !input || !list) {
            return;
        }

        const items = qsa("li", list);
        let active = 0;

        const visibleItems = () => items.filter((item) => !item.hidden);

        const filter = () => {
            const needle = input.value.trim().toLowerCase();
            items.forEach((item) => {
                const text = (item.getAttribute("data-palette-text") || item.textContent || "").toLowerCase();
                item.hidden = Boolean(needle) && text.indexOf(needle) === -1;
            });
            const shown = visibleItems();
            if (empty) {
                empty.hidden = shown.length !== 0;
            }
            active = 0;
            paint();
        };

        const paint = () => {
            const shown = visibleItems();
            items.forEach((item) => item.classList.remove("is-active"));
            if (!shown.length) {
                return;
            }
            active = Math.max(0, Math.min(active, shown.length - 1));
            const current = shown[active];
            current.classList.add("is-active");
            const focusable = qs("a, button", current);
            if (focusable) {
                focusable.scrollIntoView({ block: "nearest" });
            }
        };

        const run = () => {
            const current = visibleItems()[active];
            if (!current) {
                return;
            }
            const target = qs("a, button", current);
            if (!target) {
                return;
            }
            closeDialog(dialog);
            target.click();
        };

        const open = () => {
            if (dialog.open) {
                return;
            }
            input.value = "";
            filter();
            openDialog(dialog);
            window.requestAnimationFrame(() => input.focus());
        };

        on(qs("[data-palette-open]"), "click", open);
        on(input, "input", filter);
        on(input, "keydown", (event) => {
            if (event.key === "ArrowDown") {
                event.preventDefault();
                active += 1;
                paint();
            } else if (event.key === "ArrowUp") {
                event.preventDefault();
                active -= 1;
                paint();
            } else if (event.key === "Enter") {
                event.preventDefault();
                run();
            } else if (event.key === "Escape") {
                closeDialog(dialog);
            }
        });
        on(qs("[data-palette-logout]", dialog), "click", () => {
            const form = qs("[data-logout-form]");
            if (form) {
                closeDialog(dialog);
                form.submit();
            }
        });
        on(list, "click", (event) => {
            const target = event.target.closest("a, button");
            if (target) {
                closeDialog(dialog);
            }
        });
        on(dialog, "click", (event) => {
            if (event.target === dialog) {
                closeDialog(dialog);
            }
        });
        on(dialog, "close", () => {
            unlockBody();
        });

        document.addEventListener("keydown", (event) => {
            const typing = /^(INPUT|TEXTAREA|SELECT)$/.test(document.activeElement && document.activeElement.tagName);
            if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") {
                event.preventDefault();
                open();
                return;
            }
            if (event.key === "/" && !typing) {
                const search = qs("[data-filter-search]");
                if (search) {
                    event.preventDefault();
                    search.focus();
                }
            }
        });
    })();

    /* =====================================================================
       9. WYSIWYG editor
       ===================================================================== */
    (() => {
        const ALLOWED = [
            "P", "DIV", "BR", "SPAN", "STRONG", "B", "EM", "I", "U", "S", "STRIKE", "DEL",
            "H2", "H3", "UL", "OL", "LI", "BLOCKQUOTE", "A", "CODE", "PRE", "HR", "IMG"
        ];
        // Only these attributes survive, per tag. Everything else (styles, event
        // handlers, ids, classes, srcset, formaction…) is dropped. The stored HTML
        // is rendered raw on the public blog page, so this is the only gate.
        const ALLOWED_ATTRIBUTES = {
            A: ["href", "title"],
            IMG: ["src", "alt", "title"]
        };
        const SAFE_HREF = /^(?:https?:\/\/|mailto:|#|\/(?!\/))/i;
        const SAFE_SRC = /^(?:https?:\/\/|\/(?!\/)|data:image\/)/i;

        // The desk's form posts are decoded with the servlet container's default
        // charset for multipart bodies, so raw non-ASCII typed or pasted into a
        // note (curly quotes from Word, em dashes, accents) would arrive mangled.
        // Encoding those characters as numeric references keeps the payload pure
        // ASCII and still renders/edits identically, because the body is always
        // used as HTML. See the encoding note in the hand-over summary.
        const asciiSafe = (html) =>
            html.replace(/[^\x00-\x7F]/gu, (character) => "&#" + character.codePointAt(0) + ";");

        const sanitize = (html) => {
            const template = document.createElement("template");
            template.innerHTML = html;

            const walk = (node) => {
                Array.from(node.childNodes).forEach((child) => {
                    if (child.nodeType === Node.COMMENT_NODE) {
                        child.remove();
                        return;
                    }
                    if (child.nodeType !== Node.ELEMENT_NODE) {
                        return;
                    }

                    if (!ALLOWED.includes(child.tagName)) {
                        if (/^(SCRIPT|STYLE|IFRAME|OBJECT|EMBED|LINK|META|FORM|INPUT|BUTTON|TEXTAREA|SELECT|SVG|MATH|VIDEO|AUDIO|SOURCE|TRACK|CANVAS)$/.test(child.tagName)) {
                            child.remove();
                            return;
                        }
                        const fragment = document.createDocumentFragment();
                        while (child.firstChild) {
                            fragment.appendChild(child.firstChild);
                        }
                        child.replaceWith(fragment);
                        return;
                    }

                    const allowed = ALLOWED_ATTRIBUTES[child.tagName] || [];
                    Array.from(child.attributes).forEach((attribute) => {
                        if (!allowed.includes(attribute.name.toLowerCase())) {
                            child.removeAttribute(attribute.name);
                        }
                    });

                    if (child.tagName === "A") {
                        const href = child.getAttribute("href") || "";
                        if (!SAFE_HREF.test(href)) {
                            child.removeAttribute("href");
                        } else {
                            child.setAttribute("rel", "noopener noreferrer");
                        }
                    }

                    if (child.tagName === "IMG") {
                        const src = child.getAttribute("src") || "";
                        if (!SAFE_SRC.test(src)) {
                            child.remove();
                            return;
                        }
                        child.setAttribute("alt", child.getAttribute("alt") || "");
                    }

                    walk(child);
                });
            };

            walk(template.content);
            return asciiSafe(template.innerHTML);
        };

        qsa("[data-editor]").forEach((editor) => {
            const surface = qs("[data-editor-surface]", editor);
            const input = qs("[data-editor-input]", editor);
            const toolbar = qs("[data-editor-toolbar]", editor);
            const counter = qs("[data-editor-count]", editor);
            const form = editor.closest("form");
            if (!surface || !input) {
                return;
            }

            surface.contentEditable = "true";
            surface.spellcheck = true;

            const seed = input.value || "";
            const hasMarkup = /<\/?[a-z][\s\S]*>/i.test(seed);
            if (hasMarkup) {
                surface.innerHTML = sanitize(seed);
            } else if (seed.trim()) {
                surface.innerHTML = seed
                    .split(/\n{2,}/)
                    .map((block) => "<p>" + escapeHtml(block).replace(/\n/g, "<br>") + "</p>")
                    .join("");
            }

            const activeCommands = ["bold", "italic", "underline", "strikeThrough", "insertUnorderedList", "insertOrderedList", "formatBlock"];

            const paintToolbar = () => {
                if (!toolbar) {
                    return;
                }
                qsa("button[data-cmd]", toolbar).forEach((button) => {
                    const cmd = button.getAttribute("data-cmd");
                    if (!activeCommands.includes(cmd)) {
                        return;
                    }
                    let on_ = false;
                    try {
                        if (cmd === "formatBlock") {
                            const block = (document.queryCommandValue("formatBlock") || "").toLowerCase();
                            on_ = block === (button.getAttribute("data-value") || "").toLowerCase();
                        } else {
                            on_ = document.queryCommandState(cmd);
                        }
                    } catch (error) {
                        on_ = false;
                    }
                    button.classList.toggle("is-active", on_);
                    if (button.hasAttribute("aria-pressed")) {
                        button.setAttribute("aria-pressed", String(on_));
                    }
                });
            };

            const countWords = () => {
                const text = surface.textContent.replace(/\s+/g, " ").trim();
                const words = text ? text.split(" ").length : 0;
                if (counter) {
                    counter.textContent = words + (words === 1 ? " word" : " words");
                }
            };

            const sync = () => {
                const clean = sanitize(surface.innerHTML);
                input.value = clean;
                countWords();
                document.dispatchEvent(new CustomEvent("cms:editor-sync", { detail: { editor } }));
            };

            const exec = (command, value) => {
                surface.focus();
                try {
                    document.execCommand(command, false, value);
                } catch (error) {
                    /* command unsupported; ignore */
                }
                paintToolbar();
                sync();
            };

            const nearestBlock = () => {
                const selection = window.getSelection();
                if (!selection || !selection.rangeCount) {
                    return null;
                }
                let node = selection.anchorNode;
                while (node && node !== surface) {
                    if (node.nodeType === Node.ELEMENT_NODE && /^(P|DIV|H2|H3|LI|BLOCKQUOTE|PRE)$/.test(node.tagName)) {
                        return node.tagName;
                    }
                    node = node.parentNode;
                }
                return null;
            };

            on(toolbar, "mousedown", (event) => {
                // keep the caret inside the surface when a toolbar control is used
                if (event.target.closest("button")) {
                    event.preventDefault();
                }
            });

            on(toolbar, "click", (event) => {
                const button = event.target.closest("button[data-cmd]");
                if (!button) {
                    return;
                }
                event.preventDefault();
                const command = button.getAttribute("data-cmd");
                const value = button.getAttribute("data-value") || null;

                if (command === "createLink") {
                    const current = window.getSelection() ? String(window.getSelection()).trim() : "";
                    if (!current) {
                        return;
                    }
                    const url = window.prompt("Link URL", "https://");
                    if (url && SAFE_HREF.test(url.trim())) {
                        exec("createLink", url.trim());
                    }
                    return;
                }

                if (command === "formatBlock") {
                    const current = nearestBlock();
                    const wanted = (value || "").toUpperCase();
                    exec("formatBlock", current === wanted ? "P" : wanted);
                    return;
                }

                if (command === "removeFormat") {
                    exec("removeFormat");
                    return;
                }

                if (command === "undo" || command === "redo") {
                    exec(command);
                    return;
                }

                exec(command, value);
            });

            on(surface, "keyup", paintToolbar);
            on(surface, "mouseup", paintToolbar);
            on(surface, "input", () => {
                // Keep the hidden field (and therefore the preview + the posted
                // value) in step with the surface on every keystroke.
                countWords();
                paintToolbar();
                sync();
            });
            on(surface, "blur", sync);

            on(surface, "paste", (event) => {
                event.preventDefault();
                const html = event.clipboardData ? event.clipboardData.getData("text/html") : "";
                const text = event.clipboardData ? event.clipboardData.getData("text/plain") : "";
                const payload = html ? sanitize(html) : asciiSafe(escapeHtml(text).replace(/\n/g, "<br>"));
                try {
                    document.execCommand("insertHTML", false, payload);
                } catch (error) {
                    surface.innerHTML += payload;
                }
                sync();
            });

            on(surface, "drop", (event) => event.preventDefault());

            on(form, "submit", () => sync());

            on(surface, "keydown", (event) => {
                const meta = event.metaKey || event.ctrlKey;
                if (!meta) {
                    return;
                }
                const key = event.key.toLowerCase();
                if (key === "b" || key === "i" || key === "u") {
                    event.preventDefault();
                    exec(key === "b" ? "bold" : key === "i" ? "italic" : "underline");
                }
                if (key === "s") {
                    event.preventDefault();
                    if (form) {
                        if (typeof form.requestSubmit === "function") {
                            form.requestSubmit();
                        } else {
                            form.submit();
                        }
                    }
                }
            });

            sync();
            countWords();
            paintToolbar();
        });
    })();

    /* =====================================================================
       10. niceties: autogrow, counters, dirty guard
       ===================================================================== */
    (() => {
        const autogrow = (node) => {
            node.style.height = "auto";
            node.style.height = Math.min(node.scrollHeight + 2, 520) + "px";
        };

        qsa("[data-autogrow]").forEach((node) => {
            autogrow(node);
            on(node, "input", () => autogrow(node));
        });

        qsa("[data-count-input]").forEach((node) => {
            const out = qs("[data-count-for='" + node.getAttribute("data-count-input") + "']");
            if (!out) {
                return;
            }
            const max = Number(node.getAttribute("maxlength")) || 0;
            const update = () => {
                const used = node.value.length;
                out.textContent = max ? used + " / " + max : String(used);
                out.classList.toggle("is-warn", Boolean(max) && used > max * 0.9);
            };
            on(node, "input", update);
            update();
        });

        const form = qs("form.composer");
        if (form) {
            let dirty = false;
            let submitting = false;

            on(form, "input", () => {
                dirty = true;
            });
            on(form, "submit", () => {
                submitting = true;
            });
            qsa("[data-preview-live]", form).forEach((trigger) => {
                on(trigger, "click", () => {
                    dirty = true;
                });
            });

            window.addEventListener("beforeunload", (event) => {
                if (!dirty || submitting) {
                    return;
                }
                event.preventDefault();
                event.returnValue = "";
            });
        }
    })();
})();
