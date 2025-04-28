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
import ru.derendyaev.ideathesisUsersEtl.dto.*;
import ru.derendyaev.ideathesisUsersEtl.model.*;
import ru.derendyaev.ideathesisUsersEtl.repository.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    // Добавь новые репозитории для преподавателей
    private final EmployeeRepository employeeRepository;
    private final JobTitleRepository jobTitleRepository;
    private final StaffCategoryRepository staffCategoryRepository;
    private final EmploymentTypeRepository employmentTypeRepository;
    private final SubdivisionRepository subdivisionRepository;

    // Кэши справочников
    private final Map<String, StudentGroup> groupCache = new ConcurrentHashMap<>();
    private final Map<String, Department> departmentCache = new ConcurrentHashMap<>();
    private final Map<String, DegreeLevel> degreeLevelCache = new ConcurrentHashMap<>();
    private final Map<String, DegreeForm> degreeFormCache = new ConcurrentHashMap<>();

    // Справочники для преподавателей
    private final Map<String, JobTitle> jobTitleCache = new ConcurrentHashMap<>();
    private final Map<String, StaffCategory> staffCategoryCache = new ConcurrentHashMap<>();
    private final Map<String, EmploymentType> employmentTypeCache = new ConcurrentHashMap<>();
    private final Map<String, Subdivision> subdivisionCache = new ConcurrentHashMap<>();

    @Bean
    public Job importJob() {
        return new JobBuilder("importJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(studentStep())
                .next(employeeStep())
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

    // Новый шаг для преподавателей
    @Bean
    public Step employeeStep() {
        return new StepBuilder("employeeStep", jobRepository)
                .<EmployeeDTO, Employee>chunk(500, transactionManager)
                .reader(employeeReader())
                .processor(employeeProcessor())
                .writer(employeeWriter())
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
    public ItemReader<EmployeeDTO> employeeReader() {
        return new ItemReader<>() {
            private Iterator<EmployeeDTO> iterator;

            @Override
            @Transactional(readOnly = true)
            public EmployeeDTO read() {
                if (iterator == null) {
                    EmployeesResponse response = graphQLClient.getEmployees();
                    iterator = response.getItems().iterator();
                }
                return iterator.hasNext() ? iterator.next() : null;
            }
        };
    }

    @Bean
    @StepScope
    public ItemProcessor<EmployeeDTO, Employee> employeeProcessor() {
        return dto -> {
            UUID guid = UUID.fromString(dto.getGuid());

            return employeeRepository.findById(guid)
                    .map(existing -> updateExistingEmployee(existing, dto))
                    .orElseGet(() -> createNewEmployee(dto));
        };
    }

    private Employee updateExistingEmployee(Employee existing, EmployeeDTO dto) {
        existing.setFullName(dto.getFullName());
        existing.setSurname(dto.getSurname());
        existing.setEmail(dto.getMail());
        existing.setDateOfBirth(parseDate(dto.getDateOfBirth()));

        updateEmployeeEmployments(existing, dto.getEmployeeEmployments());
        updateUser(existing.getUser(), dto);

        return existing;
    }

    private Employee createNewEmployee(EmployeeDTO dto) {
        UUID guid = UUID.fromString(dto.getGuid());
        User user = userRepository.findById(guid)
                .orElseGet(() -> createEmployeeUser(dto));
        updateUser(user, dto);

        Employee employee = new Employee();
        employee.setGuid(guid);
        employee.setUser(user);
        employee.setFullName(dto.getFullName());
        employee.setSurname(dto.getSurname());
        employee.setEmail(dto.getMail());
        employee.setDateOfBirth(parseDate(dto.getDateOfBirth()));

        Set<EmployeeEmployment> employments = processEmployments(dto.getEmployeeEmployments(), employee);
        employee.setEmployeeEmployments(employments);

        return employee;
    }

    @Bean
    @StepScope
    public ItemWriter<Employee> employeeWriter() {
        return items -> {
            List<Employee> employees = new ArrayList<>(items.getItems());

            userRepository.saveAll(employees.stream()
                    .map(Employee::getUser)
                    .collect(Collectors.toList()));

            employeeRepository.saveAll(employees);
        };
    }

    // Методы для работы с User (новые версии для сотрудников)
    private User createEmployeeUser(EmployeeDTO dto) {
        return User.builder()
                .guid(UUID.fromString(dto.getGuid()))
                .fullName(dto.getFullName())
                .lastName(dto.getSurname())
                .userType("employee")
                .build();
    }

    private void updateUser(User user, EmployeeDTO dto) {
        user.setFullName(dto.getFullName());
        user.setLastName(dto.getSurname());
    }

    // Методы для обработки трудоустройства
    private void updateEmployeeEmployments(Employee employee, List<EmployeeEmploymentDTO> dtos) {
        Set<EmployeeEmployment> existing = employee.getEmployeeEmployments();
        existing.clear();
        existing.addAll(processEmployments(dtos, employee));
    }

    private Set<EmployeeEmployment> processEmployments(List<EmployeeEmploymentDTO> dtos, Employee employee) {
        return dtos.stream().map(empDto -> {
            EmployeeEmployment employment = new EmployeeEmployment();
            employment.setEmployee(employee);
            employment.setJobTitle(getCachedJobTitle(empDto.getJobTitle()));
            employment.setStaffCategory(getCachedStaffCategory(empDto.getStaffCategory()));
            employment.setEmploymentType(getCachedEmploymentType(empDto.getEmploymentType()));
            employment.setSubdivision(getCachedSubdivision(empDto.getSubDivision(), empDto.getSubDivisionGuid()));
            employment.setJobState(empDto.getJobState());
            return employment;
        }).collect(Collectors.toSet());
    }

    // Методы кэширования для новых справочников
    private JobTitle getCachedJobTitle(String name) {
        if (name == null) return null;
        return jobTitleCache.computeIfAbsent(name,
                key -> jobTitleRepository.findByName(name)
                        .orElseGet(() -> jobTitleRepository.save(new JobTitle(name))));
    }

    private StaffCategory getCachedStaffCategory(String name) {
        if (name == null) return null;
        return staffCategoryCache.computeIfAbsent(name,
                key -> staffCategoryRepository.findByName(name)
                        .orElseGet(() -> staffCategoryRepository.save(new StaffCategory(name))));
    }

    private EmploymentType getCachedEmploymentType(String name) {
        if (name == null) return null;
        return employmentTypeCache.computeIfAbsent(name,
                key -> employmentTypeRepository.findByName(name)
                        .orElseGet(() -> employmentTypeRepository.save(new EmploymentType(name))));
    }

    private Subdivision getCachedSubdivision(String name, String guid) {
        if (name == null || guid == null) return null;
        return subdivisionCache.computeIfAbsent(guid,
                key -> subdivisionRepository.findByGuid(UUID.fromString(guid))
                        .orElseGet(() -> subdivisionRepository.save(new Subdivision(name, UUID.fromString(guid)))));
    }

    private Date parseDate(String dateStr) {
        try {
            return dateStr != null ? new SimpleDateFormat("dd.MM.yyyy").parse(dateStr) : null;
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format: " + dateStr, e);
        }
    }
}