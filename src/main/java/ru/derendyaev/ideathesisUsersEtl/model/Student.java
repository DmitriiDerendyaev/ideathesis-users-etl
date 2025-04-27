package ru.derendyaev.ideathesisUsersEtl.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "students")
@Data
@Builder
@NoArgsConstructor // Для JPA
@AllArgsConstructor // Для Builder
public class Student {
    @Id
    private UUID guid;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private StudentGroup group;

    private Integer course;

    @Column(name = "start_year")
    private Integer startYear;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "degree_level_id")
    private DegreeLevel degreeLevel;

    @ManyToOne
    @JoinColumn(name = "degree_form_id")
    private DegreeForm degreeForm;

    @OneToOne
    @MapsId
    @JoinColumn(name = "guid")
    private User user;
}