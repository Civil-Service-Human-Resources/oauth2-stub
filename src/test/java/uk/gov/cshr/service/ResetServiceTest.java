package uk.gov.cshr.service;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.domain.Reset;
import uk.gov.cshr.domain.ResetStatus;
import uk.gov.cshr.repository.ResetRepository;
import uk.gov.service.notify.NotificationClientException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ResetServiceTest {

    public static final String EMAIL = "test@example.com";
    public static final String CODE = "abc123";
    public static final String TEMPLATE_ID = "template123";
    public static final String URL = "localhost:8080";

    @InjectMocks
    private ResetService resetService;

    @Mock
    private ResetRepository resetRepository;

    @Mock
    private NotifyService notifyService;

    @Before
    public void setUp() {
    }

    @Test
    public void shouldSaveNewResetWhenCreateNewResetForEmail() throws NotificationClientException {
        doNothing().when(notifyService).notify(EMAIL, CODE, TEMPLATE_ID, URL);

        resetService.createNewResetForEmail(EMAIL);

        ArgumentCaptor<Reset> resetArgumentCaptor = ArgumentCaptor.forClass(Reset.class);

        verify(resetRepository).save(resetArgumentCaptor.capture());

        Reset reset = resetArgumentCaptor.getValue();
        MatcherAssert.assertThat(reset.getEmail(), equalTo(EMAIL));
        MatcherAssert.assertThat(reset.getResetStatus(), equalTo(ResetStatus.PENDING));

    }

    @Test
    public void shouldModifyExistingResetWhenResetSuccessFor() throws NotificationClientException {
        doNothing().when(notifyService).notify(EMAIL, CODE, TEMPLATE_ID, URL);

        Reset expectedReset = new Reset();
        expectedReset.setEmail(EMAIL);
        expectedReset.setResetStatus(ResetStatus.PENDING);

        resetService.createSuccessfulPasswordResetForEmail(expectedReset);

        ArgumentCaptor<Reset> resetArgumentCaptor = ArgumentCaptor.forClass(Reset.class);

        verify(resetRepository).save(resetArgumentCaptor.capture());

        Reset actualReset = resetArgumentCaptor.getValue();
        MatcherAssert.assertThat(actualReset.getEmail(), equalTo(EMAIL));
        MatcherAssert.assertThat(actualReset.getResetStatus(), equalTo(ResetStatus.RESET));

    }
}