<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/jsp/layout/studio-head.jspf" %>
</head>
<body>
    <main class="login-shell">
        <section class="login-card">
            <p class="studio-brand__eyebrow">Private desk</p>
            <h1>Sign in</h1>
            <p>Use your owner account to publish work, gallery entries, and notes.</p>

            <c:if test="${param.logout ne null}">
                <div class="flash">Signed out.</div>
            </c:if>

            <c:if test="${param.error ne null or not empty authError}">
                <div class="form-error">${empty authError ? 'Unable to sign in.' : authError}</div>
            </c:if>

            <form action="${ctx}/cmsmgmnt/sign-in" method="post">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <div class="field">
                    <label for="username">Username</label>
                    <input id="username" name="username" type="text" required autocomplete="username">
                </div>
                <div class="field">
                    <label for="password">Password</label>
                    <input id="password" name="password" type="password" required autocomplete="current-password">
                </div>
                <button class="button" type="submit">Sign in</button>
            </form>

            <a class="login-card__back" href="${ctx}/">
                <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true"><path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/></svg>
                Return to home
            </a>
        </section>
    </main>
</body>
</html>
