package uk.gov.cshr.controller.signup;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.support.WebTestUtils;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.security.IdentityService;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@WithMockUser(username = "user")
public class SignupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InviteService inviteService;

    @MockBean
    private IdentityService identityService;

    @MockBean
    private CsrsService csrsService;

    @MockBean(name = "inviteRepository")
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
        String email = "user@domain.com";
        String domain = "domain.com";

        when(inviteRepository.existsByForEmailAndStatus(email, InviteStatus.PENDING)).thenReturn(false);
        when(identityService.existsByEmail(email)).thenReturn(false);
        when(identityService.getDomainFromEmailAddress(email)).thenReturn(domain);
        when(identityService.isWhitelistedDomain(domain)).thenReturn(true);

        mockMvc.perform(
                post("/signup/request")
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("email", email)
                        .param("confirmEmail", email))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("We've sent you an email")))
                .andExpect(content().string(containsString("What happens next")))
                .andExpect(content().string(containsString("We have sent you an email with a link to <strong>continue creating your account</strong>.")));
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
    public void shouldRedirectToSignupIfUserHasAlreadyBeenInvited() throws Exception {
        String email = "user@domain.com";

        when(inviteRepository.existsByForEmailAndStatus(email, InviteStatus.PENDING)).thenReturn(true);

        mockMvc.perform(
                post("/signup/request")
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("email", email)
                        .param("confirmEmail", email))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void shouldRedirectToSignupIfUserAlreadyExists() throws Exception {
        String email = "user@domain.com";

        when(inviteRepository.existsByForEmailAndStatus(email, InviteStatus.PENDING)).thenReturn(false);
        when(identityService.existsByEmail(email)).thenReturn(true);

        mockMvc.perform(
                post("/signup/request")
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("email", email)
                        .param("confirmEmail", email))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void shouldConfirmInviteSentIfAgencyTokenEmail() throws Exception {
        String email = "user@domain.com";
        String domain = "domain.com";
        AgencyToken[] agencyTokens = new AgencyToken[]{new AgencyToken()};

        when(inviteRepository.existsByForEmailAndStatus(email, InviteStatus.PENDING)).thenReturn(false);
        when(identityService.existsByEmail(email)).thenReturn(false);
        when(identityService.getDomainFromEmailAddress(email)).thenReturn(domain);
        when(identityService.isWhitelistedDomain(domain)).thenReturn(false);
        when(csrsService.getAgencyTokensForDomain(domain)).thenReturn(agencyTokens);

        mockMvc.perform(
                post("/signup/request")
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("email", email)
                        .param("confirmEmail", email))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("We've sent you an email")))
                .andExpect(content().string(containsString("What happens next")))
                .andExpect(content().string(containsString("We have sent you an email with a link to <strong>continue creating your account</strong>.")));

        Mockito.verify(inviteService).sendSelfSignupInvite(email, false);
    }

    @Test
    public void shouldNotSendInviteIfNotWhitelistedAndNotAgencyTokenEmail() throws Exception {
        String email = "user@domain.com";
        String domain = "domain.com";
        AgencyToken[] agencyTokens = new AgencyToken[]{};

        when(inviteRepository.existsByForEmailAndStatus(email, InviteStatus.PENDING)).thenReturn(false);
        when(identityService.existsByEmail(email)).thenReturn(false);
        when(identityService.getDomainFromEmailAddress(email)).thenReturn(domain);
        when(identityService.isWhitelistedDomain(domain)).thenReturn(false);
        when(csrsService.getAgencyTokensForDomain(domain)).thenReturn(agencyTokens);

        mockMvc.perform(
                post("/signup/request")
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("email", email)
                        .param("confirmEmail", email))
                .andExpect(status().is3xxRedirection());
    }


    @Test
    public void shouldSendToLoginIfSignupCodeNotValid() throws Exception {
        String code = "abc123";

        when(inviteService.isInviteValid(code)).thenReturn(false);

        mockMvc.perform(
                get("/signup/" + code)
                        .with(CsrfRequestPostProcessor.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void shouldRedirectToTokenIfInviteNotAuthorised() throws Exception {
        String code = "abc123";
        Invite invite = new Invite();
        invite.setAuthorisedInvite(false);

        when(inviteService.isInviteValid(code)).thenReturn(true);
        when(inviteRepository.findByCode(code)).thenReturn(invite);

        mockMvc.perform(
                get("/signup/" + code)
                        .with(CsrfRequestPostProcessor.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup/enterToken/" + code));
    }

    @Test
    public void shouldReturnSignupIfTokenAuthorised() throws Exception {
        String code = "abc123";
        Invite invite = new Invite();
        invite.setAuthorisedInvite(true);

        when(inviteService.isInviteValid(code)).thenReturn(true);
        when(inviteRepository.findByCode(code)).thenReturn(invite);
        when(signupFormValidator.supports(any())).thenReturn(true);

        mockMvc.perform(
                get("/signup/" + code)
                        .with(CsrfRequestPostProcessor.csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"));
    }

//    @Test
//    public void shouldNotPostIfPasswordsDifferent() throws Exception {
//        String code = "abc123";
//        String password = "password";
//        String differentPassword = "differentPassword";
//
//        when(signupFormValidator.supports(any())).thenReturn(true);
//
//        mockMvc.perform(
//                post("/signup/" + code)
//                        .with(CsrfRequestPostProcessor.csrf())
//                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
//                        .param("password", password)
//                        .param("confirmPassword", differentPassword))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(view().name("signup"));
//    }


    @Test
    public void shouldRedirectToLoginIfInviteNotValid() throws Exception {
        String code = "abc123";
        String password = "password";

        when(inviteService.isInviteValid(code)).thenReturn(false);
        when(signupFormValidator.supports(any())).thenReturn(true);

        mockMvc.perform(
                post("/signup/" + code)
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("password", password)
                        .param("confirmPassword", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void shouldRedirectToEnterTokenIfInviteNotAuthorised() throws Exception {
        String code = "abc123";
        String password = "password";
        Invite invite = new Invite();
        invite.setAuthorisedInvite(false);

        when(inviteService.isInviteValid(code)).thenReturn(true);
        when(inviteRepository.findByCode(code)).thenReturn(invite);
        when(signupFormValidator.supports(any())).thenReturn(true);

        mockMvc.perform(
                post("/signup/" + code)
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("password", password)
                        .param("confirmPassword", password))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup/enterToken/" + code));
    }

    @Test
    public void shouldReturnSignupSuccessIfInviteAuthorised() throws Exception {
        String code = "abc123";
        String password = "password";
        Invite invite = new Invite();
        invite.setAuthorisedInvite(true);

        when(inviteService.isInviteValid(code)).thenReturn(true);
        when(inviteRepository.findByCode(code)).thenReturn(invite);
        when(signupFormValidator.supports(any())).thenReturn(true);

        mockMvc.perform(
                post("/signup/" + code)
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("password", password)
                        .param("confirmPassword", password))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("signupSuccess"));
    }

    @Test
    public void shouldRedirectToLoginIfInviteNotValidFromToken() throws Exception {
        String code = "abc123";

        when(inviteService.isInviteValid(code)).thenReturn(false);

        mockMvc.perform(
                get("/signup/enterToken/" + code)
                        .with(CsrfRequestPostProcessor.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void shouldReturnEnterToken() throws Exception {
        String code = "abc123";
        String email = "test@example.com";

        OrganisationalUnitDto[] organisationalUnits = new OrganisationalUnitDto[]{new OrganisationalUnitDto()};

        Invite invite = new Invite();
        invite.setForEmail(email);
        invite.setAuthorisedInvite(false);

        when(inviteService.isInviteValid(code)).thenReturn(true);
        when(inviteRepository.findByCode(code)).thenReturn(invite);

        when(csrsService.getOrganisationalUnitsFormatted()).thenReturn(organisationalUnits);

        mockMvc.perform(
                get("/signup/enterToken/" + code)
                        .with(CsrfRequestPostProcessor.csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("enterToken"));
    }

    @Test
    public void shouldRedirectOnEnterTokenIfTokenAuth() throws Exception {
        String code = "abc123";
        String email = "test@example.com";

        Invite invite = new Invite();
        invite.setForEmail(email);
        invite.setAuthorisedInvite(true);

        OrganisationalUnitDto[] organisationalUnits = new OrganisationalUnitDto[]{new OrganisationalUnitDto()};

        when(inviteService.isInviteValid(code)).thenReturn(true);
        when(inviteRepository.findByCode(code)).thenReturn(invite);

        when(csrsService.getOrganisationalUnitsFormatted()).thenReturn(organisationalUnits);

        mockMvc.perform(
                get("/signup/enterToken/" + code)
                        .with(CsrfRequestPostProcessor.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup/" + code));
    }


    @Test
    public void shouldRedirectToLoginIfTokenInviteInvalid() throws Exception {
        String code = "abc123";
        String organisation = "org";
        String token = "token123";

        when(inviteService.isInviteValid(code)).thenReturn(false);

        mockMvc.perform(
                post("/signup/enterToken/" + code)
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("organisation", organisation)
                        .param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void shouldRedirectToSignupIfInviteValid() throws Exception {
        String code = "abc123";
        String organisation = "org";
        String token = "token123";
        String email = "test@example.com";
        String domain = "example.com";

        Invite invite = new Invite();
        invite.setForEmail(email);
        invite.setAuthorisedInvite(true);

        AgencyToken agencyToken = new AgencyToken();
        Optional<AgencyToken> optionalAgencyToken = Optional.of(agencyToken);

        when(inviteService.isInviteValid(code)).thenReturn(true);
        when(inviteRepository.findByCode(code)).thenReturn(invite);
        when(identityService.getDomainFromEmailAddress(email)).thenReturn(domain);
        when(csrsService.getAgencyTokenForDomainTokenOrganisation(domain, token, organisation)).thenReturn(optionalAgencyToken);

        mockMvc.perform(
                post("/signup/enterToken/" + code)
                        .with(CsrfRequestPostProcessor.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("organisation", organisation)
                        .param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup/" + code));
    }


    @Test
    public void shouldRedirectToEnterTokenIfInviteValid() throws Exception {
        String code = "abc123";
        String organisation = "org";
        String token = "token123";
        String email = "test@example.com";
        String domain = "example.com";

        String password = "Strongpassword123";
        String confirmPassword = "Strongpassword123";

        Invite invite = new Invite();
        invite.setForEmail(email);
        invite.setAuthorisedInvite(false);

        when(inviteService.isInviteValid(code)).thenReturn(true);
        when(inviteRepository.findByCode(code)).thenReturn(invite);
        when(identityService.getDomainFromEmailAddress(email)).thenReturn(domain);
        when(signupFormValidator.supports(any())).thenReturn(true);


        mockMvc.perform(
                post("/signup/" + code)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("password", password)
                        .param("confirmPassword", confirmPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup/enterToken/" + code));
    }

    private static class CsrfRequestPostProcessor implements RequestPostProcessor {

        private boolean useInvalidToken = false;

        private boolean asHeader = false;

        public static CsrfRequestPostProcessor csrf() {
            return new CsrfRequestPostProcessor();
        }

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
    }
}