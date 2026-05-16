package com.fabriciodev.authsystem.repository;

import com.fabriciodev.authsystem.domain.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface UserRepository extends CrudRepository<User, UUID> {
    // Project uses email as unique login — expose finder for authentication flows
    User findByEmail(String email);
}
