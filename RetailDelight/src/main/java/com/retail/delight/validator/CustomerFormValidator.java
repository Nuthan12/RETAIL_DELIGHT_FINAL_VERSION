package com.retail.delight.validator;

import org.apache.commons.validator.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.retail.delight.form.CustomerForm;
import com.retail.delight.utils.Utils;

@Component
public class CustomerFormValidator implements Validator {

   private EmailValidator emailValidator = EmailValidator.getInstance();
   
	private static final Logger logger = LoggerFactory.getLogger(CustomerFormValidator.class);

   // This validator only checks for the CustomerForm.
   @Override
   public boolean supports(Class<?> clazz) {
      return clazz == CustomerForm.class;
   }

   @Override
   public void validate(Object target, Errors errors) {
      CustomerForm custInfo = (CustomerForm) target;

      // Check the fields of CustomerForm.
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "NotEmpty.customerForm.name");
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "NotEmpty.customerForm.email");
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "address", "NotEmpty.customerForm.address");
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "phone", "NotEmpty.customerForm.phone");

      if (!emailValidator.isValid(custInfo.getEmail())) {
    	 logger.error("The customer email entry is invalid, {}",custInfo.getEmail());
         errors.rejectValue("email", "Pattern.customerForm.email");
      }
   }

}
