package uk.gov.cshr.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.cshr.controller.emailUpdate.RedirectController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RedirectControllerTest {

    @InjectMocks
    private RedirectController redirectController;

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setup() {

        // this must be called for the @Mock annotations above to be processed
        // and for the mock service to be injected into the controller under
        // test.
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(redirectController).build();
    }

    @Test
    public void givenAnInvalidEmailDomain_whenNotAValidEmailDomain_thenShouldRedirectToSignInPage() throws Exception {

        this.mockMvc.perform(get("/invalid"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/logout"))
                .andExpect(flash().attribute("status","Your organisation is unable to use this service. Please contact your line manager."));
    }
}
