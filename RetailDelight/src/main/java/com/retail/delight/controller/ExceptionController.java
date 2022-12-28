package com.retail.delight.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionController {
	 @ExceptionHandler(Exception.class)
	    public String exceptionHandling(Model model) {

	 

	        return "/Exception";
	    }

}
