package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reset;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.ResetRepository;
import uk.gov.cshr.service.ResetService;
import uk.gov.service.notify.NotificationClientException;

import javax.validation.Valid;
import java.util.Optional;

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

    @GetMapping
    public String reset() {
        return "user-reset";
    }

    @PostMapping
    public String reset(@RequestParam(value = "email") String email, RedirectAttributes redirectAttributes) throws Exception {
        LOGGER.info("Resetting {} ", email);

        if (!identityRepository.existsByEmail(email)) {
            LOGGER.info("{} tried to reset but does not exist", email);
            redirectAttributes.addFlashAttribute("status", email + " does not exist");
            return "redirect:/reset";
        }

        resetService.createNewResetForEmail(email);

        return "user-checkEmail";
    }

    @GetMapping("/{code}")
    public String signup(@PathVariable(value = "code") String code, RedirectAttributes redirectAttributes, Model model) {
        LOGGER.info("User on reset screen with code {}", code);

        if (!resetRepository.existsByCode(code)) {
            LOGGER.info("{} reset code does not exist", code);
            redirectAttributes.addFlashAttribute("status", "There was an error with your reset, please try again.");
            return "redirect:/reset";
        }

        Reset reset = resetRepository.findByCode(code);

        if (reset == null || reset.getEmail() == null) {
            LOGGER.info("Reset does not exist with code {}", code);
            return "redirect:/reset";
        }

        Identity identity = identityRepository.findFirstByActiveTrueAndEmailEquals(reset.getEmail());

        ResetForm resetForm = new ResetForm();
        resetForm.setCode(code);

        model.addAttribute("resetPasswordForm", resetForm);
        model.addAttribute("uid", identity.getUid());

        return "user-passwordForm";
    }

    @PostMapping("/{uid}")
    public String resetPassword(@PathVariable(value = "uid") String uid, @ModelAttribute @Valid ResetForm resetForm) throws NotificationClientException {
        String code = resetForm.getCode();

        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);

        if (!optionalIdentity.isPresent()) {
            return "redirect:/reset";
        }

        Identity identity = optionalIdentity.get();

        if (identity == null || identity.getEmail() == null) {
            LOGGER.info("Identity does not exist with reset code {}", code);
            return "redirect:/reset";
        }

        Reset reset = resetRepository.findByCode(code);

        if (reset == null || reset.getEmail() == null) {
            LOGGER.info("Reset does not exist with code {}", code);
            return "redirect:/reset";
        }

        if (!reset.getEmail().equals(identity.getEmail())) {
            LOGGER.info("Reset email and Identity email do not match for code {}, and email", code, identity.getEmail());
            return "redirect:/reset";
        }

        identity.setPassword(passwordEncoder.encode(resetForm.getPassword()));

        identityRepository.save(identity);

        resetService.createSuccessfulPasswordResetForEmail(reset);

        LOGGER.info("Password reset successfully for {}", identity.getEmail());

        return "redirect:/login";
    }
}
