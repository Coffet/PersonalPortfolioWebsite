# Portfolio review rules

Spring Boot 3.5 WAR, Java 17, JSP + JSTL, SQLite, plain CSS. Public site is JSP. Owner edits at `/cmsmgmnt` only.

## Prefer

- Flag real bugs, auth holes, path traversal, XSS/SQLi, broken uploads, and leaked secrets.
- CMS writes go through authenticated `/cmsmgmnt` controllers. Public pages only read.
- Uploads stay under `storage/uploads/`. Reject path escape, unexpected types, and oversized files.
- SQL uses prepared statements / named parameters. Schema changes go through Flyway.
- Login is a BCrypt hash in SQLite. Seed credentials live only in gitignored `application-local.properties` or systemd `Environment=`. Never invent or restore a default password in git.
- `/admin` and `/studio` must stay 404. Do not add those routes.

## Ignore

- “Add auto-deploy on push to main.” Live go-live is a manual WAR copy on purpose.
- Style-only nits, CSS taste, and “use a preprocessor / React / Thymeleaf instead.”
- Asking for a default CMS password in the repo.

## Stack facts

- `git push` updates GitHub source only. Visitors see the WAR already running.
- Images are files on disk. Content is SQLite. Do not move uploads into the JAR or into git.
