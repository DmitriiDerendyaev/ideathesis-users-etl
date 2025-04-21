package ru.derendyaev.ideathesisUsersEtl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.derendyaev.ideathesisUsersEtl.model.EmploymentType;

import java.util.Optional;

public interface EmploymentTypeRepository extends JpaRepository<EmploymentType, Long> {
    Optional<EmploymentType> findByName(String name);
}
