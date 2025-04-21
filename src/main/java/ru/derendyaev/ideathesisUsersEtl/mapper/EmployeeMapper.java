package ru.derendyaev.ideathesisUsersEtl.mapper;

import ru.derendyaev.ideathesisUsersEtl.dto.EmployeeDTO;
import ru.derendyaev.ideathesisUsersEtl.dto.EmployeeEmploymentDTO;
import ru.derendyaev.ideathesisUsersEtl.model.*;
import ru.derendyaev.ideathesisUsersEtl.repository.JobTitleRepository;
import ru.derendyaev.ideathesisUsersEtl.repository.StaffCategoryRepository;
import ru.derendyaev.ideathesisUsersEtl.repository.EmploymentTypeRepository;
import ru.derendyaev.ideathesisUsersEtl.repository.SubdivisionRepository;
import ru.derendyaev.ideathesisUsersEtl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EmployeeMapper {

    private final UserRepository userRepository;
    private final JobTitleRepository jobTitleRepository;
    private final StaffCategoryRepository staffCategoryRepository;
    private final EmploymentTypeRepository employmentTypeRepository;
    private final SubdivisionRepository subdivisionRepository;

    public Employee map(EmployeeDTO dto) {
        User user = userRepository.findById(UUID.fromString(dto.getGuid()))
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setGuid(UUID.fromString(dto.getGuid()));
                    newUser.setFullName(dto.getFullName());
                    newUser.setLastName(dto.getSurname());
                    newUser.setEmail(dto.getMail());
                    newUser.setUserType("employee");
                    return newUser;
                });

        Employee employee = new Employee();
        employee.setGuid(user.getGuid());
        employee.setUser(user);
        employee.setDateOfBirth(parseDate(dto.getDateOfBirth()));

        if (dto.getEmployeeEmployments() != null) {
            dto.getEmployeeEmployments().forEach(empEmpDto -> {
                EmployeeEmployment empEmp = new EmployeeEmployment();
                empEmp.setEmployee(employee);

                empEmp.setJobTitle(jobTitleRepository.findByName(empEmpDto.getJobTitle())
                        .orElseGet(() -> {
                            JobTitle newJobTitle = new JobTitle();
                            newJobTitle.setName(empEmpDto.getJobTitle());
                            return jobTitleRepository.save(newJobTitle);
                        }));

                empEmp.setStaffCategory(staffCategoryRepository.findByName(empEmpDto.getStaffCategory())
                        .orElseGet(() -> {
                            StaffCategory newStaffCategory = new StaffCategory();
                            newStaffCategory.setName(empEmpDto.getStaffCategory());
                            return staffCategoryRepository.save(newStaffCategory);
                        }));

                empEmp.setEmploymentType(employmentTypeRepository.findByName(empEmpDto.getEmploymentType())
                        .orElseGet(() -> {
                            EmploymentType newEmploymentType = new EmploymentType();
                            newEmploymentType.setName(empEmpDto.getEmploymentType());
                            return employmentTypeRepository.save(newEmploymentType);
                        }));

                empEmp.setSubdivision(subdivisionRepository.findByGuid(UUID.fromString(empEmpDto.getSubDivisionGuid()))
                        .orElseGet(() -> {
                            Subdivision newSubdivision = new Subdivision();
                            newSubdivision.setName(empEmpDto.getSubDivision());
                            newSubdivision.setGuid(UUID.fromString(empEmpDto.getSubDivisionGuid()));
                            return subdivisionRepository.save(newSubdivision);
                        }));

                empEmp.setJobState(empEmpDto.getJobState());
                employee.getEmployeeEmployments().add(empEmp);
            });
        }

        return employee;
    }

    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            return formatter.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse date", e);
        }
    }
}