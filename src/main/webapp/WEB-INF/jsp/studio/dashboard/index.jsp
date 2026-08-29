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
                    <h1>What needs attention</h1>
                    <p>Drafts wait here. Published pieces already live on the public site.</p>
                </div>
            </header>

            <section class="desk-shortcuts" aria-label="Create">
                <a class="shortcut-card" href="${ctx}/cmsmgmnt/projects/new">
                    <span>Projects</span>
                    <strong>New project</strong>
                </a>
                <a class="shortcut-card" href="${ctx}/cmsmgmnt/gallery/new">
                    <span>Gallery</span>
                    <strong>New entry</strong>
                </a>
                <a class="shortcut-card" href="${ctx}/cmsmgmnt/blog/new">
                    <span>Blog</span>
                    <strong>New note</strong>
                </a>
            </section>

            <section class="panel" style="margin-bottom: 18px;">
                <h2>Drafts</h2>
                <c:choose>
                    <c:when test="${empty draftProjects and empty draftGallery and empty draftPosts}">
                        <p class="empty-state">Nothing waiting. Create a piece, or leave it quiet.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="work-list">
                            <c:forEach items="${draftProjects}" var="project">
                                <article class="work-item">
                                    <div class="work-item__media">
                                        <c:if test="${not empty project.cardImagePath}">
                                            <img src="${ctx}${project.cardImagePath}" alt="" width="112" height="80">
                                        </c:if>
                                    </div>
                                    <div class="work-item__copy">
                                        <h2>${project.title}</h2>
                                        <p>Project · <span class="chip chip--draft">Draft</span></p>
                                    </div>
                                    <a class="button-ghost" href="${ctx}/cmsmgmnt/projects/${project.id}/edit">Continue</a>
                                </article>
                            </c:forEach>
                            <c:forEach items="${draftGallery}" var="entry">
                                <article class="work-item">
                                    <div class="work-item__media">
                                        <c:if test="${not empty entry.media}">
                                            <img src="${ctx}${entry.media[0].filePath}" alt="" width="112" height="80">
                                        </c:if>
                                    </div>
                                    <div class="work-item__copy">
                                        <h2>${entry.title}</h2>
                                        <p>Gallery · <span class="chip chip--draft">Draft</span></p>
                                    </div>
                                    <a class="button-ghost" href="${ctx}/cmsmgmnt/gallery/${entry.id}/edit">Continue</a>
                                </article>
                            </c:forEach>
                            <c:forEach items="${draftPosts}" var="post">
                                <article class="work-item">
                                    <div class="work-item__media">
                                        <c:if test="${not empty post.coverImagePath}">
                                            <img src="${ctx}${post.coverImagePath}" alt="" width="112" height="80">
                                        </c:if>
                                    </div>
                                    <div class="work-item__copy">
                                        <h2>${post.title}</h2>
                                        <p>Blog · <span class="chip chip--draft">Draft</span></p>
                                    </div>
                                    <a class="button-ghost" href="${ctx}/cmsmgmnt/blog/${post.id}/edit">Continue</a>
                                </article>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <section class="panel">
                <h2>Recent activity</h2>
                <c:choose>
                    <c:when test="${empty recentAudit}">
                        <p class="empty-state">Activity will appear after you save or publish.</p>
                    </c:when>
                    <c:otherwise>
                        <ol class="activity-list">
                            <c:forEach items="${recentAudit}" var="entry">
                                <li>
                                    <div class="activity-list__copy">
                                        <strong>${entry.title}</strong>
                                        <p class="activity-list__meta">
                                            <span>${entry.kindLabel}</span>
                                            <c:if test="${entry.actionLabel ne 'Removed'}">
                                                <span aria-hidden="true">·</span>
                                                <span>${entry.actionLabel}</span>
                                            </c:if>
                                            <span class="chip chip--${entry.statusTone}">${entry.statusLabel}</span>
                                        </p>
                                    </div>
                                    <span class="muted">${entry.createdAt}</span>
                                </li>
                            </c:forEach>
                        </ol>
                    </c:otherwise>
                </c:choose>
            </section>

    <%@ include file="/WEB-INF/jsp/layout/desk-shell-close.jspf" %>
</body>
</html>
