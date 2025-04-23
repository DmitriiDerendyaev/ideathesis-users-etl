package ru.derendyaev.ideathesisUsersEtl.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "degree_forms")
@Data
public class DegreeForm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;
}