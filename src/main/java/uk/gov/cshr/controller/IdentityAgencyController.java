package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.dto.IdentityAgencyDTO;
import uk.gov.cshr.repository.IdentityRepository;

import java.util.Optional;

@Slf4j
@RestController
public class IdentityAgencyController {

    private IdentityRepository identityRepository;

    @Autowired
    public IdentityAgencyController(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    @GetMapping(value = "/identity/agency/{uid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IdentityAgencyDTO> findByUid(@PathVariable String uid) {
        log.info("Getting agency token uid for user with uid " + uid);
        Optional<Identity> identity = identityRepository.findFirstByUid(uid);
        return identity
                .map(i -> buildResponse(i))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponseEntity<IdentityAgencyDTO> buildResponse(Identity i) {
        IdentityAgencyDTO responseDTO = new IdentityAgencyDTO();
        responseDTO.setAgencyTokenUid(i.getAgencyTokenUid());
        responseDTO.setUid(i.getUid());
        return ResponseEntity.ok(responseDTO);
    }

}
