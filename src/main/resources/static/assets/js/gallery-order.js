(() => {
    const grid = document.querySelector("[data-order-grid]");
    const menus = Array.from(document.querySelectorAll("[data-order-menu]"));
    const empty = document.querySelector("[data-order-empty]");
    if (!menus.length) {
        return;
    }

    const state = {
        date: "newest",
        category: "all"
    };

    const closeAll = (except) => {
        menus.forEach((menu) => {
            if (menu !== except) {
                menu.removeAttribute("open");
            }
        });
    };

    const cards = () => Array.from(grid ? grid.querySelectorAll("[data-published-at]") : []);

    const fillCategories = () => {
        const holder = document.querySelector("[data-category-options]");
        if (!holder) {
            return;
        }

        const names = [...new Set(cards().map((card) => card.dataset.category || "Gallery"))].sort((a, b) =>
            a.localeCompare(b)
        );

        names.forEach((name) => {
            const button = document.createElement("button");
            button.type = "button";
            button.setAttribute("role", "option");
            button.dataset.orderValue = name;
            button.textContent = name;
            holder.appendChild(button);
        });
    };

    const apply = () => {
        if (!grid) {
            return;
        }

        const items = cards();
        items.sort((a, b) => {
            const aTime = Date.parse(a.dataset.publishedAt || "") || 0;
            const bTime = Date.parse(b.dataset.publishedAt || "") || 0;
            return state.date === "oldest" ? aTime - bTime : bTime - aTime;
        });

        items.forEach((card, index) => {
            const category = card.dataset.category || "Gallery";
            const visible = state.category === "all" || category === state.category;
            card.hidden = !visible;
            card.style.setProperty("--stagger", String(index));
            if (visible) {
                grid.appendChild(card);
            }
        });

        const visibleCount = items.filter((card) => !card.hidden).length;
        if (empty) {
            empty.hidden = visibleCount !== 0;
        }
    };

    const setSelected = (menu, value) => {
        const label = menu.querySelector("[data-order-label]");
        const type = menu.dataset.orderType;
        state[type] = value;

        menu.querySelectorAll("[data-order-value]").forEach((option) => {
            option.setAttribute("aria-selected", option.dataset.orderValue === value ? "true" : "false");
        });

        if (label && type === "date") {
            label.textContent = value === "oldest" ? "Oldest" : "Date";
        }
        if (label && type === "category") {
            label.textContent = value === "all" ? "Category" : value;
        }

        apply();
        menu.removeAttribute("open");
    };

    fillCategories();

    menus.forEach((menu) => {
        menu.addEventListener("toggle", () => {
            if (menu.open) {
                closeAll(menu);
            }
        });

        menu.addEventListener("click", (event) => {
            const option = event.target.closest("[data-order-value]");
            if (!option || !menu.contains(option)) {
                return;
            }
            event.preventDefault();
            setSelected(menu, option.dataset.orderValue);
        });
    });

    document.addEventListener("pointerdown", (event) => {
        if (!menus.some((menu) => menu.contains(event.target))) {
            closeAll();
        }
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            closeAll();
        }
    });

    apply();
})();
