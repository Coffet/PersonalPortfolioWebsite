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
                <h1>${project.id > 0 ? 'Edit project' : 'New project'}</h1>
                <a href="${ctx}/cmsmgmnt/projects">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/></svg>
                    Back to projects
                </a>
            </div>

            <c:url var="saveAction" value="/cmsmgmnt/projects/save">
                <c:param name="${_csrf.parameterName}" value="${_csrf.token}"/>
            </c:url>
            <form class="composer" action="${saveAction}" method="post" enctype="multipart/form-data">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <input type="hidden" name="id" value="${project.id}">

                <div class="composer__stage">
                    <label class="visually-hidden" for="title">Title</label>
                    <input class="composer__title" id="title" name="title" value="${project.title}" required maxlength="120" placeholder="Project name" ${project.id == 0 ? 'autofocus' : ''}>

                    <label class="visually-hidden" for="summary">Summary</label>
                    <textarea class="composer__lede" id="summary" name="summary" required maxlength="280" placeholder="One or two sentences for the homepage card" rows="2">${project.summary}</textarea>

                    <label class="visually-hidden" for="narrative">Narrative</label>
                    <textarea class="composer__body" id="narrative" name="narrative" required placeholder="Tell the story of the work">${project.narrative}</textarea>
                </div>

                <aside class="composer__rail">
                    <section class="inspector">
                        <h2>Publish</h2>
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
                            <button class="button" type="submit">Save project</button>
                        </div>
                    </section>

                    <section class="inspector">
                        <h2>Images</h2>
                        <label class="dropzone" data-file-field data-empty-caption="Add a card image">
                            <input id="cardImageFile" name="cardImageFile" type="file" accept="image/*">
                            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h4l1.4-2h5.2L16 7h4v12H4z" fill="none" stroke="currentColor" stroke-width="1.75"/><circle cx="12" cy="13" r="3.2" fill="none" stroke="currentColor" stroke-width="1.75"/></svg>
                            <strong>Card image</strong>
                            <span data-file-caption>Homepage card</span>
                            <div class="drop-preview" data-file-preview></div>
                        </label>
                        <c:if test="${not empty project.cardImagePath}">
                            <div class="current-cover">
                                <img src="${ctx}${project.cardImagePath}" alt="Current card image for ${project.title}">
                                <span>Current card image</span>
                            </div>
                        </c:if>
                        <label class="dropzone" data-file-field data-empty-caption="Add gallery images">
                            <input id="galleryFiles" name="galleryFiles" type="file" accept="image/*" multiple>
                            <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3.5" y="5" width="7" height="7" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.75"/><rect x="13.5" y="5" width="7" height="7" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.75"/><rect x="3.5" y="14" width="7" height="5" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.75"/><rect x="13.5" y="14" width="7" height="5" rx="1.2" fill="none" stroke="currentColor" stroke-width="1.75"/></svg>
                            <strong>Project gallery</strong>
                            <span data-file-caption>Detail page images</span>
                            <div class="drop-preview" data-file-preview></div>
                        </label>
                        <c:if test="${not empty project.media}">
                            <div class="media-strip">
                                <c:forEach items="${project.media}" var="media">
                                    <article class="media-card">
                                        <img src="${ctx}${media.filePath}" alt="${empty media.altText ? project.title : media.altText}">
                                    </article>
                                </c:forEach>
                            </div>
                        </c:if>
                    </section>

                    <section class="inspector">
                        <h2>Facts</h2>
                        <div class="inspector-grid">
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
                            <input id="role" name="role" value="${project.role}" required maxlength="180">
                        </div>
                        <div class="field">
                            <label for="tools">Tools</label>
                            <input id="tools" name="tools" value="${project.tools}" required maxlength="280">
                        </div>
                    </section>

                    <section class="inspector">
                        <h2>Link</h2>
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
                        <h2>Card look</h2>
                        <div class="field">
                            <label for="cardGradient">Card color</label>
                            <input id="cardGradient" name="cardGradient" value="${project.cardGradient}" maxlength="255">
                        </div>
                        <div class="inspector-grid">
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
