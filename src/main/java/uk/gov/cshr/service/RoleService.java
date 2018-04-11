package uk.gov.cshr.service;

import uk.gov.cshr.domain.Role;

import java.util.Optional;

public interface RoleService {
    Role createNewRole(Role role);
    Role updateRole(Role role);
    Role deleteRole(Role role);
    Role findRole(String name);
    Optional<Role> getRole(Long id);
    Iterable<Role> findAll();
}