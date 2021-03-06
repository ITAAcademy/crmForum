package com.intita.forum;

import java.util.concurrent.Executor;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestContextListener;


@SpringBootApplication
@EnableAutoConfiguration
@EnableScheduling
//@Import({WebSocketTraceChannelInterceptor.class, WebSocketTraceChannelInterceptorAutoConfiguration.class})
//@ComponentScan("org.springframework.boot.actuate.trace")
public class Application extends SpringBootServletInitializer  implements AsyncConfigurer  {


	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) {

		try {
			SpringApplication.run(Application.class, args);	
		} catch (Exception e) {
			
		}

	}

    @Override public void onStartup( ServletContext servletContext ) throws ServletException {
        super.onStartup( servletContext );
        servletContext.addListener( new RequestContextListener() ); 
    }
    
	@Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(10);
        taskExecutor.setThreadNamePrefix("LULExecutor-");
        taskExecutor.initialize();
        return taskExecutor;
    }
    
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
    @Bean  
    public SessionFactory sessionFactory(HibernateEntityManagerFactory hemf){  
        return hemf.getSessionFactory();  
    } 

}
