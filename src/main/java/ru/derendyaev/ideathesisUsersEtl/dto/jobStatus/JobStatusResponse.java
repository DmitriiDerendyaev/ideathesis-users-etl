package ru.derendyaev.ideathesisUsersEtl.dto.jobStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record JobStatusResponse(
        String status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Duration duration,
        String exitCode,
        String exitMessage,
        Map<String, String> parameters,
        List<StepStatus> stepStatuses
) {}