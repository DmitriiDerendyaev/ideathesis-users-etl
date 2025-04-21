package ru.derendyaev.ideathesisUsersEtl.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;


import java.util.Date;
import java.util.UUID;

@Entity
@Data
public class User {
    @Id
    private UUID guid;
    private String fullName;
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private String phone;
    private String userType;
    private Date createdAt;
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