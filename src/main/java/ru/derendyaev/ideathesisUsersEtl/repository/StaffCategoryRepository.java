package ru.derendyaev.ideathesisUsersEtl.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.derendyaev.ideathesisUsersEtl.model.StaffCategory;

import java.util.Optional;

public interface StaffCategoryRepository extends JpaRepository<StaffCategory, Long> {
    Optional<StaffCategory> findByName(String code);
}