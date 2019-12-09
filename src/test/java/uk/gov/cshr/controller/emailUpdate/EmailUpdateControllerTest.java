package uk.gov.cshr.controller.emailUpdate;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.service.CsrsService;

import javax.transaction.Transactional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional*/
@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@WithMockUser(username = "user")
public class EmailUpdateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CsrsService csrsService;

    // TODO - FIX
    @Ignore
    @Test
    public void whenEnterTokenGet_shouldDisplayEmailUpdatedNowEnterTokenPage() throws Exception {
        OrganisationalUnitDto[] organisations = new OrganisationalUnitDto[1];
        organisations[0] = new OrganisationalUnitDto();

        when(csrsService.getOrganisationalUnitsFormatted()).thenReturn(organisations);

        mockMvc.perform(
                get("/emailUpdated/enterToken/mydomain/myuid")
                       .with(csrf()))
        //)
                .andExpect(status().isOk())
                .andExpect(model().attribute("organisations", organisations))
                .andExpect(model().attribute("domain", "mydomain"))
                .andExpect(model().attributeExists("newEnterTokenForm"))
                .andExpect(view().name("enterTokenSinceEmailUpdate"));
                //.andExpect(content().string("enterTokenSinceEmailUpdate"));
    }

    // TODO - FIX
    @Ignore
    @Test
    public void whenEnterTokenPost_shouldSubmitToken() throws Exception {
        OrganisationalUnitDto[] organisations = new OrganisationalUnitDto[1];
        organisations[0] = new OrganisationalUnitDto();

        when(csrsService.getOrganisationalUnitsFormatted()).thenReturn(organisations);

        mockMvc.perform(
                post("/emailUpdated/enterToken/mydomain/myuid")
                      .with(csrf()))
       // )
                .andExpect(status().isOk())
                .andExpect(model().attribute("organisations", organisations))
                .andExpect(model().attribute("domain", "mydomain"))
                .andExpect(model().attributeExists("enterTokenForm"))
                .andExpect(content().string("enterTokenSinceEmailUpdate"));
    }

}
