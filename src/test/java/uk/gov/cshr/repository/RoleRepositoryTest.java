package uk.gov.cshr.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
        // There will be two roles stored on load
        assertThat(repository.count(), equalTo(2L));

        Role role = createRole();

        repository.save(role);

        assertThat(repository.count(), equalTo(3L));
    }

    @Test
    public void shouldReturnFirstByName() {
        Role role = createRole();

        repository.save(role);

        Role actualRole = repository.findFirstByNameEquals("name");

        assertThat(actualRole.getId(), notNullValue());
        assertThat(actualRole.getName(), equalTo("name"));
        assertThat(actualRole.getDescription(), equalTo("description"));
    }

    private Role createRole() {
        return createRole("name", "description");
    }

    private Role createRole(String name, String description) {
        Role role = new Role(name, description);

        return role;
    }

}