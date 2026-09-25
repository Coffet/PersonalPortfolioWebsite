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
                    <a class="back-link" href="${ctx}/cmsmgmnt/blog">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/></svg>
                        Blog
                    </a>
                    <h1>${post.id > 0 ? 'Edit note' : 'New note'}</h1>
                </div>
                <div class="page-actions">
                    <button class="btn-ghost" type="button" data-preview-live="blog" data-preview-state="Unsaved preview">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2.5 12S6 6 12 6s9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6z" fill="none" stroke="currentColor" stroke-width="1.7"/><circle cx="12" cy="12" r="2.6" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                        Preview
                    </button>
                    <button class="btn" type="submit" form="blog-form">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12.5 10 17l9-10" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/></svg>
                        Save note
                    </button>
                </div>
            </div>

            <c:url var="saveAction" value="/cmsmgmnt/blog/save">
                <c:param name="${_csrf.parameterName}" value="${_csrf.token}"/>
            </c:url>
            <form class="composer" id="blog-form" action="${saveAction}" method="post" enctype="multipart/form-data">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <input type="hidden" name="id" value="${post.id}">

                <div class="composer__stage">
                    <label class="visually-hidden" for="title">Title</label>
                    <input class="composer__title" id="title" name="title" value="${post.title}" required maxlength="120"
                           placeholder="Title of the note" ${post.id == 0 ? 'autofocus' : ''}>

                    <div class="field">
                        <label for="excerpt">Summary</label>
                        <textarea class="composer__lede" id="excerpt" name="excerpt" required maxlength="280" rows="2"
                                  data-autogrow data-count-input="excerpt"
                                  placeholder="A short line for the blog list &mdash; what would make someone open this?">${post.excerpt}</textarea>
                        <p class="hint">Shown on the public blog card and as the opening line of the note.</p>
                    </div>

                    <div class="editor" data-editor>
                        <div class="editor__toolbar" data-editor-toolbar role="toolbar" aria-label="Formatting">
                            <button class="tb tb--wide" type="button" data-cmd="formatBlock" data-value="H2" title="Heading 2">H2</button>
                            <button class="tb tb--wide" type="button" data-cmd="formatBlock" data-value="H3" title="Heading 3">H3</button>
                            <button class="tb tb--wide" type="button" data-cmd="formatBlock" data-value="P" title="Paragraph">&para;</button>
                            <span class="tb-divider" aria-hidden="true"></span>
                            <button class="tb" type="button" data-cmd="bold" aria-pressed="false" title="Bold (Ctrl+B)">
                                <span class="visually-hidden">Bold</span>
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M7 5h6.2a3.6 3.6 0 0 1 0 7.2H7zM7 12.2h6.9a3.9 3.9 0 0 1 0 7.8H7z" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/></svg>
                            </button>
                            <button class="tb" type="button" data-cmd="italic" aria-pressed="false" title="Italic (Ctrl+I)">
                                <span class="visually-hidden">Italic</span>
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M15 5h-5M14 19H9M14.5 5l-4 14" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                            </button>
                            <button class="tb" type="button" data-cmd="underline" aria-pressed="false" title="Underline (Ctrl+U)">
                                <span class="visually-hidden">Underline</span>
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M7 5v6a5 5 0 0 0 10 0V5M6 20h12" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                            </button>
                            <button class="tb" type="button" data-cmd="strikeThrough" aria-pressed="false" title="Strikethrough">
                                <span class="visually-hidden">Strikethrough</span>
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 12h12M8.4 8.2C9 6.8 10.3 6 12 6c2 0 3.4 1 3.4 2.6M15.8 15.4c-.5 1.5-1.9 2.6-3.9 2.6-2.2 0-3.7-1.1-3.9-2.8" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                            </button>
                            <span class="tb-divider" aria-hidden="true"></span>
                            <button class="tb" type="button" data-cmd="insertUnorderedList" aria-pressed="false" title="Bulleted list">
                                <span class="visually-hidden">Bulleted list</span>
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M9 7h11M9 12h11M9 17h11" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/><circle cx="5" cy="7" r="1.3" fill="currentColor"/><circle cx="5" cy="12" r="1.3" fill="currentColor"/><circle cx="5" cy="17" r="1.3" fill="currentColor"/></svg>
                            </button>
                            <button class="tb" type="button" data-cmd="insertOrderedList" aria-pressed="false" title="Numbered list">
                                <span class="visually-hidden">Numbered list</span>
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M9.5 7h10M9.5 12h10M9.5 17h10" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/><path d="M4 5.6h1V9M3.4 15.2c.3-.6.9-1 1.5-1 .8 0 1.4.5 1.4 1.2 0 1-2.9 1.4-2.9 3.1h3" fill="none" stroke="currentColor" stroke-width="1.35" stroke-linecap="round" stroke-linejoin="round"/></svg>
                            </button>
                            <button class="tb" type="button" data-cmd="formatBlock" data-value="BLOCKQUOTE" title="Quote">
                                <span class="visually-hidden">Quote</span>
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M8 7.5H5.5A1.5 1.5 0 0 0 4 9v3.5h3.5V16H4M19 7.5h-2.5A1.5 1.5 0 0 0 15 9v3.5h3.5V16H15" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/></svg>
                            </button>
                            <button class="tb" type="button" data-cmd="formatBlock" data-value="PRE" title="Code block">
                                <span class="visually-hidden">Code block</span>
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m9 8-4 4 4 4M15 8l4 4-4 4" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/></svg>
                            </button>
                            <span class="tb-divider" aria-hidden="true"></span>
                            <button class="tb" type="button" data-cmd="createLink" title="Add a link">
                                <span class="visually-hidden">Add a link</span>
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M10 13.5a3.6 3.6 0 0 0 5.1 0l2.6-2.6a3.6 3.6 0 0 0-5.1-5.1l-1 1M14 10.5a3.6 3.6 0 0 0-5.1 0L6.3 13a3.6 3.6 0 0 0 5.1 5.1l1-1" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/></svg>
                            </button>
                            <button class="tb" type="button" data-cmd="unlink" title="Remove link">
                                <span class="visually-hidden">Remove link</span>
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M9.5 14.5 6.8 17a3.4 3.4 0 0 1-4.8-4.8l2.6-2.6M14.5 9.5 17.2 7a3.4 3.4 0 0 1 4.8 4.8l-2.6 2.6M4 4l16 16" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/></svg>
                            </button>
                            <button class="tb" type="button" data-cmd="insertHorizontalRule" title="Divider">
                                <span class="visually-hidden">Divider</span>
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 12h16" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round"/></svg>
                            </button>
                            <button class="tb" type="button" data-cmd="removeFormat" title="Clear formatting">
                                <span class="visually-hidden">Clear formatting</span>
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 6h9M11 6 8 18M6 18h7M15 12l5 6M20 12l-5 6" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/></svg>
                            </button>
                            <span class="tb-divider" aria-hidden="true"></span>
                            <button class="tb" type="button" data-cmd="undo" title="Undo">
                                <span class="visually-hidden">Undo</span>
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M9 8H5V4M5.5 8.2A8 8 0 1 1 4.6 14" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/></svg>
                            </button>
                            <button class="tb" type="button" data-cmd="redo" title="Redo">
                                <span class="visually-hidden">Redo</span>
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M15 8h4V4M18.5 8.2A8 8 0 1 0 19.4 14" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/></svg>
                            </button>
                        </div>

                        <div class="editor__surface" data-editor-surface role="textbox" aria-multiline="true"
                             aria-label="Note body" data-placeholder="Write the note &mdash; headings, lists, quotes and links are all supported."></div>

                        <textarea class="visually-hidden" id="body" name="body" data-editor-input hidden>${post.body}</textarea>

                        <div class="editor__foot">
                            <span data-editor-count>0 words</span>
                            <span>Paste from anywhere &mdash; formatting is cleaned to match the site. <kbd>Ctrl</kbd> + <kbd>S</kbd> saves.</span>
                        </div>
                    </div>
                </div>

                <aside class="composer__rail">
                    <section class="inspector">
                        <div class="inspector__head">
                            <h2>Publish</h2>
                        </div>
                        <label class="switch">
                            <input type="checkbox" name="published" ${post.published ? 'checked' : ''}>
                            <span class="switch__ui" aria-hidden="true"></span>
                            <span class="switch__copy">
                                <strong>Visible on the site</strong>
                                <span>Off keeps this as a draft</span>
                            </span>
                        </label>
                        <div class="composer-actions">
                            <button class="btn" type="submit">
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12.5 10 17l9-10" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/></svg>
                                Save note
                            </button>
                            <button class="btn-ghost" type="button" data-preview-live="blog" data-preview-state="Unsaved preview">
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2.5 12S6 6 12 6s9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6z" fill="none" stroke="currentColor" stroke-width="1.7"/><circle cx="12" cy="12" r="2.6" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                                Preview
                            </button>
                        </div>
                        <p class="inspector__hint">Saving with &ldquo;Visible&rdquo; on publishes instantly. There is no separate publish step.</p>
                    </section>

                    <section class="inspector">
                        <div class="inspector__head">
                            <h2>Cover image</h2>
                        </div>
                        <div class="upload-field" data-file-field data-empty-caption="Add a cover image">
                            <label class="dropzone" for="coverImageFile">
                                <input id="coverImageFile" name="coverImageFile" type="file" accept="image/*">
                                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h4l1.4-2h5.2L16 7h4v12H4z" fill="none" stroke="currentColor" stroke-width="1.7"/><circle cx="12" cy="13" r="3.2" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                                <strong>Cover image</strong>
                                <span data-file-caption>Drop or choose a file</span>
                            </label>
                            <div class="drop-preview" data-file-preview></div>
                        </div>
                        <c:if test="${not empty post.coverImagePath}">
                            <div class="current-cover">
                                <button class="media-preview-trigger" type="button" data-preview-src="${ctx}${post.coverImagePath}" aria-label="Preview current cover">
                                    <img src="${ctx}${post.coverImagePath}" alt="Current cover for <c:out value='${post.title}'/>">
                                </button>
                                <span>Current cover &mdash; click to preview</span>
                            </div>
                        </c:if>
                    </section>

                    <section class="inspector">
                        <div class="inspector__head">
                            <h2>Order</h2>
                        </div>
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
