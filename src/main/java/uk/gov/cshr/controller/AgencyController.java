package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.domain.AgencyTokenCapacityUsedDto;
import uk.gov.cshr.service.AgencyTokenCapacityService;

@Slf4j
@RestController
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

    @DeleteMapping("/{uid}")
    public ResponseEntity deleteAgencyToken(@PathVariable(value = "uid") String uid) {
        log.debug("Deleting agency token {}", uid);
        try {
            agencyTokenCapacityService.deleteAgencyToken(uid);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Unexpected error calling deleteAgencyToken with uid = {}, {}", uid, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
