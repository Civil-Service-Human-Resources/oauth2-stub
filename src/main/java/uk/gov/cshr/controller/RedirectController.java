package uk.gov.cshr.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

@Controller
public class RedirectController {

    private static final String STATUS_ATTRIBUTE = "status";

    @Value("${lpg.uiUrl}")
    private String lpgUiUrl;

    @Value("${lpg.changeOrgUrl}")
    private String lpgChangeOrgUrl;

    @GetMapping("/invalid")
    public RedirectView notAValidEmailDomain(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        // TODO - ASK WHAT SHOULD HAPPEN IN THIS SCENARIO
        redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, "Your organisation is unable to use this service. Please contact your line manager.");
        return new RedirectView("/logout");
    }

    @RequestMapping("/redirectToUIHomePage")
    public RedirectView goToUIHomePage(Model model) {
        model.asMap().clear();
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(lpgUiUrl);
        return redirectView;
    }

    @RequestMapping("/redirectToUIChangeOrgPage")
    public ResponseEntity<Object> redirectToExternalUrl() throws URISyntaxException {
        URI ui = new URI(lpgUiUrl);
        UriComponents uriComponents = UriComponentsBuilder.newInstance().uri(ui)
                .path("/").query("updateOrg={keyword}").buildAndExpand("true");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(uriComponents.toUri());
        return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
    }

}
