package uk.gov.cshr.domain;

import javax.persistence.*;

@Entity
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String uid;

    @Column(length = 100)
    private String password;

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

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", uid=" + uid +
                ", password='" + password + '\'' +
                ", active=" + active +
                '}';
    }
}
