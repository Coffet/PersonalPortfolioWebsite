<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/jsp/layout/studio-head.jspf" %>
</head>
<body>
    <main class="login-shell">
        <aside class="login-visual">
            <div class="login-visual__brand">
                <span class="logo-mark" role="img" aria-label="Coft"></span>
            </div>
            <div class="login-visual__copy">
                <h2>Your content, one calm desk.</h2>
                <p>Projects, gallery and blog — published beautifully from a single place.</p>
            </div>
            <span class="login-visual__orb" aria-hidden="true"></span>
        </aside>

        <section class="login-panel">
            <a class="login-back" href="${ctx}/">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/></svg>
                Back to site
            </a>

            <div class="login-card">
                <h1>Welcome Back!</h1>
                <p class="login-card__sub">Login for access of the content management system.</p>

                <c:if test="${param.logout ne null}">
                    <div class="notice notice--ok">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12.5 10 17l9-10" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/></svg>
                        <span>Signed out.</span>
                    </div>
                </c:if>

                <c:if test="${param.error ne null or not empty authError}">
                    <div class="notice notice--error">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 8v5M12 16.5h.01M12 4.5l8.5 14.5H3.5L12 4.5z" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/></svg>
                        <span>${empty authError ? 'Unable to sign in.' : authError}</span>
                    </div>
                </c:if>

                <form class="login-form" action="${ctx}/cmsmgmnt/sign-in" method="post">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <div class="field">
                        <label for="username">Username</label>
                        <input id="username" name="username" type="text" placeholder="your.username" required autocomplete="username" autofocus>
                    </div>
                    <div class="field">
                        <label for="password">Password</label>
                        <div class="input-wrap">
                            <input id="password" name="password" type="password" placeholder="••••••••" required autocomplete="current-password">
                            <button class="pw-toggle" type="button" data-pw-toggle="password" aria-pressed="false" aria-label="Show password">
                                <svg class="pw-eye" viewBox="0 0 24 24" aria-hidden="true"><path d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12Z" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/><circle cx="12" cy="12" r="2.6" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>
                                <svg class="pw-eye-off" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 4l16 16M10.6 7.1A8 8 0 0 1 12 7c6 0 9.5 5 9.5 5a15 15 0 0 1-2.3 3M6.5 6.5C3.5 8.5 2.5 12 2.5 12s3.5 5 9.5 5c1.3 0 2.5-.2 3.5-.5M9.9 9.9a3 3 0 0 0 4.2 4.2" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/></svg>
                            </button>
                        </div>
                    </div>
                    <div class="login-row">
                        <label class="login-check"><input type="checkbox" name="remember-me"> <span>Remember me</span></label>
                        <button type="button" class="login-link" id="forgot-pw-trigger">Forgot Password?</button>
                    </div>

                    <button class="btn-login" type="submit">Login</button>
                </form>
            </div>
        </section>
    </main>

    <dialog class="login-dialog" id="forgot-pw-dialog">
        <span class="login-dialog__accent" aria-hidden="true"></span>
        <div class="login-dialog__body">
            <div class="login-dialog__head">
                <div class="login-dialog__title">
                    <span class="login-dialog__icon" aria-hidden="true">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="8" cy="15" r="4"/><path d="M10.85 12.15 19 4M15 8l2 2M12.5 10.5l1.5 1.5"/></svg>
                    </span>
                    <h2>Where to find your password</h2>
                </div>
                <button type="button" class="login-dialog__close" data-dialog-close aria-label="Close">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                </button>
            </div>
            <p>Your sign-in password isn't stored in the dashboard — the server sets it once, when the database is first created.</p>
            <ul>
                <li><strong>Local development</strong> — edit <code>application-local.properties</code> and set <code>portfolio.seed.owner.password</code>.</li>
                <li><strong>Live server</strong> — set <code>PORTFOLIO_SEED_OWNER_PASSWORD</code> in the <code>Environment=</code> line of the <code>portfolio.service</code> systemd unit.</li>
            </ul>
            <p class="login-dialog__note">This seed value is only used on first boot. After that, ask the server admin to update the configuration.</p>
            <button type="button" class="btn-login" data-dialog-close>Got it</button>
        </div>
    </dialog>
    <script src="${ctx}/assets/js/login.js?v=2" defer></script>
</body>
</html>
