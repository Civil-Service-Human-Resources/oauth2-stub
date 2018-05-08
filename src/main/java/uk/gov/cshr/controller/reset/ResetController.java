package uk.gov.cshr.controller.reset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.controller.signup.SignupForm;
import uk.gov.cshr.controller.signup.SignupFormValidator;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reset;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.ResetRepository;
import uk.gov.cshr.service.ResetService;
import uk.gov.service.notify.NotificationClientException;

import javax.validation.Valid;

@Controller
@RequestMapping("/reset")
public class ResetController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResetController.class);

    @Autowired
    private ResetService resetService;

    @Autowired
    private ResetRepository resetRepository;

    @Autowired
    private IdentityRepository identityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SignupFormValidator signupFormValidator;

    @GetMapping
    public String reset() {
        return "user-reset";
    }

    @PostMapping
    public String requestReset(@RequestParam(value = "email") String email) throws Exception {
        LOGGER.info("Requesting reset for {} ", email);

        resetService.notifyForResetRequest(email);

        return "user-checkEmail";
    }

    @GetMapping("/{code}")
    public String loadResetForm(@PathVariable(value = "code") String code, RedirectAttributes redirectAttributes, Model model) {
        LOGGER.info("User on reset screen with code {}", code);

        if (!resetRepository.existsByCode(code)) {
            LOGGER.info("{} reset code does not exist", code);
            redirectAttributes.addFlashAttribute("status", "There was an error with your reset, please try again.");
            return "redirect:/login";
        }

        ResetForm resetForm = new ResetForm();
        resetForm.setCode(code);

        model.addAttribute("resetPasswordForm", resetForm);

        return "user-passwordForm";
    }

    @PostMapping("/{code}")
    public String resetPassword(@PathVariable(value = "code") String code, @ModelAttribute @Valid ResetForm resetForm) throws NotificationClientException {
        Reset reset = resetRepository.findByCode(code);

        if (resetService.isResetExpired(reset)) {
            LOGGER.info("Reset has expired for code {}", code);
            return "redirect:/reset";
        }

        if (reset == null || reset.getEmail() == null) {
            LOGGER.info("Reset does not exist with code {}", code);
            return "redirect:/reset";
        }

        Identity identity = identityRepository.findFirstByActiveTrueAndEmailEquals(reset.getEmail());

        if (!reset.getEmail().equals(identity.getEmail())) {
            LOGGER.info("Reset email and Identity email do not match for code {}, and email", code, identity.getEmail());
            return "redirect:/reset";
        }

        if (identity == null || identity.getEmail() == null) {
            LOGGER.info("Identity does not exist with reset code {}", code);
            return "redirect:/reset";
        }

        identity.setPassword(passwordEncoder.encode(resetForm.getPassword()));

        identityRepository.save(identity);

        resetService.notifyOfSuccessfulReset(reset);

        LOGGER.info("Password reset successfully for {}", identity.getEmail());

        return "user-passwordReset";
    }

    @InitBinder
    public void setupValidation(WebDataBinder binder) {
        if (binder.getTarget() instanceof SignupForm) {
            binder.addValidators(signupFormValidator);
        }
    }
}
