package uk.gov.cshr.domain;

import lombok.Data;
import org.apache.commons.lang3.RandomStringUtils;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
public class EmailUpdate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String code = RandomStringUtils.random(40, true, true);

    private String email;

    @ManyToOne
    private Identity identity;
    private Instant timestamp;
}