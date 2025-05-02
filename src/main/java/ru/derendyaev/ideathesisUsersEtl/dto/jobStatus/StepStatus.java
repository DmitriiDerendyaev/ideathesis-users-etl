package ru.derendyaev.ideathesisUsersEtl.dto.jobStatus;

import java.time.Duration;

public record StepStatus(
        String name,
        String status,
        Integer readCount,
        Integer writeCount,
        Integer skipCount,
        Integer errorCount,
        Duration duration
) {}