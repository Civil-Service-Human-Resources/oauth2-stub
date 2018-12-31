package uk.gov.cshr.controller.signup;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.web.support.WebTestUtils;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import uk.gov.cshr.config.UserSecurityConfig;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.security.IdentityService;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest({SignupController.class, UserSecurityConfig.class})
@RunWith(SpringRunner.class)
public class SignupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InviteService inviteService;

    @MockBean
    private IdentityService identityService;

    @MockBean
    private InviteRepository inviteRepository;

    @MockBean
    private SignupFormValidator signupFormValidator;

    @Test
    public void shouldReturnCreateAccountForm() throws Exception {
        mockMvc.perform(
                get("/signup/request")
                        .with(CsrfRequestPostProcessor.csrf())
                )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"email\"")))
                .andExpect(content().string(containsString("id=\"confirmEmail\"")));
    }

    @Test
    public void shouldConfirmInviteSent() throws Exception {
        mockMvc.perform(
                post("/signup/request")
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("email", "user@domain.org")
                        .param("confirmEmail", "user@domain.org"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("We've sent you an email")))
                .andExpect(content().string(containsString("What happens next")))
                .andExpect(content().string(containsString("We have sent you an email with a link to continue creating your account.")));
    }

    @Test
    public void shouldFailValidationIfEmailAddressNotInWhitelist() throws Exception {
        mockMvc.perform(
                post("/signup/request")
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("email", "user@blah.org")
                        .param("confirmEmail", "user@blah.org"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Your organisation is unable to use this service. Please contact your line manager.")));
    }

    @Test
    public void shouldFailValidationIfEmailAddressIsNotValid() throws Exception {
        mockMvc.perform(
                post("/signup/request")
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("email", "userdomain.org")
                        .param("confirmEmail", "userdomain.org"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Email address is not valid")));
    }

    @Test
    public void shouldFailValidationIfEmailAddressesDoNotMatch() throws Exception {
        mockMvc.perform(
                post("/signup/request")
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("email", "user@domain.org")
                        .param("confirmEmail", "user1@domain.org"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Email addresses do not match")));
    }


    private static class CsrfRequestPostProcessor implements RequestPostProcessor {

        private boolean useInvalidToken = false;

        private boolean asHeader = false;

        @Override
        public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            CsrfTokenRepository repository = WebTestUtils.getCsrfTokenRepository(request);
            CsrfToken token = repository.generateToken(request);
            repository.saveToken(token, request, new MockHttpServletResponse());
            String tokenValue = useInvalidToken ? "invalid" + token.getToken() : token
                    .getToken();
            if (asHeader) {
                request.setAttribute(token.getHeaderName(), token);
            }
            else {
                request.setAttribute(token.getParameterName(), token);
            }
            return request;
        }

        public RequestPostProcessor invalidToken() {
            this.useInvalidToken = true;
            return this;
        }

        public RequestPostProcessor asHeader() {
            this.asHeader = true;
            return this;
        }

        public static CsrfRequestPostProcessor csrf() {
            return new CsrfRequestPostProcessor();
        }
    }
}