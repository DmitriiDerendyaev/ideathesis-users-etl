package ru.derendyaev.ideathesisUsersEtl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;

@SpringBootApplication
@ConfigurationPropertiesScan
public class IdeathesisUsersEtlApplication {

	private static final Logger logger = LoggerFactory.getLogger(IdeathesisUsersEtlApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Ideathesis Users ETL Application...");
		SpringApplication.run(IdeathesisUsersEtlApplication.class, args);
	}

	@Component
	public static class JobRunner implements ApplicationRunner {

		private static final Logger logger = LoggerFactory.getLogger(JobRunner.class);

		private final JobLauncher jobLauncher;
		private final Job etlJob;

		@Autowired
		public JobRunner(JobLauncher jobLauncher, Job etlJob) {
			this.jobLauncher = jobLauncher;
			this.etlJob = etlJob;
		}

		@Override
		public void run(ApplicationArguments args) throws Exception {
			logger.info("Preparing to launch ETL Job...");
			JobParameters jobParameters = new JobParametersBuilder()
					.addLong("startAt", System.currentTimeMillis())
					.toJobParameters();

			try {
				JobExecution execution = jobLauncher.run(etlJob, jobParameters);
				logger.info("ETL Job launched. Status: {}", execution.getStatus());
			} catch (Exception e) {
				logger.error("ETL Job failed to start", e);
				throw e;
			}
		}
	}
}