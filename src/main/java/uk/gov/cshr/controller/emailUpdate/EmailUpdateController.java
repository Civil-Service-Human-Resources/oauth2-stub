package uk.gov.cshr.controller.emailUpdate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.controller.signup.EnterTokenForm;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.exception.BadRequestException;
import uk.gov.cshr.exception.NotEnoughSpaceAvailableException;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.exception.UnableToAllocateAgencyTokenException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.security.IdentityService;

import javax.validation.Valid;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/emailUpdated")
public class EmailUpdateController {

    private static final String STATUS_ATTRIBUTE = "status";

    private final IdentityService identityService;

    private final CsrsService csrsService;

    private final IdentityRepository identityRepository;

    public EmailUpdateController(
                            IdentityService identityService,
                            CsrsService csrsService,
                            IdentityRepository identityRepository) {
        this.identityService = identityService;
        this.csrsService = csrsService;
        this.identityRepository = identityRepository;
    }

    @GetMapping(path = "/enterToken/{domain}/{uid}")
    public String enterToken(Model model,
                             @PathVariable("domain") String domain,
                             @PathVariable("uid") String uid) {

        log.info("User accessing token-based email updated screen");

        OrganisationalUnitDto[] organisations = csrsService.getOrganisationalUnitsFormatted();

        model.addAttribute("organisations", organisations);
        model.addAttribute("emailUpdatedRecentlyEnterTokenForm", new EmailUpdatedRecentlyEnterTokenForm());
        model.addAttribute("domain", domain);

        return "enterTokenSinceEmailUpdate";
    }

    @PostMapping(path = "/enterToken/{domain}/{uid}")
    public String submitToken(Model model,
                              @PathVariable("domain") String domain,
                              @PathVariable("uid") String uid,
                              @ModelAttribute @Valid EmailUpdatedRecentlyEnterTokenForm form,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {

        log.info("User attempting token-based sign up since updating their email");

        if (bindingResult.hasErrors()) {
            model.addAttribute("emailUpdatedRecentlyEnterTokenForm", form);
            return "enterTokenSinceEmailUpdate";
        }

        try {
            Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);
            if(optionalIdentity.isPresent()) {
                csrsService.updateSpacesAvailable(domain, form.getToken(), form.getOrganisation(), false);
                log.info("User submitted Enter Token form with domain = {}, token = {}, org = {}", domain, form.getToken(), form.getOrganisation());
                identityService.resetRecentlyUpdatedEmailFlag(optionalIdentity.get());
                String url = String.format("/updateOrganisation/enterOrganisation/%s/%s", domain, uid);
               return "redirect:"+url;
            } else {
                log.info("No identity found for uid {}", uid);
                throw new ResourceNotFoundException();
            }
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Incorrect token for this organisation");
            return "redirect:/emailUpdated/enterToken/" + domain + "/" + uid;
        } catch (NotEnoughSpaceAvailableException e) {
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Not enough spaces available on this token");
            return "redirect:/emailUpdated/enterToken/" + domain + "/" + uid;
        } catch (BadRequestException e) {
            return "redirect:/login";
        } catch (UnableToAllocateAgencyTokenException e) {
            return "redirect:/login";
        }

    }

}
