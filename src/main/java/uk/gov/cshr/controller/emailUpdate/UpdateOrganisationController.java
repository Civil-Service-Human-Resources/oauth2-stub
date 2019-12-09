package uk.gov.cshr.controller.emailUpdate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.exception.*;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.security.IdentityService;

import javax.validation.Valid;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/updateOrganisation")
public class UpdateOrganisationController {

    private static final String STATUS_ATTRIBUTE = "status";

    private final IdentityService identityService;

    private final CsrsService csrsService;

    private final IdentityRepository identityRepository;

    public UpdateOrganisationController(
            IdentityService identityService,
            CsrsService csrsService,
            IdentityRepository identityRepository) {
        this.identityService = identityService;
        this.csrsService = csrsService;
        this.identityRepository = identityRepository;
    }

    @GetMapping(path = "/enterOrganisation/{domain}/{uid}")
    public String enterOrganisation(Model model,
                             @PathVariable("domain") String domain,
                             @PathVariable("uid") String uid) {

        log.info("User accessing update or confirm your organisation screen");

        OrganisationalUnitDto[] organisations = csrsService.getOrganisationalUnitsFormatted();

        model.addAttribute("organisations", organisations);
        model.addAttribute("enterOrganisationForm", new EnterOrganisationForm());
        model.addAttribute("domain", domain);

        return "enterOrganisation";
    }

    @PostMapping(path = "/enterOrganisation/{domain}/{uid}")
    public String submitOrganisation(Model model,
                              @PathVariable("domain") String domain,
                              @PathVariable("uid") String uid,
                              @ModelAttribute @Valid EnterOrganisationForm form,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {

        log.info("User attempting to update or confirm their organisation since updating their organisations");

        if (bindingResult.hasErrors()) {
            model.addAttribute("enterOrganisationForm", form);
            return "enterOrganisation";
        }

        try {
            Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);
            if(optionalIdentity.isPresent()) {
                log.info("User submitted Enter organisation form with organisation = {}", form.getOrganisation());
                csrsService.updateOrganisation(uid, form.getOrganisation());
                return "redirect:/redirectToUIHomePage";
            } else {
                log.info("No identity found for uid {}", uid);
                throw new ResourceNotFoundException();
            }
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Incorrect organisation");
            return "redirect:/updateOrganisation/enterOrganisation/" + domain + "/" + uid;
        } catch (BadRequestException e) {
            return "redirect:/login";
        } catch (UnableToUpdateOrganisationException e) {
            return "redirect:/login";
        }

    }

}
