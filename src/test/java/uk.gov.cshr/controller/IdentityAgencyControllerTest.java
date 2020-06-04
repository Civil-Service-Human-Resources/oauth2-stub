package uk.gov.cshr.controller;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.utils.MockMVCFilterOverrider;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class IdentityAgencyControllerTest {

    private static final String EMAIL = "test@example.org";
    private static final String PASSWORD = "password123";
    private static final String IDENTITY_UID = "abc123";

    @MockBean
    private IdentityRepository identityRepository;

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setup() throws IllegalAccessException {
        MockMVCFilterOverrider.overrideFilterOf(mockMvc, "PatternMappingFilterProxy");
    }

    @Test
    public void shouldReturnIdentityAgencyDTOSuccessfully() throws Exception {
       when(identityRepository.findFirstByUid(eq(IDENTITY_UID))).thenReturn(Optional.of(createIdentity()));

        mockMvc.perform(get("/identity/agency/" + IDENTITY_UID))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.uid", CoreMatchers.is(IDENTITY_UID)))
                .andExpect(jsonPath("$.agencyTokenUid", CoreMatchers.is("456")));
    }

    @Test
    public void shouldReturnNotFoundWhenIdentityNotFound() throws Exception {
        when(identityRepository.findFirstByUid(eq(IDENTITY_UID))).thenReturn(Optional.empty());

        mockMvc.perform(get("/identity/agency/" + IDENTITY_UID))
                .andExpect(status().isNotFound());
    }

    private Identity createIdentity() {
        Identity identity = new Identity(IDENTITY_UID, EMAIL, PASSWORD, true, false, null, Instant.now(), false, false);
        identity.setAgencyTokenUid("456");
        return  identity;
    }

}
