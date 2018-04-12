package uk.gov.cshr.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.domain.Role;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository repository;

    @Test
    public void shouldSaveRole() {
        Role role = createRole();

        repository.save(role);

        assertThat(role.getId(), notNullValue());
        assertThat(role.getName(), equalTo("name"));
        assertThat(role.getDescription(), equalTo("description"));
    }

    private Role createRole() {
        return createRole("name", "description");
    }

    private Role createRole(String name, String description) {
        Role role = new Role(name, description);

        return role;
    }

}