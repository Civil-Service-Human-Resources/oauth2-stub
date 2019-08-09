package uk.gov.cshr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.service.CsrsService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Controller
public class TestController {

    private CsrsService csrsService;

    public TestController(CsrsService csrsService) {
        this.csrsService = csrsService;
    }

    // e.g http://localhost:8080/test?domain=example.com&token=token123&code=co
    @RequestMapping("/test")
    public String login(@RequestParam String domain, @RequestParam String token, @RequestParam String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<AgencyToken> agencyTokenForDomainTokenOrganisation = csrsService.getAgencyTokenForDomainTokenOrganisation(domain, token, code);

        return "login";
    }

    @RequestMapping("/test2")
    public String orgUnits() throws IOException {
        OrganisationalUnitDto[] orgUnitsFormatted = csrsService.getOrganisationalUnitsFormatted();

        return "login";
    }
}