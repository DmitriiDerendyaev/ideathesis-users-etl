package ru.derendyaev.ideathesisUsersEtl.dto;

import lombok.Data;

import java.util.List;

@Data
public class EmployeesResponse {
    private List<EmployeeDTO> items;
}
