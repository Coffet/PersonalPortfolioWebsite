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
                    <p class="eyebrow">Content</p>
                    <h1>Projects</h1>
                    <p>Case studies for the homepage and the public work pages.</p>
                </div>
                <div class="page-actions">
                    <a class="btn" href="${ctx}/cmsmgmnt/projects/new">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                        New project
                    </a>
                </div>
            </header>

            <div data-filter-scope>
                <div class="toolbar">
                    <label class="search">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="6.5" fill="none" stroke="currentColor" stroke-width="1.7"/><path d="m16 16 4 4" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/></svg>
                        <span class="visually-hidden">Search projects</span>
                        <input type="search" data-filter-search placeholder="Search projects&hellip;" autocomplete="off">
                    </label>
                    <div class="segmented" role="group" aria-label="Filter by state">
                        <button type="button" data-filter-status="all" aria-pressed="true">All</button>
                        <button type="button" data-filter-status="live" aria-pressed="false">Live</button>
                        <button type="button" data-filter-status="draft" aria-pressed="false">Drafts</button>
                    </div>
                    <p class="toolbar__count" data-filter-count></p>
                </div>

                <c:choose>
                    <c:when test="${empty projects}">
                        <div class="empty">
                            <span class="empty__icon" aria-hidden="true">
                                <svg viewBox="0 0 24 24"><rect x="4" y="5" width="16" height="14" rx="2" fill="none" stroke="currentColor" stroke-width="1.7"/><path d="M8 9h8M8 13h5" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/></svg>
                            </span>
                            <strong>No projects yet</strong>
                            <p>Add one and it can lead the homepage, with its own images and case study.</p>
                            <a class="btn" href="${ctx}/cmsmgmnt/projects/new">Create the first project</a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="entries">
                            <c:forEach items="${projects}" var="project">
                                <article class="entry" data-row data-status="${project.published ? 'live' : 'draft'}"
                                         data-search="<c:out value='${project.title} ${project.summary} ${project.yearLabel}'/>">
                                    <div class="entry__thumb ${empty project.cardImagePath ? 'entry__thumb--empty' : ''}">
                                        <c:choose>
                                            <c:when test="${not empty project.cardImagePath}">
                                                <img src="${ctx}${project.cardImagePath}" alt="" width="92" height="68" loading="lazy">
                                            </c:when>
                                            <c:otherwise>
                                                <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="4" y="5" width="16" height="14" rx="2" fill="none" stroke="currentColor" stroke-width="1.7"/><path d="M8 9h8M8 13h5" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/></svg>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="entry__copy">
                                        <h2 class="entry__title"><c:out value="${project.title}"/></h2>
                                        <p class="entry__meta">
                                            <span class="chip ${project.published ? 'chip--live' : 'chip--draft'}">${project.published ? 'Live' : 'Draft'}</span>
                                            <c:if test="${project.featured}">
                                                <span class="chip chip--neutral chip--plain">Featured</span>
                                            </c:if>
                                            <span><c:out value="${project.yearLabel}"/></span>
                                        </p>
                                    </div>
                                    <div class="entry__actions">
                                        <button class="icon-btn" type="button"
                                                data-preview-open="project-${project.id}"
                                                data-preview-name="<c:out value='${project.title}'/>"
                                                data-preview-state="Project &middot; ${project.published ? 'live' : 'draft'}"
                                                <c:if test="${project.published}">data-preview-live="/work/${project.id}"</c:if>
                                                title="Preview as it looks on the public site"
                                                aria-label="Preview <c:out value='${project.title}'/>">
                                            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2.5 12S6 6 12 6s9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6z" fill="none" stroke="currentColor" stroke-width="1.7"/><circle cx="12" cy="12" r="2.6" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                                        </button>
                                        <c:if test="${project.published}">
                                            <a class="icon-btn" href="${ctx}/work/${project.id}" target="_blank" rel="noopener noreferrer" title="Open the live page" aria-label="Open the live page for <c:out value='${project.title}'/>">
                                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M10 6H6a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-4M14 4h6v6M10 14 20 4" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/></svg>
                                            </a>
                                        </c:if>
                                        <a class="btn-ghost btn--sm" href="${ctx}/cmsmgmnt/projects/${project.id}/edit">Edit</a>
                                        <form action="${ctx}/cmsmgmnt/projects/${project.id}/delete" method="post"
                                              data-confirm="&ldquo;<c:out value='${project.title}'/>&rdquo; and its images will be removed from the desk and the public site."
                                              data-confirm-title="Delete this project?">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                            <button class="icon-btn icon-btn--danger" type="submit" title="Delete" aria-label="Delete <c:out value='${project.title}'/>">
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
                <c:forEach items="${projects}" var="project">
                    <template data-preview-key="project-${project.id}">
                        <%@ include file="/WEB-INF/jsp/studio/preview/project-preview.jspf" %>
                    </template>
                </c:forEach>
            </div>

    <%@ include file="/WEB-INF/jsp/layout/desk-shell-close.jspf" %>
</body>
</html>
