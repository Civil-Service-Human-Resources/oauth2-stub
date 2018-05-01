package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.service.ResetService;

@Controller
@RequestMapping("/reset")
public class ResetController {
    private static final Logger LOGGER = LoggerFactory.getLogger(InviteController.class);

    @Autowired
    private ResetService resetService;

    @GetMapping
    public String reset() {
        return "reset";
    }

    @PostMapping
    public String reset(@RequestParam(value = "email") String email) throws Exception {
        LOGGER.info("Resetting {} ", email);

        resetService.createNewResetForEmail(email);

        return "resetSuccess";
    }

    @GetMapping("/{code}")
    public String signup(@PathVariable(value = "code") String code) {
        LOGGER.info("User resetting password screen with code {}", code);

        return "login";
    }
}
