package ru.derendyaev.ideathesisUsersEtl.batch;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import ru.derendyaev.ideathesisUsersEtl.batch.config.StepExecutionLogger;
import ru.derendyaev.ideathesisUsersEtl.client.GraphQLClient;
import ru.derendyaev.ideathesisUsersEtl.dto.StudentDTO;
import ru.derendyaev.ideathesisUsersEtl.dto.EmployeeDTO;
import ru.derendyaev.ideathesisUsersEtl.dto.StudentsResponse;
import ru.derendyaev.ideathesisUsersEtl.model.*;
import ru.derendyaev.ideathesisUsersEtl.mapper.StudentMapper;
import ru.derendyaev.ideathesisUsersEtl.mapper.EmployeeMapper;
import ru.derendyaev.ideathesisUsersEtl.repository.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    private final StepExecutionLogger stepExecutionLogger;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GraphQLClient graphQLClient;

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final DepartmentRepository departmentRepository;
    private final DegreeLevelRepository degreeLevelRepository;
    private final DegreeFormRepository degreeFormRepository;

    // Кэши справочников
    private final Map<String, StudentGroup> groupCache = new ConcurrentHashMap<>();
    private final Map<String, Department> departmentCache = new ConcurrentHashMap<>();
    private final Map<String, DegreeLevel> degreeLevelCache = new ConcurrentHashMap<>();
    private final Map<String, DegreeForm> degreeFormCache = new ConcurrentHashMap<>();

    @Bean
    public Job importJob() {
        return new JobBuilder("importJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(studentStep())
                .build();
    }

    @Bean
    public Step studentStep() {
        return new StepBuilder("studentStep", jobRepository)
                .<StudentDTO, Student>chunk(500, transactionManager)
                .reader(studentReader())
                .processor(studentProcessor())
                .writer(studentWriter())
                .faultTolerant()
                .skip(DuplicateKeyException.class)
                .skipLimit(100)
                .retryLimit(3)
                .retry(OptimisticLockingFailureException.class)
                .listener(stepExecutionLogger)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<StudentDTO> studentReader() {
        return new ItemReader<>() {
            private Iterator<StudentDTO> iterator;

            @Override
            @Transactional(readOnly = true)
            public StudentDTO read() {
                if (iterator == null) {
                    StudentsResponse response = graphQLClient.getStudents();
                    iterator = response.getItems().iterator();
                }
                return iterator.hasNext() ? iterator.next() : null;
            }
        };
    }

    @Bean
    @StepScope
    public ItemProcessor<StudentDTO, Student> studentProcessor() {
        return dto -> {
            UUID guid = UUID.fromString(dto.getGuid());

            return studentRepository.findById(guid)
                    .map(existing -> updateExistingStudent(existing, dto))
                    .orElseGet(() -> createNewStudent(dto));
        };
    }

    private Student updateExistingStudent(Student existing, StudentDTO dto) {
        existing.setCourse(dto.getCourse());
        existing.setStartYear(dto.getStartYear());
        existing.setGroup(getCachedGroup(dto.getGroup()));
        existing.setDepartment(getCachedDepartment(dto.getDepartment()));
        existing.setDegreeLevel(getCachedDegreeLevel(dto.getDegreeLevel()));
        existing.setDegreeForm(getCachedDegreeForm(dto.getDegreeForm()));

        updateUser(existing.getUser(), dto);
        return existing;
    }

    private Student createNewStudent(StudentDTO dto) {
        UUID guid = UUID.fromString(dto.getGuid());
        User user = userRepository.findById(guid)
                .orElseGet(() -> createUser(dto));
        updateUser(user, dto);

        return Student.builder()
                .guid(guid)
                .user(user)
                .group(getCachedGroup(dto.getGroup()))
                .department(getCachedDepartment(dto.getDepartment()))
                .degreeLevel(getCachedDegreeLevel(dto.getDegreeLevel()))
                .degreeForm(getCachedDegreeForm(dto.getDegreeForm()))
                .course(dto.getCourse())
                .startYear(dto.getStartYear())
                .build();
    }

    @Bean
    @StepScope
    public ItemWriter<Student> studentWriter() {
        return items -> {
            List<Student> students = new ArrayList<>(items.getItems());

            userRepository.saveAll(students.stream()
                    .map(Student::getUser)
                    .collect(Collectors.toList()));

            studentRepository.saveAll(students);
        };
    }

    // Методы кэширования справочников
    private StudentGroup getCachedGroup(String name) {
        if (name == null) return null;
        return groupCache.computeIfAbsent(name,
                key -> studentGroupRepository.findByName(name)
                        .orElseGet(() -> studentGroupRepository.save(new StudentGroup(name))));
    }

    private Department getCachedDepartment(String name) {
        if (name == null) return null;
        return departmentCache.computeIfAbsent(name,
                key -> departmentRepository.findByName(name)
                        .orElseGet(() -> departmentRepository.save(new Department(name))));
    }

    private DegreeLevel getCachedDegreeLevel(String name) {
        if (name == null) return null;
        return degreeLevelCache.computeIfAbsent(name,
                key -> degreeLevelRepository.findByName(name)
                        .orElseGet(() -> degreeLevelRepository.save(new DegreeLevel(name))));
    }

    private DegreeForm getCachedDegreeForm(String name) {
        if (name == null) return null;
        return degreeFormCache.computeIfAbsent(name,
                key -> degreeFormRepository.findByName(name)
                        .orElseGet(() -> degreeFormRepository.save(new DegreeForm(name))));
    }

    private User createUser(StudentDTO dto) {
        return User.builder()
                .guid(UUID.fromString(dto.getGuid()))
                .fullName(dto.getFullName())
                .firstName(dto.getFirstName())
                .lastName(dto.getSurname())
                .middleName(dto.getMiddleName())
                .userType("student")
                .build();
    }

    private void updateUser(User user, StudentDTO dto) {
        user.setFullName(dto.getFullName());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getSurname());
        user.setMiddleName(dto.getMiddleName());
    }
}