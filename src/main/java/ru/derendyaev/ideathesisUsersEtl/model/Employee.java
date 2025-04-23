package ru.derendyaev.ideathesisUsersEtl.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "employees")
@Data
public class Employee {
    @Id
    private UUID guid;

    @Column(name = "full_name")
    private String fullName;

    private String surname;

    @Column(unique = true)
    private String email;

    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmployeeEmployment> employeeEmployments;

    @OneToOne
    @MapsId
    @JoinColumn(name = "guid")
    private User user;
}