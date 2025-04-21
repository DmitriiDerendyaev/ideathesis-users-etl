package ru.derendyaev.ideathesisUsersEtl.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class EmployeeEmployment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_guid")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "job_title_id")
    private JobTitle jobTitle;

    @ManyToOne
    @JoinColumn(name = "staff_category_id")
    private StaffCategory staffCategory;

    @ManyToOne
    @JoinColumn(name = "employment_type_id")
    private EmploymentType employmentType;

    @ManyToOne
    @JoinColumn(name = "subdivision_id")
    private Subdivision subdivision;

    private String jobState;
}