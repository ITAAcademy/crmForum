package com.intita.forum.config;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.intita.forum.models.IntitaUser;
import com.intita.forum.models.IntitaUser.IntitaUserRoles;
import com.intita.forum.services.IntitaUserService;
import com.intita.forum.services.RedisService;
import com.intita.forum.util.SerializedPhpParser;
/**
 * 
 * @author Nicolas Haiduchok
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider{
	@Autowired	RedisService redisService;
	@Autowired IntitaUserService userService;

	private	SerializedPhpParser serializedPhpParser;
	

	@Value("${redis.id}")
	private String redisId;
	private final static Logger log = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Authentication token = (Authentication) authentication;

				ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();

				Cookie[] array = attr.getRequest().getCookies();
				HttpSession session = attr.getRequest().getSession();
				RequestContextHolder.currentRequestAttributes().getSessionId();				//session.getServletContext().getSessionCookieConfig().setName("CHAT_SESSION");
				//session.setMaxInactiveInterval(3600*12);
				session.setMaxInactiveInterval(120);
				
				String value = null;
				String IntitaLg = "ua";
				String IntitaIdStr = null;
				if(array != null)
					for(Cookie cook : array)
					{
						if(cook.getName().equals("JSESSIONID"))
						{
							System.out.println(cook.getValue());
							value = cook.getValue();
							session.setAttribute("id", value);
							String phpSession = redisService.getKeyValue(value);



							if(phpSession != null)
							{
								try {
									System.out.println(phpSession);
									serializedPhpParser = new SerializedPhpParser(phpSession);
									IntitaIdStr = (String)serializedPhpParser.findPatern(redisId);
									IntitaLg = (String)serializedPhpParser.find("lg");
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									throw new UsernameNotFoundException(redisId);
							
								}
							}
							break;
						}
					}
				Long intitaIdLong = null;
				try{
						intitaIdLong = Long.parseLong(IntitaIdStr);
				}
				catch(NumberFormatException e){
				log.info(e.getMessage());
				//throw new UsernameNotFoundException(IntitaIdStr);
				session.setMaxInactiveInterval(5);
				return new UsernamePasswordAuthenticationToken(new IntitaUser("anonymousUser", ""), token.getCredentials(),null);
				///return new UsernamePasswordAuthenticationToken("", token.getCredentials(), authorities);
				}
					Object obj_s = session.getAttribute("forumId");

				session.setAttribute("chatLg", IntitaLg);
				IntitaUser user = userService.getById(intitaIdLong);
				Set<IntitaUserRoles> roles = userService.getRoles(user.getId());
				List<SimpleGrantedAuthority> authoritiesList = new ArrayList<SimpleGrantedAuthority>();
				for (IntitaUserRoles role : roles){
					authoritiesList.add(new SimpleGrantedAuthority(role.name()));
				}
				Authentication auth = new UsernamePasswordAuthenticationToken(user, token.getCredentials(), authoritiesList);
				
				
		
				GrantedAuthority autfsdh = new SimpleGrantedAuthority("test");
				//	auth.setAuthenticated(true);
				return auth;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	public Authentication autorization()
	{
		return autorization(this);
	}

	public Authentication autorization(AuthenticationProvider authenticationProvider)
	{
		if(SecurityContextHolder.getContext().getAuthentication() != null && authenticationProvider != null)
		{
			System.out.println(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
			//if(!SecurityContextHolder.getContext().getAuthentication().isAuthenticated())
			Authentication auth = authenticationProvider.authenticate(SecurityContextHolder.getContext().getAuthentication());
			
			SecurityContextHolder.getContext().setAuthentication(auth);
			//SecurityContextHolder.clearContext();
			
			
			return auth;
		}
		else
			return null;

	}

}