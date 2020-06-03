package uk.gov.cshr.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.domain.AgencyToken;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.domain.ReactivationStatus;
import uk.gov.cshr.exception.ResourceNotFoundException;
import uk.gov.cshr.repository.ReactivationRepository;
import uk.gov.cshr.service.security.IdentityService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReactivationServiceTest {

    private static final String EMAIL = "test@example.com";
    private static final String CODE = "code";
    private static final String UID = "UID";
    @Mock
    private ReactivationRepository reactivationRepository;

    @Mock
    private IdentityService identityService;

    @Captor
    private ArgumentCaptor<Reactivation> reactivationArgumentCaptor;

    @InjectMocks
    private ReactivationService reactivationService;

    @Test
    public void shouldReactivateIdentity() {
        Reactivation reactivation = new Reactivation();
        reactivation.setEmail(EMAIL);
        reactivation.setCode(CODE);

        AgencyToken agencyToken = new AgencyToken();
        agencyToken.setUid(UID);

        Identity identity = new Identity();

        ArgumentCaptor<Reactivation> reactivationArgumentCaptor = ArgumentCaptor.forClass(Reactivation.class);

        when(identityService.getIdentityByEmail(EMAIL)).thenReturn(identity);
        doNothing().when(identityService).reactivateIdentity(identity, agencyToken);

        reactivationService.reactivateIdentity(reactivation, agencyToken);

        verify(reactivationRepository).save(reactivationArgumentCaptor.capture());

        Reactivation reactivationArgumentCaptorValue = reactivationArgumentCaptor.getValue();
        assertEquals(ReactivationStatus.REACTIVATED, reactivationArgumentCaptorValue.getReactivationStatus());
    }

    @Test
    public void shouldGetReactivationByCodeAndStatus() {
        Reactivation reactivation = new Reactivation();
        reactivation.setCode(CODE);

        when(reactivationRepository
                .findFirstByCodeAndReactivationStatusEquals(CODE, ReactivationStatus.PENDING))
                .thenReturn(Optional.of(reactivation));

        assertEquals(reactivation, reactivationService.getReactivationByCodeAndStatus(CODE, ReactivationStatus.PENDING));
    }

    @Test
    public void shouldReturnTrueIfExistsByReactivationByCodeAndStatus() {
        Reactivation reactivation = new Reactivation();
        reactivation.setCode(CODE);

        when(reactivationRepository
                .existsByCodeAndReactivationStatusEquals(CODE, ReactivationStatus.PENDING)).thenReturn(true);

        assertTrue(reactivationService.existsByCodeAndStatus(CODE, ReactivationStatus.PENDING));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void shouldThrowResourceNotFoundExceptionIfReactivationDoesNotExist() {
        when(reactivationRepository
                .findFirstByCodeAndReactivationStatusEquals(CODE, ReactivationStatus.PENDING))
                .thenReturn(Optional.empty());

        reactivationService.getReactivationByCodeAndStatus(CODE, ReactivationStatus.PENDING);
    }
}
