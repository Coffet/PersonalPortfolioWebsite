PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS cms_users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    active INTEGER NOT NULL DEFAULT 1,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TEXT,
    last_login_at TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS projects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    slug TEXT NOT NULL UNIQUE,
    summary TEXT NOT NULL,
    year_label TEXT NOT NULL,
    role TEXT NOT NULL,
    tools TEXT NOT NULL,
    external_link TEXT,
    link_label TEXT,
    card_image_path TEXT,
    fallback_image_path TEXT,
    card_gradient TEXT,
    card_image_mode TEXT NOT NULL DEFAULT 'cover',
    card_image_scale REAL NOT NULL DEFAULT 1.0,
    narrative TEXT NOT NULL,
    featured INTEGER NOT NULL DEFAULT 0,
    published INTEGER NOT NULL DEFAULT 0,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_media (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL,
    file_path TEXT NOT NULL,
    alt_text TEXT,
    media_order INTEGER NOT NULL DEFAULT 0,
    is_cover INTEGER NOT NULL DEFAULT 0,
    original_filename TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS gallery_entries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    slug TEXT NOT NULL UNIQUE,
    intro_text TEXT NOT NULL,
    body TEXT NOT NULL,
    category TEXT,
    published INTEGER NOT NULL DEFAULT 0,
    sort_order INTEGER NOT NULL DEFAULT 0,
    published_at TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS gallery_media (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    gallery_entry_id INTEGER NOT NULL,
    file_path TEXT NOT NULL,
    alt_text TEXT,
    media_order INTEGER NOT NULL DEFAULT 0,
    is_cover INTEGER NOT NULL DEFAULT 0,
    original_filename TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (gallery_entry_id) REFERENCES gallery_entries(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS blog_posts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    slug TEXT NOT NULL UNIQUE,
    excerpt TEXT NOT NULL,
    body TEXT NOT NULL,
    cover_image_path TEXT,
    published INTEGER NOT NULL DEFAULT 0,
    sort_order INTEGER NOT NULL DEFAULT 0,
    published_at TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    actor_username TEXT,
    action_type TEXT NOT NULL,
    target_type TEXT,
    target_id TEXT,
    summary TEXT,
    ip_address TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_projects_sort ON projects (published, sort_order, id DESC);
CREATE INDEX IF NOT EXISTS idx_project_media_sort ON project_media (project_id, media_order, id);
CREATE INDEX IF NOT EXISTS idx_gallery_entries_sort ON gallery_entries (published, sort_order, id DESC);
CREATE INDEX IF NOT EXISTS idx_gallery_media_sort ON gallery_media (gallery_entry_id, media_order, id);
CREATE INDEX IF NOT EXISTS idx_blog_posts_sort ON blog_posts (published, sort_order, id DESC);
CREATE INDEX IF NOT EXISTS idx_audit_log_created ON audit_log (created_at DESC);
