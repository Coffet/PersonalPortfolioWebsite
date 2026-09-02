<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/jsp/layout/public-head.jspf" %>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css?v=14">
    <link rel="stylesheet" href="${ctx}/assets/css/gallery.css?v=40">
</head>
<body class="has-settled-header">
    <c:set var="headerSettled" value="true" />
    <c:set var="headerCycle" value="true" />
    <c:set var="headerLocation" value="You are now on: Gallery" />
    <%@ include file="/WEB-INF/jsp/layout/public-header.jspf" %>

    <main class="page-shell">
        <div class="wrap detail-layout">
            <a class="page-return" href="${ctx}/gallery">
                <svg class="page-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                    <path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Return
            </a>

            <section class="page-intro">
                <div class="detail-meta">
                    <c:forEach items="${entry.tags}" var="tag">
                        <span class="gallery-card__tag">${tag}</span>
                    </c:forEach>
                    <span>${empty entry.publishedAt ? 'Published' : fn:substring(entry.publishedAt, 0, 10)}</span>
                </div>
                <h1 class="detail-title">${entry.title}</h1>
                <c:if test="${not empty entry.introText}">
                    <p class="detail-copy">${entry.introText}</p>
                </c:if>
            </section>

            <c:if test="${not empty entry.body}">
                <section class="detail-richtext">
                    <p>${entry.body}</p>
                </section>
            </c:if>

            <c:if test="${not empty entry.media}">
                <div class="gallery-stage" data-gallery-stage>
                    <c:forEach items="${entry.media}" var="media" varStatus="mediaStatus">
                        <img src="${ctx}${media.filePath}"
                             alt="${empty media.altText ? entry.title : media.altText}"
                             decoding="async"
                             <c:if test="${not mediaStatus.first}">hidden</c:if>>
                    </c:forEach>
                </div>
            </c:if>
        </div>
    </main>
    <footer class="gallery-dock">
        <div class="wrap gallery-dock__inner">
            <p>&copy; coft</p>
            <nav class="gallery-dock__nav" aria-label="Footer">
                <a href="${ctx}/#about">About</a>
                <a href="mailto:KennyCCW@protonmail.com">Contact</a>
            </nav>
        </div>
    </footer>
    <script src="${ctx}/assets/js/gallery-viewer.js?v=16" defer></script>
</body>
</html>
