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
                <h1>${entry.id > 0 ? 'Edit gallery entry' : 'New gallery entry'}</h1>
                <a href="${ctx}/cmsmgmnt/gallery">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/></svg>
                    Back to gallery
                </a>
            </div>

            <c:url var="saveAction" value="/cmsmgmnt/gallery/save">
                <c:param name="${_csrf.parameterName}" value="${_csrf.token}"/>
            </c:url>
            <form class="composer" action="${saveAction}" method="post" enctype="multipart/form-data">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <input type="hidden" name="id" value="${entry.id}">

                <div class="composer__stage">
                    <div class="upload-field composer__hero" data-file-field data-empty-caption="Drop several images, or choose files">
                        <label class="dropzone dropzone--hero" for="mediaFiles">
                            <input id="mediaFiles" name="mediaFiles" type="file" accept="image/*" multiple>
                            <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3.5" y="5" width="7" height="7" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.75"/><rect x="13.5" y="5" width="7" height="7" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.75"/><rect x="3.5" y="14" width="7" height="5" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.75"/><rect x="13.5" y="14" width="7" height="5" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.75"/></svg>
                            <strong>Images</strong>
                            <span data-file-caption>Drop several images, or choose files</span>
                        </label>
                        <div class="drop-preview" data-file-preview></div>
                    </div>

                    <c:if test="${not empty entry.media}">
                        <p class="upload-field__label">Already on this entry — click to preview</p>
                        <div class="media-strip">
                            <c:forEach items="${entry.media}" var="media">
                                <article class="media-card">
                                    <button class="media-preview-trigger" type="button" data-preview-src="${ctx}${media.filePath}" aria-label="Preview ${empty media.altText ? entry.title : media.altText}">
                                        <img src="${ctx}${media.filePath}" alt="${empty media.altText ? entry.title : media.altText}">
                                    </button>
                                </article>
                            </c:forEach>
                        </div>
                    </c:if>

                    <label class="visually-hidden" for="title">Title</label>
                    <input class="composer__title" id="title" name="title" value="${entry.title}" required maxlength="120" placeholder="Title" ${entry.id == 0 ? 'autofocus' : ''}>

                    <label class="visually-hidden" for="introText">Intro (optional)</label>
                    <textarea class="composer__lede" id="introText" name="introText" maxlength="280" placeholder="A short intro under the title (optional)" rows="2">${entry.introText}</textarea>

                    <label class="visually-hidden" for="body">Body (optional)</label>
                    <textarea class="composer__body" id="body" name="body" placeholder="Notes, process, whatever belongs with the images (optional)">${entry.body}</textarea>
                </div>

                <aside class="composer__rail">
                    <section class="inspector">
                        <h2>Publish</h2>
                        <label class="switch">
                            <input type="checkbox" name="published" ${entry.published ? 'checked' : ''}>
                            <span class="switch__ui" aria-hidden="true"></span>
                            <span class="switch__copy">
                                <strong>Visible on the site</strong>
                                <span>Off keeps this as a draft</span>
                            </span>
                        </label>
                        <div class="composer-actions">
                            <button class="button" type="submit">Save entry</button>
                        </div>
                    </section>

                    <section class="inspector">
                        <h2>Details</h2>
                        <div class="field">
                            <label for="category">Tag</label>
                            <input id="category" name="category" value="${entry.category}" maxlength="80" placeholder="UI, Motion, study">
                            <p class="hint">Saved with the entry. It appears after someone opens it, not on the gallery grid.</p>
                        </div>
                        <div class="field">
                            <label for="sortOrder">Position</label>
                            <input id="sortOrder" name="sortOrder" type="number" value="${entry.sortOrder}">
                            <p class="hint">Lower numbers appear first.</p>
                        </div>
                    </section>
                </aside>
            </form>

    <%@ include file="/WEB-INF/jsp/layout/desk-shell-close.jspf" %>
</body>
</html>
