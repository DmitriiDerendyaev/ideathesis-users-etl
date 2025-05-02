package ru.derendyaev.ideathesisUsersEtl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.time.LocalDateTime;

@SpringBootApplication
@ConfigurationPropertiesScan
public class IdeathesisUsersEtlApplication {

	private static final Logger logger = LoggerFactory.getLogger(IdeathesisUsersEtlApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Ideathesis Users ETL Application...");
		logger.info("Current time: {}", LocalDateTime.now());
		SpringApplication.run(IdeathesisUsersEtlApplication.class, args);
	}
}