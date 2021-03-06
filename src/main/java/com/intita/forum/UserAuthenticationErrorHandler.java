package com.intita.forum;

import java.io.IOException;



import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.intita.forum.models.IntitaUser;
import com.intita.forum.services.IntitaUserService;

import org.springframework.security.core.AuthenticationException;

@Component
public class UserAuthenticationErrorHandler implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
	@Autowired
	IntitaUserService userService;

	
    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
 
        Object userName = event.getAuthentication().getPrincipal();
        Object credentials = event.getAuthentication().getCredentials();
        System.out.println("Failed login using USERNAME " + userName);
       System.out.println("Failed login using PASSWORD " + credentials); 
       
       Long id =  (long) 50;
       System.out.println("User  " + userService.getIntitaUser("admin").getPassword());   //.getUser(id).getEmail()); 
       /*for (Long i = (long) 40; i < 57; i++) 
       {
    	   User user = userService.getUser(i);
    	   if (user != null)
    	   System.out.println("User " + i + " : email " + user.getEmail()); 
       }*/
       
       
    }
}