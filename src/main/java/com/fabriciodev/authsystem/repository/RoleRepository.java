package com.fabriciodev.authsystem.repository;

import com.fabriciodev.authsystem.domain.entity.Role;
import com.fabriciodev.authsystem.domain.enums.RoleType;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<Role, Long> {
    Role findByName(RoleType name);
}

