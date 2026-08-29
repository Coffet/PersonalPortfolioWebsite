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
                    <h1>Projects</h1>
                    <p>Work that appears on the homepage and in project pages.</p>
                </div>
                <div class="studio-actions">
                    <a class="button" href="${ctx}/cmsmgmnt/projects/new">New project</a>
                </div>
            </header>

            <c:choose>
                <c:when test="${empty projects}">
                    <p class="empty-state">No projects yet. Add one and it can show on the public homepage.</p>
                </c:when>
                <c:otherwise>
                    <div class="work-list">
                        <c:forEach items="${projects}" var="project">
                            <article class="work-item">
                                <div class="work-item__media">
                                    <c:if test="${not empty project.cardImagePath}">
                                        <img src="${ctx}${project.cardImagePath}" alt="${project.title}" width="112" height="80" loading="lazy">
                                    </c:if>
                                </div>
                                <div class="work-item__copy">
                                    <h2>${project.title}</h2>
                                    <p>
                                        ${project.yearLabel}
                                        ·
                                        <span class="chip ${project.published ? 'chip--live' : 'chip--draft'}">${project.published ? 'Live' : 'Draft'}</span>
                                    </p>
                                </div>
                                <div class="studio-actions">
                                    <c:if test="${project.published}">
                                        <a class="button-ghost" href="${ctx}/work/${project.id}" target="_blank" rel="noopener noreferrer">View live</a>
                                    </c:if>
                                    <a class="button-ghost" href="${ctx}/cmsmgmnt/projects/${project.id}/edit">Edit</a>
                                    <form action="${ctx}/cmsmgmnt/projects/${project.id}/delete" method="post">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <button class="button-danger" type="submit" onclick="return confirm('Delete this project?')">Delete</button>
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
