package uk.gov.cshr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.domain.User;
import uk.gov.cshr.repository.RoleRepository;
import uk.gov.cshr.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public User createNewUser(String email, String password, boolean status) {
        Set<Role> roleSet = new HashSet<>();
        roleSet.add(roleRepository.findFirstByNameEquals("USER"));
        return userRepository.save(new User(UUID.randomUUID().toString(), email, passwordEncoder.encode(password), status, roleSet));
    }

    @Override
    public User findActiveUser(String email) {
        return userRepository.findFirstByActiveTrueAndEmailEquals(email);
    }

    @Override
    public Boolean isValidCredentials(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }
}
