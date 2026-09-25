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
                    <p class="eyebrow">Overview</p>
                    <h1>What needs attention</h1>
                    <p>Drafts wait here. Published pieces are already live on the public site.</p>
                </div>
                <div class="page-actions">
                    <button class="btn-ghost" type="button" data-palette-open>
                        <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="6.5" fill="none" stroke="currentColor" stroke-width="1.7"/><path d="m16 16 4 4" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/></svg>
                        Jump to
                    </button>
                </div>
            </header>

            <section class="stats" aria-label="Content at a glance">
                <article class="stat">
                    <p class="stat__label">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="4" y="5" width="16" height="14" rx="2" fill="none" stroke="currentColor" stroke-width="1.7"/><path d="M8 9h8M8 13h5" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/></svg>
                        Projects
                    </p>
                    <p class="stat__value">${counts.projects}</p>
                    <p class="stat__note">${fn:length(draftProjects)} waiting as draft</p>
                </article>
                <article class="stat">
                    <p class="stat__label">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3.5" y="5" width="7" height="7" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.7"/><rect x="13.5" y="5" width="7" height="7" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.7"/><rect x="3.5" y="14" width="7" height="5" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.7"/><rect x="13.5" y="14" width="7" height="5" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                        Gallery
                    </p>
                    <p class="stat__value">${counts.galleryEntries}</p>
                    <p class="stat__note">${fn:length(draftGallery)} waiting as draft</p>
                </article>
                <article class="stat">
                    <p class="stat__label">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 5h9a3 3 0 0 1 3 3v11H8a2 2 0 0 1-2-2z" fill="none" stroke="currentColor" stroke-width="1.7"/><path d="M9 9h8M9 13h6" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/></svg>
                        Blog
                    </p>
                    <p class="stat__value">${counts.blogPosts}</p>
                    <p class="stat__note">${fn:length(draftPosts)} waiting as draft</p>
                </article>
                <article class="stat">
                    <p class="stat__label">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 7v5l3 2" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/><circle cx="12" cy="12" r="8.5" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                        Audit entries
                    </p>
                    <p class="stat__value">${counts.auditEntries}</p>
                    <p class="stat__note">Sign-ins and content changes</p>
                </article>
            </section>

            <section aria-label="Create" style="margin-bottom: 22px;">
                <div class="shortcut-grid">
                    <a class="shortcut" href="${ctx}/cmsmgmnt/projects/new">
                        <span class="shortcut__icon">
                            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                        </span>
                        <strong>New project</strong>
                        <span>Case study for the homepage</span>
                        <span class="shortcut__arrow" aria-hidden="true">&#8599;</span>
                    </a>
                    <a class="shortcut" href="${ctx}/cmsmgmnt/gallery/new">
                        <span class="shortcut__icon">
                            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                        </span>
                        <strong>New gallery entry</strong>
                        <span>A set of images with notes</span>
                        <span class="shortcut__arrow" aria-hidden="true">&#8599;</span>
                    </a>
                    <a class="shortcut" href="${ctx}/cmsmgmnt/blog/new">
                        <span class="shortcut__icon">
                            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                        </span>
                        <strong>New blog note</strong>
                        <span>Write with the rich editor</span>
                        <span class="shortcut__arrow" aria-hidden="true">&#8599;</span>
                    </a>
                </div>
            </section>

            <div class="grid-2">
                <section class="panel" aria-labelledby="drafts-title">
                    <div class="panel__head">
                        <div>
                            <h2 id="drafts-title">Drafts</h2>
                            <p>Preview opens the public look, right here.</p>
                        </div>
                    </div>
                    <div class="panel__body panel__body--tight">
                        <c:choose>
                            <c:when test="${empty draftProjects and empty draftGallery and empty draftPosts}">
                                <div class="empty">
                                    <span class="empty__icon" aria-hidden="true">
                                        <svg viewBox="0 0 24 24"><path d="M20 7L10 17l-5-5" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/></svg>
                                    </span>
                                    <strong>Nothing waiting</strong>
                                    <p>Every piece is either published or cleared. Start something new whenever you like.</p>
                                    <a class="btn-ghost" href="${ctx}/cmsmgmnt/blog/new">Write a note</a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="entries">
                                    <c:forEach items="${draftProjects}" var="project">
                                        <article class="entry">
                                            <div class="entry__thumb ${empty project.cardImagePath ? 'entry__thumb--empty' : ''}">
                                                <c:choose>
                                                    <c:when test="${not empty project.cardImagePath}">
                                                        <img src="${ctx}${project.cardImagePath}" alt="" width="92" height="68">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="4" y="5" width="16" height="14" rx="2" fill="none" stroke="currentColor" stroke-width="1.7"/><path d="M8 9h8M8 13h5" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/></svg>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <div class="entry__copy">
                                                <h2 class="entry__title"><c:out value="${project.title}"/></h2>
                                                <p class="entry__meta">
                                                    <span class="chip chip--draft">Draft</span>
                                                    <span>Project</span>
                                                </p>
                                            </div>
                                            <div class="entry__actions">
                                                <button class="icon-btn" type="button"
                                                        data-preview-open="project-${project.id}"
                                                        data-preview-name="<c:out value='${project.title}'/>"
                                                        data-preview-state="Project &middot; draft"
                                                        title="Preview as it will look on the site"
                                                        aria-label="Preview <c:out value='${project.title}'/>">
                                                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2.5 12S6 6 12 6s9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6z" fill="none" stroke="currentColor" stroke-width="1.7"/><circle cx="12" cy="12" r="2.6" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                                                </button>
                                                <a class="btn-ghost btn--sm" href="${ctx}/cmsmgmnt/projects/${project.id}/edit">Continue</a>
                                            </div>
                                        </article>
                                    </c:forEach>

                                    <c:forEach items="${draftGallery}" var="entry">
                                        <article class="entry">
                                            <div class="entry__thumb ${empty entry.media ? 'entry__thumb--empty' : ''}">
                                                <c:choose>
                                                    <c:when test="${not empty entry.media}">
                                                        <img src="${ctx}${entry.media[0].filePath}" alt="" width="92" height="68">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="4" y="5" width="16" height="14" rx="2" fill="none" stroke="currentColor" stroke-width="1.7"/><path d="m8 15 2.5-3 2 2.2L16 11l4 4" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/></svg>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <div class="entry__copy">
                                                <h2 class="entry__title"><c:out value="${entry.title}"/></h2>
                                                <p class="entry__meta">
                                                    <span class="chip chip--draft">Draft</span>
                                                    <span>Gallery</span>
                                                </p>
                                            </div>
                                            <div class="entry__actions">
                                                <button class="icon-btn" type="button"
                                                        data-preview-open="gallery-${entry.id}"
                                                        data-preview-name="<c:out value='${entry.title}'/>"
                                                        data-preview-state="Gallery &middot; draft"
                                                        title="Preview as it will look on the site"
                                                        aria-label="Preview <c:out value='${entry.title}'/>">
                                                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2.5 12S6 6 12 6s9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6z" fill="none" stroke="currentColor" stroke-width="1.7"/><circle cx="12" cy="12" r="2.6" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                                                </button>
                                                <a class="btn-ghost btn--sm" href="${ctx}/cmsmgmnt/gallery/${entry.id}/edit">Continue</a>
                                            </div>
                                        </article>
                                    </c:forEach>

                                    <c:forEach items="${draftPosts}" var="post">
                                        <article class="entry">
                                            <div class="entry__thumb ${empty post.coverImagePath ? 'entry__thumb--empty' : ''}">
                                                <c:choose>
                                                    <c:when test="${not empty post.coverImagePath}">
                                                        <img src="${ctx}${post.coverImagePath}" alt="" width="92" height="68">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 5h9a3 3 0 0 1 3 3v11H8a2 2 0 0 1-2-2z" fill="none" stroke="currentColor" stroke-width="1.7"/><path d="M9 9h8M9 13h6" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/></svg>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <div class="entry__copy">
                                                <h2 class="entry__title"><c:out value="${post.title}"/></h2>
                                                <p class="entry__meta">
                                                    <span class="chip chip--draft">Draft</span>
                                                    <span>Blog</span>
                                                </p>
                                            </div>
                                            <div class="entry__actions">
                                                <button class="icon-btn" type="button"
                                                        data-preview-open="blog-${post.id}"
                                                        data-preview-name="<c:out value='${post.title}'/>"
                                                        data-preview-state="Blog &middot; draft"
                                                        title="Preview as it will look on the site"
                                                        aria-label="Preview <c:out value='${post.title}'/>">
                                                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2.5 12S6 6 12 6s9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6z" fill="none" stroke="currentColor" stroke-width="1.7"/><circle cx="12" cy="12" r="2.6" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                                                </button>
                                                <a class="btn-ghost btn--sm" href="${ctx}/cmsmgmnt/blog/${post.id}/edit">Continue</a>
                                            </div>
                                        </article>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </section>

                <section class="panel" aria-labelledby="activity-title">
                    <div class="panel__head">
                        <div>
                            <h2 id="activity-title">Recent activity</h2>
                            <p>An audit trail of saves, publishes and removals.</p>
                        </div>
                    </div>
                    <div class="panel__body panel__body--tight">
                        <c:choose>
                            <c:when test="${empty recentAudit}">
                                <div class="empty">
                                    <span class="empty__icon" aria-hidden="true">
                                        <svg viewBox="0 0 24 24"><path d="M12 7v5l3 2" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/><circle cx="12" cy="12" r="8.5" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                                    </span>
                                    <strong>No activity yet</strong>
                                    <p>Save or publish something and it will show up here.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <ol class="feed">
                                    <c:forEach items="${recentAudit}" var="entry">
                                        <li>
                                            <span class="feed__mark" aria-hidden="true">
                                                <svg viewBox="0 0 24 24"><path d="M5 12.5 10 17l9-10" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
                                            </span>
                                            <span class="feed__copy">
                                                <strong><c:out value="${entry.title}"/></strong>
                                                <span class="feed__meta">
                                                    <span>${entry.kindLabel}</span>
                                                    <c:if test="${entry.actionLabel ne 'Removed'}">
                                                        <span aria-hidden="true">&middot;</span>
                                                        <span>${entry.actionLabel}</span>
                                                    </c:if>
                                                    <span class="chip chip--${entry.statusTone}">${entry.statusLabel}</span>
                                                </span>
                                            </span>
                                            <span class="feed__time">${entry.createdAt}</span>
                                        </li>
                                    </c:forEach>
                                </ol>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </section>
            </div>

            <div class="preview-store" hidden aria-hidden="true">
                <c:forEach items="${draftProjects}" var="project">
                    <template data-preview-key="project-${project.id}">
                        <%@ include file="/WEB-INF/jsp/studio/preview/project-preview.jspf" %>
                    </template>
                </c:forEach>
                <c:forEach items="${draftGallery}" var="entry">
                    <template data-preview-key="gallery-${entry.id}">
                        <%@ include file="/WEB-INF/jsp/studio/preview/gallery-preview.jspf" %>
                    </template>
                </c:forEach>
                <c:forEach items="${draftPosts}" var="post">
                    <template data-preview-key="blog-${post.id}">
                        <%@ include file="/WEB-INF/jsp/studio/preview/blog-preview.jspf" %>
                    </template>
                </c:forEach>
            </div>

    <%@ include file="/WEB-INF/jsp/layout/desk-shell-close.jspf" %>
</body>
</html>
