package ru.derendyaev.ideathesisUsersEtl.model;


import jakarta.persistence.*;
import lombok.Data;


import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "users") // Исправлено на "users" вместо зарезервированного "user"
@Data
public class User {
    @Id
    private UUID guid;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(unique = true)
    private String email;

    private String phone;

    @Column(name = "user_type")
    private String userType;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}