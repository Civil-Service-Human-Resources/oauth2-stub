package uk.gov.cshr.service.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.domain.Client;
import uk.gov.cshr.repository.ClientRepository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ClientDetailsServiceTest {

    @InjectMocks
    private ClientDetailsService clientDetailsService;

    @Mock
    private ClientRepository clientRepository;

    @Test
    public void shouldLoadClientByValidId() {

        final String uid = "uid";
        final Client client = new Client(uid, "password", true);

        when(clientRepository.findFirstByActiveTrueAndUidEquals(uid))
                .thenReturn(client);

        ClientDetails clientDetails = clientDetailsService.loadClientByClientId(uid);

        assertThat(clientDetails, notNullValue());
        assertThat(clientDetails.getClientId(), equalTo(uid));
    }

    @Test(expected = ClientRegistrationException.class)
    public void shouldThrowErrorWhenNoClientFound() {

        final String uid = "uid";

        when(clientRepository.findFirstByActiveTrueAndUidEquals(uid))
                .thenReturn(null);

        clientDetailsService.loadClientByClientId(uid);
    }
}
