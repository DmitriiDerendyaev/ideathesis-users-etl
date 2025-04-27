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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
// BatchConfig.java
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GraphQLClient graphQLClient;

    // Репозитории
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final DepartmentRepository departmentRepository;
    private final DegreeLevelRepository degreeLevelRepository;
    private final DegreeFormRepository degreeFormRepository;

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
                .<StudentDTO, Student>chunk(50, transactionManager)
                .reader(studentReader())
                .processor(studentProcessor())
                .writer(studentWriter())
                .faultTolerant()
                .skipPolicy(new AlwaysSkipItemSkipPolicy())
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

            // Обработка пользователя
            User user = userRepository.findById(guid)
                    .orElseGet(() -> User.builder()
                            .guid(guid)
                            .userType("student")
                            .build());

            updateUser(user, dto);

            // Обработка связанных сущностей
            return Student.builder()
                    .guid(guid)
                    .user(user)
                    .group(getOrCreateGroup(dto.getGroup()))
                    .department(getOrCreateDepartment(dto.getDepartment()))
                    .degreeLevel(getOrCreateDegreeLevel(dto.getDegreeLevel()))
                    .degreeForm(getOrCreateDegreeForm(dto.getDegreeForm()))
                    .course(dto.getCourse())
                    .startYear(dto.getStartYear())
                    .build();
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Student> studentWriter() {
        return items -> {
            // Безопасное преобразование типов
            List<Student> students = new ArrayList<>(items.getItems());

            List<User> users = students.stream()
                    .map(Student::getUser)
                    .collect(Collectors.toList());

            userRepository.saveAll(users);
            studentRepository.saveAll(students);
        };
    }

    private void updateUser(User user, StudentDTO dto) {
        user.setFullName(dto.getFullName());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getSurname());
        user.setMiddleName(dto.getMiddleName());
    }

    private StudentGroup getOrCreateGroup(String name) {
        if (name == null) return null;
        return studentGroupRepository.findByName(name)
                .orElseGet(() -> studentGroupRepository.save(
                        new StudentGroup(name)));
    }

    private Department getOrCreateDepartment(String name) {
        if (name == null) return null;
        return departmentRepository.findByName(name)
                .orElseGet(() -> departmentRepository.save(
                        new Department(name)));
    }

    private DegreeLevel getOrCreateDegreeLevel(String name) {
        if (name == null) return null;
        return degreeLevelRepository.findByName(name)
                .orElseGet(() -> degreeLevelRepository.save(
                        new DegreeLevel(name)));
    }

    private DegreeForm getOrCreateDegreeForm(String name) {
        if (name == null) return null;
        return degreeFormRepository.findByName(name)
                .orElseGet(() -> degreeFormRepository.save(
                        new DegreeForm(name)));
    }
}