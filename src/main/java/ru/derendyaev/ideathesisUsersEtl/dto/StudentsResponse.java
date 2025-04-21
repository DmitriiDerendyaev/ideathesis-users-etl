package ru.derendyaev.ideathesisUsersEtl.dto;

import lombok.Data;

import java.util.List;

@Data
public class StudentsResponse {
    private List<StudentDTO> items;
}