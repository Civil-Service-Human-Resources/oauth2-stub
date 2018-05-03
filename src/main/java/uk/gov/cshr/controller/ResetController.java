package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.ResetRepository;
import uk.gov.cshr.service.ResetService;

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
    public String signup(@PathVariable(value = "code") String code, RedirectAttributes redirectAttributes) {
        LOGGER.info("User on reset screen with code {}", code);

        if (!resetRepository.existsByCode(code)) {
            LOGGER.info("{} reset code does not exist", code);
            redirectAttributes.addFlashAttribute("status", "There was an error with your reset, please try again.");
            return "redirect:/reset";
        }

        return "user-passwordForm";
    }
}
