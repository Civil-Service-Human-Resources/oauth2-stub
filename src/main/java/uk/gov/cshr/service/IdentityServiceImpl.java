package uk.gov.cshr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.RoleRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class IdentityServiceImpl implements IdentityService {

    @Autowired
    private IdentityRepository identityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    public Identity createNewIdentity(String email, String password, boolean status) {
        Set<Role> roleSet = new HashSet<>();
        roleSet.add(roleRepository.findFirstByNameEquals("USER"));
        return identityRepository.save(new Identity(UUID.randomUUID().toString(), email, passwordEncoder.encode(password), status, roleSet));
    }

    @Override
    public Identity findActiveIdentity(String email) {
        return identityRepository.findFirstByActiveTrueAndEmailEquals(email);
    }

    @Override
    public Boolean isValidCredentials(Identity identity, String password) {
        return passwordEncoder.matches(password, identity.getPassword());
    }
}
