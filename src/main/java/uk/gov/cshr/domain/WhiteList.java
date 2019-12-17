
package uk.gov.cshr.domain;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name="whitelisted_domains")
public class WhiteList implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String domain;

    public WhiteList(String domain) {
        this.domain = domain;
    }

    public WhiteList() {
    }

}
