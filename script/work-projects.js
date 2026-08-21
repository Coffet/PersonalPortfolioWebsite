// This is the data for the work projects.
// It is used to populate the work grid and the work modal.
// The data is structured as follows:
// - title: The title of the project.
// - desc: The description of the project.
// - year: The year the project was completed.
// - role: The role of the project.
// - tools: The tools used to create the project.
// - link: The link to the project.
// - linkLabel: The label for the link.
// - fallbackImage: A general fallback image used when cardImage/modalImage(s) are missing.
// - cardImage: The image for the project card.
// - modalImage: Single image for the project modal (optional if modalImages is used).
// - modalImages: Array of images shown in View Details (preferred).
// - cardGradient: The gradient for the project card.
// - cardImageMode: "cover" (default) or "contain" for wide logos.
// - cardImageScale: Optional number (0–1). When mode is "contain", scales the logo down.


// Format example:
// window.WORK_PROJECTS = {
//   someId: {
//     title: "Project Name",
//     desc: "Short description.",
//     year: "2026",
//     role: "What you did.",
//     tools: "Tool A, Tool B",
//     link: "https://example.com",
//     linkLabel: "Visit project",
//     fallbackImage: "./image/PJKT_x_img/logo.png",
//     cardImage: "./image/PJKT_x_img/logo.png",
//     modalImage: "./image/PJKT_x_img/shot.png",
//     modalImages: [
//       "./image/PJKT_x_img/shot-01.png",
//       "./image/PJKT_x_img/shot-02.png",
//     ],
//     cardGradient: "linear-gradient(135deg, #000 0%, #111 100%)",
//     cardImageMode: "cover"/"contain",
//     cardImageScale: x.x,
//   },
//   anotherId: { ... },
// };

window.WORK_PROJECTS = {
    pj1: {
        title: "Singularity",
        desc: "AI-assisted software requirements gathering platform.",
        year: "2025–2026",
        role: "Frontend & backend development, UI/UX design.",
        tools: "React, Node.js, SQLite3, MinIO, Figma, express.js",
        link: "https://github.com/Chung1045/singularity",
        linkLabel: "GitHub",
        fallbackImage: "./image/PJKT_1_img/PJKT_1_Logo.png",
        cardImage: "./image/PJKT_1_img/PJKT_1_Logo.png",
        modalImages: [
            "./image/PJKT_1_img/PJKT_1_Modal_01.png",
            "./image/PJKT_1_img/PJKT_1_Modal_02.png",
            "./image/PJKT_1_img/PJKT_1_Modal_03.png",
            "./image/PJKT_1_img/PJKT_1_Modal_04.png",
            "./image/PJKT_1_img/PJKT_1_Modal_05.png",
            "./image/PJKT_1_img/PJKT_1_Modal_06.png",
            "./image/PJKT_1_img/PJKT_1_Modal_07.png",
            "./image/PJKT_1_img/PJKT_1_Modal_08.png",
            "./image/PJKT_1_img/PJKT_1_Modal_09.png",
            "./image/PJKT_1_img/PJKT_1_Modal_10.png",
            "./image/PJKT_1_img/PJKT_1_Modal_11.png",
        ],
        cardGradient: "linear-gradient(180deg,rgb(255, 255, 255) 0%,rgb(255, 255, 255) 100%)",
        cardImageMode: "contain",
        cardImageScale: 0.2,
    },
};
