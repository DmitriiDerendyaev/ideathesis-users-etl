package ru.derendyaev.ideathesisUsersEtl.repository;

import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.derendyaev.ideathesisUsersEtl.model.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

}
