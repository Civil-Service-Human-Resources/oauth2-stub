package uk.gov.cshr.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.cshr.domain.Status;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.cshr.service.AuthenticationDetails;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.security.IdentityService;

@Controller
@RequestMapping("/signup")
public class SignupController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InviteController.class);

    @Autowired
    private AuthenticationDetails authenticationDetails;

    @Autowired
    private InviteService inviteService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private InviteRepository inviteRepository;

    @Autowired
    private IdentityRepository identityRepository;

    @GetMapping("/{code}")
    public String signup(@PathVariable(value = "code") String code) {
        LOGGER.info("{} on signup screen with code {}", authenticationDetails.getCurrentUsername(), code);

        if (inviteRepository.existsByCode(code)) {
            if (!inviteService.isCodeExpired(code)) {
                identityService.createIdentityFromInvitedUser(inviteRepository.findByCode(code));
                inviteService.updateInviteByCode(code, Status.ACCEPTED);
                LOGGER.info("Successful signup");
            }
        }

        return "signup";
    }

    // is code valid
    // should be in db
    // shouldnt be expired (time calc)
    // should be pending
    // what is current time minus expiry time (24hrs) mark as expired
    // create identity


}
