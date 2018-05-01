package uk.gov.cshr.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Reset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String code;

    @Column
    @Enumerated(EnumType.STRING)
    private ResetStatus resetStatus;

    private Date invitedAt;

    private Date acceptedAt;

    @Column
    private String email;

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

    public ResetStatus getResetStatus() {
        return resetStatus;
    }

    public void setResetStatus(ResetStatus resetStatus) {
        this.resetStatus = resetStatus;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
