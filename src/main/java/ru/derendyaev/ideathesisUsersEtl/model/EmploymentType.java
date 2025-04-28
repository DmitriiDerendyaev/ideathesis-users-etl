package ru.derendyaev.ideathesisUsersEtl.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employment_types")
@Data
@Builder
@NoArgsConstructor // Для JPA
@AllArgsConstructor // Для Builder
public class EmploymentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    public EmploymentType(String name) {
        this.name = name;
    }
}