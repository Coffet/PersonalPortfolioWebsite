function scrollToSection() {
    const mainContent = document.getElementById('main-content');
    mainContent.scrollIntoView({ behavior: 'smooth' });
}

document.addEventListener('DOMContentLoaded', () => {
    const introContainer = document.getElementById('intro-container');
    const mainContent = document.getElementById('main-content');
    const logo = document.getElementById('animated-logo');
    const navLinks = document.getElementById('nav-links');

    // Ensure initial state
    logo.style.opacity = '0';
    navLinks.style.opacity = '0';
// Disable scrolling during intro
document.body.style.overflow = 'hidden';
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
    // First, make the logo visible with a gentle fade-in
    logo.style.transition = 'opacity 1.5s ease-in-out';
    logo.style.opacity = '1';

    // After the logo appears, fade it out
    setTimeout(() => {
        introContainer.style.backgroundColor = 'black';
        logo.style.transition = 'opacity 2.5s ease-in-out';
        logo.style.opacity = '0';

        // After the fade-out, prepare for the header transition
        setTimeout(() => {
            introContainer.classList.add('final-state');

            // Fade in nav links
            navLinks.style.transition = 'opacity 1s ease-in-out';
            navLinks.style.opacity = '1';

            // Fade in the logo again
            setTimeout(() => {
                logo.style.transition = 'opacity 2.5s ease-in-out';
                logo.style.opacity = '1';

                // Enable the logo to be clickable after the animation
                logo.style.cursor = 'pointer';
                logo.addEventListener('click', () => {
                    window.location.href = 'index.html';
                });

                // Show main content
                setTimeout(() => {
                    mainContent.style.display = 'block';
                    mainContent.style.opacity = '1';
                    // Explicitly enable scrolling
                    document.body.style.overflow = 'auto';
                }, 1000);
            }, 1000); // Delay for the final fade-in
        }, 1000); // Delay for the header transition
    }, 2000); // Delay for the fade-out after the initial fade-in
});

// const token = process.env.GITHUB_TOKEN; // Access the token from environment variables
    
const token = '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'; // Replace with your actual token

if (!token) {
        console.error('GitHub token is not set');
        return;
    }
    // Fetch GitHub user data
    async function fetchGitHubUser() {
        const username = 'Coffet'; // Replace with your GitHub username
        try {
            const response = await fetch(`https://api.github.com/users/${username}/repos?sort=updated`, {
                headers: {
                    Authorization: `token ${token}`
                }
            });
            if (!response.ok) {
                throw new Error(`Failed to fetch user data: ${response.status}`);
            }
            const userData = await response.json();
            const userContainer = document.getElementById('user-container');
            if (!userContainer) {
                console.error('User container not found');
                return;
            }
            userContainer.innerHTML = `
                <h2>${userData.name}</h2>
                <p>${userData.bio || 'No bio available'}</p>
                <p>Followers: ${userData.followers} | Following: ${userData.following}</p>
                <p>Public Repos: ${userData.public_repos}</p>
                <a href="${userData.html_url}" target="_blank">View Profile</a>
            `;
        } catch (error) {
            console.error('Error fetching GitHub user:', error);
        }
    }

    // Add GitHub repository fetching functionality with dynamic language colors
    async function fetchGitHubRepos() {
        const username = 'Coffet'; // Replace with your GitHub username
        let languageColors = {};

        // Fetch language colors dynamically
        try {
            const colorsResponse = await fetch('https://raw.githubusercontent.com/ozh/github-colors/master/colors.json');
            if (colorsResponse.ok) {
                languageColors = await colorsResponse.json();
            } else {
                console.error('Failed to fetch language colors');
            }
        } catch (error) {
            console.error('Error fetching language colors:', error);
        }

        try {
            // Fetch repositories
            const response = await fetch(`https://api.github.com/users/${username}/repos?sort=updated`, {
                headers: {
                    Authorization: `token ${token}`
                }
            });            if (!response.ok) {
                throw new Error(`Failed to fetch repositories: ${response.status}`);
            }
            const repos = await response.json();
            
            const reposContainer = document.getElementById('repos-container');
            if (!reposContainer) {
                console.error('Repos container not found');
                return;
            }
            reposContainer.innerHTML = '';

            for (const repo of repos) {
                try {
                    // Fetch languages for each repository
                    const languagesResponse = await fetch(repo.languages_url);
                    if (!languagesResponse.ok) {
                        throw new Error(`Failed to fetch languages for ${repo.name}`);
                    }
                    const languagesData = await languagesResponse.json();
                    const languages = Object.keys(languagesData);

                    const repoCard = document.createElement('div');
                    repoCard.className = 'repo-card';
                    
                    // Create language dots with dynamic colors and fallback
                    const languageDots = languages.length > 0 
                        ? languages.map(lang => {
                            const color = languageColors[lang] ? languageColors[lang].color : '#cccccc';
                            return `<span class="language-dot" style="background-color: ${color};" title="${lang}"></span>`;
                        }).join('')
                        : '<span class="language-dot" style="background-color: #cccccc;" title="Not Specified"></span>';

                    repoCard.innerHTML = `
                        <h3><a href="${repo.html_url}" target="_blank">${repo.name}</a></h3>
                        <p>${repo.description || 'No description available'}</p>
                        <div class="repo-stats">
                            <span>⭐ ${repo.stargazers_count}</span>
                            <span>🍴 ${repo.forks_count}</span>
                            <span>Updated: ${new Date(repo.updated_at).toLocaleDateString()}</span>
                        </div>
                        <div class="repo-languages">
                            <span class="language-label">Languages:</span> 
                            <div class="language-dots">
                                ${languageDots}
                            </div>
                        </div>
                    `;
                    reposContainer.appendChild(repoCard);
                } catch (langError) {
                    console.error(`Error fetching languages for ${repo.name}:`, langError);
                    // Continue with next repo if languages fetch fails
                }
            }
        } catch (error) {
            console.error('Error fetching GitHub repos:', error);
            const reposContainer = document.getElementById('repos-container');
            if (reposContainer) {
            
                reposContainer.innerHTML = `
                <div class="repo-error">
                    <p>Error loading repositories</p>
                    <p>If this happens, it means that the site is fetching public data via unauthenticated requests of the GitHub API. </p>
                    <p>Unauthenticated requests are associated with the originating IP address, not with the user or application that made the request. </p>
                    <p>The primary rate limit for unauthenticated requests is <span style="color: red; font-weight: bold; font-size: 1.3em;">60</span> requests per hour.</p>
                    <p>Please try again later or contact the site owner.</p>
                </div>
            `;
            
            }
        }
    }


    
    // Call the function after the intro animation
    setTimeout(fetchGitHubRepos, 4500);
});
