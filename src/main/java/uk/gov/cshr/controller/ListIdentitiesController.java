package uk.gov.cshr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.dto.IdentityDTO;
import uk.gov.cshr.repository.IdentityRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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

    @GetMapping(value = "/api/identities", params = "emailAddress")
    public ResponseEntity<IdentityDTO> findByEmailAddress(@RequestParam String emailAddress) {

        Identity identity = identityRepository.findFirstByActiveTrueAndEmailEquals(emailAddress);
        if (identity != null) {
            return ResponseEntity.ok(new IdentityDTO(identity));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/api/identities", params = "uid")
    public ResponseEntity<IdentityDTO> findByUid(@RequestParam String uid) {
        Optional<Identity> identity = identityRepository.findFirstByUid(uid);
        return identity
                .map(i -> ResponseEntity.ok(new IdentityDTO(i)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
