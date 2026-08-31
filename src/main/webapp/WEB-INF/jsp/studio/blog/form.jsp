<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/jsp/layout/studio-head.jspf" %>
</head>
<body class="is-composer">
    <%@ include file="/WEB-INF/jsp/layout/desk-shell-open.jspf" %>

            <div class="composer-bar">
                <h1>${post.id > 0 ? 'Edit note' : 'New note'}</h1>
                <a href="${ctx}/cmsmgmnt/blog">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/></svg>
                    Back to blog
                </a>
            </div>

            <c:url var="saveAction" value="/cmsmgmnt/blog/save">
                <c:param name="${_csrf.parameterName}" value="${_csrf.token}"/>
            </c:url>
            <form class="composer" action="${saveAction}" method="post" enctype="multipart/form-data">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <input type="hidden" name="id" value="${post.id}">

                <div class="composer__stage">
                    <label class="visually-hidden" for="title">Title</label>
                    <input class="composer__title" id="title" name="title" value="${post.title}" required maxlength="120" placeholder="Title" ${post.id == 0 ? 'autofocus' : ''}>

                    <label class="visually-hidden" for="excerpt">Excerpt</label>
                    <textarea class="composer__lede" id="excerpt" name="excerpt" required maxlength="280" placeholder="A short line for the blog list" rows="2">${post.excerpt}</textarea>

                    <label class="visually-hidden" for="body">Body</label>
                    <textarea class="composer__body" id="body" name="body" required placeholder="Write the note">${post.body}</textarea>
                </div>

                <aside class="composer__rail">
                    <section class="inspector">
                        <h2>Publish</h2>
                        <label class="switch">
                            <input type="checkbox" name="published" ${post.published ? 'checked' : ''}>
                            <span class="switch__ui" aria-hidden="true"></span>
                            <span class="switch__copy">
                                <strong>Visible on the site</strong>
                                <span>Off keeps this as a draft</span>
                            </span>
                        </label>
                        <div class="composer-actions">
                            <button class="button" type="submit">Save note</button>
                        </div>
                    </section>

                    <section class="inspector">
                        <h2>Cover</h2>
                        <div class="upload-field" data-file-field data-empty-caption="Add a cover image">
                            <label class="dropzone" for="coverImageFile">
                                <input id="coverImageFile" name="coverImageFile" type="file" accept="image/*">
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h4l1.4-2h5.2L16 7h4v12H4z" fill="none" stroke="currentColor" stroke-width="1.75"/><circle cx="12" cy="13" r="3.2" fill="none" stroke="currentColor" stroke-width="1.75"/></svg>
                                <strong>Cover image</strong>
                                <span data-file-caption>Drop or choose a file</span>
                            </label>
                            <div class="drop-preview" data-file-preview></div>
                        </div>
                        <c:if test="${not empty post.coverImagePath}">
                            <div class="current-cover">
                                <button class="media-preview-trigger" type="button" data-preview-src="${ctx}${post.coverImagePath}" aria-label="Preview current cover">
                                    <img src="${ctx}${post.coverImagePath}" alt="Current cover for ${post.title}">
                                </button>
                                <span>Current cover — click to preview</span>
                            </div>
                        </c:if>
                    </section>

                    <section class="inspector">
                        <h2>List</h2>
                        <div class="field">
                            <label for="sortOrder">Position</label>
                            <input id="sortOrder" name="sortOrder" type="number" value="${post.sortOrder}">
                            <p class="hint">Lower numbers appear first.</p>
                        </div>
                    </section>
                </aside>
            </form>

    <%@ include file="/WEB-INF/jsp/layout/desk-shell-close.jspf" %>
</body>
</html>
