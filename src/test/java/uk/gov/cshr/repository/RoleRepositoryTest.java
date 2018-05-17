package uk.gov.cshr.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.domain.Role;

import javax.transaction.Transactional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository repository;

    @Test
    public void shouldSaveRole() {
        long repositoryCount = repository.count();

        Role role = createRole();

        repository.save(role);

        assertThat(repository.count(), equalTo(repositoryCount + 1));
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