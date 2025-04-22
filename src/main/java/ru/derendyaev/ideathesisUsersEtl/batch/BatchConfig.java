package ru.derendyaev.ideathesisUsersEtl.batch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import ru.derendyaev.ideathesisUsersEtl.batch.config.JobCompletionNotificationListener;
import ru.derendyaev.ideathesisUsersEtl.batch.config.StepExecutionLogger;
import ru.derendyaev.ideathesisUsersEtl.client.GraphQLClient;
import ru.derendyaev.ideathesisUsersEtl.dto.StudentDTO;
import ru.derendyaev.ideathesisUsersEtl.dto.EmployeeDTO;
import ru.derendyaev.ideathesisUsersEtl.model.Student;
import ru.derendyaev.ideathesisUsersEtl.model.Employee;
import ru.derendyaev.ideathesisUsersEtl.mapper.StudentMapper;
import ru.derendyaev.ideathesisUsersEtl.mapper.EmployeeMapper;
import ru.derendyaev.ideathesisUsersEtl.repository.StudentRepository;
import ru.derendyaev.ideathesisUsersEtl.repository.EmployeeRepository;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
    private static final Logger logger = LoggerFactory.getLogger(BatchConfig.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private GraphQLClient graphQLClient;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private EmployeeMapper employeeMapper;
    @Bean
    public Job etlJob() {
        return new JobBuilder("etlJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(studentStep())
                .next(employeeStep())
//                .listener(jobExecution -> {
//                    logger.info("Job {} completed with status {}",
//                            jobExecution.getJobInstance().getJobName(),
//                            jobExecution.getStatus());
//                })
                .build();
    }

    @Bean
    public Step studentStep() {
        return new StepBuilder("studentStep", jobRepository)
                .<StudentDTO, Student>chunk(10, transactionManager)
                .reader(studentReader())
                .processor(studentProcessor())
                .writer(studentWriter())
                .listener(new StepExecutionLogger())
                .build();
    }

    @Bean
    public Step employeeStep() {
        return new StepBuilder("employeeStep", jobRepository)
                .<EmployeeDTO, Employee>chunk(10, transactionManager)
                .reader(employeeReader())
                .processor(employeeProcessor())
                .writer(employeeWriter())
                .listener(new StepExecutionLogger())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<StudentDTO> studentReader() {
        return new StudentReader(graphQLClient);
    }

    @Bean
    @StepScope
    public ItemProcessor<StudentDTO, Student> studentProcessor() {
        return studentDTO -> {
            logger.debug("Processing student: {}", studentDTO);
            return studentMapper.map(studentDTO);
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Student> studentWriter() {
        RepositoryItemWriter<Student> writer = new RepositoryItemWriter<>();
        writer.setRepository(studentRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    @StepScope
    public ItemReader<EmployeeDTO> employeeReader() {
        return new EmployeeReader(graphQLClient);
    }

    @Bean
    @StepScope
    public ItemProcessor<EmployeeDTO, Employee> employeeProcessor() {
        return employeeDTO -> {
            logger.debug("Processing employee: {}", employeeDTO);
            return employeeMapper.map(employeeDTO);
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Employee> employeeWriter() {
        RepositoryItemWriter<Employee> writer = new RepositoryItemWriter<>();
        writer.setRepository(employeeRepository);
        writer.setMethodName("save");
        return writer;
    }
}