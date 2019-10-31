package uk.gov.cshr.domain;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

@Entity
public class Identity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 36)
    private String uid;

    @Column(unique = true, length = 150)
    @Email
    private String email;

    @Column(length = 100)
    private String password;

    @Column(name = "failed_login_attempts")
    private Long failedLoginAttempts;

    private boolean active;

    private boolean locked;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "identity_role",
            joinColumns = @JoinColumn(name = "identity_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Role> roles;

    private Instant lastLoggedIn;

    private boolean deletionNotificationSent;

    public Identity() {
    }

    public Identity(String uid, String email, String password, boolean active, boolean locked, Set<Role> roles, Instant lastLoggedIn, boolean deletionNotificationSent, Long failedLoginAttempts) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.active = active;
        this.roles = roles;
        this.locked = locked;
        this.lastLoggedIn = lastLoggedIn;
        this.deletionNotificationSent = deletionNotificationSent;
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Instant getLastLoggedIn() {
        return lastLoggedIn;
    }

    public void setLastLoggedIn(Instant lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }

    public boolean isDeletionNotificationSent() {
        return deletionNotificationSent;
    }

    public void setDeletionNotificationSent(boolean deletionNotificationSent) {
        this.deletionNotificationSent = deletionNotificationSent;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Identity setFailedLoginAttempts(Long failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
        return this;
    }

    @Override
    public String toString() {
        return "Identity{" +
                "id=" + id +
                ", uid='" + uid + '\'' +
                ", active=" + active +
                ", locked=" + locked +
                '}';
    }

    public static class Builder {
        private Long id;
        private String uid;
        private String email;
        private String password;
        private Long failedLoginAttempts;
        private boolean active;
        private boolean locked;
        private Set<Role> roles;
        private Instant lastLoggedIn;
        private boolean deletionNotificationSent;

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public Builder setUid(String uid) {
            this.uid = uid;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setFailedLoginAttempts(Long failedLoginAttempts) {
            this.failedLoginAttempts = failedLoginAttempts;
            return this;
        }

        public Builder setActive(boolean active) {
            this.active = active;
            return this;
        }

        public Builder setLocked(boolean locked) {
            this.locked = locked;
            return this;
        }

        public Builder setRoles(Set<Role> roles) {
            this.roles = roles;
            return this;
        }

        public Builder setLastLoggedIn(Instant lastLoggedIn) {
            this.lastLoggedIn = lastLoggedIn;
            return this;
        }

        public Builder setDeletionNotificationSent(boolean deletionNotificationSent) {
            this.deletionNotificationSent = deletionNotificationSent;
            return this;
        }

        public Identity build() {
            return new Identity(uid, email, password, active, locked, roles, lastLoggedIn, deletionNotificationSent, failedLoginAttempts);
        }
    }
}
