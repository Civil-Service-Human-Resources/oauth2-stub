package uk.gov.cshr.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Entity
public class Invite implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, length = 40, nullable = false)
    private String code;

    @Column(length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private InviteStatus status;

    @OneToOne(optional = false)
    private Identity inviter;

    @Column(nullable = false)
    private Date invitedAt;

    private Date acceptedAt;

    @Column(length = 150, nullable = false)
    private String forEmail;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "invite_role",
            joinColumns = @JoinColumn(name = "invite_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Role> forRoles;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public InviteStatus getStatus() {
        return status;
    }

    public void setStatus(InviteStatus status) {
        this.status = status;
    }

    public Identity getInviter() {
        return inviter;
    }

    public void setInviter(Identity inviter) {
        this.inviter = inviter;
    }

    public Date getInvitedAt() {
        return invitedAt;
    }

    public void setInvitedAt(Date invitedAt) {
        this.invitedAt = invitedAt;
    }

    public Date getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Date acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public String getForEmail() {
        return forEmail;
    }

    public void setForEmail(String forEmail) {
        this.forEmail = forEmail;
    }

    public Set<Role> getForRoles() {
        return forRoles;
    }

    public void setForRoles(Set<Role> forRoles) {
        this.forRoles = forRoles;
    }

    @Override
    public String toString() {
        return "Invite{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", status=" + status +
                ", inviter=" + inviter +
                ", invitedAt=" + invitedAt +
                ", acceptedAt=" + acceptedAt +
                ", forEmail='" + forEmail + '\'' +
                ", forRoles=" + forRoles +
                '}';
    }

}