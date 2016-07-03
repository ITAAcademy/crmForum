package com.intita.forum.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;

import com.intita.forum.domain.SessionProfanity;
import com.intita.forum.event.ParticipantRepository;
import com.intita.forum.util.ProfanityChecker;
/**
 * 
 * @author Nicolas Haiduchok
 */
@Configuration
public class ChatConfig {

	private static final int MAX_PROFANITY_LEVEL = 5;

	@Bean
	@Description("Keeps connected users")
	public ParticipantRepository participantRepository() {
		return new ParticipantRepository();
	}

	@Bean
	@Scope(value = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
	@Description("Keeps track of the level of profanity of a websocket session")
	public SessionProfanity sessionProfanity() {
		return new SessionProfanity(MAX_PROFANITY_LEVEL);
	}

	@Bean
	@Description("Utility class to check the number of profanities and filter them")
	public ProfanityChecker profanityFilter() {
		Set<String> profanities = new HashSet<>(Arrays.asList("damn", "crap", "ass"));
		ProfanityChecker checker = new ProfanityChecker();
		checker.setProfanities(profanities);
		return checker;
	}
	
	@Bean
    @ConditionalOnMissingBean(RequestContextListener.class)
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }


}
