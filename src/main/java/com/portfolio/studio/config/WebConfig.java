package com.portfolio.studio.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
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
        // Filenames are UUIDs, so a long immutable cache is safe and avoids re-downloading
        // the same picture when the gallery viewer opens.
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations(location)
            .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable());

        // The CV lives outside the WAR (storage/cv) so it can be replaced without a rebuild.
        // Served at the root so the URL is just domain/Chow_Cheuk_Wai_CV_AcaCert.pdf.
        Path cvRoot = Paths.get(portfolioProperties.getStorage().getCvRoot()).toAbsolutePath().normalize();
        registry.addResourceHandler("/Chow_Cheuk_Wai_CV_AcaCert.pdf")
            .addResourceLocations(cvRoot.toUri().toString())
            .setCacheControl(CacheControl.noCache());
    }
}
