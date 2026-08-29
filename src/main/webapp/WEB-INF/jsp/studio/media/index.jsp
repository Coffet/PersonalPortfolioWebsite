<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/jsp/layout/studio-head.jspf" %>
</head>
<body>
    <%@ include file="/WEB-INF/jsp/layout/desk-shell-open.jspf" %>

            <header class="studio-header">
                <div>
                    <h1>Media</h1>
                    <p>A quiet index of images already attached to projects, gallery entries, and notes.</p>
                </div>
            </header>

            <c:choose>
                <c:when test="${empty mediaItems}">
                    <p class="empty-state">No images yet. Upload from a project, gallery entry, or blog note.</p>
                </c:when>
                <c:otherwise>
                    <div class="media-strip">
                        <c:forEach items="${mediaItems}" var="item">
                            <article class="media-card">
                                <img src="${ctx}${item.filePath}" alt="${empty item.altText ? item.ownerTitle : item.altText}" loading="lazy">
                                <div class="media-card__body">
                                    <strong>${item.ownerTitle}</strong>
                                    <span class="muted">${item.kind}</span>
                                </div>
                            </article>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>

    <%@ include file="/WEB-INF/jsp/layout/desk-shell-close.jspf" %>
</body>
</html>
