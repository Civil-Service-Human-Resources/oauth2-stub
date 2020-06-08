package uk.gov.cshr.controller.cshr;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.cshr.config.ResourceServerConfigurationTestConfig;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.utils.MockMVCFilterOverrider;

import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(ResourceServerConfigurationTestConfig.class)
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ListIdentitiesControllerTest {
    //ListIdentitiesControllerTest
    private static final String CSRS_CLIENT_ID = "123";

    private static final String EMAIL = "test@example.org";
    private static final String PASSWORD = "password123";
    private static final String IDENTITY_UID = "abc123";

    @MockBean
    private IdentityRepository identityRepository;

    @Autowired
    private WebApplicationContext webapp;

    private MockMvc mockMvc;

    @Before
    public void setup() throws IllegalAccessException {
        mockMvc = MockMvcBuilders.webAppContextSetup(webapp)
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

        MockMVCFilterOverrider.overrideFilterOf(mockMvc, "PatternMappingFilterProxy");
    }

    @Test
    public void shouldReturnIdentityAgencyDTOSuccessfully() throws Exception {
        when(identityRepository.findFirstByUid(eq(IDENTITY_UID))).thenReturn(Optional.of(createIdentity()));

        mockMvc.perform(get("/api/identity/agency/" + IDENTITY_UID)
                .with(user("theclient").authorities(() -> "CLIENT")))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.uid", CoreMatchers.is(IDENTITY_UID)))
                .andExpect(jsonPath("$.agencyTokenUid", CoreMatchers.is("456")));

        verify(identityRepository, times(1)).findFirstByUid(eq(IDENTITY_UID));
    }

    @Test
    public void shouldReturnNotFoundWhenIdentityNotFound() throws Exception {
        when(identityRepository.findFirstByUid(eq(IDENTITY_UID))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/identity/agency/" + IDENTITY_UID)
                .with(user("theclient").authorities(() -> "CLIENT")))
                .andExpect(status().isNotFound());

        verify(identityRepository, times(1)).findFirstByUid(eq(IDENTITY_UID));
    }

    private Identity createIdentity() {
        Identity identity = new Identity(IDENTITY_UID, EMAIL, PASSWORD, true, false, null, Instant.now(), false, false);
        identity.setAgencyTokenUid("456");
        return identity;
    }

}


