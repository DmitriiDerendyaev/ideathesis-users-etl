package ru.derendyaev.ideathesisUsersEtl.dto;

import lombok.Data;

@Data
public class StudentDTO {
    private String fullName;
    private String guid;
    private String firstName;
    private String surname;
    private String middleName;
    private String department;
    private String group;
    private Integer course;
    private Integer startYear;
    private String degreeLevel;
    private String degreeForm;
}
