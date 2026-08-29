package com.portfolio.studio;

import com.portfolio.studio.config.PortfolioProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PortfolioProperties.class)
public class PortfolioStudioApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortfolioStudioApplication.class, args);
	}

}
