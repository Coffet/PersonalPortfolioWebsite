package com.portfolio.studio.service;

import java.time.LocalDateTime;

import com.portfolio.studio.model.CmsUser;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class StudioUserDetailsService implements UserDetailsService {

    private final PortfolioService portfolioService;

    public StudioUserDetailsService(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CmsUser cmsUser = portfolioService.findCmsUserByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        if (cmsUser.getLockedUntil() != null && cmsUser.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new LockedException("Account is temporarily locked.");
        }

        return User.withUsername(cmsUser.getUsername())
            .password(cmsUser.getPasswordHash())
            .roles("OWNER")
            .disabled(!cmsUser.isActive())
            .accountLocked(cmsUser.getLockedUntil() != null && cmsUser.getLockedUntil().isAfter(LocalDateTime.now()))
            .build();
    }
}
