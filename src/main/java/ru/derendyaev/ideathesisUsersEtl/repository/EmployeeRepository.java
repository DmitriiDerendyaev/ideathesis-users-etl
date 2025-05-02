package ru.derendyaev.ideathesisUsersEtl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.derendyaev.ideathesisUsersEtl.model.Employee;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByGuid(UUID guid);
}