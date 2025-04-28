package ru.derendyaev.ideathesisUsersEtl.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "employees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Employee {
    @Id
    @EqualsAndHashCode.Include
    private UUID guid;

    @Column(name = "full_name")
    private String fullName;

    private String surname;

    @Column(unique = true, nullable = true) // Разрешаем NULL
    private String email;

    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<EmployeeEmployment> employeeEmployments;

    @OneToOne
    @MapsId
    @JoinColumn(name = "guid")
    private User user;

    @Version
    private Long version;
}