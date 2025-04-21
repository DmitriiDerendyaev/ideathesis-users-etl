package ru.derendyaev.ideathesisUsersEtl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.derendyaev.ideathesisUsersEtl.model.EmployeeEmployment;

public interface EmployeeEmploymentRepository extends JpaRepository<EmployeeEmployment, Long> {
}
