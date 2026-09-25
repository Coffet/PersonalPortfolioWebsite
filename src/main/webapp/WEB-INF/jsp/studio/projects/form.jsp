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
                    <a class="back-link" href="${ctx}/cmsmgmnt/projects">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/></svg>
                        Projects
                    </a>
                    <h1>${project.id > 0 ? 'Edit project' : 'New project'}</h1>
                </div>
                <div class="page-actions">
                    <button class="btn-ghost" type="button" data-preview-live="project" data-preview-state="Unsaved preview">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2.5 12S6 6 12 6s9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6z" fill="none" stroke="currentColor" stroke-width="1.7"/><circle cx="12" cy="12" r="2.6" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                        Preview
                    </button>
                    <button class="btn" type="submit" form="project-form">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12.5 10 17l9-10" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/></svg>
                        Save project
                    </button>
                </div>
            </div>

            <c:url var="saveAction" value="/cmsmgmnt/projects/save">
                <c:param name="${_csrf.parameterName}" value="${_csrf.token}"/>
            </c:url>
            <form class="composer" id="project-form" action="${saveAction}" method="post" enctype="multipart/form-data">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <input type="hidden" name="id" value="${project.id}">

                <div class="composer__stage">
                    <label class="visually-hidden" for="title">Title</label>
                    <input class="composer__title" id="title" name="title" value="${project.title}" required maxlength="120"
                           placeholder="Project name" ${project.id == 0 ? 'autofocus' : ''}>

                    <div class="field">
                        <label for="summary">Summary</label>
                        <textarea class="composer__lede" id="summary" name="summary" required maxlength="280" rows="2"
                                  data-autogrow data-count-input="summary"
                                  placeholder="One or two sentences for the homepage card">${project.summary}</textarea>
                    </div>

                    <div class="field">
                        <label for="narrative">Narrative</label>
                        <textarea class="composer__body" id="narrative" name="narrative" required rows="12" data-autogrow
                                  placeholder="Tell the story of the work &mdash; the problem, the decisions, the outcome.">${project.narrative}</textarea>
                        <p class="hint">Plain text is fine. Line breaks are preserved on the public page.</p>
                    </div>
                </div>

                <aside class="composer__rail">
                    <section class="inspector">
                        <div class="inspector__head">
                            <h2>Publish</h2>
                        </div>
                        <label class="switch">
                            <input type="checkbox" name="published" ${project.published ? 'checked' : ''}>
                            <span class="switch__ui" aria-hidden="true"></span>
                            <span class="switch__copy">
                                <strong>Visible on the site</strong>
                                <span>Off keeps this as a draft</span>
                            </span>
                        </label>
                        <label class="switch">
                            <input type="checkbox" name="featured" ${project.featured ? 'checked' : ''}>
                            <span class="switch__ui" aria-hidden="true"></span>
                            <span class="switch__copy">
                                <strong>Featured</strong>
                                <span>Pin it toward the front of the homepage</span>
                            </span>
                        </label>
                        <div class="composer-actions">
                            <button class="btn" type="submit">
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12.5 10 17l9-10" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/></svg>
                                Save project
                            </button>
                            <button class="btn-ghost" type="button" data-preview-live="project" data-preview-state="Unsaved preview">
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2.5 12S6 6 12 6s9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6z" fill="none" stroke="currentColor" stroke-width="1.7"/><circle cx="12" cy="12" r="2.6" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                                Preview
                            </button>
                        </div>
                    </section>

                    <section class="inspector">
                        <div class="inspector__head">
                            <h2>Images</h2>
                        </div>
                        <div class="upload-field" data-file-field data-empty-caption="Add a card image">
                            <label class="dropzone" for="cardImageFile">
                                <input id="cardImageFile" name="cardImageFile" type="file" accept="image/*">
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h4l1.4-2h5.2L16 7h4v12H4z" fill="none" stroke="currentColor" stroke-width="1.7"/><circle cx="12" cy="13" r="3.2" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                                <strong>Card image</strong>
                                <span data-file-caption>Homepage card</span>
                            </label>
                            <div class="drop-preview" data-file-preview></div>
                        </div>
                        <c:if test="${not empty project.cardImagePath}">
                            <div class="current-cover">
                                <button class="media-preview-trigger" type="button" data-preview-src="${ctx}${project.cardImagePath}" aria-label="Preview current card image">
                                    <img src="${ctx}${project.cardImagePath}" alt="Current card image for <c:out value='${project.title}'/>">
                                </button>
                                <span>Current card image &mdash; click to preview</span>
                            </div>
                        </c:if>

                        <div class="upload-field" data-file-field data-empty-caption="Add gallery images">
                            <label class="dropzone" for="galleryFiles">
                                <input id="galleryFiles" name="galleryFiles" type="file" accept="image/*" multiple>
                                <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3.5" y="5" width="7" height="7" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.7"/><rect x="13.5" y="5" width="7" height="7" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.7"/><rect x="3.5" y="14" width="7" height="5" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.7"/><rect x="13.5" y="14" width="7" height="5" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                                <strong>Project gallery</strong>
                                <span data-file-caption>Detail page images</span>
                            </label>
                            <div class="drop-preview" data-file-preview></div>
                        </div>
                        <c:if test="${not empty project.media}">
                            <div class="media-strip">
                                <c:forEach items="${project.media}" var="media">
                                    <article class="media-card">
                                        <button class="media-preview-trigger" type="button" data-preview-src="${ctx}${media.filePath}" aria-label="Preview <c:out value='${empty media.altText ? project.title : media.altText}'/>">
                                            <img src="${ctx}${media.filePath}" alt="<c:out value='${empty media.altText ? project.title : media.altText}'/>">
                                        </button>
                                    </article>
                                </c:forEach>
                            </div>
                        </c:if>
                    </section>

                    <section class="inspector">
                        <div class="inspector__head">
                            <h2>Facts</h2>
                        </div>
                        <div class="field-grid">
                            <div class="field">
                                <label for="yearLabel">Year</label>
                                <input id="yearLabel" name="yearLabel" value="${project.yearLabel}" required maxlength="40">
                            </div>
                            <div class="field">
                                <label for="sortOrder">Position</label>
                                <input id="sortOrder" name="sortOrder" type="number" value="${project.sortOrder}">
                            </div>
                        </div>
                        <div class="field">
                            <label for="role">Role</label>
                            <textarea id="role" name="role" required maxlength="180" rows="3" data-autogrow>${project.role}</textarea>
                        </div>
                        <div class="field">
                            <label for="tools">Tools</label>
                            <textarea id="tools" name="tools" required maxlength="280" rows="3" data-autogrow>${project.tools}</textarea>
                        </div>
                    </section>

                    <section class="inspector">
                        <div class="inspector__head">
                            <h2>Link</h2>
                        </div>
                        <div class="field">
                            <label for="externalLink">URL</label>
                            <input id="externalLink" name="externalLink" value="${project.externalLink}" maxlength="255" placeholder="https://">
                        </div>
                        <div class="field">
                            <label for="linkLabel">Label</label>
                            <input id="linkLabel" name="linkLabel" value="${project.linkLabel}" maxlength="80" placeholder="Visit project">
                        </div>
                    </section>

                    <section class="inspector">
                        <div class="inspector__head">
                            <h2>Card look</h2>
                        </div>
                        <div class="field">
                            <label for="cardGradient">Card colour</label>
                            <input id="cardGradient" name="cardGradient" value="${project.cardGradient}" maxlength="255" placeholder="CSS colour or gradient">
                        </div>
                        <div class="field-grid">
                            <div class="field">
                                <label for="cardImageMode">Image fit</label>
                                <select id="cardImageMode" name="cardImageMode">
                                    <option value="cover" ${project.cardImageMode eq 'cover' ? 'selected' : ''}>Fill</option>
                                    <option value="contain" ${project.cardImageMode eq 'contain' ? 'selected' : ''}>Fit</option>
                                </select>
                            </div>
                            <div class="field">
                                <label for="cardImageScale">Scale</label>
                                <input id="cardImageScale" name="cardImageScale" type="number" step="0.1" min="0.1" max="1" value="${empty project.cardImageScale ? 1 : project.cardImageScale}">
                            </div>
                        </div>
                    </section>
                </aside>
            </form>

    <%@ include file="/WEB-INF/jsp/layout/desk-shell-close.jspf" %>
</body>
</html>
