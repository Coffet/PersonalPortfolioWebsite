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
                    <h1>Gallery</h1>
                    <p>Entries published here appear on the public gallery.</p>
                </div>
                <div class="studio-actions">
                    <a class="button" href="${ctx}/cmsmgmnt/gallery/new">New entry</a>
                </div>
            </header>

            <c:choose>
                <c:when test="${empty entries}">
                    <p class="empty-state">No gallery entries yet. Publish one and it will appear on the public Gallery page.</p>
                </c:when>
                <c:otherwise>
                    <div class="work-list">
                        <c:forEach items="${entries}" var="entry">
                            <article class="work-item">
                                <div class="work-item__media">
                                    <c:if test="${not empty entry.media}">
                                        <img src="${ctx}${entry.media[0].filePath}" alt="${empty entry.media[0].altText ? entry.title : entry.media[0].altText}" width="112" height="80" loading="lazy">
                                    </c:if>
                                </div>
                                <div class="work-item__copy">
                                    <h2>${entry.title}</h2>
                                    <p>
                                        ${empty entry.category ? 'Gallery' : entry.category}
                                        ·
                                        <span class="chip ${entry.published ? 'chip--live' : 'chip--draft'}">${entry.published ? 'Live' : 'Draft'}</span>
                                    </p>
                                </div>
                                <div class="studio-actions">
                                    <c:if test="${entry.published}">
                                        <a class="button-ghost" href="${ctx}/gallery/${entry.id}" target="_blank" rel="noopener noreferrer">View live</a>
                                    </c:if>
                                    <a class="button-ghost" href="${ctx}/cmsmgmnt/gallery/${entry.id}/edit">Edit</a>
                                    <form action="${ctx}/cmsmgmnt/gallery/${entry.id}/delete" method="post">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <button class="button-danger" type="submit" onclick="return confirm('Delete this gallery entry?')">Delete</button>
                                    </form>
                                </div>
                            </article>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>

    <%@ include file="/WEB-INF/jsp/layout/desk-shell-close.jspf" %>
</body>
</html>
