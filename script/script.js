// Function to scroll to the main content section smoothly
function scrollToSection() {
    const mainContent = document.getElementById('main-content');
    mainContent.scrollIntoView({ behavior: 'smooth' });
}

// Devicons object for programming language and tool icons
const devicons = {
    Git: '<i class="devicon-git-plain" style="color: #555"></i>',
    Github: '<i class="devicon-github-plain" style="color: #1688f0"></i>',
    Chrome: '<i class="devicon-chrome-plain" style="color: #1688f0"></i>',
    Assembly: '<i class="devicon-labview-plain colored"></i> Assembly',
    'C#': '<i class="devicon-csharp-plain colored"></i> C#',
    'C++': '<i class="devicon-cplusplus-plain colored"></i> C++',
    C: '<i class="devicon-c-plain colored"></i> C',
    Clojure: '<i class="devicon-clojure-plain colored"></i> C',
    CoffeeScript: '<i class="devicon-coffeescript-plain colored"></i> CoffeeScript',
    Crystal: '<i class="devicon-crystal-plain colored"></i> Crystal',
    CSS: '<i class="devicon-css3-plain colored"></i> CSS',
    Dart: '<i class="devicon-dart-plain colored"></i> Dart',
    Dockerfile: '<i class="devicon-docker-plain colored"></i> Docker',
    Elixir: '<i class="devicon-elixir-plain colored"></i> Elixir',
    Elm: '<i class="devicon-elm-plain colored"></i> Elm',
    Erlang: '<i class="devicon-erlang-plain colored"></i> Erlang',
    'F#': '<i class="devicon-fsharp-plain colored"></i> F#',
    Go: '<i class="devicon-go-plain colored"></i> Go',
    Groovy: '<i class="devicon-groovy-plain colored"></i> Groovy',
    HTML: '<i class="devicon-html5-plain colored"></i> HTML',
    Haskell: '<i class="devicon-haskell-plain colored"></i> Haskell',
    Java: '<i class="devicon-java-plain colored" style="color: #ffca2c"></i> Java',
    JavaScript: '<i class="devicon-javascript-plain colored"></i> JavaScript',
    Julia: '<i class="devicon-julia-plain colored"></i> Julia',
    'Jupyter Notebook': '<i class="devicon-jupyter-plain colored"></i> Jupyter',
    Kotlin: '<i class="devicon-kotlin-plain colored" style="color: #796bdc"></i> Kotlin',
    Latex: '<i class="devicon-latex-plain colored"></i> Latex',
    Lua: '<i class="devicon-lua-plain-wordmark colored" style="color: #0000d0"></i> Lua',
    Matlab: '<i class="devicon-matlab-plain colored"></i> Matlab',
    Nim: '<i class="devicon-nixos-plain colored" style="color: #FFC200"></i> Nim',
    Nix: '<i class="devicon-nixos-plain colored"></i> Nix',
    ObjectiveC: '<i class="devicon-objectivec-plain colored"></i> ObjectiveC',
    OCaml: '<i class="devicon-ocaml-plain colored"></i> OCaml',
    Perl: '<i class="devicon-perl-plain colored"></i> Perl',
    PHP: '<i class="devicon-php-plain colored"></i> PHP',
    PLSQL: '<i class="devicon-sqlite-plain colored"></i> PLSQL',
    Processing: '<i class="devicon-processing-plain colored" style="color: #0096D8"></i> Processing',
    Python: '<i class="devicon-python-plain colored" style="color: #3472a6"></i> Python',
    R: '<i class="devicon-r-plain colored"></i> R',
    Ruby: '<i class="devicon-ruby-plain colored"></i> Ruby',
    Rust: '<i class="devicon-rust-plain colored" style="color: #DEA584"></i> Rust',
    Sass: '<i class="devicon-sass-original colored"></i> Sass',
    Scala: '<i class="devicon-scala-plain colored"></i> Scala',
    Shell: '<i class="devicon-bash-plain colored" style="color: #89E051"></i> Shell',
    Solidity: '<i class="devicon-solidity-plain colored"></i> Solidity',
    Stylus: '<i class="devicon-stylus-plain colored"></i> Stylus',
    Svelte: '<i class="devicon-svelte-plain colored"></i> Svelte',
    Swift: '<i class="devicon-swift-plain colored"></i> Swift',
    Terraform: '<i class="devicon-terraform-plain colored"></i> Terraform',
    TypeScript: '<i class="devicon-typescript-plain colored"></i> TypeScript',
    'Vim Script': '<i class="devicon-vim-plain colored"></i> Vim Script',
    Vue: '<i class="devicon-vuejs-plain colored"></i> Vue'
};

// Constants for GitHub data fetching
const username = 'Coffet'; // Using 'Coffet' from the second code
const maxPages = 3; // Fetch up to 3 pages of repositories
const hideForks = true; // Hide forked repositories

// Note: Define your GitHub token here or use an environment variable
// const token = 'your-personal-access-token-here';
// Alternatively, uncomment the line below if using an environment variable
// const token = process.env.GITHUB_TOKEN;

// Fetch GitHub profile data
const getProfile = async () => {
    try {
        const res = await fetch(
            `https://api.github.com/users/${username}`
             // ,{
        //     headers: {
        //         Accept: 'application/vnd.github+json',
        //         Authorization: 'token your-personal-access-token-here'
        //     }
        // }
        );
        if (!res.ok) {
            throw new Error(`Failed to fetch user data: ${res.status}`);
        }
        const profile = await res.json();
        displayProfile(profile);
    } catch (error) {
        console.error('Error fetching GitHub user:', error);
        const userInfo = document.querySelector('#user-container');
        if (userInfo) {
            userInfo.innerHTML = '<p>Error loading profile data</p>';
        }
    }
};

// Display GitHub profile data in #user-container
const displayProfile = (profile) => {
    const userInfo = document.querySelector('#user-container');
    if (userInfo) {
        userInfo.innerHTML = `
            <figure>
                <img alt="user avatar" src="${profile.avatar_url}" />
            </figure>
            <div>
                <h2><a href="${profile.blog || profile.html_url}" target="_blank"><strong>${profile.name || 'No name'} - ${profile.login}</strong></a></h2>
                <p>${profile.bio || 'No bio available'}</p>
                <p>
                    Followers: <strong>${profile.followers}</strong>
                    Repos: <strong>${profile.public_repos}</strong>
                    Gists: <strong>${profile.public_gists}</strong>
                </p>
                <p>
                    Work: ${profile.company || 'Not specified'}
                    Location: ${profile.location || 'Not specified'}
                </p>
            </div>
        `;
    } else {
        console.error('User container (#user-container) not found');
    }
};

// Fetch GitHub repositories with pagination
const getRepos = async () => {
    try {
        let repos = [];
        for (let i = 1; i <= maxPages; i++) {
            const res = await fetch(
                `https://api.github.com/users/${username}/repos?sort=pushed&per_page=100&page=${i}`
                //, {
                //     headers: {
                //         Authorization: `token ${token}` // Remove this line if not using a token
                //     }
                // }
            );
            if (!res.ok) {
                throw new Error(`Failed to fetch repositories: ${res.status}`);
            }
            const data = await res.json();
            repos = repos.concat(data);
        }
        // Sort by stars and forks (stars take precedence)
        repos.sort((a, b) => b.stargazers_count - a.stargazers_count);
        repos.sort((a, b) => b.forks_count - a.forks_count);
        displayRepos(repos);
    } catch (error) {
        console.error('Error fetching GitHub repos:', error);
        const reposContainer = document.querySelector('#repos-container');
        if (reposContainer) {
            reposContainer.innerHTML = '<p>Error loading repositories</p>';
        }
    }
};

// Display GitHub repositories in #repos-container
const displayRepos = (repos) => {
    const reposContainer = document.querySelector('#repos-container');
    if (reposContainer) {
        reposContainer.innerHTML = '';
        for (const repo of repos) {
            if (repo.fork && hideForks) {
                continue;
            }
            const repoCard = document.createElement('div');
            repoCard.className = 'repo-card';
            let innerHTML = `
                <h3><a href="${repo.html_url}" target="_blank">${repo.name}</a></h3>
                <p>${repo.description || 'No description available'}</p>
                <div class="repo-stats">
            `;
            if (repo.stargazers_count > 0) {
                innerHTML += `<a href="${repo.html_url}/stargazers"><span>⭐ ${repo.stargazers_count}</span></a>`;
            }
            if (repo.language) {
                innerHTML += `<span>${devicons[repo.language] || repo.language}</span>`;
            }
            if (repo.forks_count > 0) {
                innerHTML += `<a href="${repo.html_url}/network/members"><span>🍴 ${repo.forks_count}</span></a>`;
            }
            innerHTML += `</div><div class="repo-links">`;
            innerHTML += `<a class="link-btn" href="${repo.html_url}" target="_blank">${devicons['Github']} Code</a>`;
            if (repo.homepage && repo.homepage !== '') {
                innerHTML += `<a class="link-btn" href="${repo.homepage}" target="_blank">${devicons['Chrome']} Live</a>`;
            }
            innerHTML += `</div>`;
            repoCard.innerHTML = innerHTML;
            reposContainer.appendChild(repoCard);
        }
    } else {
        console.error('Repos container (#repos-container) not found');
    }
};

// Animation and content loading on DOM ready
document.addEventListener('DOMContentLoaded', () => {
    const introContainer = document.getElementById('intro-container');
    const mainContent = document.getElementById('main-content');
    const logo = document.getElementById('animated-logo');
    const navLinks = document.getElementById('nav-links');

    // Ensure initial state
    logo.style.opacity = '0';
    navLinks.style.opacity = '0';
    document.body.style.overflow = 'hidden'; // Disable scrolling during intro

    // Add smooth scroll behavior for navigation links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            document.querySelector(this.getAttribute('href')).scrollIntoView({
                behavior: 'smooth'
            });
        });
    });

    // Add intersection observer for fade-in animations
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, { threshold: 0.1 });

    // Apply fade-in animation to sections
    document.querySelectorAll('section').forEach(section => {
        section.style.opacity = '0';
        section.style.transform = 'translateY(20px)';
        section.style.transition = 'all 1s ease-out';
        observer.observe(section);
    });

    // Sequence the animations with smoother transitions
    requestAnimationFrame(() => {
        // Fade in the logo
        logo.style.transition = 'opacity 1.5s ease-in-out';
        logo.style.opacity = '1';

        setTimeout(() => {
            introContainer.style.backgroundColor = 'black';
            logo.style.transition = 'opacity 2.5s ease-in-out';
            logo.style.opacity = '0';

            setTimeout(() => {
                introContainer.classList.add('final-state');

                // Fade in nav links
                navLinks.style.transition = 'opacity 1s ease-in-out';
                navLinks.style.opacity = '1';

                setTimeout(() => {
                    logo.style.transition = 'opacity 2.5s ease-in-out';
                    logo.style.opacity = '1';

                    // Enable logo click
                    logo.style.cursor = 'pointer';
                    logo.addEventListener('click', () => {
                        window.location.href = 'index.html';
                    });

                    // Show main content
                    setTimeout(() => {
                        mainContent.style.display = 'block';
                        mainContent.style.opacity = '1';
                        document.body.style.overflow = 'auto';

                        // Fetch GitHub data after animation
                        setTimeout(() => {
                            getProfile();
                            getRepos();
                        }, 1000); // Small delay to ensure visibility
                    }, 1000);
                }, 1000);
            }, 1000);
        }, 2000);
    });
});
