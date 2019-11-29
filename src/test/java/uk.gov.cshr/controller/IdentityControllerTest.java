package uk.gov.cshr.controller;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.RoleRepository;
import uk.gov.cshr.service.AuthenticationDetails;
import uk.gov.cshr.service.security.IdentityService;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class IdentityControllerTest {

    private final Boolean ACTIVE = true;
    private final Boolean LOCKED = false;
    private final String DESCRIPTION = "User";
    private final String EMAIL = "email";
    private final String NAME = "User";
    private final String PASSWORD = "password";
    private final Set<Role> ROLES = new HashSet();
    private static final String UID = "uid";
    private final String USERNAME = "test";
    private final String[] roleID = {"1"};
    private static String EMAIL_SUCCESSFULLY_UPDATED_URL;
    @InjectMocks
    private IdentityController identityController;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private IdentityRepository identityRepository;

    @Mock
    private AuthenticationDetails authenticationDetails;

    @Mock
    private IdentityService identityService;

    @BeforeClass
    public static void setUpAll() {
        EMAIL_SUCCESSFULLY_UPDATED_URL = "/management/identities/update/"+UID+"/emailSuccessfullyUpdated";
    }

    @Before
    public void setup() {

        // this must be called for the @Mock annotations above to be processed
        // and for the mock service to be injected into the controller under
        // test.
        MockitoAnnotations.initMocks(this);


        this.mockMvc = MockMvcBuilders.standaloneSetup(identityController).build();

        ArrayList<Identity> identities = new ArrayList<>();

        identities.add(new Identity(UID, EMAIL, PASSWORD, ACTIVE, LOCKED, ROLES, Instant.now(), false, false));
        ArrayList<Role> roles = new ArrayList<>();

        roles.add(new Role(NAME, DESCRIPTION));
        when(identityRepository.findAll()).thenReturn(identities);
        when(authenticationDetails.getCurrentUsername()).thenReturn(USERNAME);
        when(roleRepository.findAll()).thenReturn(roles);

    }


    @Test
    public void shouldLoadIdentitiesSuccessfully() throws Exception {
        this.mockMvc.perform(get("/management/identities"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(model().attribute("identities", hasItem(allOf(hasProperty("email", is(EMAIL))))))
                .andDo(print());
    }


    @Test
    public void shouldNotLoadIdentityToEditWhenNoExistent() throws Exception {
        this.mockMvc.perform(get("/management/identities/update/1")).andExpect(redirectedUrl("/management/identities"));
    }

    @Test
    public void shouldLoadIdentityToEdit() throws Exception {

        Identity identity = new Identity(UID, EMAIL, PASSWORD, ACTIVE, LOCKED, ROLES, Instant.now(), false, false);
        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.of(identity));
        this.mockMvc.perform(get("/management/identities/update/uid"))
                .andExpect(model().attribute("identity", hasProperty("uid", is(UID))));
    }

    @Test
    public void shouldSaveEditedIdentity() throws Exception {

        Identity identity = new Identity(UID, EMAIL, PASSWORD, ACTIVE, LOCKED, ROLES, Instant.now(), false, false);
        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.of(identity));


        when(roleRepository.findById(1L)).thenReturn(Optional.of(new Role(NAME, DESCRIPTION)));
        this.mockMvc.perform(post("/management/identities/update")
                .param("uid", UID)
                .param("active", ACTIVE.toString())
                .param("roleId", roleID)
                .param("locked", "true"));

        ArgumentCaptor<Identity> identityCaptor = ArgumentCaptor.forClass(Identity.class);

        verify(identityRepository).save(identityCaptor.capture());

        Identity actualSavedIdentity = identityCaptor.getValue();
        assertThat(actualSavedIdentity.getUid(), equalTo(UID));
        assertThat(actualSavedIdentity.getEmail(), equalTo(EMAIL));
        assertTrue(actualSavedIdentity.isLocked());
        assertFalse(actualSavedIdentity.isEmailRecentlyUpdated());
    }

    @Test
    public void shouldInsertRolesByIDForEditedIdentity() throws Exception {

        Identity identity = new Identity(UID, EMAIL, PASSWORD, ACTIVE, LOCKED, ROLES, Instant.now(), false, false);
        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.of(identity));


        when(roleRepository.findById(1L)).thenReturn(Optional.of(new Role(NAME, DESCRIPTION)));
        this.mockMvc.perform(post("/management/identities/update")
                .param("uid", UID)
                .param("active", ACTIVE.toString())
                .param("roleId", roleID));

        ArgumentCaptor<Identity> identityCaptor = ArgumentCaptor.forClass(Identity.class);

        verify(identityRepository).save(identityCaptor.capture());

        Identity actualSavedIdentity = identityCaptor.getValue();
        ArrayList<Role> roles = new ArrayList(actualSavedIdentity.getRoles());

        assertThat(roles.get(0).getName(), equalTo(NAME));
        assertFalse(actualSavedIdentity.isEmailRecentlyUpdated());
    }

    @Test
    public void shouldUpdateIdentityEmailRecentlyUpdatedFieldToFalse() throws Exception {

        Identity identity = new Identity(UID, EMAIL, PASSWORD, ACTIVE, LOCKED, ROLES, Instant.now(), false, true);
        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.of(identity));


        when(roleRepository.findById(1L)).thenReturn(Optional.of(new Role(NAME, DESCRIPTION)));
        this.mockMvc.perform(post("/management/identities/update")
                .param("emailRecentlyUpdated", "false")
                .param("uid", UID)
                .param("active", ACTIVE.toString())
                .param("roleId", roleID));

        ArgumentCaptor<Identity> identityCaptor = ArgumentCaptor.forClass(Identity.class);

        verify(identityRepository).save(identityCaptor.capture());

        Identity actualSavedIdentity = identityCaptor.getValue();
        ArrayList<Role> roles = new ArrayList(actualSavedIdentity.getRoles());

        assertThat(roles.get(0).getName(), equalTo(NAME));
        assertThat(actualSavedIdentity.isEmailRecentlyUpdated(), is(false));
    }

    @Test
    public void givenAnExistingIdentity_whenEmailSuccessfullyUpdated_thenShouldUpdateEmailRecentlyUpdatedFieldToFalse() throws Exception {
        Identity identity = new Identity(UID, EMAIL, PASSWORD, ACTIVE, LOCKED, ROLES, Instant.now(), false, true);
        when(identityRepository.findFirstByUid(eq(UID))).thenReturn(Optional.of(identity));

        this.mockMvc.perform(post(EMAIL_SUCCESSFULLY_UPDATED_URL))
                .andExpect(status().isNoContent());

        ArgumentCaptor<Identity> identityCaptor = ArgumentCaptor.forClass(Identity.class);

        verify(identityRepository).save(identityCaptor.capture());

        Identity actualSavedIdentity = identityCaptor.getValue();
        assertThat(actualSavedIdentity.isEmailRecentlyUpdated(), is(false));
    }

    @Test
    public void givenAnNonExistingIdentity_whenEmailSuccessfullyUpdated_thenShouldReturnNotFound() throws Exception {

        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.empty());

        this.mockMvc.perform(post(EMAIL_SUCCESSFULLY_UPDATED_URL))
                .andExpect(status().isNotFound());

        verify(identityRepository, never()).save(any());
    }

    @Test
    public void givenATechnicalExceptionOccurs_whenEmailSuccessfullyUpdated_thenShouldReturnInternalServerError() throws Exception {

        Identity identity = new Identity(UID, EMAIL, PASSWORD, ACTIVE, LOCKED, ROLES, Instant.now(), false, true);
        when(identityRepository.findFirstByUid(eq(UID))).thenReturn(Optional.of(identity));
        when(identityRepository.save(any())).thenThrow(new RuntimeException());

        this.mockMvc.perform(post(EMAIL_SUCCESSFULLY_UPDATED_URL))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void givenAnInvalidEmailDomain_whenNotAValidEmailDomain_thenShouldRedirectToSignInPage() throws Exception {

        this.mockMvc.perform(get("/management/identities/someone@notanalloweddomain.com/notValid"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup/request"))
                .andExpect(flash().attribute("status","Your organisation is unable to use this service. Please contact your line manager."));
    }

}
