(() => {
    const grid = document.querySelector("[data-order-grid]");
    const menus = Array.from(document.querySelectorAll("[data-order-menu]"));
    const tabsRoot = document.querySelector("[data-filter-tabs]");
    const empty = document.querySelector("[data-order-empty]");
    if (!menus.length && !tabsRoot) {
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
        const hasDateMenu = menus.some((menu) => menu.dataset.orderType === "date");
        if (hasDateMenu) {
            items.sort((a, b) => {
                const aTime = Date.parse(a.dataset.publishedAt || "") || 0;
                const bTime = Date.parse(b.dataset.publishedAt || "") || 0;
                return state.date === "oldest" ? aTime - bTime : bTime - aTime;
            });
        }

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

    const setTab = (tab) => {
        if (!tabsRoot || !tab) {
            return;
        }
        state.category = tab.dataset.filter || "all";
        const tabs = Array.from(tabsRoot.querySelectorAll("[data-filter]"));
        tabs.forEach((item) => {
            const selected = item === tab;
            item.setAttribute("aria-selected", selected ? "true" : "false");
            item.tabIndex = selected ? 0 : -1;
        });
        apply();
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

    if (tabsRoot) {
        const tabs = Array.from(tabsRoot.querySelectorAll("[data-filter]"));
        tabs.forEach((tab, index) => {
            tab.tabIndex = index === 0 ? 0 : -1;
        });

        tabsRoot.addEventListener("click", (event) => {
            const tab = event.target.closest("[data-filter]");
            if (!tab || !tabsRoot.contains(tab)) {
                return;
            }
            setTab(tab);
        });

        tabsRoot.addEventListener("keydown", (event) => {
            const current = event.target.closest("[data-filter]");
            if (!current || !tabsRoot.contains(current)) {
                return;
            }
            const currentIndex = tabs.indexOf(current);
            if (currentIndex < 0) {
                return;
            }
            let nextIndex = currentIndex;
            if (event.key === "ArrowRight" || event.key === "ArrowDown") {
                nextIndex = (currentIndex + 1) % tabs.length;
            } else if (event.key === "ArrowLeft" || event.key === "ArrowUp") {
                nextIndex = (currentIndex - 1 + tabs.length) % tabs.length;
            } else if (event.key === "Home") {
                nextIndex = 0;
            } else if (event.key === "End") {
                nextIndex = tabs.length - 1;
            } else {
                return;
            }
            event.preventDefault();
            tabs[nextIndex].focus();
            setTab(tabs[nextIndex]);
        });
    }

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
