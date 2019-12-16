package uk.gov.cshr.controller.organisation;

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
import uk.gov.cshr.service.EmailUpdateService;
import uk.gov.cshr.service.security.IdentityService;

import javax.validation.Valid;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/organisation")
public class OrganisationController {

    private static final String STATUS_ATTRIBUTE = "status";

    private final IdentityService identityService;

    private final CsrsService csrsService;

    private final IdentityRepository identityRepository;

    private final EmailUpdateService emailUpdateService;

    public OrganisationController(
            IdentityService identityService,
            CsrsService csrsService,
            IdentityRepository identityRepository,
            EmailUpdateService emailUpdateService) {
        this.identityService = identityService;
        this.csrsService = csrsService;
        this.identityRepository = identityRepository;
        this.emailUpdateService = emailUpdateService;
    }

    @GetMapping(path = "/enterOrganisation")
    public String enterOrganisation(Model model) {

        log.info("User accessing update or confirm your organisation screen");

        if(model.containsAttribute("enterOrganisationForm")) {
            OrganisationalUnitDto[] organisations = csrsService.getOrganisationalUnitsFormatted();
            model.addAttribute("organisations", organisations);
            return "enterOrganisation";
        } else {

            String domain = (String) model.asMap().get("domain");
            String uid = (String) model.asMap().get("uid");

            OrganisationalUnitDto[] organisations = csrsService.getOrganisationalUnitsFormatted();

            model.addAttribute("organisations", organisations);
            EnterOrganisationForm form = new EnterOrganisationForm();
            form.setDomain(domain);
            form.setUid(uid);
            model.addAttribute("enterOrganisationForm", form);
            return "enterOrganisation";
        }

    }

    @PostMapping(path = "/enterOrganisation")
    public String submitOrganisation(Model model,
                              @ModelAttribute @Valid EnterOrganisationForm form,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {

        log.info("User attempting to update or confirm their organisation");


        if (bindingResult.hasErrors()) {
            model.addAttribute("enterOrganisationForm", form);
            return "enterOrganisation";
        }

        String uid = form.getUid();

        try {
            Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);
            if(optionalIdentity.isPresent()) {
                log.info("User submitted Enter organisation form with organisation = {}", form.getOrganisation());

                String newDomain = identityService.getDomainFromEmailAddress(optionalIdentity.get().getEmail());
                boolean isTokenPerson = identityService.isWhitelistedDomain(newDomain);

                if(isTokenPerson) {
                    String newToken = csrsService.getOrgCode(uid);
                    if(newToken == null) {
                        redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Incorrect organisation");
                        redirectAttributes.addFlashAttribute("enterOrganisationForm", form);
                        return "redirect:/organisation/enterOrganisation";
                    }
                    emailUpdateService.updateOrganisationUpdateAgencyTokenSpacesAndResetFlag(form.getDomain(), newToken, form.getOrganisation(), uid);
                } else {
                    emailUpdateService.updateOrganisationAndResetFlag(form.getOrganisation(), uid);
                }

                return "redirect:/redirectToUIHomePage";
            } else {
                log.info("No identity found for uid {}", uid);
                return "redirect:/login";
            }
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Incorrect organisation");
            redirectAttributes.addFlashAttribute("enterOrganisationForm", form);
            return "redirect:/organisation/enterOrganisation";
        } catch (BadRequestException e) {
            return "redirect:/login";
        } catch (Exception e) {
            return "redirect:/login";
        }

    }

}
