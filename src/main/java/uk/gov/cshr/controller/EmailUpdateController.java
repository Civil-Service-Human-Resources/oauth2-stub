package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.controller.signup.EnterTokenForm;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.exception.BadRequestException;
import uk.gov.cshr.exception.NotEnoughSpaceAvailableException;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.exception.UnableToAllocateAgencyTokenException;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.security.IdentityService;

import javax.validation.Valid;

@Slf4j
@Controller
@RequestMapping("/emailUpdated")
public class EmailUpdateController {

    private static final String STATUS_ATTRIBUTE = "status";

    private final IdentityService identityService;

    private final CsrsService csrsService;

    private final String lpgUiUrl;

    public EmailUpdateController(
                            IdentityService identityService,
                            CsrsService csrsService,
                            @Value("${lpg.uiUrl}") String lpgUiUrl) {

        this.identityService = identityService;
        this.csrsService = csrsService;
        this.lpgUiUrl = lpgUiUrl;
    }

    @GetMapping(path = "/enterToken/{domain}")
    public String enterToken(Model model, @PathVariable("domain") String domain) {

        log.info("User accessing token-based email updated screen");

        OrganisationalUnitDto[] organisations = csrsService.getOrganisationalUnitsFormatted();

        model.addAttribute("organisations", organisations);
        model.addAttribute("enterTokenForm", new EnterTokenForm());
        model.addAttribute("domain", domain);

        return "enterTokenSinceEmailUpdate";
    }

    @PostMapping(path = "/enterToken/{domain}")
    public String submitToken(Model model,
                              @PathVariable("domain") String domain,
                              @ModelAttribute @Valid EnterTokenForm form,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {

        log.info("User attempting token-based sign up since updating their email");

        if (bindingResult.hasErrors()) {
            model.addAttribute("enterTokenForm", form);
            return "enterTokenSinceEmailUpdate";
        }

        try {
            csrsService.updateSpacesAvailable(domain, form.getToken(), form.getOrganisation(), false);
            log.info("User submitted Enter Token form with domain = {}, token = {}, org = {}", domain, form.getToken(), form.getOrganisation());
            return "redirect:" + lpgUiUrl + "/profile/organisation";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Incorrect token for this organisation");
            return "redirect:/emailUpdated/enterToken/" + domain;
        } catch (NotEnoughSpaceAvailableException e) {
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Not enough spaces available on this token");
            return "redirect:/emailUpdated/enterToken/" + domain;
        } catch (BadRequestException e) {
            return "redirect:/login";
        } catch (UnableToAllocateAgencyTokenException e) {
            return "redirect:/login";
        }

    }

}
