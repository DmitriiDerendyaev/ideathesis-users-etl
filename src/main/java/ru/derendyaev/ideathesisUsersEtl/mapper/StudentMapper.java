package ru.derendyaev.ideathesisUsersEtl.mapper;


import ru.derendyaev.ideathesisUsersEtl.dto.StudentDTO;
import ru.derendyaev.ideathesisUsersEtl.model.*;
import ru.derendyaev.ideathesisUsersEtl.repository.DepartmentRepository;
import ru.derendyaev.ideathesisUsersEtl.repository.DegreeFormRepository;
import ru.derendyaev.ideathesisUsersEtl.repository.DegreeLevelRepository;
import ru.derendyaev.ideathesisUsersEtl.repository.StudentGroupRepository;
import ru.derendyaev.ideathesisUsersEtl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StudentMapper {

    private final UserRepository userRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final DepartmentRepository departmentRepository;
    private final DegreeLevelRepository degreeLevelRepository;
    private final DegreeFormRepository degreeFormRepository;

    public Student map(StudentDTO dto) {
        User user = userRepository.findById(UUID.fromString(dto.getGuid()))
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setGuid(UUID.fromString(dto.getGuid()));
                    newUser.setFullName(dto.getFullName());
                    newUser.setFirstName(dto.getFirstName());
                    newUser.setLastName(dto.getSurname());
                    newUser.setMiddleName(dto.getMiddleName());
                    newUser.setUserType("student");
                    return newUser;
                });

        Student student = new Student();
        student.setGuid(user.getGuid());
        student.setUser(user);
        student.setCourse(dto.getCourse());
        student.setStartYear(dto.getStartYear());

        student.setGroup(studentGroupRepository.findByName(dto.getGroup())
                .orElseGet(() -> {
                    StudentGroup newGroup = new StudentGroup();
                    newGroup.setName(dto.getGroup());
                    return studentGroupRepository.save(newGroup);
                }));

        student.setDepartment(departmentRepository.findByName(dto.getDepartment())
                .orElseGet(() -> {
                    Department newDepartment = new Department();
                    newDepartment.setName(dto.getDepartment());
                    return departmentRepository.save(newDepartment);
                }));

        student.setDegreeLevel(degreeLevelRepository.findByName(dto.getDegreeLevel())
                .orElseGet(() -> {
                    DegreeLevel newDegreeLevel = new DegreeLevel();
                    newDegreeLevel.setName(dto.getDegreeLevel());
                    return degreeLevelRepository.save(newDegreeLevel);
                }));

        student.setDegreeForm(degreeFormRepository.findByName(dto.getDegreeForm())
                .orElseGet(() -> {
                    DegreeForm newDegreeForm = new DegreeForm();
                    newDegreeForm.setName(dto.getDegreeForm());
                    return degreeFormRepository.save(newDegreeForm);
                }));

        return student;
    }
}