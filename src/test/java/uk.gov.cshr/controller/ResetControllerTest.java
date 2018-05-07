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
import uk.gov.cshr.controller.reset.ResetController;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reset;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.ResetRepository;
import uk.gov.cshr.service.ResetService;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

    private static final String EMAIL = "test@example.com";
    private static final String CODE = "abc123";
    private static final String UID = "uid123";
    private static final Boolean ACTIVE = true;
    private static final String PASSWORD = "password";
    private static final Set<Role> ROLES = new HashSet();

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
    public void shouldRedirectSignupIfInvalidCode() throws Exception {
        when(resetRepository.existsByCode(CODE)).thenReturn(false);

        this.mockMvc.perform(get("/reset/" + CODE))
                .andExpect(redirectedUrl("/reset"));

    }

    @Test
    public void shouldForwardSignupIfResetExists() throws Exception {
        when(resetRepository.existsByCode(CODE)).thenReturn(true);

        Reset reset = new Reset();
        reset.setEmail(EMAIL);
        reset.setCode(CODE);

        when(resetRepository.findByCode(CODE)).thenReturn(reset);

        Identity identity = new Identity(UID, EMAIL, PASSWORD, ACTIVE, ROLES);
        when(identityRepository.findFirstByActiveTrueAndEmailEquals(EMAIL)).thenReturn(identity);

        this.mockMvc.perform(get("/reset/" + CODE))
                .andExpect(forwardedUrl("user-passwordForm"));
    }

    @Test
    public void shouldRedirectSignupIfResetDoesntExists() throws Exception {
        when(resetRepository.existsByCode(CODE)).thenReturn(true);

        when(resetRepository.findByCode(CODE)).thenReturn(null);

        this.mockMvc.perform(get("/reset/" + CODE))
                .andExpect(redirectedUrl("/reset"));
    }

    @Test
    public void shouldRedirectSignupIfResetExistsButEmailDoesnt() throws Exception {
        when(resetRepository.existsByCode(CODE)).thenReturn(true);

        Reset reset = new Reset();
        reset.setEmail(null); //setting email to null
        reset.setCode(CODE);

        when(resetRepository.findByCode(CODE)).thenReturn(reset);

        this.mockMvc.perform(get("/reset/" + CODE))
                .andExpect(redirectedUrl("/reset"));
    }

    @Test
    public void shouldRedirectIfIdentityNotPresent() throws Exception {
        Optional<Identity> optionalIdentity = Optional.empty();

        when(identityRepository.findFirstByUid(UID)).thenReturn(optionalIdentity);

        this.mockMvc.perform(post("/reset/" + CODE)
                .param("code", CODE)
                .param("uid", UID))
                .andExpect(redirectedUrl("/reset"));
    }

    @Test
    public void shouldRedirectIfIdentityIsNull() throws Exception {
        Identity identity = new Identity(UID, EMAIL, PASSWORD, ACTIVE, ROLES);

        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.of(identity));

        Reset reset = new Reset();
        reset.setCode(CODE);
        reset.setEmail(EMAIL);

        when(resetRepository.findByCode(CODE)).thenReturn(reset);

        this.mockMvc.perform(post("/reset/" + CODE)
                .param("code", CODE)
                .param("uid", UID))
                .andExpect(redirectedUrl("/reset"));
    }

    @Test
    public void shouldRedirectIfResetAndIdentityDoNotMatch() throws Exception {
        Identity identity = new Identity(UID, EMAIL, PASSWORD, ACTIVE, ROLES);

        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.of(identity));

        Reset reset = new Reset();
        reset.setCode(CODE);
        reset.setEmail("differentemail@example.com");

        when(resetRepository.findByCode(CODE)).thenReturn(reset);

        this.mockMvc.perform(post("/reset/" + CODE)
                .param("code", CODE)
                .param("uid", UID))
                .andExpect(redirectedUrl("/reset"));
    }

}