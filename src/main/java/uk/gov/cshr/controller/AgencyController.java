package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cshr.domain.AgencyTokenCapacityUsedDto;
import uk.gov.cshr.service.AgencyTokenCapacityService;

@RestController
@Slf4j
@RequestMapping("/agency")
public class AgencyController {

    private AgencyTokenCapacityService agencyTokenCapacityService;

    public AgencyController(AgencyTokenCapacityService agencyTokenCapacityService) {
        this.agencyTokenCapacityService = agencyTokenCapacityService;
    }

    @GetMapping("/{uid}")
    public ResponseEntity<AgencyTokenCapacityUsedDto> getSpacesUsedForAgencyToken(@PathVariable(value = "uid") String uid) {
        log.debug("Getting spaces used for agency token {}", uid);

        try {
            return ResponseEntity.ok(agencyTokenCapacityService.getSpacesUsedByAgencyToken(uid));
        } catch (Exception e) {
            log.error("Unexpected error calling getSpacesUsedForAgencyToken with uid = {}, {}", uid, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
