package uk.gov.cshr.controller.emailUpdate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.web.support.WebTestUtils;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.cshr.config.SpringSecurityTestConfig;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.OrganisationalUnitDto;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.EmailUpdateService;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.IdentityService;

import javax.servlet.Filter;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SpringSecurityTestConfig.class)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmailUpdateControllerTest {

    private static final String NORMAL_TEST_USER_UID = "uid";

    private static final String VERIFY_EMAIL_URL = "/account/email/verify/1234567891234567";

    private static final String EXPECTED_CODE = "1234567891234567";

    private MockMvc mockMvc;

    @Value("${lpg.uiUrl}")
    private String lpgUiUrl;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter springSecurityFilterChain;

    @Autowired
    private UserDetailsService userDetailsService;

    @MockBean
    private EmailUpdateService emailUpdateService;

    @MockBean
    private IdentityService identityService;

    @MockBean
    private CsrsService csrsService;

    @Captor
    private ArgumentCaptor<Identity> identityArgumentCaptor;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity(springSecurityFilterChain))
                .build();

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void givenAValidRequest_whenEnterToken_thenShouldDisplayEnterTokenPageWithAllPossibleOrganisations() throws Exception {
        // given
        OrganisationalUnitDto[] organisations = new OrganisationalUnitDto[1];
        organisations[0] = new OrganisationalUnitDto();
        when(csrsService.getOrganisationalUnitsFormatted()).thenReturn(organisations);

        // only called with 2 flash attributes.  From redirect controller.
        mockMvc.perform(
                get("/emailUpdated/enterToken")
                        .with(CsrfRequestPostProcessor.csrf())
                        .flashAttr("uid", "myuid")
                        .flashAttr("domain", "mydomain"))
                .andExpect(status().isOk())
                .andExpect(model().size(4))
                .andExpect(model().attribute("organisations", organisations))
                .andExpect(model().attribute("domain", "mydomain"))
                .andExpect(model().attribute("uid", "myuid"))
                .andExpect(model().attributeExists("emailUpdatedRecentlyEnterTokenForm"))
                .andExpect(view().name("enterTokenSinceEmailUpdate"))
                .andDo(print());
    }

    @Test
    public void givenAValidTokenForm_whenCheckToken_thenShouldSubmitToken() throws Exception {
        OrganisationalUnitDto[] organisations = new OrganisationalUnitDto[1];
        organisations[0] = new OrganisationalUnitDto();

        when(csrsService.getOrganisationalUnitsFormatted()).thenReturn(organisations);

        mockMvc.perform(
                post("/emailUpdated/enterToken")
                        .with(CsrfRequestPostProcessor.csrf())
                 )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(lpgUiUrl))
                .andExpect(model().attribute("organisations", organisations))
                .andExpect(model().attribute("domain", "mydomain"))
                .andExpect(model().attributeExists("enterTokenForm"));
    }

    private Authentication prepareAuthentication(String userNameToAuthenticateWith) {
        IdentityDetails identityDetails = (IdentityDetails) userDetailsService.loadUserByUsername(userNameToAuthenticateWith);
        return new UsernamePasswordAuthenticationToken(identityDetails, identityDetails.getPassword(), identityDetails.getAuthorities());
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
