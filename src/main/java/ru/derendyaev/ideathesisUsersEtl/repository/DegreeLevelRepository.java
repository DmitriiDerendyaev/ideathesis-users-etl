package ru.derendyaev.ideathesisUsersEtl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.derendyaev.ideathesisUsersEtl.model.DegreeLevel;

import java.util.Optional;

public interface DegreeLevelRepository extends JpaRepository<DegreeLevel, Long> {
    Optional<DegreeLevel> findByName(String name);
}
