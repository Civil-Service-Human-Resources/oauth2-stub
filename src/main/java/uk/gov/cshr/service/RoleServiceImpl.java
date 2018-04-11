package uk.gov.cshr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.RoleRepository;

import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Role createNewRole(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public Role updateRole(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public Role deleteRole(Role role) {
        return null;
    }

    @Override
    public Role findRole(String name) {
        return roleRepository.findFirstByNameEquals(name);
    }

    @Override
    public Iterable<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    public Optional<Role> getRole(Long id) {
        return roleRepository.findById(id);
    }
}