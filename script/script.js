document.addEventListener('DOMContentLoaded', () => {
    // 1. Initial Setup for GSAP Animation
    gsap.registerPlugin(ScrollTrigger);

    const tl = gsap.timeline({
        onComplete: () => {
            // Enable scrolling after intro
            document.body.style.overflow = 'auto';
            document.body.style.overflowX = 'hidden';
            
            // Initialize ScrollTrigger animations for main content
            initScrollAnimations();
            
            // Load GitHub Data
            getProfile();
            getRepos();
        }
    });

    const header = document.getElementById('header');
    const headerBg = document.querySelector('.header-bg');
    const colorPanels = document.querySelectorAll('.color-panel');
    const logo = document.getElementById('logo');
    const navLinks = document.getElementById('nav-links');
    const mainContent = document.getElementById('main-content');

    // Make sure main content is visible for layout but opacity 0
    mainContent.style.display = 'block';

    // GSAP controls transform during tweens; CSS translate(-50%,-50%) would be lost otherwise,
    // so the intro logo drifts off-center on mobile until the header shrink.
    gsap.set(logo, {
        left: '50%',
        top: '50%',
        xPercent: -50,
        yPercent: -50,
    });

    // 2. The GSAP Timeline (The Opening Animation)
    
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

    const isMobile = window.innerWidth <= 768;

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

    const headerHeight = isMobile ? '80px' : '100px';
    const logoSize = isMobile ? '70px' : '100px';
    const logoTop = isMobile ? '40px' : '50px';
    const logoLeft = isMobile ? '40px' : '50px'; /* Give it proper left margin on mobile */

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
        borderBottom: '1px solid rgba(255,255,255,0.05)',
        duration: 1.2,
        ease: "power3.inOut"
    }, "<")
    .to(logo, {
        width: logoSize,
        height: logoSize,
        top: logoTop,
        left: logoLeft,
        xPercent: 0,
        yPercent: -50,
        duration: 1.2,
        ease: "power3.inOut"
    }, "<")
    
    // Step 6: Fade in nav links & main content
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


    // 3. ScrollTrigger Animations
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
                    start: "top 85%", // Trigger when top of element hits 85% of viewport
                    toggleActions: "play none none none"
                }
            });
        });
        
        // Bento Grid Stagger
        gsap.to('.bento-item', {
            y: 0,
            opacity: 1,
            duration: 0.8,
            stagger: 0.1,
            ease: "power3.out",
            scrollTrigger: {
                trigger: '.bento-grid',
                start: "top 80%",
            }
        });
    }

    // Set current year in footer
    document.getElementById('year').textContent = new Date().getFullYear();

    // 4. GitHub Data Fetching (Refactored for modern UI)
    const username = 'Coffet';
    const maxPages = 2; // Fetch up to 2 pages for speed
    const hideForks = true;

    // Devicons Mapping (Simplified to modern classes)
    const getDevicon = (lang) => {
        if (!lang) return '';
        const map = {
            'JavaScript': 'javascript-plain colored',
            'TypeScript': 'typescript-plain colored',
            'HTML': 'html5-plain colored',
            'CSS': 'css3-plain colored',
            'Python': 'python-plain colored',
            'Java': 'java-plain colored',
            'C++': 'cplusplus-plain colored',
            'C#': 'csharp-plain colored',
            'PHP': 'php-plain colored',
            'Ruby': 'ruby-plain colored',
            'Go': 'go-plain colored',
            'Rust': 'rust-plain colored',
            'Swift': 'swift-plain colored',
            'Kotlin': 'kotlin-plain colored',
            'Dart': 'dart-plain colored',
            'Vue': 'vuejs-plain colored',
            'React': 'react-original colored',
            'Assembly': 'labview-plain colored',
            'Shell': 'bash-plain colored'
        };
        const iconClass = map[lang];
        return iconClass ? `<i class="devicon-${iconClass}"></i>` : `<span>●</span>`;
    };

    const getProfile = async () => {
        try {
            const res = await fetch(`https://api.github.com/users/${username}`);
            if (!res.ok) throw new Error(`Failed to fetch user: ${res.status}`);
            const profile = await res.json();
            
            const statsContainer = document.getElementById('github-stats-container');
            if (statsContainer) {
                statsContainer.innerHTML = `
                    <div class="stat-row">
                        <span class="stat-label">Public Repos</span>
                        <span class="stat-value">${profile.public_repos}</span>
                    </div>
                    <div class="stat-row">
                        <span class="stat-label">Followers</span>
                        <span class="stat-value">${profile.followers}</span>
                    </div>
                    <div class="stat-row">
                        <span class="stat-label">Following</span>
                        <span class="stat-value">${profile.following}</span>
                    </div>
                    <div style="margin-top: 16px;">
                        <a href="${profile.html_url}" target="_blank" class="btn-link" style="width: 100%; justify-content: center; background: rgba(255,255,255,0.1);">
                            <i class="devicon-github-original"></i> View GitHub Profile
                        </a>
                    </div>
                `;
            }
        } catch (error) {
            console.error('Error fetching profile:', error);
            const statsContainer = document.getElementById('github-stats-container');
            if (statsContainer) statsContainer.innerHTML = '<p>Error loading stats</p>';
        }
    };

    const getRepos = async () => {
        try {
            let repos = [];
            for (let i = 1; i <= maxPages; i++) {
                const res = await fetch(`https://api.github.com/users/${username}/repos?sort=pushed&per_page=100&page=${i}`);
                if (!res.ok) throw new Error(`Failed to fetch repos: ${res.status}`);
                const data = await res.json();
                repos = repos.concat(data);
                if (data.length < 100) break; // Reached last page
            }
            
            // Filter forks and sort by stars
            repos = repos.filter(repo => !hideForks || !repo.fork);
            repos.sort((a, b) => b.stargazers_count - a.stargazers_count);
            
            // Display top 6 repos
            displayRepos(repos.slice(0, 6));
        } catch (error) {
            console.error('Error fetching repos:', error);
            const container = document.getElementById('repos-container');
            if (container) container.innerHTML = '<p>Error loading repositories</p>';
        }
    };

    const displayRepos = (repos) => {
        const container = document.getElementById('repos-container');
        if (!container) return;
        
        container.innerHTML = '';
        
        repos.forEach(repo => {
            const card = document.createElement('div');
            card.className = 'repo-card';
            
            card.innerHTML = `
                <div class="repo-header">
                    <a href="${repo.html_url}" target="_blank" class="repo-title">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 22v-4a4.8 4.8 0 0 0-1-3.5c3 0 6-2 6-5.5.08-1.25-.27-2.48-1-3.5.28-1.15.28-2.35 0-3.5 0 0-1 0-3 1.5-2.64-.5-5.36-.5-8 0C6 2 5 2 5 2c-.3 1.15-.3 2.35 0 3.5A5.403 5.403 0 0 0 4 9c0 3.5 3 5.5 6 5.5-.39.49-.68 1.05-.85 1.65-.17.6-.22 1.23-.15 1.85v4"/><path d="M9 18c-4.51 2-5-2-7-2"/></svg>
                        ${repo.name}
                    </a>
                </div>
                <p class="repo-desc">${repo.description || 'A brilliant project waiting to be discovered.'}</p>
                <div class="repo-meta">
                    ${repo.language ? `<span class="repo-meta-item">${getDevicon(repo.language)} ${repo.language}</span>` : ''}
                    ${repo.stargazers_count > 0 ? `<span class="repo-meta-item">⭐ ${repo.stargazers_count}</span>` : ''}
                    ${repo.forks_count > 0 ? `<span class="repo-meta-item">🍴 ${repo.forks_count}</span>` : ''}
                </div>
            `;
            container.appendChild(card);
        });
    };
});