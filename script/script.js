document.addEventListener('DOMContentLoaded', () => {
    const WORK_PROJECTS = window.WORK_PROJECTS || {};
    gsap.registerPlugin(ScrollTrigger);

    const header = document.getElementById('header');
    const headerBg = document.querySelector('.header-bg');
    const colorPanels = document.querySelectorAll('.color-panel');
    const logo = document.getElementById('logo');
    const navLinks = document.getElementById('nav-links');
    const mainContent = document.getElementById('main-content');
    const isMobile = window.innerWidth <= 768;
    const barInset = isMobile ? '8px' : '30px';
    const statusInset = isMobile ? '16px' : '45px';
    const headerHeight = '75px';
    const logoSize = isMobile ? '70px' : '80px';

    const setIntroHeight = () => {
        if (window.innerWidth > 768) return;
        const h = window.visualViewport?.height ?? window.innerHeight;
        document.documentElement.style.setProperty('--intro-screen-height', `${h}px`);
    };

    setIntroHeight();

    const onIntroViewportChange = () => setIntroHeight();
    window.visualViewport?.addEventListener('resize', onIntroViewportChange);
    window.addEventListener('orientationchange', onIntroViewportChange);
    window.addEventListener('resize', onIntroViewportChange);

    const stopIntroViewportWatch = () => {
        window.visualViewport?.removeEventListener('resize', onIntroViewportChange);
        window.removeEventListener('orientationchange', onIntroViewportChange);
        window.removeEventListener('resize', onIntroViewportChange);
    };

    const INTRO_KEY = 'portfolioIntroAt';
    localStorage.removeItem(INTRO_KEY);

    const shouldSkipIntro = () => Boolean(sessionStorage.getItem(INTRO_KEY));

    const markIntroPlayed = () => {
        sessionStorage.setItem(INTRO_KEY, '1');
        localStorage.removeItem(INTRO_KEY);
    };

    const lockIntroScroll = () => {
        document.documentElement.classList.add('intro-lock');
        document.body.classList.add('intro-lock');
    };

    const unlockIntroScroll = () => {
        document.documentElement.classList.remove('intro-lock');
        document.body.classList.remove('intro-lock');
    };

    const finishIntro = () => {
        stopIntroViewportWatch();
        unlockIntroScroll();
        document.body.style.overflow = 'auto';
        document.body.style.overflowX = 'hidden';
        initScrollAnimations();
        markIntroPlayed();
    };

    function initScrollAnimations() {
        const fadeUpElements = document.querySelectorAll('.fade-up');
        const revealLine = window.innerHeight * 0.85;

        fadeUpElements.forEach((el) => {
            const alreadyInView = el.getBoundingClientRect().top < revealLine;
            const tween = {
                y: 0,
                opacity: 1,
                duration: 1,
                ease: "power3.out",
            };

            if (alreadyInView) {
                gsap.to(el, tween);
            } else {
                tween.scrollTrigger = {
                    trigger: el,
                    start: "top 85%",
                    toggleActions: "play none none none"
                };
                gsap.to(el, tween);
            }
        });
    }

    function initCursorBloom() {
        const bloomCards = document.querySelectorAll('.social-card, .work-card');

        bloomCards.forEach((card) => {
            const rect = () => card.getBoundingClientRect();
            const workBody = card.querySelector('.work-body');
            const center = () => {
                const bounds = rect();
                return { x: bounds.width / 2, y: bounds.height / 2 };
            };

            let target = center();
            let primary = center();
            let trail = center();
            let frameId = null;
            let hovering = false;

            const render = () => {
                primary.x += (target.x - primary.x) * 0.18;
                primary.y += (target.y - primary.y) * 0.18;
                trail.x += (primary.x - trail.x) * 0.1;
                trail.y += (primary.y - trail.y) * 0.1;

                card.style.setProperty('--cursor-x', `${primary.x}px`);
                card.style.setProperty('--cursor-y', `${primary.y}px`);
                card.style.setProperty('--cursor-x-2', `${trail.x}px`);
                card.style.setProperty('--cursor-y-2', `${trail.y}px`);

                if (workBody) {
                    const cardBounds = rect();
                    const bodyBounds = workBody.getBoundingClientRect();
                    const offsetX = bodyBounds.left - cardBounds.left;
                    const offsetY = bodyBounds.top - cardBounds.top;
                    const clamp = (value, min, max) => Math.min(Math.max(value, min), max);
                    const bodyPrimaryX = clamp(primary.x - offsetX, 0, bodyBounds.width);
                    const bodyPrimaryY = clamp(primary.y - offsetY, 0, bodyBounds.height);
                    const bodyTrailX = clamp(trail.x - offsetX, 0, bodyBounds.width);
                    const bodyTrailY = clamp(trail.y - offsetY, 0, bodyBounds.height);

                    workBody.style.setProperty('--cursor-x', `${bodyPrimaryX}px`);
                    workBody.style.setProperty('--cursor-y', `${bodyPrimaryY}px`);
                    workBody.style.setProperty('--cursor-x-2', `${bodyTrailX}px`);
                    workBody.style.setProperty('--cursor-y-2', `${bodyTrailY}px`);
                }

                const settled =
                    Math.abs(target.x - primary.x) < 0.5 &&
                    Math.abs(target.y - primary.y) < 0.5 &&
                    Math.abs(primary.x - trail.x) < 0.5 &&
                    Math.abs(primary.y - trail.y) < 0.5;

                if (!hovering && settled) {
                    frameId = null;
                    return;
                }

                frameId = window.requestAnimationFrame(render);
            };

            const start = () => {
                if (frameId !== null) return;
                frameId = window.requestAnimationFrame(render);
            };

            const setCursorPosition = (event) => {
                const rect = card.getBoundingClientRect();
                target = {
                    x: event.clientX - rect.left,
                    y: event.clientY - rect.top,
                };
                hovering = true;
                start();
            };

            card.addEventListener('pointerenter', (event) => {
                hovering = true;
                setCursorPosition(event);
            });
            card.addEventListener('pointermove', setCursorPosition);
            card.addEventListener('pointerleave', () => {
                hovering = false;
                target = center();
                start();
            });
        });
    }

    async function fetchEncouragementFromInternet() {
        const controller = new AbortController();
        const timeout = window.setTimeout(() => controller.abort(), 4000);

        try {
            // Allowed by current CSP: connect-src 'self' https://api.github.com
            const response = await fetch('https://api.github.com/zen', {
                method: 'GET',
                cache: 'no-store',
                signal: controller.signal,
                headers: {
                    Accept: 'text/plain',
                },
            });

            if (!response.ok) {
                return 'Keep going. Your next project is on the way.';
            }

            const text = (await response.text()).replace(/\s+/g, ' ').trim();
            return text || 'Keep going. Your next project is on the way.';
        } catch (_) {
            return 'Keep going. Your next project is on the way.';
        } finally {
            window.clearTimeout(timeout);
        }
    }

    function hydrateWorkCardsFromMap() {
        const workGrid = document.getElementById('work-grid');
        if (!workGrid) return;

        const defaultCardGradient = 'linear-gradient(180deg, #d0d0d0 0%, #8d8d8d 100%)';
        const projectEntries = Object.entries(WORK_PROJECTS);

        workGrid.replaceChildren();

        if (projectEntries.length === 0) {
            const emptyWrap = document.createElement('div');
            emptyWrap.className = 'work-empty-message';

            const emptyTitle = document.createElement('p');
            emptyTitle.className = 'work-empty-title';
            emptyTitle.textContent = 'No projects yet. I will add them soon. I promise!';

            const emptySub = document.createElement('p');
            emptySub.className = 'work-empty-encouragement';
            emptySub.textContent = 'Fetching encouragement from the internet...';

            emptyWrap.append(emptyTitle, emptySub);
            workGrid.appendChild(emptyWrap);
            fetchEncouragementFromInternet().then((line) => {
                emptySub.textContent = line;
            });
            return;
        }

        projectEntries.forEach(([projectId, project]) => {
            const card = document.createElement('article');
            card.className = 'work-card';
            card.dataset.projectId = projectId;

            const mediaEl = document.createElement('div');
            mediaEl.className = 'work-media';
            mediaEl.setAttribute('aria-hidden', 'true');

            const cardImage = (project.cardImage || project.image || '').trim();
            const cardGradient = project.cardGradient || defaultCardGradient;

            if (cardImage) {
                const imageEl = document.createElement('img');
                imageEl.src = cardImage;
                imageEl.alt = `${project.title || 'Project'} preview`;
                imageEl.loading = 'lazy';
                imageEl.decoding = 'async';
                mediaEl.appendChild(imageEl);
                mediaEl.style.background = '#101010';
            } else {
                mediaEl.style.background = cardGradient;
            }

            const bodyEl = document.createElement('div');
            bodyEl.className = 'work-body';

            const titleEl = document.createElement('h3');
            titleEl.className = 'work-title';
            titleEl.textContent = project.title || 'Project Name';

            const descEl = document.createElement('p');
            descEl.className = 'work-desc';
            descEl.textContent = project.desc || 'Project description';

            const yearEl = document.createElement('p');
            yearEl.className = 'work-date';
            yearEl.textContent = project.year || 'Year';

            const buttonEl = document.createElement('button');
            buttonEl.className = 'work-link';
            buttonEl.type = 'button';
            buttonEl.setAttribute('data-work-modal-trigger', '');
            buttonEl.innerHTML = 'View Details <span aria-hidden="true">›</span>';

            bodyEl.append(titleEl, descEl, yearEl, buttonEl);
            card.append(mediaEl, bodyEl);
            workGrid.appendChild(card);
        });

    }

    function initWorkModal() {
        const modal = document.getElementById('work-modal');
        if (!modal) return;

        const dialog = modal.querySelector('.work-modal__dialog');
        const backdrop = modal.querySelector('.work-modal__backdrop');
        const closeButton = modal.querySelector('.work-modal__close');
        const closeTargets = modal.querySelectorAll('[data-work-modal-close]');
        const triggers = document.querySelectorAll('[data-work-modal-trigger]');
        const titleEl = document.getElementById('work-modal-title');
        const descEl = document.getElementById('work-modal-desc');
        const projectYearEl = document.getElementById('work-modal-year');
        const roleEl = document.getElementById('work-modal-role');
        const toolsEl = document.getElementById('work-modal-tools');
        const linkEl = document.getElementById('work-modal-link');
        const mediaEl = document.getElementById('work-modal-media');
        const mediaImgEl = document.getElementById('work-modal-media-img');
        const animatedParts = modal.querySelectorAll(
            '.work-modal__title, .work-modal__desc, .work-modal__year, .work-modal__media, .work-modal__meta-item'
        );
        const focusableSelector = 'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])';
        let lastFocused = null;
        let isAnimating = false;

        const setVisitLink = (href, label) => {
            const normalizedHref = href?.trim();
            const linkLabel = label?.trim() || 'Visit project';

            if (normalizedHref && normalizedHref !== '#') {
                linkEl.textContent = linkLabel;
                linkEl.href = normalizedHref;
                linkEl.target = '_blank';
                linkEl.rel = 'noopener noreferrer';
                linkEl.classList.remove('is-disabled');
                linkEl.removeAttribute('aria-disabled');
            } else {
                linkEl.textContent = 'Link available on request';
                linkEl.removeAttribute('href');
                linkEl.removeAttribute('target');
                linkEl.removeAttribute('rel');
                linkEl.classList.add('is-disabled');
                linkEl.setAttribute('aria-disabled', 'true');
            }
        };

        const populateModal = (card) => {
            const projectId = card.dataset.projectId;
            const project = WORK_PROJECTS[projectId] || {};

            titleEl.textContent = project.title || 'Project Name 1';
            descEl.textContent = project.desc || 'Project description';
            projectYearEl.textContent = project.year || '2026';
            roleEl.textContent = project.role || 'Details coming soon.';
            toolsEl.textContent = project.tools || 'Tools coming soon.';
            setVisitLink(project.link, project.linkLabel);

            const setModalImage = (src, altText) => {
                mediaEl.style.background = 'none';
                mediaEl.classList.add('has-image');
                mediaImgEl.src = src;
                mediaImgEl.alt = altText;
                mediaImgEl.classList.remove('is-hidden');
            };

            const clearModalImage = () => {
                mediaImgEl.classList.add('is-hidden');
                mediaImgEl.removeAttribute('src');
                mediaImgEl.alt = '';
                mediaEl.classList.remove('has-image');
            };

            const normalizedImage = (project.modalImage || project.image || '').trim();
            if (normalizedImage) {
                setModalImage(normalizedImage, `${titleEl.textContent} preview image`);
                return;
            }

            const sourceCardImage = card.querySelector('.work-media img');
            if (sourceCardImage?.src) {
                setModalImage(sourceCardImage.src, sourceCardImage.alt || `${titleEl.textContent} preview image`);
                return;
            }

            clearModalImage();
            const sourceMedia = card.querySelector('.work-media');
            if (sourceMedia) {
                mediaEl.style.background = window.getComputedStyle(sourceMedia).background;
            }
        };

        const firstProjectCard = document.querySelector('.work-card[data-project-id]');
        if (firstProjectCard) {
            populateModal(firstProjectCard);
        }

        const resetAnimatedStyles = () => {
            gsap.set([backdrop, dialog, ...animatedParts], { clearProps: 'opacity,transform' });
        };

        const completeClose = () => {
            modal.classList.remove('is-open');
            modal.setAttribute('aria-hidden', 'true');
            document.body.classList.remove('modal-open');
            isAnimating = false;

            if (lastFocused instanceof HTMLElement) {
                lastFocused.focus();
            }
        };

        const closeModal = () => {
            if (!modal.classList.contains('is-open')) return;

            gsap.killTweensOf([backdrop, dialog, ...animatedParts]);

            isAnimating = true;
            gsap.timeline({
                defaults: { ease: 'power2.inOut' },
                onComplete: () => {
                    resetAnimatedStyles();
                    completeClose();
                },
            })
            .to(animatedParts, {
                opacity: 0,
                y: 18,
                duration: 0.24,
                stagger: 0.028,
            }, 0)
            .to(dialog, {
                opacity: 0,
                y: 30,
                scale: 0.965,
                duration: 0.32,
            }, 0.02)
            .to(backdrop, {
                opacity: 0,
                duration: 0.28,
            }, 0.04);
        };

        const trapFocus = (event) => {
            if (!modal.classList.contains('is-open') || event.key !== 'Tab') return;

            const focusables = [...dialog.querySelectorAll(focusableSelector)].filter(
                (el) => !el.hasAttribute('disabled') && !el.classList.contains('is-disabled')
            );

            if (focusables.length === 0) return;

            const first = focusables[0];
            const last = focusables[focusables.length - 1];

            if (event.shiftKey && document.activeElement === first) {
                event.preventDefault();
                last.focus();
            } else if (!event.shiftKey && document.activeElement === last) {
                event.preventDefault();
                first.focus();
            }
        };

        const openModal = (card, trigger) => {
            populateModal(card);
            lastFocused = trigger;
            modal.classList.add('is-open');
            modal.setAttribute('aria-hidden', 'false');
            document.body.classList.add('modal-open');

            gsap.killTweensOf([backdrop, dialog, ...animatedParts]);

            isAnimating = true;
            gsap.set(backdrop, { opacity: 0 });
            gsap.set(dialog, { opacity: 0, y: 40, scale: 0.95 });
            gsap.set(animatedParts, { opacity: 0, y: 26 });

            gsap.timeline({
                defaults: { ease: 'power3.out' },
                onComplete: () => {
                    isAnimating = false;
                    window.requestAnimationFrame(() => closeButton?.focus());
                },
            })
            .to(backdrop, {
                opacity: 1,
                duration: 0.3,
            }, 0)
            .to(dialog, {
                opacity: 1,
                y: 0,
                scale: 1,
                duration: 0.44,
            }, 0.04)
            .to(animatedParts, {
                opacity: 1,
                y: 0,
                duration: 0.34,
                stagger: 0.05,
            }, 0.18);
        };

        triggers.forEach((trigger) => {
            trigger.addEventListener('click', () => {
                const card = trigger.closest('.work-card');
                if (!card) return;
                openModal(card, trigger);
            });
        });

        closeTargets.forEach((target) => {
            target.addEventListener('click', closeModal);
        });

        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape') {
                closeModal();
            }

            trapFocus(event);
        });
    }

    const snapToTopbar = () => {
        stopIntroViewportWatch();
        unlockIntroScroll();
        gsap.set(colorPanels, { display: 'none' });
        gsap.set(header, { height: headerHeight });
        gsap.set(headerBg, {
            height: headerHeight,
            width: '100vw',
            background: '#000000',
            borderBottom: 'none',
            zIndex: 6,
        });
        gsap.set(logo, {
            opacity: 1,
            width: logoSize,
            height: logoSize,
            top: '50%',
            left: barInset,
            xPercent: 0,
            yPercent: -50,
        });
        gsap.set(navLinks, { opacity: 1, right: statusInset });
        gsap.set(mainContent, { display: 'block', opacity: 1 });
        gsap.set('.fade-up', { opacity: 1, y: 0 });
        document.body.style.overflow = 'auto';
        document.body.style.overflowX = 'hidden';
        initScrollAnimations();
    };

    mainContent.style.display = 'block';
    hydrateWorkCardsFromMap();
    initCursorBloom();
    initWorkModal();
    lockIntroScroll();

    if (shouldSkipIntro()) {
        snapToTopbar();
    } else {
        const tl = gsap.timeline({ onComplete: finishIntro });

        gsap.set(logo, {
            left: '50%',
            top: '50%',
            xPercent: -50,
            yPercent: -50,
        });

        // Step 1: Black screen, logo breathes
        tl.to(logo, {
            opacity: 1,
            duration: 1.5,
            ease: "power2.inOut",
            delay: 0.2
        })
        .to(logo, {
            opacity: 0.3,
            duration: 1.2,
            yoyo: true,
            repeat: 1,
            ease: "power1.inOut"
        });

        if (!isMobile) {
            // Step 2: Transition to 40% on left side
            tl.to(headerBg, {
                width: '40vw',
                duration: 0.8,
                ease: "expo.inOut"
            }, "+=0.2")
            .to(logo, {
                left: '20vw',
                duration: 0.8,
                ease: "expo.inOut"
            }, "<")

            // Step 3: Right side transitions 4 colors one by one
            .to('.c1', { left: '40vw', duration: 0.25, ease: "power2.out" })
            .set('.c2', { left: '40vw' })
            .to('.c2', { left: '55vw', duration: 0.25, ease: "power2.out" })
            .set('.c3', { left: '55vw' })
            .to('.c3', { left: '70vw', duration: 0.25, ease: "power2.out" })
            .set('.c4', { left: '70vw' })
            .to('.c4', { left: '85vw', duration: 0.25, ease: "power2.out" })

            // Step 4: Black takes over completely
            .set(headerBg, { zIndex: 6 }, "+=0.4")
            .to(headerBg, {
                width: '100vw',
                duration: 1.2,
                ease: "power3.inOut"
            }, "<")
            .to(logo, {
                left: '50vw',
                duration: 1.2,
                ease: "power3.inOut"
            }, "<")
        }

        // Step 5: Transition to topbar
        tl.set(colorPanels, { display: "none" }, isMobile ? "+=0.2" : "+=1.0")
        .to(header, {
            height: headerHeight,
            duration: 1.2,
            ease: "power3.inOut"
        }, "<")
        .to(headerBg, {
            height: headerHeight,
            background: '#000000',
            backdropFilter: 'none',
            borderBottom: 'none',
            duration: 1.2,
            ease: "power3.inOut"
        }, "<")
        .to(logo, {
            width: logoSize,
            height: logoSize,
            top: '50%',
            left: barInset,
            xPercent: 0,
            yPercent: -50,
            duration: 1.2,
            ease: "power3.inOut"
        }, "<")
        .to(navLinks, {
            right: statusInset,
            duration: 1.2,
            ease: "power3.inOut"
        }, "<")

        // Step 6: Fade in status & main content
        .to(navLinks, {
            opacity: 1,
            duration: 0.6,
            ease: "power2.out"
        }, "-=0.2")
        .to(mainContent, {
            opacity: 1,
            duration: 0.8,
            ease: "power2.out"
        }, "-=0.4");
    }

    const yearEl = document.getElementById('year');
    if (yearEl) yearEl.textContent = new Date().getFullYear();

    // Dynamic intro panel colors
    const colorPalettes = [
        [
            'oklch(0.72 0.18 245)',
            'oklch(0.70 0.19 285)',
            'oklch(0.75 0.16 325)',
            'oklch(0.68 0.20 35)'
        ],
        [
            'oklch(0.76 0.16 165)',
            'oklch(0.71 0.19 195)',
            'oklch(0.73 0.17 255)',
            'oklch(0.78 0.14 310)'
        ],
        [
            'oklch(0.69 0.21 295)',
            'oklch(0.74 0.18 330)',
            'oklch(0.71 0.20 15)',
            'oklch(0.72 0.15 210)'
        ],
        [
            'oklch(0.79 0.17 28)',
            'oklch(0.73 0.19 65)',
            'oklch(0.67 0.18 185)',
            'oklch(0.75 0.16 270)'
        ]
    ];

    let currentPaletteIndex = 0;

    function updateAccentColors(paletteIndex) {
        const palette = colorPalettes[paletteIndex % colorPalettes.length];
        const root = document.documentElement;

        root.style.setProperty('--c1', palette[0]);
        root.style.setProperty('--c2', palette[1]);
        root.style.setProperty('--c3', palette[2]);
        root.style.setProperty('--c4', palette[3]);
    }

    function getRandomInterval() {
        return (2 + Math.random() * 2) * 60 * 1000;
    }

    function startColorCycler() {
        const STORAGE_KEY = 'portfolioColorPaletteIndex';
        const savedIndex = localStorage.getItem(STORAGE_KEY);

        if (savedIndex !== null) {
            currentPaletteIndex = parseInt(savedIndex, 10);
        } else {
            currentPaletteIndex = Math.floor(Math.random() * colorPalettes.length);
        }

        updateAccentColors(currentPaletteIndex);

        const cycleColors = () => {
            currentPaletteIndex = (currentPaletteIndex + 1) % colorPalettes.length;
            updateAccentColors(currentPaletteIndex);
            localStorage.setItem(STORAGE_KEY, currentPaletteIndex.toString());
            setTimeout(cycleColors, getRandomInterval());
        };

        setTimeout(cycleColors, getRandomInterval());
    }

    startColorCycler();
});
