package uk.gov.cshr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cshr.dto.IdentityDTO;
import uk.gov.cshr.repository.IdentityRepository;

import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@RestController
public class ListIdentitiesController {

    private IdentityRepository identityRepository;

    @Autowired
    public ListIdentitiesController(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    @GetMapping("/api/identities")
    public ResponseEntity<List<IdentityDTO>> listIdentities() {

        return ResponseEntity.ok(StreamSupport.stream(identityRepository.findAll().spliterator(), false)
                .map(IdentityDTO::new)
                .collect(toList()));
    }
}
