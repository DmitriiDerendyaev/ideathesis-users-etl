package ru.derendyaev.ideathesisUsersEtl.batch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
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
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
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

    @Autowired
    private CustomStudentWriter customStudentWriter; // Добавляем автовайр кастомного writer'а

    @Bean
    public Job etlJob() {
        return new JobBuilder("etlJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(studentStep())
                .next(employeeStep())
                .build();
    }

    @Bean
    public Step studentStep() {
        return new StepBuilder("studentStep", jobRepository)
                .<StudentDTO, Student>chunk(1, transactionManager)
                .reader(studentReader())
                .processor(studentProcessor())
                .writer(customStudentWriter) // Используем кастомный writer
                .listener(new StepExecutionLogger())
                .faultTolerant()
                .skip(OptimisticLockingFailureException.class)
                .skipLimit(10)
                .build();
    }

    @Bean
    public Step employeeStep() {
        return new StepBuilder("employeeStep", jobRepository)
                .<EmployeeDTO, Employee>chunk(1, transactionManager)
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
            logger.info("Processing student: {}", studentDTO);
            return studentMapper.map(studentDTO);
        };
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
            logger.info("Processing employee: {}", employeeDTO);
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