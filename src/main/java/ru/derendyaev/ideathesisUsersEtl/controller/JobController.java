package ru.derendyaev.ideathesisUsersEtl.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    private final JobLauncher jobLauncher;
    private final Job importJob; // Это ваша джоба из BatchConfig
    private final JobExplorer jobExplorer;

    /**
     * Запуск джобы вручную через POST /api/jobs/start
     */
    @PostMapping("/start")
    public ResponseEntity<String> startJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addDate("run.date", new Date())
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();

            logger.info("Starting ETL job manually via REST at {}", new Date());

            JobExecution execution = jobLauncher.run(importJob, jobParameters);
            logger.info("Job started with ID: {}", execution.getId());

            return ResponseEntity.ok("Job started successfully. Execution ID: " + execution.getId());
        } catch (JobExecutionAlreadyRunningException e) {
            logger.warn("Job is already running.");
            return ResponseEntity.status(503).body("Job is already running.");
        } catch (JobRestartException | JobInstanceAlreadyCompleteException e) {
            logger.error("Error restarting job", e);
            return ResponseEntity.status(500).body("Error restarting job: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to start job", e);
            return ResponseEntity.status(500).body("Failed to start job: " + e.getMessage());
        }
    }

    /**
     * Получение статуса последнего запуска джобы через GET /api/jobs/status
     */
    @GetMapping("/status")
    public ResponseEntity<JobStatusResponse> getJobStatus() {
        List<JobInstance> jobInstances = jobExplorer.getJobInstances("importJob", 0, 1);
        if (jobInstances.isEmpty()) {
            return ResponseEntity.ok(new JobStatusResponse("No job instances found.", null, null));
        }

        JobInstance lastJobInstance = jobInstances.get(0);
        List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(lastJobInstance);
        if (jobExecutions.isEmpty()) {
            return ResponseEntity.ok(new JobStatusResponse("No executions for the job instance.", null, null));
        }

        JobExecution lastExecution = jobExecutions.get(0);

        String status = lastExecution.getStatus().toString();
        LocalDateTime startTime = lastExecution.getStartTime();
        LocalDateTime endTime = lastExecution.getEndTime();

        logger.info("Last job status: {}, started at: {}, ended at: {}", status, startTime, endTime);

        return ResponseEntity.ok(new JobStatusResponse(status, startTime, endTime));
    }

    /**
     * DTO для ответа с информацией о статусе джобы
     */
    public record JobStatusResponse(String status, LocalDateTime startTime, LocalDateTime endTime) {}
}