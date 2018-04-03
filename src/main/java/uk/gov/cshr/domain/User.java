package uk.gov.cshr.domain;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.Set;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String uid;

    @Column(unique = true, length = 150)
    @Email
    private String email;

    @Column(length = 100)
    private String password;

    private boolean active;

    @ManyToMany
    private Set<Role> roles;

    protected User() {
    }

    public User(String uid, String email, String password, boolean active, Set<Role> roles) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.active = active;
        this.roles = roles;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public String getPassword() {
        return password;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", active=" + active +
                ", roles=" + roles +
                '}';
    }
}
