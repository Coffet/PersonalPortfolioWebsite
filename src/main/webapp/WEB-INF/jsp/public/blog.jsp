<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/jsp/layout/public-head.jspf" %>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css?v=2">
    <link rel="stylesheet" href="${ctx}/assets/css/gallery.css?v=13">
</head>
<body class="has-settled-header">
    <c:set var="headerSettled" value="true" />
    <c:set var="headerLocation" value="You are now at: Blog" />
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
                <h1 class="page-title">Blog</h1>
                <p class="page-copy">A place for notes, process, and things that are still becoming.</p>
            </section>

            <div class="order-bar">
                <p class="order-bar__label" id="blog-order-label">Order by</p>
                <div class="order-bar__controls" role="group" aria-labelledby="blog-order-label">
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
                </div>
            </div>

            <c:choose>
                <c:when test="${empty blogPosts}">
                    <div class="empty-state">
                        <strong>No notes published yet.</strong>
                        <span>When a post goes live, it will appear here as a quiet reading list.</span>
                    </div>
                </c:when>
                <c:otherwise>
                    <section class="blog-grid" aria-label="Blog posts" data-order-grid>
                        <c:forEach items="${blogPosts}" var="post" varStatus="status">
                            <a class="blog-card"
                               href="${ctx}/blog/${post.id}"
                               data-published-at="${empty post.publishedAt ? post.createdAt : post.publishedAt}"
                               data-category="Blog"
                               style="--stagger: ${status.index}">
                                <c:if test="${not empty post.coverImagePath}">
                                    <div class="blog-card__media">
                                        <img src="${ctx}${post.coverImagePath}"
                                             alt="${post.title}"
                                             width="640"
                                             height="400"
                                             <c:if test="${status.index > 1}">loading="lazy"</c:if>
                                             decoding="async">
                                    </div>
                                </c:if>
                                <div class="blog-card__meta">
                                    <span>Blog</span>
                                    <span>${empty post.publishedAt ? 'Published' : fn:substring(post.publishedAt, 0, 10)}</span>
                                </div>
                                <h2 class="blog-card__title">${post.title}</h2>
                                <p class="blog-card__text">${post.excerpt}</p>
                            </a>
                        </c:forEach>
                    </section>
                </c:otherwise>
            </c:choose>
        </div>
    </main>
    <script src="${ctx}/assets/js/gallery-order.js?v=2" defer></script>
</body>
</html>
