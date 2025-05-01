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
import ru.derendyaev.ideathesisUsersEtl.service.StudentPreprocessor;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StudentMapper {
    private final StudentPreprocessor preprocessor;

    public Student map(StudentDTO dto) {
        return preprocessor.prepareStudent(dto);
    }
}