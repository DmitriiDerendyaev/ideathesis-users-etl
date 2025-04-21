package ru.derendyaev.ideathesisUsersEtl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.derendyaev.ideathesisUsersEtl.model.Student;

import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {

}
