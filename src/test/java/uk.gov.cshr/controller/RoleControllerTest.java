package uk.gov.cshr.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.RoleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.service.RoleService;

import javax.transaction.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional

public class RoleControllerTest {

    @InjectMocks
    private RoleController roleController;


    @Autowired
    private MockMvc mockMvc;

    @Mock
    private RoleService roleService;


    @Before
    public void setup() {

        // this must be called for the @Mock annotations above to be processed
        // and for the mock service to be injected into the controller under
        // test.
        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();

    }

    private final String NAME = "User";
    private final String DESCRIPTION = "User";



    @Before
    public void setup() {

        // this must be called for the @Mock annotations above to be processed
        // and for the mock service to be injected into the controller under
        // test.
        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();

    }

    private final String NAME = "User";
    private final String DESCRIPTION = "User";


    @Test
    public void shouldLoadRolesSuccessfully() throws Exception {
        ArrayList<Role> roles = new ArrayList<>();
        roles.add(new Role(NAME,DESCRIPTION));
        when(roleService.findAll()).thenReturn(roles);

        this.mockMvc.perform(get("/management/roles"))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
    }


    @Test
    public void shouldNotLoadRoleToEditWhenNoExistent() throws Exception {
        this.mockMvc.perform(get("/management/roles/edit/1")).andExpect(redirectedUrl("/management/roles"));

    }

    @Test
    public void shouldLoadRoleToEdit() throws Exception {

        Role role  = new Role("User",DESCRIPTION);
        role.setId(1L);
        when(roleService.getRole(1L)).thenReturn(Optional.of(role));

        this.mockMvc.perform(get("/management/roles/edit/1"))
                .andExpect (model().attribute("role", hasProperty("name", is(NAME))))
                .andExpect(model().attribute("role", hasProperty("description", is(DESCRIPTION))));
    }

    @Test
    public void shouldSaveEditedRole() throws Exception {

        this.mockMvc.perform(post("/management/roles/edit")
                .param("name",NAME)
                .param("description",DESCRIPTION));

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);

        verify(roleService).updateRole(roleCaptor.capture());

        Role role = roleCaptor.getValue();
        assertThat(role.getDescription(),equalTo(DESCRIPTION));
        assertThat(role.getName(),equalTo(NAME));

    }

}
