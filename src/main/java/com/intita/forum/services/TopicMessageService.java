package com.intita.forum.services;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.*;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intita.forum.models.ForumTopic;
import com.intita.forum.models.ForumUser;
import com.intita.forum.models.TopicMessage;
import com.intita.forum.repositories.TopicMessageRepository;

@Service
public class TopicMessageService {

	@Autowired
	private TopicMessageRepository topicMessageRepository;

	@Autowired 
	private ForumUsersService forumUserService;

	@Autowired
	private ForumTopicService forumTopicService;


	@Transactional(readOnly=true)
	public ArrayList<TopicMessage> getMesagges(){
		return (ArrayList<TopicMessage>) IteratorUtils.toList(topicMessageRepository.findAll().iterator());
	}
	@Transactional(readOnly=true)
	public TopicMessage getMessage(Long id){
		return topicMessageRepository.findOne(id);
	}
	@Transactional(readOnly=true)
	public ArrayList<TopicMessage> getMessagesByForumUserId(Long id) {

		return topicMessageRepository.findByAuthor(forumUserService.getChatUser(id));
	}
	@Transactional(readOnly=true)
	public ArrayList<TopicMessage> getMessagesByTopic(ForumTopic topic) {
		return topicMessageRepository.findByTopic(topic);
	}
	@Transactional(readOnly=true)
	public TopicMessage getLastMessageByTopic(ForumTopic topic){
		return topicMessageRepository.findFirstByTopicOrderByDateDesc(topic);
	}
	@Transactional(readOnly=true)
	public ArrayList<TopicMessage> getFirst20TipicMessagesByTopic(ForumTopic topic) {
		return topicMessageRepository.findFirst20ByTopicOrderByIdDesc(topic);
	}

	public ArrayList<TopicMessage> getMessagesByTopicId(Long topicId) {

		return topicMessageRepository.findByTopic(new ForumTopic(topicId));
	}

	@Transactional()
	public boolean addMessage(ForumUser user, ForumTopic topic,String body) {
		if(user == null || topic == null || body == null) return false;
		//have premition?
		TopicMessage topicMessage = new TopicMessage(user,topic,body);
		topicMessageRepository.save(topicMessage);
		return true;
	}
	@Transactional()
	public boolean addMessage(TopicMessage message) {
		if (message==null) return false;
		topicMessageRepository.save(message);
		return true;
	}
	@Transactional()
	public boolean addMessages(Iterable<TopicMessage> messages) {
		if (messages==null) return false;
		topicMessageRepository.save(messages);
		return true;
	}

	@Transactional(readOnly=true)
	public ArrayList<TopicMessage> getMessagesByDate(Date date) {
		return topicMessageRepository.findAllByDateAfter(date);
	}

}
