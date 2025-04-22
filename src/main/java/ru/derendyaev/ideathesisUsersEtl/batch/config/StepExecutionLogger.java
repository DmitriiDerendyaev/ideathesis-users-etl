package ru.derendyaev.ideathesisUsersEtl.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class StepExecutionLogger implements StepExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(StepExecutionLogger.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {
        logger.info("Starting step: {}", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        logger.info("Step {} completed with status: {}, readCount: {}, writeCount: {}",
                stepExecution.getStepName(),
                stepExecution.getExitStatus(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount());
        return stepExecution.getExitStatus();
    }
}