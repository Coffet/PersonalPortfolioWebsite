<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/jsp/layout/public-head.jspf" %>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css?v=2">
    <link rel="stylesheet" href="${ctx}/assets/css/gallery.css?v=17">
</head>
<body class="has-settled-header">
    <c:set var="headerSettled" value="true" />
    <c:set var="headerLocation" value="You are now at: Gallery" />
    <%@ include file="/WEB-INF/jsp/layout/public-header.jspf" %>

    <main class="page-shell">
        <div class="wrap">
            <section class="page-intro">
                <a class="page-return" href="${ctx}/">
                    <svg class="page-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                        <path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    Return
                </a>
                <h1 class="page-title">Gallery</h1>
                <p class="page-copy">Welcome to my gallery, where i share some insights in my design process, or some personal stuffs</p>
            </section>

            <div class="gallery-split">
                <div class="gallery-split__main">
                    <div class="order-bar">
                        <p class="order-bar__label" id="gallery-order-label">Order by</p>
                        <div class="order-bar__controls" role="group" aria-labelledby="gallery-order-label">
                            <details class="order-menu" data-order-menu data-order-type="date">
                                <summary>
                                    <svg class="page-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                                        <rect x="3.5" y="5" width="17" height="15.5" rx="2" fill="none" stroke="currentColor" stroke-width="1.75"/>
                                        <path d="M8 3.5v3M16 3.5v3M3.5 10h17" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round"/>
                                    </svg>
                                    <span data-order-label>Date</span>
                                    <svg class="page-icon page-icon--chevron" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                                        <path d="M6 9l6 6 6-6" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                                    </svg>
                                </summary>
                                <div class="order-menu__list" role="listbox" aria-label="Order by date">
                                    <button type="button" role="option" data-order-value="newest" aria-selected="true">Newest</button>
                                    <button type="button" role="option" data-order-value="oldest">Oldest</button>
                                </div>
                            </details>

                            <details class="order-menu" data-order-menu data-order-type="category">
                                <summary>
                                    <svg class="page-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                                        <path d="M4 7h16M4 12h10M4 17h7" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round"/>
                                    </svg>
                                    <span data-order-label>Category</span>
                                    <svg class="page-icon page-icon--chevron" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                                        <path d="M6 9l6 6 6-6" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                                    </svg>
                                </summary>
                                <div class="order-menu__list" role="listbox" aria-label="Filter by category" data-category-options>
                                    <button type="button" role="option" data-order-value="all" aria-selected="true">All</button>
                                </div>
                            </details>
                        </div>
                    </div>

                    <c:choose>
                        <c:when test="${empty galleryEntries}">
                            <div class="empty-state">
                                <strong>Nothing on the wall yet.</strong>
                                <span>When a gallery entry is published, it will land here with its images and notes.</span>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <section class="gallery-grid" aria-label="Gallery entries" data-order-grid>
                                <c:forEach items="${galleryEntries}" var="entry" varStatus="status">
                                    <article class="gallery-card"
                                             data-published-at="${empty entry.publishedAt ? entry.createdAt : entry.publishedAt}"
                                             data-category="${empty entry.category ? 'Gallery' : entry.category}"
                                             style="--stagger: ${status.index}">
                                        <c:if test="${not empty entry.media}">
                                            <button type="button"
                                                    class="gallery-card__media${fn:length(entry.media) > 1 ? ' is-panorama' : ''}"
                                                    data-gallery-viewer
                                                    aria-label="View pictures for ${entry.title}">
                                                <c:forEach items="${entry.media}" var="media" varStatus="mediaStatus">
                                                    <img src="${ctx}${media.filePath}"
                                                         alt="${empty media.altText ? entry.title : media.altText}"
                                                         <c:if test="${status.index > 1 || mediaStatus.index > 0}">loading="lazy"</c:if>
                                                         decoding="async">
                                                </c:forEach>
                                            </button>
                                        </c:if>
                                        <div class="gallery-card__body">
                                            <div class="gallery-card__meta">
                                                <span class="gallery-card__tag">${empty entry.category ? 'Gallery' : entry.category}</span>
                                                <span>${empty entry.publishedAt ? 'Published' : fn:substring(entry.publishedAt, 0, 10)}</span>
                                            </div>
                                            <h2 class="gallery-card__title">
                                                <a href="${ctx}/gallery/${entry.id}">${entry.title}</a>
                                            </h2>
                                            <c:if test="${not empty entry.introText}">
                                                <p class="gallery-card__text">${entry.introText}</p>
                                            </c:if>
                                        </div>
                                    </article>
                                </c:forEach>
                            </section>
                            <p class="order-empty" data-order-empty hidden>No entries match that category.</p>
                        </c:otherwise>
                    </c:choose>
                </div>

                <c:if test="${not empty featuredEntry}">
                    <aside class="gallery-split__aside featured-selection" aria-labelledby="featured-selection-title">
                        <h2 class="featured-selection__title" id="featured-selection-title">Featured Selection</h2>
                        <article class="gallery-card gallery-card--featured">
                            <c:if test="${not empty featuredEntry.media}">
                                <button type="button"
                                        class="gallery-card__media${fn:length(featuredEntry.media) > 1 ? ' is-panorama' : ''}"
                                        data-gallery-viewer
                                        aria-label="View pictures for ${featuredEntry.title}">
                                    <c:forEach items="${featuredEntry.media}" var="media">
                                        <img src="${ctx}${media.filePath}"
                                             alt="${empty media.altText ? featuredEntry.title : media.altText}"
                                             decoding="async">
                                    </c:forEach>
                                </button>
                            </c:if>
                            <div class="gallery-card__body">
                                <div class="gallery-card__meta">
                                    <span class="gallery-card__tag">${empty featuredEntry.category ? 'Gallery' : featuredEntry.category}</span>
                                    <span>${empty featuredEntry.publishedAt ? 'Published' : fn:substring(featuredEntry.publishedAt, 0, 10)}</span>
                                </div>
                                <h3 class="gallery-card__title">
                                    <a href="${ctx}/gallery/${featuredEntry.id}">${featuredEntry.title}</a>
                                </h3>
                                <c:if test="${not empty featuredEntry.introText}">
                                    <p class="gallery-card__text">${featuredEntry.introText}</p>
                                </c:if>
                            </div>
                        </article>
                    </aside>
                </c:if>
            </div>
        </div>
    </main>
    <script src="${ctx}/assets/js/gallery-order.js?v=3" defer></script>
    <script src="${ctx}/assets/js/gallery-viewer.js?v=5" defer></script>
</body>
</html>
