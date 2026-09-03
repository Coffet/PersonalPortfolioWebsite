<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/jsp/layout/public-head.jspf" %>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css?v=18">
    <link rel="stylesheet" href="${ctx}/assets/css/gallery.css?v=44">
</head>
<body class="has-settled-header gallery-page">
    <c:set var="headerSettled" value="true" />
    <c:set var="headerCycle" value="true" />
    <c:set var="headerLocation" value="You are now on: Gallery" />
    <%@ include file="/WEB-INF/jsp/layout/public-header.jspf" %>

    <main class="page-shell">
        <div class="wrap">
            <section class="gallery-intro">
                <a class="page-return" href="${ctx}/">
                    <svg class="page-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Return
                </a>
                <p class="gallery-kicker">Gallery</p>
                <h1 class="gallery-title">Selected visuals</h1>
                <p class="gallery-lede">Welcome to my gallery, where i share some insights in my design process, or some personal stuffs</p>
            </section>

            <c:if test="${not empty featuredEntry}">
                <section class="gallery-featured" aria-labelledby="featured-title">
                    <p class="gallery-kicker gallery-kicker--inline">Featured</p>
                    <article class="gallery-featured__card">
                        <c:set var="featuredPic" value="${not empty featuredMedia ? featuredMedia : featuredEntry.coverMedia}" />
                        <c:if test="${not empty featuredPic}">
                            <button type="button"
                                    class="gallery-featured__frame"
                                    data-gallery-viewer
                                    aria-label="View pictures for ${featuredEntry.title}">
                                <c:forEach items="${featuredEntry.media}" var="media">
                                    <img src="${ctx}${media.filePath}"
                                         alt="${empty media.altText ? featuredEntry.title : media.altText}"
                                         class="${media.id eq featuredPic.id ? 'gallery-featured__image' : ''}"
                                         <c:if test="${media.id ne featuredPic.id}">hidden</c:if>
                                         decoding="async">
                                </c:forEach>
                            </button>
                        </c:if>
                        <div class="gallery-featured__body">
                            <h2 class="gallery-featured__title" id="featured-title">
                                <a href="${ctx}/gallery/${featuredEntry.id}">${featuredEntry.title}</a>
                            </h2>
                            <c:if test="${not empty featuredEntry.introText or not empty featuredEntry.displayDate}">
                                <p class="gallery-featured__text">
                                    <c:if test="${not empty featuredEntry.introText}">${featuredEntry.introText}</c:if><c:if test="${not empty featuredEntry.introText and not empty featuredEntry.displayDate}"> &middot; </c:if><c:if test="${not empty featuredEntry.displayDate}">${featuredEntry.displayDate}</c:if>
                                </p>
                            </c:if>
                        </div>
                    </article>
                </section>
            </c:if>

            <c:choose>
                <c:when test="${empty galleryEntries}">
                    <div class="empty-state">
                        <strong>Nothing on the wall yet.</strong>
                        <span>When a gallery entry is published, it will land here with its images and notes.</span>
                    </div>
                </c:when>
                <c:otherwise>
                    <section class="gallery-all" aria-labelledby="all-title">
                        <p class="gallery-kicker gallery-kicker--inline" id="all-title">All</p>
                        <div class="gallery-mosaic">
                            <c:forEach items="${galleryEntries}" var="entry" varStatus="status">
                                <article class="gallery-tile" style="--stagger: ${status.index}">
                                    <c:if test="${not empty entry.coverMedia}">
                                        <button type="button"
                                                class="gallery-tile__frame"
                                                data-gallery-viewer
                                                aria-label="View pictures for ${entry.title}">
                                            <c:forEach items="${entry.media}" var="media" varStatus="mediaStatus">
                                                <img src="${ctx}${media.filePath}"
                                                     alt="${empty media.altText ? entry.title : media.altText}"
                                                     class="${media.id eq entry.coverMedia.id ? 'gallery-tile__image' : ''}"
                                                     <c:if test="${media.id ne entry.coverMedia.id}">hidden</c:if>
                                                     <c:if test="${status.index gt 1 or mediaStatus.index gt 0}">loading="lazy"</c:if>
                                                     decoding="async">
                                            </c:forEach>
                                        </button>
                                    </c:if>
                                    <div class="gallery-tile__body">
                                        <h2 class="gallery-tile__title">
                                            <a href="${ctx}/gallery/${entry.id}">${entry.title}</a>
                                        </h2>
                                        <c:if test="${not empty entry.introText or not empty entry.displayDate}">
                                            <p class="gallery-tile__text">
                                                <c:if test="${not empty entry.introText}">${entry.introText}</c:if><c:if test="${not empty entry.introText and not empty entry.displayDate}"> &middot; </c:if><c:if test="${not empty entry.displayDate}">${entry.displayDate}</c:if>
                                            </p>
                                        </c:if>
                                    </div>
                                </article>
                            </c:forEach>
                        </div>
                    </section>
                </c:otherwise>
            </c:choose>
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
    <script src="${ctx}/assets/js/gallery-viewer.js?v=18" defer></script>
</body>
</html>
