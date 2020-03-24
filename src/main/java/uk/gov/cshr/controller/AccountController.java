package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.controller.form.UpdateEmailForm;
import uk.gov.cshr.controller.form.UpdatePasswordForm;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.service.EmailUpdateService;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.IdentityService;

import javax.validation.Valid;

@Slf4j
@Controller
@RequestMapping("/account")
public class AccountController {
    private final IdentityService identityService;
    private final EmailUpdateService emailUpdateService;
    private final String lpgUiUrl;

    public AccountController(IdentityService identityService, EmailUpdateService emailUpdateService, @Value("${lpg.uiUrl}") String lpgUiUrl) {
        this.identityService = identityService;
        this.emailUpdateService = emailUpdateService;
        this.lpgUiUrl = lpgUiUrl;
    }

    @GetMapping("/password")
    public String updatePasswordForm(Model model, @ModelAttribute UpdatePasswordForm form) {
        model.addAttribute("updatePasswordForm", form);
        return "account/updatePassword";
    }

    @GetMapping("/passwordUpdated")
    public String passwordUpdated() {
        return "account/passwordUpdated";
    }

    @PostMapping("/password")
    public String updatePassword(Model model, @Valid @ModelAttribute UpdatePasswordForm form, BindingResult bindingResult, Authentication authentication) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("updatePasswordForm", form);
            return "account/updatePassword";
        }

        identityService.updatePasswordAndRevokeTokens(((IdentityDetails) authentication.getPrincipal()).getIdentity(), form.getNewPassword());

        return "redirect:/account/passwordUpdated";
    }

    @GetMapping("/email")
    public String updateEmailForm(Model model, @ModelAttribute UpdateEmailForm form) {
        model.addAttribute("updateEmailForm", form);
        return "account/updateEmail";
    }

    @PostMapping("/email")
    public String sendEmailVerification(Model model, @Valid @ModelAttribute UpdateEmailForm form, BindingResult bindingResult, Authentication authentication) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("updateEmailForm", form);
            return "account/updateEmail";
        }

        if(identityService.checkEmailExists(form.getEmail())) {
            log.error("Email already taken: {}", form.getEmail());
            model.addAttribute("updateEmailForm", form);
            return "redirect:/account/email?emailAlreadyTaken=true";
        }

        emailUpdateService.saveEmailUpdateAndNotify(((IdentityDetails) authentication.getPrincipal()).getIdentity(), form.getEmail());

        return "account/emailVerificationSent";
    }

    @GetMapping("/email/verify/{code}")
    public String verifyEmail(@PathVariable String code, Authentication authentication) {
        Identity identity = ((IdentityDetails) authentication.getPrincipal()).getIdentity();

        if(!emailUpdateService.verifyCode(identity, code)) {
            log.error("Unable to verify email update code: {} {}", code, identity);
            return "redirect:/account/email?invalidCode=true";
        }

        try {
            log.info("email code verified:  updating email address");
            emailUpdateService.updateEmailAddress(identity, code);
        } catch (ResourceNotFoundException e) {
            log.error("Unable to update email: {} {}", code, identity);
            return "redirect:/account/email?invalidEmail=true";
        } catch (Exception e) {
            log.error("Unable to update email: {} {}", code, identity);
            return "redirect:/account/email?errorOccurred=true";
        }

        return "redirect:/account/emailUpdated";
    }

    @GetMapping("/emailUpdated")
    public String emailUpdated() {
        return "redirect:" + lpgUiUrl + "/sign-out";
    }
}
