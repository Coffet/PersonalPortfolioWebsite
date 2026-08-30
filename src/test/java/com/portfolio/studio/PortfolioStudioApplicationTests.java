package com.portfolio.studio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class PortfolioStudioApplicationTests {

	private static final Path TEST_ROOT = createTempDirectory();

	@DynamicPropertySource
	static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url",
			() -> "jdbc:sqlite:" + TEST_ROOT.resolve("portfolio-context.db").toString().replace("\\", "/"));
		registry.add("portfolio.storage.upload-root",
			() -> TEST_ROOT.resolve("uploads").toString().replace("\\", "/"));
		registry.add("portfolio.seed.enabled", () -> "false");
	}

	@Test
	void contextLoads() {
	}

	private static Path createTempDirectory() {
		try {
			return Files.createTempDirectory("portfolio-context-");
		} catch (IOException ex) {
			throw new IllegalStateException("Unable to create test directory", ex);
		}
	}

}
