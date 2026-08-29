package com.portfolio.studio.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SeedDataInitializer implements ApplicationRunner {

    private final PortfolioService portfolioService;

    public SeedDataInitializer(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @Override
    public void run(ApplicationArguments args) {
        portfolioService.ensureSeedData();
    }
}
