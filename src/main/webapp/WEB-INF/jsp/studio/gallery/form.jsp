<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/jsp/layout/studio-head.jspf" %>
</head>
<body class="is-composer">
    <%@ include file="/WEB-INF/jsp/layout/desk-shell-open.jspf" %>

            <div class="composer-top">
                <div class="composer-top__lead">
                    <a class="back-link" href="${ctx}/cmsmgmnt/gallery">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/></svg>
                        Gallery
                    </a>
                    <h1>${entry.id > 0 ? 'Edit entry' : 'New gallery entry'}</h1>
                </div>
                <div class="page-actions">
                    <button class="btn-ghost" type="button" data-preview-live="gallery" data-preview-state="Unsaved preview">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2.5 12S6 6 12 6s9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6z" fill="none" stroke="currentColor" stroke-width="1.7"/><circle cx="12" cy="12" r="2.6" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                        Preview
                    </button>
                    <button class="btn" type="submit" form="gallery-form">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12.5 10 17l9-10" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/></svg>
                        Save entry
                    </button>
                </div>
            </div>

            <c:url var="saveAction" value="/cmsmgmnt/gallery/save">
                <c:param name="${_csrf.parameterName}" value="${_csrf.token}"/>
            </c:url>
            <form class="composer" id="gallery-form" action="${saveAction}" method="post" enctype="multipart/form-data">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <input type="hidden" name="id" value="${entry.id}">

                <div class="composer__stage">
                    <label class="visually-hidden" for="title">Title</label>
                    <input class="composer__title" id="title" name="title" value="${entry.title}" required maxlength="120"
                           placeholder="Entry title" ${entry.id == 0 ? 'autofocus' : ''}>

                    <div class="field">
                        <label for="introText">Intro <span class="hint">(optional)</span></label>
                        <textarea class="composer__lede" id="introText" name="introText" maxlength="280" rows="2"
                                  data-autogrow data-count-input="introText"
                                  placeholder="A short intro under the title">${entry.introText}</textarea>
                    </div>

                    <div class="field">
                        <label>Images</label>
                        <div class="upload-field composer__hero" data-file-field data-empty-caption="Drop several images, or choose files">
                            <label class="dropzone dropzone--hero" for="mediaFiles">
                                <input id="mediaFiles" name="mediaFiles" type="file" accept="image/*" multiple>
                                <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3.5" y="5" width="7" height="7" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.7"/><rect x="13.5" y="5" width="7" height="7" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.7"/><rect x="3.5" y="14" width="7" height="5" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.7"/><rect x="13.5" y="14" width="7" height="5" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                                <strong>Images</strong>
                                <span data-file-caption>Drop several images, or choose files</span>
                            </label>
                            <div class="drop-preview" data-file-preview></div>
                        </div>
                        <c:if test="${not empty entry.media}">
                            <p class="upload-field__label">Already on this entry &mdash; click to preview</p>
                            <div class="media-strip">
                                <c:forEach items="${entry.media}" var="media">
                                    <article class="media-card">
                                        <button class="media-preview-trigger" type="button" data-preview-src="${ctx}${media.filePath}" aria-label="Preview <c:out value='${empty media.altText ? entry.title : media.altText}'/>">
                                            <img src="${ctx}${media.filePath}" alt="<c:out value='${empty media.altText ? entry.title : media.altText}'/>">
                                        </button>
                                    </article>
                                </c:forEach>
                            </div>
                        </c:if>
                    </div>

                    <div class="field">
                        <label for="body">Notes <span class="hint">(optional)</span></label>
                        <textarea class="composer__body" id="body" name="body" rows="8" data-autogrow
                                  placeholder="Process notes, context, whatever belongs with the images">${entry.body}</textarea>
                    </div>
                </div>

                <aside class="composer__rail">
                    <section class="inspector">
                        <div class="inspector__head">
                            <h2>Publish</h2>
                        </div>
                        <label class="switch">
                            <input type="checkbox" name="published" ${entry.published ? 'checked' : ''}>
                            <span class="switch__ui" aria-hidden="true"></span>
                            <span class="switch__copy">
                                <strong>Visible on the site</strong>
                                <span>Off keeps this as a draft</span>
                            </span>
                        </label>
                        <div class="composer-actions">
                            <button class="btn" type="submit">
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12.5 10 17l9-10" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/></svg>
                                Save entry
                            </button>
                            <button class="btn-ghost" type="button" data-preview-live="gallery" data-preview-state="Unsaved preview">
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2.5 12S6 6 12 6s9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6z" fill="none" stroke="currentColor" stroke-width="1.7"/><circle cx="12" cy="12" r="2.6" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                                Preview
                            </button>
                        </div>
                    </section>

                    <section class="inspector">
                        <div class="inspector__head">
                            <h2>Details</h2>
                        </div>
                        <div class="field">
                            <label for="category">Tags</label>
                            <input id="category" name="category" value="${entry.category}" maxlength="80" placeholder="UI, Motion, Study">
                            <p class="hint">Separate with commas. Tags show after someone opens the entry, not on the gallery grid.</p>
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
