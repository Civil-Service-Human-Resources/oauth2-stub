package uk.gov.cshr.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.ResetRepository;
import uk.gov.cshr.service.ResetService;

import javax.transaction.Transactional;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ResetControllerTest {

    public static final String EMAIL = "test@example.com";
    public static final String CODE = "abc123";


    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private ResetController resetController;

    @Mock
    private IdentityRepository identityRepository;

    @Mock
    private ResetRepository resetRepository;

    @Mock
    private ResetService resetService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders.standaloneSetup(resetController).build();
    }

    @Test
    public void shouldLoadResetSuccessfully() throws Exception {
        this.mockMvc.perform(get("/reset"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(forwardedUrl("user-reset"))
                .andDo(print());
    }

    @Test
    public void shouldLoadCheckEmailIfUserNameExists() throws Exception {
        doNothing().when(resetService).createNewResetForEmail(EMAIL);
        when(identityRepository.existsByEmail(EMAIL)).thenReturn(true);

        this.mockMvc.perform(post("/reset")
                .param("email", EMAIL))
                .andExpect(forwardedUrl("user-checkEmail"));
    }

    @Test
    public void shouldLoadRedirectIfUserNameDoesntExist() throws Exception {
        when(identityRepository.existsByEmail(EMAIL)).thenReturn(false);

        this.mockMvc.perform(post("/reset")
                .param("email", EMAIL))
                .andExpect(redirectedUrl("/reset"));
    }

    @Test
    public void shouldLoadSignupWithCode() throws Exception {
        when(resetRepository.existsByCode(CODE)).thenReturn(true);

        this.mockMvc.perform(get("/reset/abc123"))
                .andExpect(forwardedUrl("user-passwordForm"));

    }

    @Test
    public void shouldRedirectSignupIfInvalidCode() throws Exception {
        when(resetRepository.existsByCode(CODE)).thenReturn(false);

        this.mockMvc.perform(get("/reset/abc123"))
                .andExpect(redirectedUrl("/reset"));

    }
}