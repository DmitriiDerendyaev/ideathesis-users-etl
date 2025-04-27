package ru.derendyaev.ideathesisUsersEtl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.derendyaev.ideathesisUsersEtl.dto.StudentDTO;
import ru.derendyaev.ideathesisUsersEtl.model.*;
import ru.derendyaev.ideathesisUsersEtl.repository.*;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentPreprocessor {

    private final UserRepository userRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final DepartmentRepository departmentRepository;
    private final DegreeLevelRepository degreeLevelRepository;
    private final DegreeFormRepository degreeFormRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Student prepareStudent(StudentDTO dto) {
        User user = prepareUser(dto);
        return Student.builder()
                .guid(user.getGuid())
                .user(user)
                .group(prepareGroup(dto.getGroup()))
                .department(prepareDepartment(dto.getDepartment()))
                .degreeLevel(prepareDegreeLevel(dto.getDegreeLevel()))
                .degreeForm(prepareDegreeForm(dto.getDegreeForm()))
                .course(dto.getCourse())
                .startYear(dto.getStartYear())
                .build();
    }

    private User prepareUser(StudentDTO dto) {
        return userRepository.findById(UUID.fromString(dto.getGuid()))
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .guid(UUID.fromString(dto.getGuid()))
                                .fullName(dto.getFullName())
                                .firstName(dto.getFirstName())
                                .lastName(dto.getSurname())
                                .middleName(dto.getMiddleName())
                                .userType("student")
                                .build()
                ));
    }

    private StudentGroup prepareGroup(String name) {
        return studentGroupRepository.findByName(name)
                .orElseGet(() -> studentGroupRepository.save(
                        new StudentGroup(name)
                ));
    }

    private Department prepareDepartment(String name) {
        return departmentRepository.findByName(name)
                .orElseGet(() -> departmentRepository.save(
                        new Department(name)
                ));
    }

    private DegreeLevel prepareDegreeLevel(String name) {
        return degreeLevelRepository.findByName(name)
                .orElseGet(() -> degreeLevelRepository.save(
                        new DegreeLevel(name)
                ));
    }

    private DegreeForm prepareDegreeForm(String name) {
        return degreeFormRepository.findByName(name)
                .orElseGet(() -> degreeFormRepository.save(
                        new DegreeForm(name)
                ));
    }
}