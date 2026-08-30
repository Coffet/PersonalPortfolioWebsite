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
                        ? "1 image ready — click to add more"
                        : staged.length + " images ready — click to add more";
                } else {
                    caption.textContent = "Click to replace " + staged[0].name;
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
