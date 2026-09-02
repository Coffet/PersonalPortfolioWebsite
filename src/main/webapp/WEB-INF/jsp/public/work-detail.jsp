<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/jsp/layout/public-head.jspf" %>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css?v=16">
    <link rel="stylesheet" href="${ctx}/assets/css/gallery.css?v=41">
</head>
<body class="has-settled-header">
    <c:set var="headerSettled" value="true" />
    <c:set var="headerLocation" value="You are now at: Work" />
    <%@ include file="/WEB-INF/jsp/layout/public-header.jspf" %>

    <main class="page-shell">
        <article class="wrap work-case">
            <a class="page-return" href="${ctx}/#work">
                <svg class="page-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                    <path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Return
            </a>

            <header class="work-case__intro">
                <p class="work-case__kicker">${project.yearLabel}</p>
                <h1 class="work-case__title">${project.title}</h1>
                <c:if test="${not empty project.summary}">
                    <p class="work-case__lede">${project.summary}</p>
                </c:if>
            </header>

            <c:if test="${not empty project.media}">
                <div class="gallery-stage work-case__stage" data-gallery-stage>
                    <c:forEach items="${project.media}" var="media" varStatus="mediaStatus">
                        <img src="${ctx}${media.filePath}"
                             alt="${empty media.altText ? project.title : media.altText}"
                             decoding="async"
                             <c:if test="${mediaStatus.first}">fetchpriority="high"</c:if>
                             <c:if test="${not mediaStatus.first}">hidden</c:if>>
                    </c:forEach>
                </div>
            </c:if>

            <div class="work-case__layout${empty project.narrative ? ' work-case__layout--solo' : ''}">
                <c:if test="${not empty project.narrative}">
                    <section class="work-case__story">
                        <p>${project.narrative}</p>
                    </section>
                </c:if>

                <aside class="work-case__facts">
                    <div>
                        <h2>Role</h2>
                        <p>${project.role}</p>
                    </div>
                    <div>
                        <h2>Tools</h2>
                        <p>${project.tools}</p>
                    </div>
                    <c:if test="${not empty project.externalLink}">
                        <div>
                            <h2>Visit</h2>
                            <a class="work-case__link" href="${project.externalLink}" target="_blank" rel="noopener noreferrer">${empty project.linkLabel ? 'Visit project' : project.linkLabel}</a>
                        </div>
                    </c:if>
                </aside>
            </div>
        </article>
    </main>
    <script src="${ctx}/assets/js/gallery-viewer.js?v=5" defer></script>
</body>
</html>
