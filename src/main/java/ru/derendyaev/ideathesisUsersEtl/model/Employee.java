package ru.derendyaev.ideathesisUsersEtl.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
public class Employee {
    @Id
    private UUID guid;
    private String fullName;
    private String surname;
    private String email;
    private Date dateOfBirth;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmployeeEmployment> employeeEmployments;

    @OneToOne
    @MapsId
    @JoinColumn(name = "guid")
    private User user;
}