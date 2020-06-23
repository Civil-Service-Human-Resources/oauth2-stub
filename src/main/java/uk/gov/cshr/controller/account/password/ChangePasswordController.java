package uk.gov.cshr.controller.account.password;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.cshr.controller.form.UpdatePasswordForm;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.IdentityService;

import javax.validation.Valid;

@Slf4j
@Controller
@RequestMapping("/account/password")
public class ChangePasswordController {
    private final IdentityService identityService;

    public ChangePasswordController(IdentityService identityService) {
        this.identityService = identityService;
    }

    @GetMapping
    public String updatePasswordForm(Model model, @ModelAttribute UpdatePasswordForm form) {
        model.addAttribute("updatePasswordForm", form);
        return "account/updatePassword";
    }

    @PostMapping
    public String updatePassword(Model model, @Valid @ModelAttribute UpdatePasswordForm form, BindingResult bindingResult, Authentication authentication) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("updatePasswordForm", form);
            return "account/updatePassword";
        }

        identityService.updatePasswordAndRevokeTokens(((IdentityDetails) authentication.getPrincipal()).getIdentity(), form.getNewPassword());

        return "redirect:/account/passwordUpdated";
    }

    @GetMapping("/passwordUpdated")
    public String passwordUpdated() {
        return "account/passwordUpdated";
    }
}
