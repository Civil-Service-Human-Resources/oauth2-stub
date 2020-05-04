package uk.gov.cshr.controller.emailUpdate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class RedirectController {

    private static final String STATUS_ATTRIBUTE = "status";

    @Value("${lpg.uiUrl}")
    private String lpgUiUrl;

    @GetMapping("/invalid")
    public RedirectView notAValidEmailDomain(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        // TODO - ASK WHAT SHOULD HAPPEN IN THIS SCENARIO - ASSUME LOG OUT WITH ERROR MESSAGE FOR NOW
        redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Your organisation is unable to use this service. Please contact your line manager.");
        return new RedirectView("/logout");
    }

    @RequestMapping("/redirectToEnterTokenPage/{domain}/{uid}")
    public RedirectView goToEnterTokenSinceEmailUpdatePage(Model model, RedirectAttributes redirectAttributes, @PathVariable String domain, @PathVariable String uid) {
        /*
         * This redirect is required in order to pass the domain and uid to the enter token page,
         * so that the associated agency token validation is possible.
         */
        redirectAttributes.addFlashAttribute("domain", domain);
        redirectAttributes.addFlashAttribute("uid", uid);
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/emailUpdated/enterToken");
        return redirectView;
    }

}
