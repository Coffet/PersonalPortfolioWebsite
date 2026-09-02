<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/jsp/layout/public-head.jspf" %>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css?v=17">
    <link rel="stylesheet" href="${ctx}/assets/css/gallery.css?v=43">
</head>
<body class="has-settled-header">
    <c:set var="headerSettled" value="true" />
    <c:set var="headerLocation" value="You are now at: Blog" />
    <%@ include file="/WEB-INF/jsp/layout/public-header.jspf" %>

    <main class="page-shell">
        <div class="wrap detail-layout">
            <a class="page-return" href="${ctx}/blog">
                <svg class="page-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                    <path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Return
            </a>

            <section class="page-intro">
                <div class="detail-meta">
                    <span>Blog</span>
                    <span>${empty post.publishedAt ? 'Published' : fn:substring(post.publishedAt, 0, 10)}</span>
                </div>
                <h1 class="detail-title">${post.title}</h1>
                <p class="detail-copy">${post.excerpt}</p>
            </section>

            <c:if test="${not empty post.coverImagePath}">
                <div class="detail-hero">
                    <img src="${ctx}${post.coverImagePath}" alt="${post.title}" width="1280" height="800" decoding="async">
                </div>
            </c:if>

            <section class="detail-richtext">
                <p>${post.body}</p>
            </section>
        </div>
    </main>
</body>
</html>
