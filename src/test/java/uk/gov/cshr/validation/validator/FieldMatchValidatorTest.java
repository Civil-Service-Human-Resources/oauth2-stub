package uk.gov.cshr.validation.validator;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.gov.cshr.exception.FieldMatchException;
import uk.gov.cshr.validation.annotation.FieldMatch;

import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BeanUtils.class)
public class FieldMatchValidatorTest {
    private FieldMatchValidator validator = new FieldMatchValidator();

    @Test
    public void shouldReturnTrueIfFieldsMatch() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class);
        FieldMatch fieldMatch = mock(FieldMatch.class);

        when(fieldMatch.first()).thenReturn("first");
        when(fieldMatch.second()).thenReturn("second");

        mockStatic(BeanUtils.class);

        Object bean = new Object();

        when(BeanUtils.getProperty(bean, "first")).thenReturn("match");
        when(BeanUtils.getProperty(bean, "second")).thenReturn("match");

        validator.initialize(fieldMatch);
        assertTrue(validator.isValid(bean, constraintValidatorContext));
    }

    @Test
    public void shouldReturnFalseIfFieldsDoNotMatch() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class);
        FieldMatch fieldMatch = mock(FieldMatch.class);

        when(fieldMatch.first()).thenReturn("first");
        when(fieldMatch.second()).thenReturn("second");

        mockStatic(BeanUtils.class);

        Object bean = new Object();

        when(BeanUtils.getProperty(bean, "first")).thenReturn("match");
        when(BeanUtils.getProperty(bean, "second")).thenReturn("no match");

        validator.initialize(fieldMatch);
        assertFalse(validator.isValid(bean, constraintValidatorContext));
    }


    @Test
    public void shouldThrowFieldMatchExceptionOnIllegalAccessException()
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class);
        FieldMatch fieldMatch = mock(FieldMatch.class);

        when(fieldMatch.first()).thenReturn("first");
        when(fieldMatch.second()).thenReturn("second");

        mockStatic(BeanUtils.class);

        Object bean = new Object();

        IllegalAccessException exception = mock(IllegalAccessException.class);

        when(BeanUtils.getProperty(bean, "first")).thenThrow(exception);
        validator.initialize(fieldMatch);

        try {
            validator.isValid(bean, constraintValidatorContext);
            fail("Expected FieldMatchException");
        } catch (FieldMatchException e) {
            assertEquals(exception, e.getCause());
        }
    }

    @Test
    public void shouldThrowFieldMatchExceptionOnNoSuchMethodException()
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class);
        FieldMatch fieldMatch = mock(FieldMatch.class);

        when(fieldMatch.first()).thenReturn("first");
        when(fieldMatch.second()).thenReturn("second");

        mockStatic(BeanUtils.class);

        Object bean = new Object();

        NoSuchMethodException exception = mock(NoSuchMethodException.class);

        when(BeanUtils.getProperty(bean, "first")).thenThrow(exception);
        validator.initialize(fieldMatch);

        try {
            validator.isValid(bean, constraintValidatorContext);
            fail("Expected FieldMatchException");
        } catch (FieldMatchException e) {
            assertEquals(exception, e.getCause());
        }
    }

    @Test
    public void shouldThrowFieldMatchExceptionOnInvocationTargetException()
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class);
        FieldMatch fieldMatch = mock(FieldMatch.class);

        when(fieldMatch.first()).thenReturn("first");
        when(fieldMatch.second()).thenReturn("second");

        mockStatic(BeanUtils.class);

        Object bean = new Object();

        InvocationTargetException exception = mock(InvocationTargetException.class);

        when(BeanUtils.getProperty(bean, "first")).thenThrow(exception);
        validator.initialize(fieldMatch);

        try {
            validator.isValid(bean, constraintValidatorContext);
            fail("Expected FieldMatchException");
        } catch (FieldMatchException e) {
            assertEquals(exception, e.getCause());
        }
    }

}