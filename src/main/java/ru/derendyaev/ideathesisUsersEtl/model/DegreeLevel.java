package ru.derendyaev.ideathesisUsersEtl.model;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "degree_levels")
@Data
public class DegreeLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;
}