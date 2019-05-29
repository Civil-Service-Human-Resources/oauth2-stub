package uk.gov.cshr.controller.reset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reset;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.ResetRepository;
import uk.gov.cshr.service.ResetService;
import uk.gov.cshr.service.security.IdentityService;
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
    private IdentityService identityService;

    @Autowired
    private ResetFormValidator resetFormValidator;

    @Value("${lpg.uiUrl}")
    private String lpgUiUrl;

    @GetMapping
    public String reset() {
        return "reset/requestReset";
    }

    @PostMapping
    public String requestReset(@RequestParam(value = "email") String email) throws Exception {
        LOGGER.info("Requesting reset for {} ", email);

        if (identityRepository.existsByEmail(email)) {
            resetService.notifyForResetRequest(email);
        }

        return "reset/checkEmail";
    }

    @GetMapping("/{code}")
    public String loadResetForm(@PathVariable(value = "code") String code, RedirectAttributes redirectAttributes, Model model) {
        LOGGER.info("User on reset screen with code {}", code);

        checkResetCodeExists(code);

        Reset reset = resetRepository.findByCode(code);

        if (isResetInvalid(reset)) {
            return "redirect:/reset";
        }

        ResetForm resetForm = new ResetForm();
        resetForm.setCode(code);

        model.addAttribute("resetForm", resetForm);

        return "reset/passwordForm";
    }


    @PostMapping("/{code}")
    public String resetPassword(@PathVariable(value = "code") String code, @ModelAttribute @Valid ResetForm resetForm, BindingResult bindingResult, Model model) throws NotificationClientException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("resetForm", resetForm);
            return "reset/passwordForm";
        }

        checkResetCodeExists(code);

        Reset reset = resetRepository.findByCode(code);

        if (isResetInvalid(reset)) {
            return "redirect:/reset";
        }

        if (reset == null || reset.getEmail() == null) {
            LOGGER.info("Reset does not exist with code {}", code);
            throw new ResourceNotFoundException();
        }

        Identity identity = identityRepository.findFirstByEmailEquals(reset.getEmail());

        if (identity == null || identity.getEmail() == null) {
            LOGGER.info("Identity does not exist with reset code {}", code);
            throw new ResourceNotFoundException();
        }

        identityService.updatePassword(identity, resetForm.getPassword());

        resetService.notifyOfSuccessfulReset(reset);

        LOGGER.info("Password reset successfully for {}", identity.getEmail());

        model.addAttribute("lpgUiUrl", lpgUiUrl);

        return "reset/passwordReset";
    }


    @InitBinder
    public void resetValidation(WebDataBinder binder) {
        if (binder.getTarget() instanceof ResetForm) {
            binder.addValidators(resetFormValidator);
        }
    }

    private boolean isResetInvalid(Reset reset) {
        if (resetService.isResetExpired(reset) || !resetService.isResetPending(reset)) {
            LOGGER.info("Reset is not valid for code {}", reset.getCode());
            return true;
        }
        return false;
    }

    private void checkResetCodeExists(String code) {
        if (!resetRepository.existsByCode(code)) {
            LOGGER.info("Reset code does not exist {}", code);
            throw new ResourceNotFoundException();
        }
    }
}
