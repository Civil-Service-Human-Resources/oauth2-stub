package uk.gov.cshr.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.domain.*;
import uk.gov.cshr.exception.VerificationCodeTypeNotFound;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VerificationCodeDeterminationServiceTest {

    private static final String CODE = "code";

    private static final String EMAIL = "test@example.com";

    @Mock
    private EmailUpdateService emailUpdateService;

    @Mock
    private ReactivationService reactivationService;

    @InjectMocks
    private VerificationCodeDeterminationService verificationCodeDeterminationService;

    @Test
    public void getCodeTypeShouldReturnEmailUpdate() {
        EmailUpdate emailUpdate = new EmailUpdate();
        emailUpdate.setEmail(EMAIL);

        when(emailUpdateService.existsByCode(CODE)).thenReturn(true);
        when(emailUpdateService.getEmailUpdateByCode(CODE)).thenReturn(emailUpdate);

        VerificationCodeDetermination verificationCodeDetermination = verificationCodeDeterminationService.getCodeType(CODE);
        assertEquals(VerificationCodeType.EMAIL_UPDATE, verificationCodeDetermination.getVerificationCodeType());
        assertEquals(EMAIL, verificationCodeDetermination.getEmail());

        verify(reactivationService, times(0)).existsByCodeAndStatus(CODE, ReactivationStatus.PENDING);
    }

    @Test
    public void getCodeTypeShouldReturnReactivation() {
        Reactivation reactivation = new Reactivation();
        reactivation.setEmail(EMAIL);

        when(emailUpdateService.existsByCode(CODE)).thenReturn(false);
        when(reactivationService.existsByCodeAndStatus(CODE, ReactivationStatus.PENDING)).thenReturn(true);
        when(reactivationService.getReactivationByCodeAndStatus(CODE, ReactivationStatus.PENDING)).thenReturn(reactivation);

        VerificationCodeDetermination verificationCodeDetermination = verificationCodeDeterminationService.getCodeType(CODE);
        assertEquals(VerificationCodeType.REACTIVATION, verificationCodeDetermination.getVerificationCodeType());
        assertEquals(EMAIL, verificationCodeDetermination.getEmail());
    }

    @Test(expected = VerificationCodeTypeNotFound.class)
    public void shouldThrowVerificationCodeTypeNotFoundException() {
        Identity identity = new Identity();
        identity.setEmail(EMAIL);

        when(emailUpdateService.existsByCode(CODE)).thenReturn(false);
        when(reactivationService.existsByCodeAndStatus(CODE, ReactivationStatus.PENDING)).thenReturn(false);

        VerificationCodeDetermination verificationCodeDetermination = verificationCodeDeterminationService.getCodeType(CODE);

        assertNotEquals(VerificationCodeType.EMAIL_UPDATE, verificationCodeDetermination.getVerificationCodeType());
        assertNotEquals(VerificationCodeType.REACTIVATION, verificationCodeDetermination.getVerificationCodeType());
    }
}
