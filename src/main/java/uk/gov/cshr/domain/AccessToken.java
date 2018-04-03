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
    private User user;

    @OneToOne
    private Client client;

    protected AccessToken() {
    }

    public AccessToken(String token, LocalDateTime createdAt, LocalDateTime expiresAt, User user, Client client) {
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.user = user;
        this.client = client;
        this.status = TokenStatus.active;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
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
                ", user=" + user +
                ", client=" + client +
                '}';
    }
}
