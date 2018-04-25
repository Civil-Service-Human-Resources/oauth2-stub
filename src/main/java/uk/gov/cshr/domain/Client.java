package uk.gov.cshr.domain;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;

@Entity
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String uid;

    @Column(length = 100)
    private String password;

    private String redirectUri;

    private boolean active;

    protected Client() {
    }

    public Client(String uid, String password, boolean active) {
        this.uid = uid;
        this.password = password;
        this.active = active;
    }

    public String getUid() {
        return uid;
    }

    public String getPassword() {
        return password;
    }

    public boolean isActive() {
        return active;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("uid", uid)
                .append("active", active)
                .append("redirectUri", redirectUri)
                .toString();
    }
}
