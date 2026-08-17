document.addEventListener('DOMContentLoaded', () => {
    gsap.registerPlugin(ScrollTrigger);

    // Mobile: 100vh is often taller than the visible viewport, so vertical centering looks
    // too low. Pin intro height to the actual visible pixels (see --intro-screen-height in CSS).
    if (window.innerWidth <= 768) {
        const h = window.visualViewport?.height ?? window.innerHeight;
        document.documentElement.style.setProperty('--intro-screen-height', `${h}px`);
    }

    const header = document.getElementById('header');
    const headerBg = document.querySelector('.header-bg');
    const colorPanels = document.querySelectorAll('.color-panel');
    const logo = document.getElementById('logo');
    const navLinks = document.getElementById('nav-links');
    const mainContent = document.getElementById('main-content');
    const isMobile = window.innerWidth <= 768;
    const barInset = isMobile ? '16px' : '30px';
    const statusInset = isMobile ? '28px' : '45px';
    const headerHeight = isMobile ? '80px' : '88px';
    const logoSize = isMobile ? '80px' : '88px';

    const INTRO_KEY = 'portfolioIntroAt';
    const INTRO_SKIP_MS = 30 * 60 * 1000;

    const shouldSkipIntro = () => {
        const last = parseInt(sessionStorage.getItem(INTRO_KEY) || localStorage.getItem(INTRO_KEY), 10);
        return Boolean(last) && Date.now() - last < INTRO_SKIP_MS;
    };

    const markIntroPlayed = () => {
        const now = String(Date.now());
        sessionStorage.setItem(INTRO_KEY, now);
        localStorage.setItem(INTRO_KEY, now);
    };

    const finishIntro = () => {
        document.body.style.overflow = 'auto';
        document.body.style.overflowX = 'hidden';
        initScrollAnimations();
        markIntroPlayed();
    };

    function initScrollAnimations() {
        const fadeUpElements = document.querySelectorAll('.fade-up');

        fadeUpElements.forEach((el) => {
            gsap.to(el, {
                y: 0,
                opacity: 1,
                duration: 1,
                ease: "power3.out",
                scrollTrigger: {
                    trigger: el,
                    start: "top 85%",
                    toggleActions: "play none none none"
                }
            });
        });
    }

    const snapToTopbar = () => {
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
