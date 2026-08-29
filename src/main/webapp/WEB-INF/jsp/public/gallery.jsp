<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/jsp/layout/public-head.jspf" %>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css?v=2">
    <link rel="stylesheet" href="${ctx}/assets/css/gallery.css?v=7">
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
                            <a class="gallery-card"
                               href="${ctx}/gallery/${entry.id}"
                               data-published-at="${empty entry.publishedAt ? entry.createdAt : entry.publishedAt}"
                               data-category="${empty entry.category ? 'Gallery' : entry.category}"
                               style="--stagger: ${status.index}">
                                <c:if test="${not empty entry.media}">
                                    <div class="gallery-card__media">
                                        <img src="${ctx}${entry.media[0].filePath}"
                                             alt="${empty entry.media[0].altText ? entry.title : entry.media[0].altText}"
                                             width="640"
                                             height="400"
                                             <c:if test="${status.index > 1}">loading="lazy"</c:if>
                                             decoding="async">
                                    </div>
                                </c:if>
                                <div class="gallery-card__meta">
                                    <span>${empty entry.category ? 'Gallery' : entry.category}</span>
                                    <span>${empty entry.publishedAt ? 'Published' : fn:substring(entry.publishedAt, 0, 10)}</span>
                                </div>
                                <h2 class="gallery-card__title">${entry.title}</h2>
                                <c:if test="${not empty entry.introText}">
                                    <p class="gallery-card__text">${entry.introText}</p>
                                </c:if>
                            </a>
                        </c:forEach>
                    </section>
                    <p class="order-empty" data-order-empty hidden>No entries match that category.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </main>
    <script src="${ctx}/assets/js/gallery-order.js?v=2" defer></script>
</body>
</html>
