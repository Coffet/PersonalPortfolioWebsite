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
                    <h1>Blog</h1>
                    <p>Notes and process writing for the public blog.</p>
                </div>
                <div class="studio-actions">
                    <a class="button" href="${ctx}/cmsmgmnt/blog/new">New note</a>
                </div>
            </header>

            <c:choose>
                <c:when test="${empty posts}">
                    <p class="empty-state">No posts yet. Write one and publish it to the public Blog page.</p>
                </c:when>
                <c:otherwise>
                    <div class="work-list">
                        <c:forEach items="${posts}" var="post">
                            <article class="work-item">
                                <div class="work-item__media">
                                    <c:if test="${not empty post.coverImagePath}">
                                        <img src="${ctx}${post.coverImagePath}" alt="${post.title}" width="112" height="80" loading="lazy">
                                    </c:if>
                                </div>
                                <div class="work-item__copy">
                                    <h2>${post.title}</h2>
                                    <p>
                                        <span class="chip ${post.published ? 'chip--live' : 'chip--draft'}">${post.published ? 'Live' : 'Draft'}</span>
                                    </p>
                                </div>
                                <div class="studio-actions">
                                    <c:if test="${post.published}">
                                        <a class="button-ghost" href="${ctx}/blog/${post.id}" target="_blank" rel="noopener noreferrer">View live</a>
                                    </c:if>
                                    <a class="button-ghost" href="${ctx}/cmsmgmnt/blog/${post.id}/edit">Edit</a>
                                    <form action="${ctx}/cmsmgmnt/blog/${post.id}/delete" method="post">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <button class="button-danger" type="submit" onclick="return confirm('Delete this post?')">Delete</button>
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
