package com.portfolio.studio.config;

import com.portfolio.studio.service.PortfolioService;
import com.portfolio.studio.service.StudioUserDetailsService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.web.multipart.support.MultipartFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
        StudioUserDetailsService studioUserDetailsService,
        PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(studioUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        DaoAuthenticationProvider authenticationProvider,
        PortfolioService portfolioService
    ) throws Exception {
        http.authenticationProvider(authenticationProvider);
        http.authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/", "/gallery", "/gallery/**", "/work/**", "/blog", "/blog/**", "/assets/**", "/uploads/**", "/cmsmgmnt/sign-in").permitAll()
            .requestMatchers("/cmsmgmnt/**").hasRole("OWNER")
            .anyRequest().permitAll()
        );

        http.requestCache(cache -> cache.requestCache(new HttpSessionRequestCache()));
        http.formLogin(form -> form
            .loginPage("/cmsmgmnt/sign-in")
            .loginProcessingUrl("/cmsmgmnt/sign-in")
            .successHandler(successHandler(portfolioService))
            .failureHandler(failureHandler(portfolioService))
        );
        http.logout(logout -> logout
            .logoutUrl("/cmsmgmnt/logout")
            .logoutSuccessUrl("/cmsmgmnt/sign-in?logout")
        );
        http.headers(headers -> headers
            .contentSecurityPolicy(csp -> csp.policyDirectives(
                "default-src 'self'; " +
                    "script-src 'self' https://cdnjs.cloudflare.com; " +
                    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                    "font-src 'self' https://fonts.gstatic.com data:; " +
                    "img-src 'self' data: https://api.github.com; " +
                    "connect-src 'self' https://api.github.com; " +
                    "base-uri 'self'; form-action 'self'; frame-ancestors 'none'; object-src 'none'"
            ))
            .frameOptions(frame -> frame.deny())
            .referrerPolicy(policy -> policy.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
        );
        CsrfTokenRequestAttributeHandler csrfRequestHandler = new CsrfTokenRequestAttributeHandler();
        csrfRequestHandler.setCsrfRequestAttributeName("_csrf");
        http.csrf(csrf -> csrf.csrfTokenRequestHandler(csrfRequestHandler));
        return http.build();
    }

    @Bean
    public FilterRegistrationBean<MultipartFilter> multipartFilter() {
        FilterRegistrationBean<MultipartFilter> registration = new FilterRegistrationBean<>(new MultipartFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    private AuthenticationSuccessHandler successHandler(PortfolioService portfolioService) {
        return (request, response, authentication) -> {
            request.getSession().removeAttribute("studioAuthError");
            portfolioService.recordSuccessfulLogin(authentication.getName(), request.getRemoteAddr());
            response.sendRedirect(request.getContextPath() + "/cmsmgmnt/dashboard");
        };
    }

    private AuthenticationFailureHandler failureHandler(PortfolioService portfolioService) {
        return (request, response, exception) -> {
            String username = request.getParameter("username");
            portfolioService.recordFailedLogin(username, request.getRemoteAddr());
            request.getSession().setAttribute("studioAuthError", friendlyErrorMessage(exception));
            response.sendRedirect(request.getContextPath() + "/cmsmgmnt/sign-in?error");
        };
    }

    private String friendlyErrorMessage(AuthenticationException exception) {
        if (exception instanceof LockedException) {
            return "Too many failed attempts. Access is temporarily locked.";
        }
        if (exception instanceof BadCredentialsException) {
            return "Invalid credentials.";
        }
        return "Unable to sign in right now.";
    }
}
