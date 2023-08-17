package com.cdac.esign.exceptions;


import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionController {

	@ExceptionHandler(RuntimeException.class)
	public ModelAndView handleException(Exception e) {
		ModelAndView mv=new ModelAndView("failure");
		mv.addObject("msg",e.getMessage());
		return mv;
	}
}
