package ru.derendyaev.ideathesisUsersEtl.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "subdivisions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subdivision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(unique = true)
    private UUID guid;

    public Subdivision(String name, UUID guid) {
        this.name = name;
        this.guid = guid;
    }
}