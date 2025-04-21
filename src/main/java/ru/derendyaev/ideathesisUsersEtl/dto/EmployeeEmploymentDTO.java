package ru.derendyaev.ideathesisUsersEtl.dto;

import lombok.Data;

@Data
public class EmployeeEmploymentDTO {
    private String jobTitle;
    private String staffCategory;
    private String employmentType;
    private String subDivision;
    private String subDivisionGuid;
    private String jobState;
}
