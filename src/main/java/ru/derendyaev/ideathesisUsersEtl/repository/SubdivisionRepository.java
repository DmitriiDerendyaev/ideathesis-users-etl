package ru.derendyaev.ideathesisUsersEtl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.derendyaev.ideathesisUsersEtl.model.Subdivision;

import java.util.Optional;
import java.util.UUID;

public interface SubdivisionRepository extends JpaRepository<Subdivision, Long> {
    Optional<Subdivision> findByGuid(UUID guid);

    Optional<Subdivision> findByName(String name);
}
