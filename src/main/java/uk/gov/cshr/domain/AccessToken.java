package uk.gov.cshr.domain;

import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
public class AccessToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    private TokenStatus status;

    @CreatedDate
    private LocalDateTime createdAt;

    @Basic
    private LocalDateTime expiresAt;

    @OneToOne
    private Identity identity;

    @OneToOne
    private Client client;

    protected AccessToken() {
    }

    public AccessToken(String token, LocalDateTime createdAt, LocalDateTime expiresAt, Identity identity, Client client) {
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.identity = identity;
        this.client = client;
        this.status = TokenStatus.active;
    }

    public String getToken() {
        return token;
    }

    public Identity getIdentity() {
        return identity;
    }

    public Long getExpiresInMinutes() {
        return ChronoUnit.MINUTES.between(createdAt, expiresAt);
    }

    @Override
    public String toString() {
        return "AccessToken{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", identity=" + identity +
                ", client=" + client +
                '}';
    }
}
