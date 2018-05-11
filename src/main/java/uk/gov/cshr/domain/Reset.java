package uk.gov.cshr.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Reset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, length = 40, nullable = false)
    private String code;

    @Column(length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private ResetStatus resetStatus;

    @Column(nullable = false)
    private Date requestedAt;

    private Date resetAt;

    @Column(length = 150, nullable = false)
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

    public Date getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Date requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Date getResetAt() {
        return resetAt;
    }

    public void setResetAt(Date resetAt) {
        this.resetAt = resetAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
