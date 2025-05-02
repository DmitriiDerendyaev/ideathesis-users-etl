package ru.derendyaev.ideathesisUsersEtl.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.derendyaev.ideathesisUsersEtl.dto.jobStatus.JobStatusResponse;
import ru.derendyaev.ideathesisUsersEtl.dto.jobStatus.StepStatus;

import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    private final JobLauncher jobLauncher;
    private final Job importJob; // Полная джоба
    private final Job importStudentsOnlyJob; // Только студенты
    private final JobExplorer jobExplorer;

    /**
     * Запуск полной джобы (студенты + преподаватели)
     */
    @PostMapping("/start")
    public ResponseEntity<String> startFullJob() {
        return startJob(importJob, "full");
    }

    /**
     * Запуск джобы только для студентов по группе
     */
    @PostMapping("/start/by-group")
    public ResponseEntity<String> startStudentOnlyJobByGroup(@RequestParam("group") String groupName) {
        if (groupName == null || groupName.isBlank()) {
            return ResponseEntity.badRequest().body("Group name is required");
        }
        return startJobWithGroup(importStudentsOnlyJob, groupName);
    }

    private ResponseEntity<String> startJob(Job job, String jobType) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("jobType", jobType)
                    .addDate("run.date", new Date())
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, jobParameters);

            return ResponseEntity.ok("Job started successfully. Execution ID: " + execution.getId());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to start job: " + e.getMessage());
        }
    }

    private ResponseEntity<String> startJobWithGroup(Job job, String groupName) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("studentGroup", groupName)
                    .addString("jobType", "students-only")
                    .addDate("run.date", new Date())
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, jobParameters);

            return ResponseEntity.ok("Job for group '" + groupName + "' started successfully. Execution ID: " + execution.getId());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to start job for group '" + groupName + "': " + e.getMessage());
        }
    }


    @GetMapping("/status")
    public ResponseEntity<JobStatusResponse> getJobStatus() {
        List<JobInstance> jobInstances = jobExplorer.getJobInstances("importJob", 0, 1);
        if (jobInstances.isEmpty()) {
            return ResponseEntity.ok(new JobStatusResponse(
                    "No job instances found.",
                    null, null, null,
                    null, null,
                    Map.of(), List.of()
            ));
        }

        JobInstance lastJobInstance = jobInstances.get(0);

        List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(lastJobInstance);
        if (jobExecutions.isEmpty()) {
            return ResponseEntity.ok(new JobStatusResponse(
                    "No executions for the job instance.",
                    null, null, null,
                    null, null,
                    Map.of(), List.of()
            ));
        }

        JobExecution lastExecution = jobExecutions.get(0);

        String status = lastExecution.getStatus().toString();
        LocalDateTime startTime = Optional.ofNullable(lastExecution.getStartTime())
                .map(date -> LocalDateTime.ofInstant(date.toInstant(ZoneOffset.UTC), ZoneId.systemDefault()))
                .orElse(null);
        LocalDateTime endTime = Optional.ofNullable(lastExecution.getEndTime())
                .map(date -> LocalDateTime.ofInstant(date.toInstant(ZoneOffset.UTC), ZoneId.systemDefault()))
                .orElse(null);
        Duration duration = (startTime != null && endTime != null) ? Duration.between(startTime, endTime) : null;

        String exitCode = lastExecution.getExitStatus().getExitCode();
        String exitMessage = lastExecution.getExitStatus().getExitDescription();

        Map<String, String> parameters = new HashMap<>();
        for (Map.Entry<String, JobParameter<?>> entry : lastExecution.getJobParameters().getParameters().entrySet()) {
            parameters.put(entry.getKey(), entry.getValue().getValue().toString());
        }

        Collection<StepExecution> stepExecutionCollection = lastExecution.getStepExecutions();
        List<StepExecution> stepExecutions = new ArrayList<>(stepExecutionCollection);

// Обработка stepExecutions с более безопасной логикой
        List<StepStatus> stepStatuses = Optional.ofNullable(stepExecutions)
                .orElse(Collections.emptyList())
                .stream()
                .map(step -> {
                    // Вычисление времени начала и окончания
                    Instant stepStart = step.getStartTime() != null
                            ? step.getStartTime().toInstant(ZoneOffset.UTC)
                            : Instant.now();
                    Instant stepEnd = step.getEndTime() != null
                            ? step.getEndTime().toInstant(ZoneOffset.UTC)
                            : Instant.now();

                    // Создание StepStatus
                    return new StepStatus(
                            step.getStepName(),
                            step.getStatus().toString(),
                            (int) step.getReadCount(),
                            (int) step.getWriteCount(),
                            (int) step.getSkipCount(),
                            (int) (step.getProcessSkipCount() + step.getReadSkipCount() + step.getWriteSkipCount()),
                            Duration.between(stepStart, stepEnd)
                    );
                })
                .toList();

        logger.info("Last job status: {}, started at: {}, ended at: {}", status, startTime, endTime);
        logger.info("Steps: {}", stepStatuses);

        return ResponseEntity.ok(new JobStatusResponse(
                status,
                startTime,
                endTime,
                duration,
                exitCode,
                exitMessage,
                parameters,
                stepStatuses
        ));
    }

//    /**
//     * DTO для ответа с информацией о статусе джобы
//     */
//    public record JobStatusResponse(String status, LocalDateTime startTime, LocalDateTime endTime) {}
}