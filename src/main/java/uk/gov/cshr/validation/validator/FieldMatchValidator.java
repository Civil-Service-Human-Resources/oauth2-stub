package uk.gov.cshr.validation.validator;

import org.apache.commons.beanutils.BeanUtils;
import uk.gov.cshr.exception.FieldMatchException;
import uk.gov.cshr.validation.annotation.FieldMatch;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;

public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {
   private String firstFieldName;
   private String secondFieldName;

   @Override
   public void initialize(final FieldMatch constraintAnnotation)
   {
      firstFieldName = constraintAnnotation.first();
      secondFieldName = constraintAnnotation.second();
   }

   @Override
   public boolean isValid(final Object value, final ConstraintValidatorContext context) {
      try {
         final Object firstObj = BeanUtils.getProperty(value, firstFieldName);
         final Object secondObj = BeanUtils.getProperty(value, secondFieldName);
         return firstObj == null && secondObj == null || firstObj != null && firstObj.equals(secondObj);
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
         throw new FieldMatchException(e);
      }
   }
}