package ru.derendyaev.ideathesisUsersEtl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.derendyaev.ideathesisUsersEtl.model.DegreeForm;

import java.util.Optional;

public interface DegreeFormRepository extends JpaRepository<DegreeForm, Long> {
    Optional<DegreeForm> findByName(String code);
}
