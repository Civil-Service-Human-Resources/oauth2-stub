package uk.gov.cshr.service;

import uk.gov.cshr.domain.Role;

public interface RoleService {
    Role createNewRole(Role role);
    Role updateRole(Role role);
    Role deleteRole(Role role);
    Role findRole(String name);
    Iterable<Role> findAll();
}