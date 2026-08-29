<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/jsp/layout/public-head.jspf" %>
    <link rel="stylesheet" href="${ctx}/assets/css/style.css?v=2">
    <link rel="stylesheet" href="${ctx}/assets/css/gallery.css?v=7">
</head>
<body class="has-settled-header">
    <c:set var="headerSettled" value="true" />
    <c:set var="headerLocation" value="You are now at: Work" />
    <%@ include file="/WEB-INF/jsp/layout/public-header.jspf" %>

    <main class="page-shell">
        <div class="wrap detail-layout">
            <a class="page-return" href="${ctx}/">
                <svg class="page-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                    <path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                Return
            </a>

            <section class="page-intro">
                <div class="detail-meta">
                    <span>${project.yearLabel}</span>
                    <span>${project.role}</span>
                </div>
                <h1 class="detail-title">${project.title}</h1>
                <p class="detail-copy">${project.summary}</p>
            </section>

            <c:if test="${not empty project.media}">
                <div class="detail-hero">
                    <img src="${ctx}${project.media[0].filePath}" alt="${project.media[0].altText}">
                </div>
            </c:if>

            <section class="detail-richtext">
                <p>${project.narrative}</p>
                <p><strong>Tools:</strong> ${project.tools}</p>
            </section>

            <div class="detail-links">
                <c:if test="${not empty project.externalLink}">
                    <a class="detail-link" href="${project.externalLink}" target="_blank" rel="noopener noreferrer">
                        ${empty project.linkLabel ? 'Visit project' : project.linkLabel}
                    </a>
                </c:if>
                <a class="detail-link detail-link--ghost" href="${ctx}/">Back to home</a>
            </div>

            <c:if test="${not empty project.media}">
                <section class="detail-gallery">
                    <c:forEach items="${project.media}" var="media">
                        <figure class="detail-gallery__item">
                            <img src="${ctx}${media.filePath}" alt="${media.altText}">
                        </figure>
                    </c:forEach>
                </section>
            </c:if>
        </div>
    </main>
</body>
</html>
