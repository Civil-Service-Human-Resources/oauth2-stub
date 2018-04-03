package uk.gov.cshr.service;

import uk.gov.cshr.domain.User;

public interface UserService {
    User createNewUser(String email, String password, boolean status);
    User findActiveUser(String email);
    Boolean isValidCredentials(User user, String password);
}
