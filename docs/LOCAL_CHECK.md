# Local Check Guide

## Open In IntelliJ
1. Open the project root in IntelliJ IDEA.
2. Let IntelliJ detect the Maven project from `pom.xml`.
3. Confirm the main source layout is under `src/main/java`, `src/main/resources`, and `src/main/webapp`.

## Run The App
Use either option:

### Option A
Run the `PortfolioStudioApplication` class from IntelliJ.

### Option B
Run the Maven wrapper from a terminal:

```powershell
.\mvnw.cmd spring-boot:run
```

## Local Access Links
- Public home: `http://localhost:8080/`
- Gallery: `http://localhost:8080/gallery`
- Work detail: `http://localhost:8080/work/singularity`
- Blog: `http://localhost:8080/blog`
- CMS sign-in: `http://localhost:8080/cmsmgmnt/sign-in`

## Seeded Studio Credentials
- Username: `studio-owner`
- Password: `ChangeMe123!`

Those values come from `src/main/resources/application.properties`:

- `portfolio.seed.owner.username`
- `portfolio.seed.owner.password`

On first startup, if `cms_users` is empty, the app BCrypt-hashes that password and stores it in SQLite:

- Database: `storage/database/portfolio.db`
- Table: `cms_users`
- Column: `password_hash`

Inspect it with:

```sql
SELECT username, password_hash FROM cms_users;
```

The hash looks like `$2a$10$...`. Login uses that stored hash, not the properties file. Changing `portfolio.seed.owner.password` later does not update an existing user.

Change the seeded password after your first real use.

## What To Inspect
1. Open `src/main/resources/db/migration/V1__init_schema.sql` and verify the schema layout.
2. Open `src/main/java/com/portfolio/studio/service/PortfolioService.java` and check the content/data wiring.
3. Open `src/main/webapp/WEB-INF/jsp/public` to inspect the public routes.
4. Open `src/main/webapp/WEB-INF/jsp/studio` to inspect the CMS routes.
5. Open `src/main/resources/static/assets/css` and confirm the site uses plain CSS only.
6. Open `storage/uploads` to confirm the upload folders are separated by content type.
7. After running the app, inspect `storage/database/portfolio.db` to see the generated SQLite file.

## Manual Verification
1. Visit `/` and confirm the old static homepage style now comes from Spring Boot.
2. Visit `/gallery` and compare it against the supplied mockup.
3. Visit `/cmsmgmnt/dashboard` while signed out and verify you are redirected to `/cmsmgmnt/sign-in`.
4. Sign in to `/cmsmgmnt/sign-in` with the seeded credentials.
5. Create or edit one project, one gallery entry, and one blog post.
6. Upload images from the CMS and confirm project uploads land in `storage/uploads/projects/PJKT_<id>_img`.
7. Refresh the public pages and confirm the updated content is visible.
