package com.portfolio.studio.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.portfolio.studio.config.PortfolioProperties;
import com.portfolio.studio.model.BlogPost;
import com.portfolio.studio.model.CmsUser;
import com.portfolio.studio.model.GalleryEntry;
import com.portfolio.studio.model.GalleryMedia;
import com.portfolio.studio.model.Project;
import com.portfolio.studio.model.ProjectMedia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PortfolioService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioService.class);
    private static final DateTimeFormatter SQL_TIMESTAMP = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final JdbcTemplate jdbcTemplate;
    private final MediaStorageService mediaStorageService;
    private final PasswordEncoder passwordEncoder;
    private final PortfolioProperties portfolioProperties;

    public PortfolioService(
        JdbcTemplate jdbcTemplate,
        MediaStorageService mediaStorageService,
        PasswordEncoder passwordEncoder,
        PortfolioProperties portfolioProperties
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.mediaStorageService = mediaStorageService;
        this.passwordEncoder = passwordEncoder;
        this.portfolioProperties = portfolioProperties;
    }

    public List<Project> listPublishedProjects() {
        List<Project> projects = jdbcTemplate.query(
            """
                SELECT * FROM projects
                WHERE published = 1
                ORDER BY featured DESC, sort_order ASC, id DESC
                """,
            projectRowMapper()
        );
        projects.forEach(project -> project.setMedia(listProjectMedia(project.getId())));
        return projects;
    }

    public List<Project> listAllProjects() {
        List<Project> projects = jdbcTemplate.query(
            """
                SELECT * FROM projects
                ORDER BY sort_order ASC, id DESC
                """,
            projectRowMapper()
        );
        projects.forEach(project -> project.setMedia(listProjectMedia(project.getId())));
        return projects;
    }

    public Optional<Project> findProjectById(long id) {
        return findOne(
            "SELECT * FROM projects WHERE id = ?",
            projectRowMapper(),
            id
        ).map(project -> {
            project.setMedia(listProjectMedia(project.getId()));
            return project;
        });
    }

    public Optional<Project> findPublishedProjectById(long id) {
        return findOne(
            "SELECT * FROM projects WHERE id = ? AND published = 1",
            projectRowMapper(),
            id
        ).map(project -> {
            project.setMedia(listProjectMedia(project.getId()));
            return project;
        });
    }

    public Optional<Project> findPublishedProjectBySlug(String slug) {
        return findOne(
            "SELECT * FROM projects WHERE slug = ? AND published = 1",
            projectRowMapper(),
            slug
        ).map(project -> {
            project.setMedia(listProjectMedia(project.getId()));
            return project;
        });
    }

    public List<Project> listDraftProjects() {
        List<Project> projects = jdbcTemplate.query(
            """
                SELECT * FROM projects
                WHERE published = 0
                ORDER BY updated_at DESC, id DESC
                """,
            projectRowMapper()
        );
        projects.forEach(project -> project.setMedia(listProjectMedia(project.getId())));
        return projects;
    }

    @Transactional
    public long saveProject(Project project, MultipartFile cardImageFile, MultipartFile[] galleryFiles) {
        sanitizeProject(project);
        boolean updating = project.getId() > 0;

        Project existing = updating ? findProjectById(project.getId()).orElse(null) : null;
        String currentCardImagePath = updating && existing != null ? existing.getCardImagePath() : project.getCardImagePath();
        String currentFallbackImagePath = updating && existing != null ? existing.getFallbackImagePath() : project.getFallbackImagePath();
        project.setCardImagePath(currentCardImagePath);
        project.setFallbackImagePath(StringUtils.hasText(currentFallbackImagePath) ? currentFallbackImagePath : currentCardImagePath);

        if (updating) {
            jdbcTemplate.update(
                """
                    UPDATE projects
                    SET title = ?, slug = ?, summary = ?, year_label = ?, role = ?, tools = ?,
                        external_link = ?, link_label = ?, card_image_path = ?, fallback_image_path = ?,
                        card_gradient = ?, card_image_mode = ?, card_image_scale = ?, narrative = ?,
                        featured = ?, published = ?, sort_order = ?, updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                project.getTitle(),
                project.getSlug(),
                project.getSummary(),
                project.getYearLabel(),
                project.getRole(),
                project.getTools(),
                blankToNull(project.getExternalLink()),
                blankToNull(project.getLinkLabel()),
                blankToNull(project.getCardImagePath()),
                blankToNull(project.getFallbackImagePath()),
                blankToNull(project.getCardGradient()),
                project.getCardImageMode(),
                project.getCardImageScale(),
                project.getNarrative(),
                project.isFeatured() ? 1 : 0,
                project.isPublished() ? 1 : 0,
                project.getSortOrder(),
                project.getId()
            );
        } else {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(
                    """
                        INSERT INTO projects (
                            title, slug, summary, year_label, role, tools, external_link, link_label,
                            card_image_path, fallback_image_path, card_gradient, card_image_mode,
                            card_image_scale, narrative, featured, published, sort_order
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                    Statement.RETURN_GENERATED_KEYS
                );
                statement.setString(1, project.getTitle());
                statement.setString(2, project.getSlug());
                statement.setString(3, project.getSummary());
                statement.setString(4, project.getYearLabel());
                statement.setString(5, project.getRole());
                statement.setString(6, project.getTools());
                statement.setString(7, blankToNull(project.getExternalLink()));
                statement.setString(8, blankToNull(project.getLinkLabel()));
                statement.setString(9, blankToNull(project.getCardImagePath()));
                statement.setString(10, blankToNull(project.getFallbackImagePath()));
                statement.setString(11, blankToNull(project.getCardGradient()));
                statement.setString(12, project.getCardImageMode());
                statement.setDouble(13, project.getCardImageScale());
                statement.setString(14, project.getNarrative());
                statement.setInt(15, project.isFeatured() ? 1 : 0);
                statement.setInt(16, project.isPublished() ? 1 : 0);
                statement.setInt(17, project.getSortOrder());
                return statement;
            }, keyHolder);
            project.setId(keyHolder.getKey().longValue());
        }

        if (cardImageFile != null && !cardImageFile.isEmpty()) {
            if (updating && existing != null) {
                mediaStorageService.deleteIfPresent(existing.getCardImagePath());
            }
            String projectFolder = projectUploadFolder(project.getId());
            String cardImagePath = mediaStorageService.store(cardImageFile, projectFolder).publicPath();
            project.setCardImagePath(cardImagePath);
            project.setFallbackImagePath(cardImagePath);
            jdbcTemplate.update(
                """
                    UPDATE projects
                    SET card_image_path = ?, fallback_image_path = ?, updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                cardImagePath,
                cardImagePath,
                project.getId()
            );
        }

        if (galleryFiles != null && galleryFiles.length > 0 && java.util.Arrays.stream(galleryFiles).anyMatch(file -> file != null && !file.isEmpty())) {
            appendProjectMedia(
                project.getId(),
                existing == null ? List.of() : existing.getMedia(),
                galleryFiles,
                project.getTitle(),
                projectUploadFolder(project.getId())
            );
        }

        logContentSaved("project", project.getId(), project.getTitle(), project.isPublished());
        return project.getId();
    }

    @Transactional
    public void deleteProject(long id) {
        findProjectById(id).ifPresent(project -> {
            project.getMedia().forEach(media -> mediaStorageService.deleteIfPresent(media.getFilePath()));
            mediaStorageService.deleteIfPresent(project.getCardImagePath());
            jdbcTemplate.update("DELETE FROM projects WHERE id = ?", id);
            logContentRemoved("project", id, project.getTitle());
        });
    }

    public List<GalleryEntry> listPublishedGalleryEntries() {
        List<GalleryEntry> entries = jdbcTemplate.query(
            """
                SELECT * FROM gallery_entries
                WHERE published = 1
                ORDER BY sort_order ASC, id DESC
                """,
            galleryRowMapper()
        );
        entries.forEach(entry -> entry.setMedia(listGalleryMedia(entry.getId())));
        return entries;
    }

    public Optional<GalleryEntry> pickRandomGalleryEntry(List<GalleryEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }

        List<GalleryEntry> withMedia = new ArrayList<>();
        for (GalleryEntry entry : entries) {
            if (entry.getMedia() != null && !entry.getMedia().isEmpty()) {
                withMedia.add(entry);
            }
        }

        List<GalleryEntry> pool = withMedia.isEmpty() ? entries : withMedia;
        return Optional.of(pool.get(ThreadLocalRandom.current().nextInt(pool.size())));
    }

    public List<GalleryEntry> listAllGalleryEntries() {
        List<GalleryEntry> entries = jdbcTemplate.query(
            "SELECT * FROM gallery_entries ORDER BY sort_order ASC, id DESC",
            galleryRowMapper()
        );
        entries.forEach(entry -> entry.setMedia(listGalleryMedia(entry.getId())));
        return entries;
    }

    public Optional<GalleryEntry> findGalleryEntryById(long id) {
        return findOne("SELECT * FROM gallery_entries WHERE id = ?", galleryRowMapper(), id)
            .map(entry -> {
                entry.setMedia(listGalleryMedia(entry.getId()));
                return entry;
            });
    }

    public Optional<GalleryEntry> findPublishedGalleryEntryById(long id) {
        return findOne("SELECT * FROM gallery_entries WHERE id = ? AND published = 1", galleryRowMapper(), id)
            .map(entry -> {
                entry.setMedia(listGalleryMedia(entry.getId()));
                return entry;
            });
    }

    public Optional<GalleryEntry> findPublishedGalleryEntryBySlug(String slug) {
        return findOne("SELECT * FROM gallery_entries WHERE slug = ? AND published = 1", galleryRowMapper(), slug)
            .map(entry -> {
                entry.setMedia(listGalleryMedia(entry.getId()));
                return entry;
            });
    }

    public List<GalleryEntry> listDraftGalleryEntries() {
        List<GalleryEntry> entries = jdbcTemplate.query(
            """
                SELECT * FROM gallery_entries
                WHERE published = 0
                ORDER BY updated_at DESC, id DESC
                """,
            galleryRowMapper()
        );
        entries.forEach(entry -> entry.setMedia(listGalleryMedia(entry.getId())));
        return entries;
    }

    @Transactional
    public long saveGalleryEntry(GalleryEntry entry, MultipartFile[] mediaFiles) {
        sanitizeGalleryEntry(entry);
        boolean updating = entry.getId() > 0;
        GalleryEntry existing = updating ? findGalleryEntryById(entry.getId()).orElse(null) : null;

        if (updating) {
            jdbcTemplate.update(
                """
                    UPDATE gallery_entries
                    SET title = ?, slug = ?, intro_text = ?, body = ?, category = ?, published = ?,
                        sort_order = ?, published_at = ?, updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                entry.getTitle(),
                entry.getSlug(),
                entry.getIntroText(),
                entry.getBody(),
                blankToNull(entry.getCategory()),
                entry.isPublished() ? 1 : 0,
                entry.getSortOrder(),
                entry.isPublished() ? nowText() : null,
                entry.getId()
            );
        } else {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(
                    """
                        INSERT INTO gallery_entries (
                            title, slug, intro_text, body, category, published, sort_order, published_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                    Statement.RETURN_GENERATED_KEYS
                );
                statement.setString(1, entry.getTitle());
                statement.setString(2, entry.getSlug());
                statement.setString(3, entry.getIntroText());
                statement.setString(4, entry.getBody());
                statement.setString(5, blankToNull(entry.getCategory()));
                statement.setInt(6, entry.isPublished() ? 1 : 0);
                statement.setInt(7, entry.getSortOrder());
                statement.setString(8, entry.isPublished() ? nowText() : null);
                return statement;
            }, keyHolder);
            entry.setId(keyHolder.getKey().longValue());
        }

        if (mediaFiles != null && mediaFiles.length > 0 && java.util.Arrays.stream(mediaFiles).anyMatch(file -> file != null && !file.isEmpty())) {
            appendGalleryMedia(entry.getId(), existing == null ? List.of() : existing.getMedia(), mediaFiles, entry.getTitle());
        }

        logContentSaved("gallery", entry.getId(), entry.getTitle(), entry.isPublished());
        return entry.getId();
    }

    @Transactional
    public void deleteGalleryEntry(long id) {
        findGalleryEntryById(id).ifPresent(entry -> {
            entry.getMedia().forEach(media -> mediaStorageService.deleteIfPresent(media.getFilePath()));
            jdbcTemplate.update("DELETE FROM gallery_entries WHERE id = ?", id);
            logContentRemoved("gallery", id, entry.getTitle());
        });
    }

    public List<BlogPost> listPublishedBlogPosts() {
        return jdbcTemplate.query(
            "SELECT * FROM blog_posts WHERE published = 1 ORDER BY sort_order ASC, id DESC",
            blogRowMapper()
        );
    }

    public List<BlogPost> listAllBlogPosts() {
        return jdbcTemplate.query(
            "SELECT * FROM blog_posts ORDER BY sort_order ASC, id DESC",
            blogRowMapper()
        );
    }

    public Optional<BlogPost> findBlogPostById(long id) {
        return findOne("SELECT * FROM blog_posts WHERE id = ?", blogRowMapper(), id);
    }

    public Optional<BlogPost> findPublishedBlogPostById(long id) {
        return findOne("SELECT * FROM blog_posts WHERE id = ? AND published = 1", blogRowMapper(), id);
    }

    public Optional<BlogPost> findPublishedBlogPostBySlug(String slug) {
        return findOne("SELECT * FROM blog_posts WHERE slug = ? AND published = 1", blogRowMapper(), slug);
    }

    public List<BlogPost> listDraftBlogPosts() {
        return jdbcTemplate.query(
            "SELECT * FROM blog_posts WHERE published = 0 ORDER BY updated_at DESC, id DESC",
            blogRowMapper()
        );
    }

    @Transactional
    public long saveBlogPost(BlogPost blogPost, MultipartFile coverImageFile) {
        sanitizeBlogPost(blogPost);
        boolean updating = blogPost.getId() > 0;
        BlogPost existing = updating ? findBlogPostById(blogPost.getId()).orElse(null) : null;

        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            if (existing != null) {
                mediaStorageService.deleteIfPresent(existing.getCoverImagePath());
            }
            blogPost.setCoverImagePath(mediaStorageService.store(coverImageFile, "blog").publicPath());
        } else if (existing != null) {
            blogPost.setCoverImagePath(existing.getCoverImagePath());
        }

        if (updating) {
            jdbcTemplate.update(
                """
                    UPDATE blog_posts
                    SET title = ?, slug = ?, excerpt = ?, body = ?, cover_image_path = ?,
                        published = ?, sort_order = ?, published_at = ?, updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """,
                blogPost.getTitle(),
                blogPost.getSlug(),
                blogPost.getExcerpt(),
                blogPost.getBody(),
                blankToNull(blogPost.getCoverImagePath()),
                blogPost.isPublished() ? 1 : 0,
                blogPost.getSortOrder(),
                blogPost.isPublished() ? nowText() : null,
                blogPost.getId()
            );
        } else {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(
                    """
                        INSERT INTO blog_posts (
                            title, slug, excerpt, body, cover_image_path, published, sort_order, published_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                    Statement.RETURN_GENERATED_KEYS
                );
                statement.setString(1, blogPost.getTitle());
                statement.setString(2, blogPost.getSlug());
                statement.setString(3, blogPost.getExcerpt());
                statement.setString(4, blogPost.getBody());
                statement.setString(5, blankToNull(blogPost.getCoverImagePath()));
                statement.setInt(6, blogPost.isPublished() ? 1 : 0);
                statement.setInt(7, blogPost.getSortOrder());
                statement.setString(8, blogPost.isPublished() ? nowText() : null);
                return statement;
            }, keyHolder);
            blogPost.setId(keyHolder.getKey().longValue());
        }

        logContentSaved("blog", blogPost.getId(), blogPost.getTitle(), blogPost.isPublished());
        return blogPost.getId();
    }

    @Transactional
    public void deleteBlogPost(long id) {
        findBlogPostById(id).ifPresent(blogPost -> {
            mediaStorageService.deleteIfPresent(blogPost.getCoverImagePath());
            jdbcTemplate.update("DELETE FROM blog_posts WHERE id = ?", id);
            logContentRemoved("blog", id, blogPost.getTitle());
        });
    }

    public Optional<CmsUser> findCmsUserByUsername(String username) {
        return findOne("SELECT * FROM cms_users WHERE username = ?", cmsUserRowMapper(), username);
    }

    @Transactional
    public void ensureSeedData() {
        ensureStudioOwner();
        removeSeedGalleryExample();
        removeSeedBlogExample();
    }

    @Transactional
    public void recordSuccessfulLogin(String username, String ipAddress) {
        jdbcTemplate.update(
            """
                UPDATE cms_users
                SET failed_login_attempts = 0, locked_until = NULL, last_login_at = ?, updated_at = CURRENT_TIMESTAMP
                WHERE username = ?
                """,
            nowText(),
            username
        );
        logAudit(username, "LOGIN_SUCCESS", "cms_user", username, "Sign-in succeeded", ipAddress);
    }

    @Transactional
    public void recordFailedLogin(String username, String ipAddress) {
        if (!StringUtils.hasText(username)) {
            logAudit("anonymous", "LOGIN_FAILURE", "cms_user", null, "Blank username attempt", ipAddress);
            return;
        }

        findCmsUserByUsername(username).ifPresent(user -> {
            int nextAttempts = user.getFailedLoginAttempts() + 1;
            LocalDateTime lockedUntil = nextAttempts >= portfolioProperties.getSecurity().getLoginLockThreshold()
                ? LocalDateTime.now().plusMinutes(portfolioProperties.getSecurity().getLoginLockMinutes())
                : null;

            jdbcTemplate.update(
                """
                    UPDATE cms_users
                    SET failed_login_attempts = ?, locked_until = ?, updated_at = CURRENT_TIMESTAMP
                    WHERE username = ?
                    """,
                nextAttempts,
                lockedUntil == null ? null : lockedUntil.format(SQL_TIMESTAMP),
                username
            );
        });

        logAudit(username, "LOGIN_FAILURE", "cms_user", username, "Sign-in failed", ipAddress);
    }

    public Map<String, Object> buildHomeViewModel() {
        LinkedHashMap<String, Object> model = new LinkedHashMap<>();
        model.put("workProjects", buildWorkProjectsMap());
        model.put("galleryEntries", listPublishedGalleryEntries());
        model.put("blogPosts", listPublishedBlogPosts());
        return model;
    }

    public Map<String, Object> buildWorkProjectsMap() {
        LinkedHashMap<String, Object> workProjects = new LinkedHashMap<>();
        List<Project> projects = listPublishedProjects();
        for (Project project : projects) {
            LinkedHashMap<String, Object> view = new LinkedHashMap<>();
            view.put("title", project.getTitle());
            view.put("desc", project.getSummary());
            view.put("year", project.getYearLabel());
            view.put("role", project.getRole());
            view.put("tools", project.getTools());
            view.put("link", project.getExternalLink());
            view.put("linkLabel", StringUtils.hasText(project.getLinkLabel()) ? project.getLinkLabel() : "Visit project");
            view.put("fallbackImage", project.getFallbackImagePath());
            view.put("cardImage", project.getCardImagePath());
            view.put("modalImages", project.getMedia().stream().map(ProjectMedia::getFilePath).toList());
            view.put("cardGradient", project.getCardGradient());
            view.put("cardImageMode", project.getCardImageMode());
            view.put("cardImageScale", project.getCardImageScale());
            view.put("detailPath", "/work/" + project.getId());
            workProjects.put(String.valueOf(project.getId()), view);
        }
        return workProjects;
    }

    public Map<String, Long> loadDashboardCounts() {
        LinkedHashMap<String, Long> counts = new LinkedHashMap<>();
        counts.put("projects", countRows("projects"));
        counts.put("galleryEntries", countRows("gallery_entries"));
        counts.put("blogPosts", countRows("blog_posts"));
        counts.put("auditEntries", countRows("audit_log"));
        return counts;
    }

    public List<Map<String, Object>> listRecentAuditEntries() {
        return jdbcTemplate.query(
            """
                SELECT actor_username, action_type, target_type, target_id, summary, ip_address, created_at
                FROM audit_log
                WHERE target_type IN ('project', 'gallery', 'blog')
                ORDER BY id DESC
                LIMIT 12
                """,
            (resultSet, rowNum) -> decorateContentActivity(resultSet)
        );
    }

    public List<Map<String, Object>> listMediaLibrary() {
        List<Map<String, Object>> items = new ArrayList<>();
        items.addAll(jdbcTemplate.query(
            """
                SELECT 'project' AS kind, p.title AS owner_title, pm.file_path AS file_path, pm.alt_text AS alt_text, pm.created_at AS created_at
                FROM project_media pm
                JOIN projects p ON p.id = pm.project_id
                ORDER BY pm.id DESC
                """,
            mediaLibraryRowMapper()
        ));
        items.addAll(jdbcTemplate.query(
            """
                SELECT 'gallery' AS kind, g.title AS owner_title, gm.file_path AS file_path, gm.alt_text AS alt_text, gm.created_at AS created_at
                FROM gallery_media gm
                JOIN gallery_entries g ON g.id = gm.gallery_entry_id
                ORDER BY gm.id DESC
                """,
            mediaLibraryRowMapper()
        ));
        items.addAll(jdbcTemplate.query(
            """
                SELECT 'blog' AS kind, b.title AS owner_title, b.cover_image_path AS file_path, b.excerpt AS alt_text, b.created_at AS created_at
                FROM blog_posts b
                WHERE b.cover_image_path IS NOT NULL AND b.cover_image_path <> ''
                ORDER BY b.id DESC
                """,
            mediaLibraryRowMapper()
        ));
        return items;
    }

    private RowMapper<Map<String, Object>> mediaLibraryRowMapper() {
        return (resultSet, rowNum) -> {
            LinkedHashMap<String, Object> item = new LinkedHashMap<>();
            item.put("kind", resultSet.getString("kind"));
            item.put("ownerTitle", resultSet.getString("owner_title"));
            item.put("filePath", resultSet.getString("file_path"));
            item.put("altText", resultSet.getString("alt_text"));
            item.put("createdAt", resultSet.getString("created_at"));
            return item;
        };
    }

    private List<ProjectMedia> listProjectMedia(long projectId) {
        return jdbcTemplate.query(
            "SELECT * FROM project_media WHERE project_id = ? ORDER BY media_order ASC, id ASC",
            projectMediaRowMapper(),
            projectId
        );
    }

    private List<GalleryMedia> listGalleryMedia(long galleryEntryId) {
        return jdbcTemplate.query(
            "SELECT * FROM gallery_media WHERE gallery_entry_id = ? ORDER BY media_order ASC, id ASC",
            galleryMediaRowMapper(),
            galleryEntryId
        );
    }

    private void appendProjectMedia(long projectId, List<ProjectMedia> existingMedia, MultipartFile[] files, String projectTitle, String folder) {
        List<MediaStorageService.StoredFile> storedFiles = mediaStorageService.storeAll(files, folder);
        if (storedFiles.isEmpty()) {
            return;
        }

        int order = existingMedia.stream().mapToInt(ProjectMedia::getMediaOrder).max().orElse(-1) + 1;
        boolean hasCover = existingMedia.stream().anyMatch(ProjectMedia::isCover);
        int added = 0;
        for (MediaStorageService.StoredFile storedFile : storedFiles) {
            jdbcTemplate.update(
                """
                    INSERT INTO project_media (project_id, file_path, alt_text, media_order, is_cover, original_filename)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                projectId,
                storedFile.publicPath(),
                projectTitle + " image " + (order + 1),
                order,
                !hasCover && added == 0 ? 1 : 0,
                storedFile.originalFilename()
            );
            order++;
            added++;
        }
    }

    private void appendGalleryMedia(long galleryEntryId, List<GalleryMedia> existingMedia, MultipartFile[] files, String title) {
        List<MediaStorageService.StoredFile> storedFiles = mediaStorageService.storeAll(files, "gallery");
        if (storedFiles.isEmpty()) {
            return;
        }

        int order = existingMedia.stream().mapToInt(GalleryMedia::getMediaOrder).max().orElse(-1) + 1;
        boolean hasCover = existingMedia.stream().anyMatch(GalleryMedia::isCover);
        int added = 0;
        for (MediaStorageService.StoredFile storedFile : storedFiles) {
            jdbcTemplate.update(
                """
                    INSERT INTO gallery_media (gallery_entry_id, file_path, alt_text, media_order, is_cover, original_filename)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                galleryEntryId,
                storedFile.publicPath(),
                title + " image " + (order + 1),
                order,
                !hasCover && added == 0 ? 1 : 0,
                storedFile.originalFilename()
            );
            order++;
            added++;
        }
    }

    private void ensureStudioOwner() {
        if (countRows("cms_users") > 0) {
            return;
        }

        String username = portfolioProperties.getSeed().getOwner().getUsername();
        String password = portfolioProperties.getSeed().getOwner().getPassword();
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            log.warn(
                "cms_users is empty and no seed owner is configured. "
                    + "Copy application-local.properties.example to application-local.properties, "
                    + "or set PORTFOLIO_SEED_OWNER_USERNAME and PORTFOLIO_SEED_OWNER_PASSWORD."
            );
            return;
        }

        jdbcTemplate.update(
            """
                INSERT INTO cms_users (username, password_hash, active)
                VALUES (?, ?, 1)
                """,
            username,
            passwordEncoder.encode(password)
        );
        logAudit(username, "SEED_OWNER_CREATED", "cms_user", username, "Seed owner account created", "system");
    }

    private void removeSeedGalleryExample() {
        jdbcTemplate.query(
            "SELECT id FROM gallery_entries WHERE slug = 'design-process-notes'",
            (resultSet, rowNum) -> resultSet.getLong("id")
        ).forEach(this::deleteGalleryEntry);
    }

    private void removeSeedBlogExample() {
        jdbcTemplate.query(
            "SELECT id FROM blog_posts WHERE slug = 'starting-the-studio-layer'",
            (resultSet, rowNum) -> resultSet.getLong("id")
        ).forEach(this::deleteBlogPost);
    }

    private long countRows(String tableName) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
        return count == null ? 0 : count;
    }

    private void sanitizeProject(Project project) {
        project.setTitle(trim(project.getTitle()));
        String existingSlug = project.getId() > 0
            ? findProjectById(project.getId()).map(Project::getSlug).orElse(null)
            : null;
        project.setSlug(resolveSlug("projects", project.getId(), project.getSlug(), project.getTitle(), existingSlug));
        project.setSummary(trim(project.getSummary()));
        project.setYearLabel(trim(project.getYearLabel()));
        project.setRole(trim(project.getRole()));
        project.setTools(trim(project.getTools()));
        project.setExternalLink(trim(project.getExternalLink()));
        project.setLinkLabel(trim(project.getLinkLabel()));
        project.setCardGradient(trim(project.getCardGradient()));
        project.setCardImageMode(StringUtils.hasText(project.getCardImageMode()) ? trim(project.getCardImageMode()) : "cover");
        project.setCardImageScale(project.getCardImageScale() == null || project.getCardImageScale() <= 0 ? 1.0 : project.getCardImageScale());
        project.setNarrative(trim(project.getNarrative()));
        if (!StringUtils.hasText(project.getFallbackImagePath())) {
            project.setFallbackImagePath(project.getCardImagePath());
        }
    }

    private void sanitizeGalleryEntry(GalleryEntry entry) {
        entry.setTitle(trim(entry.getTitle()));
        String existingSlug = entry.getId() > 0
            ? findGalleryEntryById(entry.getId()).map(GalleryEntry::getSlug).orElse(null)
            : null;
        entry.setSlug(resolveSlug("gallery_entries", entry.getId(), entry.getSlug(), entry.getTitle(), existingSlug));
        entry.setIntroText(nullToEmpty(entry.getIntroText()));
        entry.setBody(nullToEmpty(entry.getBody()));
        entry.setCategory(trim(entry.getCategory()));
    }

    private void sanitizeBlogPost(BlogPost blogPost) {
        blogPost.setTitle(trim(blogPost.getTitle()));
        String existingSlug = blogPost.getId() > 0
            ? findBlogPostById(blogPost.getId()).map(BlogPost::getSlug).orElse(null)
            : null;
        blogPost.setSlug(resolveSlug("blog_posts", blogPost.getId(), blogPost.getSlug(), blogPost.getTitle(), existingSlug));
        blogPost.setExcerpt(trim(blogPost.getExcerpt()));
        blogPost.setBody(trim(blogPost.getBody()));
    }

    private void logContentSaved(String targetType, long id, String title, boolean published) {
        String action = targetType.toUpperCase(Locale.ROOT) + (published ? "_SAVED_PUBLISHED" : "_SAVED_DRAFT");
        logAudit(getSeedUsername(), action, targetType, String.valueOf(id), title, "system");
    }

    private void logContentRemoved(String targetType, long id, String title) {
        logAudit(getSeedUsername(), targetType.toUpperCase(Locale.ROOT) + "_DELETED", targetType, String.valueOf(id), title, "system");
    }

    private Map<String, Object> decorateContentActivity(ResultSet resultSet) throws SQLException {
        String actionType = resultSet.getString("action_type");
        String targetType = resultSet.getString("target_type");
        LinkedHashMap<String, Object> item = new LinkedHashMap<>();
        item.put("actorUsername", resultSet.getString("actor_username"));
        item.put("actionType", actionType);
        item.put("targetType", targetType);
        item.put("targetId", resultSet.getString("target_id"));
        item.put("title", resultSet.getString("summary"));
        item.put("summary", resultSet.getString("summary"));
        item.put("ipAddress", resultSet.getString("ip_address"));
        item.put("createdAt", resultSet.getString("created_at"));
        item.put("kindLabel", switch (targetType == null ? "" : targetType) {
            case "project" -> "Project";
            case "gallery" -> "Gallery";
            case "blog" -> "Blog";
            default -> "Desk";
        });

        boolean removed = actionType != null && actionType.endsWith("_DELETED");
        boolean published = actionType != null && actionType.contains("PUBLISHED");
        boolean draft = actionType != null && actionType.contains("DRAFT");
        if (removed) {
            item.put("actionLabel", "Removed");
            item.put("statusLabel", "Removed");
            item.put("statusTone", "removed");
        } else if (published) {
            item.put("actionLabel", "Saved");
            item.put("statusLabel", "Published");
            item.put("statusTone", "live");
        } else if (draft) {
            item.put("actionLabel", "Saved");
            item.put("statusLabel", "Draft");
            item.put("statusTone", "draft");
        } else {
            item.put("actionLabel", "Saved");
            item.put("statusLabel", "Saved");
            item.put("statusTone", "draft");
        }
        return item;
    }

    private void logAudit(String actorUsername, String actionType, String targetType, String targetId, String summary, String ipAddress) {
        jdbcTemplate.update(
            """
                INSERT INTO audit_log (actor_username, action_type, target_type, target_id, summary, ip_address)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
            actorUsername,
            actionType,
            targetType,
            targetId,
            summary,
            ipAddress
        );
    }

    private String getSeedUsername() {
        String username = portfolioProperties.getSeed().getOwner().getUsername();
        return StringUtils.hasText(username) ? username : "system";
    }

    private <T> Optional<T> findOne(String sql, RowMapper<T> mapper, Object... args) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, mapper, args));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    private RowMapper<Project> projectRowMapper() {
        return (resultSet, rowNum) -> {
            Project project = new Project();
            project.setId(resultSet.getLong("id"));
            project.setTitle(resultSet.getString("title"));
            project.setSlug(resultSet.getString("slug"));
            project.setSummary(resultSet.getString("summary"));
            project.setYearLabel(resultSet.getString("year_label"));
            project.setRole(resultSet.getString("role"));
            project.setTools(resultSet.getString("tools"));
            project.setExternalLink(resultSet.getString("external_link"));
            project.setLinkLabel(resultSet.getString("link_label"));
            project.setCardImagePath(resultSet.getString("card_image_path"));
            project.setFallbackImagePath(resultSet.getString("fallback_image_path"));
            project.setCardGradient(resultSet.getString("card_gradient"));
            project.setCardImageMode(resultSet.getString("card_image_mode"));
            project.setCardImageScale(resultSet.getDouble("card_image_scale"));
            project.setNarrative(resultSet.getString("narrative"));
            project.setFeatured(resultSet.getInt("featured") == 1);
            project.setPublished(resultSet.getInt("published") == 1);
            project.setSortOrder(resultSet.getInt("sort_order"));
            project.setCreatedAt(resultSet.getString("created_at"));
            project.setUpdatedAt(resultSet.getString("updated_at"));
            return project;
        };
    }

    private RowMapper<ProjectMedia> projectMediaRowMapper() {
        return (resultSet, rowNum) -> {
            ProjectMedia media = new ProjectMedia();
            media.setId(resultSet.getLong("id"));
            media.setProjectId(resultSet.getLong("project_id"));
            media.setFilePath(resultSet.getString("file_path"));
            media.setAltText(resultSet.getString("alt_text"));
            media.setMediaOrder(resultSet.getInt("media_order"));
            media.setCover(resultSet.getInt("is_cover") == 1);
            media.setOriginalFilename(resultSet.getString("original_filename"));
            return media;
        };
    }

    private RowMapper<GalleryEntry> galleryRowMapper() {
        return (resultSet, rowNum) -> {
            GalleryEntry entry = new GalleryEntry();
            entry.setId(resultSet.getLong("id"));
            entry.setTitle(resultSet.getString("title"));
            entry.setSlug(resultSet.getString("slug"));
            entry.setIntroText(resultSet.getString("intro_text"));
            entry.setBody(resultSet.getString("body"));
            entry.setCategory(resultSet.getString("category"));
            entry.setPublished(resultSet.getInt("published") == 1);
            entry.setSortOrder(resultSet.getInt("sort_order"));
            entry.setPublishedAt(resultSet.getString("published_at"));
            entry.setCreatedAt(resultSet.getString("created_at"));
            entry.setUpdatedAt(resultSet.getString("updated_at"));
            return entry;
        };
    }

    private RowMapper<GalleryMedia> galleryMediaRowMapper() {
        return (resultSet, rowNum) -> {
            GalleryMedia media = new GalleryMedia();
            media.setId(resultSet.getLong("id"));
            media.setGalleryEntryId(resultSet.getLong("gallery_entry_id"));
            media.setFilePath(resultSet.getString("file_path"));
            media.setAltText(resultSet.getString("alt_text"));
            media.setMediaOrder(resultSet.getInt("media_order"));
            media.setCover(resultSet.getInt("is_cover") == 1);
            media.setOriginalFilename(resultSet.getString("original_filename"));
            return media;
        };
    }

    private RowMapper<BlogPost> blogRowMapper() {
        return (resultSet, rowNum) -> {
            BlogPost blogPost = new BlogPost();
            blogPost.setId(resultSet.getLong("id"));
            blogPost.setTitle(resultSet.getString("title"));
            blogPost.setSlug(resultSet.getString("slug"));
            blogPost.setExcerpt(resultSet.getString("excerpt"));
            blogPost.setBody(resultSet.getString("body"));
            blogPost.setCoverImagePath(resultSet.getString("cover_image_path"));
            blogPost.setPublished(resultSet.getInt("published") == 1);
            blogPost.setSortOrder(resultSet.getInt("sort_order"));
            blogPost.setPublishedAt(resultSet.getString("published_at"));
            blogPost.setCreatedAt(resultSet.getString("created_at"));
            blogPost.setUpdatedAt(resultSet.getString("updated_at"));
            return blogPost;
        };
    }

    private RowMapper<CmsUser> cmsUserRowMapper() {
        return (resultSet, rowNum) -> {
            CmsUser cmsUser = new CmsUser();
            cmsUser.setId(resultSet.getLong("id"));
            cmsUser.setUsername(resultSet.getString("username"));
            cmsUser.setPasswordHash(resultSet.getString("password_hash"));
            cmsUser.setActive(resultSet.getInt("active") == 1);
            cmsUser.setFailedLoginAttempts(resultSet.getInt("failed_login_attempts"));
            cmsUser.setLockedUntil(parseDateTime(resultSet.getString("locked_until")));
            cmsUser.setLastLoginAt(parseDateTime(resultSet.getString("last_login_at")));
            return cmsUser;
        };
    }

    private LocalDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return LocalDateTime.parse(value, SQL_TIMESTAMP);
    }

    private String nowText() {
        return LocalDateTime.now().format(SQL_TIMESTAMP);
    }

    private String projectUploadFolder(long projectId) {
        return "projects/PJKT_" + projectId + "_img";
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String resolveSlug(String table, long id, String incoming, String title, String existing) {
        if (id > 0 && StringUtils.hasText(existing) && !StringUtils.hasText(incoming)) {
            return existing;
        }
        String seed = StringUtils.hasText(incoming) ? incoming : title;
        return uniqueSlug(table, seed, id);
    }

    private String uniqueSlug(String table, String value, long currentId) {
        if (!Set.of("projects", "gallery_entries", "blog_posts").contains(table)) {
            throw new IllegalArgumentException("Unsupported slug table.");
        }

        String base = slugify(value);
        if (!StringUtils.hasText(base)) {
            base = "item";
        }

        String candidate = base;
        int suffix = 2;
        while (slugExists(table, candidate, currentId)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private boolean slugExists(String table, String slug, long currentId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM " + table + " WHERE slug = ? AND id <> ?",
            Integer.class,
            slug,
            currentId
        );
        return count != null && count > 0;
    }

    private String slugify(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim()
            .toLowerCase()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-|-$)", "");
    }
}
