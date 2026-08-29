package com.portfolio.studio.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final PortfolioProperties portfolioProperties;

    public WebConfig(PortfolioProperties portfolioProperties) {
        this.portfolioProperties = portfolioProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadRoot = Paths.get(portfolioProperties.getStorage().getUploadRoot()).toAbsolutePath().normalize();
        String location = uploadRoot.toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
