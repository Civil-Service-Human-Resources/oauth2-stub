package uk.gov.cshr.domain;

import lombok.Data;

@Data
public class AgencyToken {
    private String token;
    private int capacity;
    private int capacityUsed;
}
