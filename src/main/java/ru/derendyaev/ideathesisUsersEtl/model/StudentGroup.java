package ru.derendyaev.ideathesisUsersEtl.model;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class StudentGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
}