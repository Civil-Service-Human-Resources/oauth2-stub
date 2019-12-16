package uk.gov.cshr.controller.emailUpdate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.CsrsService;

import javax.validation.Valid;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/emailUpdated")
public class EmailUpdateController {

    private static final String STATUS_ATTRIBUTE = "status";

    private final CsrsService csrsService;

    private final IdentityRepository identityRepository;

    public EmailUpdateController(
                            CsrsService csrsService,
                            IdentityRepository identityRepository) {
        this.csrsService = csrsService;
        this.identityRepository = identityRepository;
    }

    @GetMapping(path = "/enterToken")
    public String enterToken(Model model) {

        log.info("User accessing token-based email updated screen");

        if(model.containsAttribute("emailUpdatedRecentlyEnterTokenForm")) {
            OrganisationalUnitDto[] organisations = csrsService.getOrganisationalUnitsFormatted();
            model.addAttribute("organisations", organisations);
            return "enterTokenSinceEmailUpdate";
        } else {
            String domain = (String) model.asMap().get("domain");
            String uid = (String) model.asMap().get("uid");

            OrganisationalUnitDto[] organisations = csrsService.getOrganisationalUnitsFormatted();

            model.addAttribute("organisations", organisations);
            EmailUpdatedRecentlyEnterTokenForm form = new EmailUpdatedRecentlyEnterTokenForm();
            form.setDomain(domain);
            form.setUid(uid);
            model.addAttribute("emailUpdatedRecentlyEnterTokenForm", form);
            return "enterTokenSinceEmailUpdate";
        }

    }

    @PostMapping(path = "/enterToken")
    public String checkToken(Model model,
                              @ModelAttribute @Valid EmailUpdatedRecentlyEnterTokenForm form,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {

        log.info("User attempting token-based sign up since updating their email");

        String domain = form.getDomain();
        String uid = form.getUid();

        if (bindingResult.hasErrors()) {
            model.addAttribute("emailUpdatedRecentlyEnterTokenForm", form);
            return "enterTokenSinceEmailUpdate";
        }

        try {
            Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);
            if(optionalIdentity.isPresent()) {
                log.info("User checking Enter Token form with domain = {}, token = {}, org = {}", domain, form.getToken(), form.getOrganisation());
                boolean ok = csrsService.checkTokenExists(domain, form.getToken(), form.getOrganisation(), false);
                if(ok){
                    redirectAttributes.addFlashAttribute("uid", uid);
                    redirectAttributes.addFlashAttribute("domain", domain);
                    redirectAttributes.addFlashAttribute("tokenPerson", true);
                    String url = "/organisation/enterOrganisation";
                    return "redirect:"+url;
                } else {
                    throw new ResourceNotFoundException();
                }
            } else {
                log.info("No identity found for uid {}", uid);
                return "redirect:/login";
            }
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Incorrect token for this organisation");
            redirectAttributes.addFlashAttribute("emailUpdatedRecentlyEnterTokenForm", form);
            return "redirect:/emailUpdated/enterToken";
        } catch (Exception e) {
            return "redirect:/login";
        }

    }

}
