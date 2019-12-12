package uk.gov.cshr.controller.emailUpdate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${lpg.uiUrl}")
    private String lpgUiUrl;

    @Test
    public void givenAnInvalidEmailDomain_whenNotAValidEmailDomain_thenShouldRedirectToSignInPage() throws Exception {

        this.mockMvc.perform(get("/invalid"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/logout"))
                .andExpect(flash().attribute("status","Your organisation is unable to use this service. Please contact your line manager."));
    }

    @Test
    public void whenGoToUIHomePage_shouldRedirectToTheLPGUIHomePage() throws Exception {
        mockMvc.perform(
                get("/redirectToUIHomePage"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(lpgUiUrl))
                .andExpect(model().size(0));
    }

    @Test
    public void whenGoToChangeOrgPage_shouldRedirectToTheEnterOrganisationPage() throws Exception {
        String expectedRedirectToUrl = "/organisation/enterOrganisation";

        mockMvc.perform(
                get("/redirectToChangeOrgPage/mydomain/myuid"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectToUrl))
                .andExpect(flash().attribute("domain","mydomain"))
                .andExpect(flash().attribute("uid","myuid"));
    }

    @Test
    public void whenGoToEnterTokenPage_shouldRedirectToTheEnterTokenPage() throws Exception {
        String expectedRedirectToUrl = "/emailUpdated/enterToken";

        mockMvc.perform(
                get("/redirectToEnterTokenPage/mydomain/myuid"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectToUrl))
                .andExpect(flash().attribute("domain","mydomain"))
                .andExpect(flash().attribute("uid","myuid"));
    }

}
