package uk.gov.cshr.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.RoleRepository;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void shouldLoadRolesSuccessfully() throws Exception {
        this.mockMvc.perform(get("/management/roles"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void shouldCreateRoleSuccessfully() throws Exception {
        assertThat(roleRepository.count(), equalTo(0L));

        this.mockMvc.perform(post("/management/roles/create")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("name", "User")
                .param("description", "User's role"))
                .andDo(print());

        assertThat(roleRepository.count(), equalTo(1L));

        Optional<Role> role = roleRepository.findById(1);
        assertThat(role, notNullValue());
        assertThat(role.get().getName(), equalTo("User"));
        assertThat(role.get().getDescription(), equalTo("User's role"));

    }
}
