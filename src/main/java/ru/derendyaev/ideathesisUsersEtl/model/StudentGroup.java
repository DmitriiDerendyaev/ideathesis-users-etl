package ru.derendyaev.ideathesisUsersEtl.model;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "student_groups")
@Data
public class StudentGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;
}