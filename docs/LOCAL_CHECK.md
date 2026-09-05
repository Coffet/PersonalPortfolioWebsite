# Local check — run it on your PC for development

This is how you **run the site on localhost for day-to-day work**. You do not need a VPS, nginx, Cloudflare, or a WAR.

The public internet is a different machine. How to **create** that machine and how to **copy a WAR onto it** is the [README](../README.md). This file is only: clone → your own password → start Java → walk the code → click the pages.

There is **no default CMS password in git**. A sample login in a public repo becomes the password people type on a real server. That is why local setup is “copy a file and type your own pair,” not “user/pass printed here.”

---

## Contents

1. [What this is / is not](#1-what-this-is--is-not)
2. [What you need](#2-what-you-need)
3. [Open in IntelliJ](#3-open-in-intellij)
4. [Set up and run (required)](#4-set-up-and-run-required)
5. [Local URLs](#5-local-urls)
6. [Structure](#6-structure)
7. [File-by-file](#7-file-by-file)
8. [Schema](#8-schema)
9. [Local vs server passwords](#9-local-vs-server-passwords)
10. [Should / should not](#10-should--should-not)
11. [Click-through verification](#11-click-through-verification)
12. [Tests](#12-tests)
13. [Troubleshooting](#13-troubleshooting)
14. [When you want it on the internet](#14-when-you-want-it-on-the-internet)
15. [Optional local MinIO](#15-optional-local-minio)

---

## 1. What this is / is not

| You are doing this | You are not doing this |
|---|---|
| Developing and inspecting source | Updating coft.moe |
| Creating a **local** SQLite file | Touching Linode’s database |
| Using gitignored `application-local.properties` | Editing systemd on the VPS |

```
Dev (this file):   JDK → spring-boot:run → http://localhost:8080
Live (README):     mvn package → scp WAR → systemctl restart
```

---

## 2. What you need

- **JDK 17**. In a terminal: `java -version` → `17`.
- **Git**.
- Optional: **IntelliJ IDEA** (Community is enough).
- Maven as a global install: **not required**. Use `.\mvnw.cmd` (Windows) or `./mvnw` (Linux/macOS).

Windows examples below use PowerShell. On Linux/macOS replace `copy` with `cp` and `.\mvnw.cmd` with `./mvnw`.

---

## 3. Open in IntelliJ

1. **File → Open** the folder that contains `pom.xml` (repository root). Not `src/`, not `docs/`.
2. Trust the project. Let Maven import.
3. **File → Project Structure → Project SDK:** JDK 17.
4. You should see `src/main/java`, `src/main/resources`, `src/main/webapp`.
5. Open `src/main/resources/application.properties`. There must be **no** committed `portfolio.seed.owner.password=`. Seed comes from the optional local file.

> **Annotation:** If IntelliJ suggests running a leftover static HTML file, ignore it. The app is `com.portfolio.studio.PortfolioStudioApplication`. Run configuration → Working directory = project root so `application-local.properties` and `storage/` are found.

---

## 4. Set up and run (required)

From the repo root:

```powershell
cd C:\Users\User\Desktop\PersonalPortfolioWebsite
copy application-local.properties.example application-local.properties
notepad application-local.properties
```

Set **your** username and password (both lines). Not the example strings. Not the VPS login.

```
portfolio.seed.owner.username=...
portfolio.seed.owner.password=...
```

Confirm git will not take the file:

```powershell
git status
```

Then start:

```powershell
.\mvnw.cmd spring-boot:run
```

Or the green arrow on `PortfolioStudioApplication`.

First boot with an empty `storage/database/portfolio.db` creates the owner from that file. Later boots skip seed. The log warns if `cms_users` is empty and no seed was configured.

Stop: `Ctrl+C` in the terminal, or stop the run in IntelliJ.

---

## 5. Local URLs

After “Started PortfolioStudioApplication”:

| Page | URL |
|---|---|
| Home | http://localhost:8080/ |
| Gallery | http://localhost:8080/gallery |
| Work | http://localhost:8080/ (on the home page; empty until you add a project) |
| Blog | http://localhost:8080/blog |
| CMS sign-in | http://localhost:8080/cmsmgmnt/sign-in |
| CMS desk | http://localhost:8080/cmsmgmnt/dashboard (after login) |

`/admin` and `/studio` should not be a desk. 404 is correct. To rename `/cmsmgmnt`, see [README → CMS](../README.md#cms).

---

## 6. Structure

```
PersonalPortfolioWebsite/
├── pom.xml
├── mvnw.cmd / mvnw
├── application-local.properties.example
├── application-local.properties          you create this; gitignored
├── README.md                             VPS + WAR + this local path in short
├── docs/LOCAL_CHECK.md                   this file
├── deploy/                               ignore while developing locally
├── storage/                              created on first run
└── src/main/{java, resources, webapp}
```

---

## 7. File-by-file

Walk these so you know what you are running.

| # | File | Confirm |
|---|---|---|
| 1 | `pom.xml` | Java 17, Spring Boot 3.5, war, SQLite, Jasper, JSTL. No React. |
| 2 | `application.properties` | Port 8080, SQLite path, upload root, MinIO flags default off, optional import of `./application-local.properties`. |
| 3 | `application-local.properties.example` | Seed placeholders plus commented MinIO keys. |
| 4 | `.gitignore` | Local properties, `storage/database/*.db`, uploads, `.env`. |
| 5 | `db/migration/V1__init_schema.sql` | Users, projects, gallery, blog, audit. |
| 6 | `SecurityConfig.java` | Public site + CMS sign-in open; rest of `/cmsmgmnt/**` needs `OWNER`. |
| 7 | `PublicController.java` | `/`, `/gallery`, `/work`, `/blog`. |
| 8 | `StudioController.java` | CMS CRUD. |
| 9 | `PortfolioService.java` | Writes, uploads, `ensureStudioOwner()`. |
| 10 | `jsp/public/` | Visitor pages. |
| 11 | `jsp/studio/` | CMS pages. |
| 12 | `static/assets/css` | Plain CSS. |
| 13 | `storage/` after a run | `portfolio.db` and upload folders. |

---

## 8. Schema

Login lives in `cms_users`:

- `username`
- `password_hash` (`$2a$10$...`)
- `active`, `failed_login_attempts`, `locked_until`

Public pages only show **published** content.

Local file: `storage/database/portfolio.db`

```sql
SELECT username, password_hash, active FROM cms_users;
```

---

## 9. Local vs server passwords

| Place | How |
|---|---|
| This PC (dev) | `application-local.properties` |
| Linode | `Environment=` in `/etc/systemd/system/portfolio.service` |

They **must not** be the same pair. There is no `/etc/portfolio.env` on the live coft.moe host.

If `cms_users` already has a row, changing the local file does nothing. That is expected.

---

## 10. Should / should not

**Do**

- Invent a local-only password.
- Keep `application-local.properties` untracked.
- Use `spring-boot:run` every day for UI/CMS work.
- Create sample content in the CMS; it stays in local SQLite.
- Run tests before you make a WAR.

**Do not**

- Put the example password on the VPS.
- Commit the local properties file or `portfolio.db`.
- Expect this file to print a working login.
- Delete `portfolio.db` because “code looks old.”
- `git pull` this tree onto `/var/www` and call it deploy.
- Point IntelliJ at a subfolder and wonder why Maven is gone.

---

## 11. Click-through verification

1. Home loads with CSS from `/assets/...`.
2. Gallery/blog may be empty — that is a new DB.
3. `/cmsmgmnt/dashboard` while signed out → sign-in page.
4. Sign in with **your** pair.
5. Create a project, a gallery entry, a blog post; publish them; upload an image.
6. Project images under `storage/uploads/projects/PJKT_<id>_img`.
7. Refresh `/`, `/gallery`, `/blog` — no `mvn package` needed.
8. Sign out. The desk should not stay open.

---

## 12. Tests

```powershell
.\mvnw.cmd test
```

They use temporary databases. They must not contain the live CMS password. They do not need a running MinIO.

---

## 13. Troubleshooting

**Sign-in fails**  
Wrong pair, or old DB. Query `cms_users`. Wipe `storage/database/*.db` only if you accept losing local CMS data, then restart **with** the local file in place.

**Log: no seed owner is configured**  
File missing, or working directory is not the repo root.

**`git status` shows the local properties file**  
Do not add it.

**Port 8080 busy**  
Stop the other Java process.

**No CSS**  
Wrong URL or not running `PortfolioStudioApplication`.

**Locked out**  
Five failures → ~15 minutes. Wait, or clear `locked_until` locally.

**IntelliJ ignores the local file**  
Set working directory to the project root.

---

## 14. When you want it on the internet

That is not this file. README:

1. [Make the WAR](../README.md#make-the-war)
2. No machine yet → [Make the VPS and host](../README.md#make-the-vps-and-host) (create the Linode, SSH, bootstrap, **your** password, scp)
3. Machine already exists → [If you already have a server](../README.md#if-you-already-have-a-server)

Do **not** copy this PC’s password onto the VPS.

---

## 15. Optional local MinIO

Default is still `storage/uploads`. You can develop forever without MinIO.

To try the addon on this PC:

1. Run MinIO on `127.0.0.1:9000` (binary or Docker). Do not publish 9000.
2. Put endpoint, bucket, and keys in gitignored `application-local.properties` (see the example file). Set `portfolio.storage.s3.enabled=true`.
3. Restart Java. Upload one image in `/cmsmgmnt`. Confirm it on `/gallery`, `/`, or `/blog`.
4. Set `portfolio.storage.s3.migrate=true`, restart once, then set it back to `false`. Logs say how many objects copied. Disk files stay.
5. To prove Java is reading MinIO, briefly rename `storage/uploads` (or point `upload-root` at an empty folder) and reload those pages.
6. Only after that looks right: set `portfolio.storage.s3.delete-local-after-verify=true`, restart once, then set it back to `false`.

`git push` does not start MinIO or move files. The live box is a separate install (`deploy/bootstrap-minio.sh`) after a WAR with this code is on the VPS.
