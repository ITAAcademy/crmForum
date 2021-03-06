package com.intita.forum.config;

import java.security.Principal;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.IntitaUser;
import com.intita.forum.models.IntitaUser.IntitaUserRoles;
import com.intita.forum.services.ForumCategoryService;
import com.intita.forum.services.IntitaUserService;
/**
 * 
 * @author Nicolas Haiduchok, Zinchuk Roman
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableAutoConfiguration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private CustomAuthenticationProvider authenticationProvider;
	@Autowired
	private CustomFilter authenticationTokenFilter;

	
	
	/*@Autowired
	private RequestContextFilter requestContextFilter;*/

	/*  @Autowired
	DataSource dataSource;

    @Autowired
	public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {

	  auth.jdbcAuthentication().dataSource(dataSource)
		.usersByUsernameQuery(
			"select email,password, enabled from user where email=?")
		.authoritiesByUsernameQuery(
			"select email, role from user_roles where username=?");
	}	*/

	@Bean
	public AuthenticationSuccessHandler successHandler() {
	    SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
	    handler.setUseReferer(true);
	    return handler;
	}
	@Autowired
	AuditEventConfiguration sdfsd;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		http
		.csrf().disable()
		//.addFilterAfter(authenticationTokenFilter, BasicAuthenticationFilter.class)
		//.addFilterBefore( authenticationTokenFilter, SecurityContextPersistenceFilter.class)
		.formLogin()
		.loginPage("/login")
		.successHandler(successHandler())
		.passwordParameter("password")
		//.defaultSuccessUrl("/chatFrame.html")
		.permitAll()
		.and()
		.logout()
		//.logoutSuccessUrl("/index.html")
		.permitAll()
		.and()
		.authorizeRequests()
		//.antMatchers("/view/category/{categoryId}/**").access("@forumCategoryService.checkCategoryAccessToUser(authentication,request)")
		.antMatchers("/","/login").permitAll()
		.anyRequest().authenticated();

		/*
		 * ATENTION 
		 * 
		 */
		http.exceptionHandling().authenticationEntryPoint(sdfsd);
		http.headers()
		.frameOptions().sameOrigin()
		.httpStrictTransportSecurity().disable();
		http.headers()
		.defaultsDisabled()
		.cacheControl();



		//http.headers().addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN));



	}

	private static final String SECURE_ADMIN_PASSWORD = "rockandroll";
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

		auth.authenticationProvider(authenticationProvider);
	}
	/*
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new ShaPasswordEncoder());
          //  .passwordEncoder(new BCryptPasswordEncoder()); 
    }
	 */
}


