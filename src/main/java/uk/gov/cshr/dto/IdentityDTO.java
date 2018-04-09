package uk.gov.cshr.dto;

import uk.gov.cshr.domain.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IdentityDTO {
    private String username;
    private String uid;
    private List<String> roles = new ArrayList<>();

    public IdentityDTO(String username, String uid, Set<Role> roles) {
        this.username = username;
        this.uid = uid;
        roles.forEach(role -> this.roles.add(role.getName()));
    }

    public String getUsername() {
        return username;
    }

    public String getUid() {
        return uid;
    }

    public List<String> getRoles() {
        return roles;
    }
}
