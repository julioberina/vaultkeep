package com.julioberina.vaultkeep.repository;

import com.julioberina.vaultkeep.model.ERole;
import com.julioberina.vaultkeep.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
	Optional<Role> findByName(ERole name);
}
