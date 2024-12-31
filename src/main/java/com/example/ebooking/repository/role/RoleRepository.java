package com.example.ebooking.repository.role;

import com.example.ebooking.model.Role;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRole(Role.RoleName roleName);

    Optional<Set<Role>> findByRoleIn(Set<String> roleNames);
}
