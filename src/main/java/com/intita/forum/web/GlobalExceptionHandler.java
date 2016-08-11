package com.intita.forum.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {
	public static final String DEFAULT_ERROR_VIEW = "error";
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	@ResponseStatus(HttpStatus.CONFLICT)  // 409
    @ExceptionHandler(NumberFormatException.class)
    public void handleConflict(NumberFormatException e) {
        log.info(e.getMessage());
    }
	@ResponseStatus(HttpStatus.CONFLICT)  // 409
	@ExceptionHandler(JpaObjectRetrievalFailureException.class)
	public void handleConflict(ObjectRetrievalFailureException  e) {
		log.info(e.getMessage());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)  // 409
    @ExceptionHandler(value = {Exception.class, RuntimeException.class})
    public ModelAndView defaultErrorHandler(HttpServletRequest request, Exception e) {
            ModelAndView mav = new ModelAndView(DEFAULT_ERROR_VIEW);      
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();    
        mav.addObject("exception", "Невідома помилка");
        mav.addObject("errorCode",400);
        log.error(exceptionAsString);
        return mav;
    }
	
	@ResponseStatus(HttpStatus.UNAUTHORIZED) 
    @ExceptionHandler(value = {UsernameNotFoundException.class})
    public ModelAndView usernameNotFoundExceptionErrorHandler(HttpServletRequest request, UsernameNotFoundException e) {
            ModelAndView mav = new ModelAndView(DEFAULT_ERROR_VIEW);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();    
        mav.addObject("exception", "Необхідна авторизація");
        mav.addObject("errorCode",401);
        log.error(exceptionAsString);
        return mav;
    }
    @ExceptionHandler(value = {AccessDeniedException.class})
    public ModelAndView accessDeniedErrorHandler(HttpServletRequest request, AccessDeniedException e) {
            ModelAndView mav = new ModelAndView(DEFAULT_ERROR_VIEW);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();    
        mav.addObject("exception", "Відсутні права для цієї сторінки");
        mav.addObject("errorCode",403);
        log.error(exceptionAsString);
        return mav;
    }
    
    
    
}
