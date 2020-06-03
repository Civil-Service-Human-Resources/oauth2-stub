package uk.gov.cshr.domain;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@ToString
public class Reactivation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, length = 40, nullable = false)
    private String code;

    @Column(length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private ReactivationStatus reactivationStatus;

    @Column(nullable = false)
    private Date requestedAt;

    private Date reactivatedAt;

    @Column(length = 150, nullable = false)
    private String email;

    public Reactivation(String code, ReactivationStatus reactivationStatus, Date requestedAt, String email) {
        this.code = code;
        this.reactivationStatus = reactivationStatus;
        this.requestedAt = requestedAt;
        this.email = email;
    }

    public Reactivation() {
    }
}
