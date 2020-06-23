package uk.gov.cshr.domain;

import lombok.Data;

@Data
public class AgencyTokenCapacityUsedDto {
    private Long capacityUsed;

    public AgencyTokenCapacityUsedDto(Long capacityUsed) {
        this.capacityUsed = capacityUsed;
    }
}
