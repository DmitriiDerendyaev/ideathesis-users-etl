package ru.derendyaev.ideathesisUsersEtl.dto;

import lombok.Data;

import java.util.List;

@Data
public class EmployeeDTO {
    private String fullName;
    private String guid;
    private String surname;
    private String mail;
    private String dateOfBirth;
    private List<EmployeeEmploymentDTO> employeeEmployments;
}