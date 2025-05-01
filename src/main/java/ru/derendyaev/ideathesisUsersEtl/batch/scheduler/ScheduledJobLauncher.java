package ru.derendyaev.ideathesisUsersEtl.batch.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling // Включает поддержку scheduled tasks
public class ScheduledJobLauncher {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledJobLauncher.class);

    private final JobLauncher jobLauncher;
    private final Job etlJob;

    @Autowired
    public ScheduledJobLauncher(JobLauncher jobLauncher, Job etlJob) {
        this.jobLauncher = jobLauncher;
        this.etlJob = etlJob;
    }

    // Запуск ежедневно в 2:35 AM
    @Scheduled(cron = "0 35 14 * * ?")
    public void runETLJob() {
        logger.info("Scheduled ETL Job started...");
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();
            JobExecution execution = jobLauncher.run(etlJob, jobParameters);
            logger.info("Scheduled ETL Job completed. Status: {}", execution.getStatus());
        } catch (Exception e) {
            logger.error("Scheduled ETL Job failed", e);
        }
    }
}