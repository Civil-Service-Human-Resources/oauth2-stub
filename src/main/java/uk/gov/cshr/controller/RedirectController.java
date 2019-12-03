package uk.gov.cshr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class RedirectController {

    private static final String STATUS_ATTRIBUTE = "status";

    @GetMapping("/invalid")
    public RedirectView notAValidEmailDomain(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        // TODO - ASK WHAT SHOULD HAPPEN IN THIS SCENARIO
        redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Your organisation is unable to use this service. Please contact your line manager.");
        return new RedirectView("/logout");
    }

}
