<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/jsp/layout/studio-head.jspf" %>
</head>
<body>
    <%@ include file="/WEB-INF/jsp/layout/desk-shell-open.jspf" %>

            <header class="page-head">
                <div class="page-head__copy">
                    <p class="eyebrow">Library</p>
                    <h1>Media</h1>
                    <p>Every image already attached to a project, gallery entry or note. Click any image to open it full size.</p>
                </div>
            </header>

            <div data-filter-scope>
                <div class="toolbar">
                    <label class="search">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="6.5" fill="none" stroke="currentColor" stroke-width="1.7"/><path d="m16 16 4 4" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/></svg>
                        <span class="visually-hidden">Search media</span>
                        <input type="search" data-filter-search placeholder="Search by title or type&hellip;" autocomplete="off">
                    </label>
                    <p class="toolbar__count" data-filter-count></p>
                </div>

                <c:choose>
                    <c:when test="${empty mediaItems}">
                        <div class="empty">
                            <span class="empty__icon" aria-hidden="true">
                                <svg viewBox="0 0 24 24"><rect x="4" y="5" width="16" height="14" rx="2" fill="none" stroke="currentColor" stroke-width="1.7"/><path d="m8 15 2.5-3 2 2.2L16 11l4 4" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/><circle cx="9" cy="9" r="1.2" fill="currentColor"/></svg>
                            </span>
                            <strong>No images yet</strong>
                            <p>Upload from a project, gallery entry or blog note and it will be collected here automatically.</p>
                            <a class="btn-ghost" href="${ctx}/cmsmgmnt/gallery/new">Start with a gallery entry</a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="media-strip">
                            <c:forEach items="${mediaItems}" var="item">
                                <article class="media-card" data-row
                                         data-search="<c:out value='${item.ownerTitle} ${item.kind} ${item.altText}'/>">
                                    <img src="${ctx}${item.filePath}"
                                         alt="<c:out value='${empty item.altText ? item.ownerTitle : item.altText}'/>"
                                         loading="lazy">
                                    <div class="media-card__body">
                                        <strong><c:out value="${item.ownerTitle}"/></strong>
                                        <span><c:out value="${item.kind}"/></span>
                                    </div>
                                </article>
                            </c:forEach>
                        </div>
                        <div class="empty" data-filter-empty hidden>
                            <strong>Nothing matches that</strong>
                            <p>Try a different word &mdash; the search covers titles, types and alt text.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

    <%@ include file="/WEB-INF/jsp/layout/desk-shell-close.jspf" %>
</body>
</html>
