<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/jsp/layout/studio-head.jspf" %>
</head>
<body>
    <%@ include file="/WEB-INF/jsp/layout/desk-shell-open.jspf" %>

            <header class="page-head">
                <div class="page-head__copy">
                    <p class="eyebrow">Content</p>
                    <h1>Gallery</h1>
                    <p>Image sets with notes. Published entries appear on the public gallery.</p>
                </div>
                <div class="page-actions">
                    <a class="btn" href="${ctx}/cmsmgmnt/gallery/new">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                        New entry
                    </a>
                </div>
            </header>

            <div data-filter-scope>
                <div class="toolbar">
                    <label class="search">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="6.5" fill="none" stroke="currentColor" stroke-width="1.7"/><path d="m16 16 4 4" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/></svg>
                        <span class="visually-hidden">Search entries</span>
                        <input type="search" data-filter-search placeholder="Search entries and tags&hellip;" autocomplete="off">
                    </label>
                    <div class="segmented" role="group" aria-label="Filter by state">
                        <button type="button" data-filter-status="all" aria-pressed="true">All</button>
                        <button type="button" data-filter-status="live" aria-pressed="false">Live</button>
                        <button type="button" data-filter-status="draft" aria-pressed="false">Drafts</button>
                    </div>
                    <p class="toolbar__count" data-filter-count></p>
                </div>

                <c:choose>
                    <c:when test="${empty entries}">
                        <div class="empty">
                            <span class="empty__icon" aria-hidden="true">
                                <svg viewBox="0 0 24 24"><rect x="3.5" y="5" width="7" height="7" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.7"/><rect x="13.5" y="5" width="7" height="7" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.7"/><rect x="3.5" y="14" width="7" height="5" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.7"/><rect x="13.5" y="14" width="7" height="5" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                            </span>
                            <strong>No gallery entries yet</strong>
                            <p>Publish one and it lands on the public gallery with its own images and notes.</p>
                            <a class="btn" href="${ctx}/cmsmgmnt/gallery/new">Create the first entry</a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="entries">
                            <c:forEach items="${entries}" var="entry">
                                <article class="entry" data-row data-status="${entry.published ? 'live' : 'draft'}"
                                         data-search="<c:out value='${entry.title} ${entry.category} ${entry.introText}'/>">
                                    <div class="entry__thumb ${empty entry.media ? 'entry__thumb--empty' : ''}">
                                        <c:choose>
                                            <c:when test="${not empty entry.media}">
                                                <img src="${ctx}${entry.media[0].filePath}" alt="" width="92" height="68" loading="lazy">
                                            </c:when>
                                            <c:otherwise>
                                                <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="4" y="5" width="16" height="14" rx="2" fill="none" stroke="currentColor" stroke-width="1.7"/><path d="m8 15 2.5-3 2 2.2L16 11l4 4" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/></svg>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="entry__copy">
                                        <h2 class="entry__title"><c:out value="${entry.title}"/></h2>
                                        <p class="entry__meta">
                                            <span class="chip ${entry.published ? 'chip--live' : 'chip--draft'}">${entry.published ? 'Live' : 'Draft'}</span>
                                            <c:if test="${not empty entry.category}">
                                                <span class="chip chip--neutral chip--plain"><c:out value="${entry.category}"/></span>
                                            </c:if>
                                            <span>${fn:length(entry.media)} image${fn:length(entry.media) eq 1 ? '' : 's'}</span>
                                        </p>
                                    </div>
                                    <div class="entry__actions">
                                        <button class="icon-btn" type="button"
                                                data-preview-open="gallery-${entry.id}"
                                                data-preview-name="<c:out value='${entry.title}'/>"
                                                data-preview-state="Gallery &middot; ${entry.published ? 'live' : 'draft'}"
                                                <c:if test="${entry.published}">data-preview-live="/gallery/${entry.id}"</c:if>
                                                title="Preview as it looks on the public site"
                                                aria-label="Preview <c:out value='${entry.title}'/>">
                                            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2.5 12S6 6 12 6s9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6z" fill="none" stroke="currentColor" stroke-width="1.7"/><circle cx="12" cy="12" r="2.6" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                                        </button>
                                        <c:if test="${entry.published}">
                                            <a class="icon-btn" href="${ctx}/gallery/${entry.id}" target="_blank" rel="noopener noreferrer" title="Open the live page" aria-label="Open the live page for <c:out value='${entry.title}'/>">
                                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M10 6H6a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-4M14 4h6v6M10 14 20 4" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/></svg>
                                            </a>
                                        </c:if>
                                        <a class="btn-ghost btn--sm" href="${ctx}/cmsmgmnt/gallery/${entry.id}/edit">Edit</a>
                                        <form action="${ctx}/cmsmgmnt/gallery/${entry.id}/delete" method="post"
                                              data-confirm="&ldquo;<c:out value='${entry.title}'/>&rdquo; and its images will be removed from the desk and the public site."
                                              data-confirm-title="Delete this gallery entry?">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                            <button class="icon-btn icon-btn--danger" type="submit" title="Delete" aria-label="Delete <c:out value='${entry.title}'/>">
                                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 7h14M10 7V5h4v2M8 7l.8 12h6.4L16 7" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/></svg>
                                            </button>
                                        </form>
                                    </div>
                                </article>
                            </c:forEach>
                        </div>
                        <div class="empty" data-filter-empty hidden>
                            <strong>Nothing matches that</strong>
                            <p>Try another word, or switch the filter back to All.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="preview-store" hidden aria-hidden="true">
                <c:forEach items="${entries}" var="entry">
                    <template data-preview-key="gallery-${entry.id}">
                        <%@ include file="/WEB-INF/jsp/studio/preview/gallery-preview.jspf" %>
                    </template>
                </c:forEach>
            </div>

    <%@ include file="/WEB-INF/jsp/layout/desk-shell-close.jspf" %>
</body>
</html>
