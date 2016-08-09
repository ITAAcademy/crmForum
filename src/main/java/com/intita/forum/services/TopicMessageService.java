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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intita.forum.models.ForumTopic;
import com.intita.forum.models.IntitaUser;
import com.intita.forum.models.TopicMessage;
import com.intita.forum.repositories.TopicMessageRepository;

@Service
public class TopicMessageService {

	@Autowired
	private TopicMessageRepository topicMessageRepository;

	@Autowired
	private ForumTopicService forumTopicService;
	
	@Autowired
	private IntitaUserService intitaUserService;

	
	@Value("${forum.messagesCountPerPage}")
	private int messagesCountPerPage;

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

		return topicMessageRepository.findByAuthor(intitaUserService.getById(id));
	}
	@Transactional(readOnly=true)
	public Page<TopicMessage> getMessagesByTopic(ForumTopic topic, int page) {
		return topicMessageRepository.findByTopic(topic, new PageRequest(page,messagesCountPerPage));
	}
	@Transactional(readOnly=true)
	public TopicMessage getLastMessageByTopic(ForumTopic topic){
		return topicMessageRepository.findFirstByTopicOrderByDateDesc(topic);
	}
	@Transactional(readOnly=true)
	public ArrayList<TopicMessage> getFirst20TipicMessagesByTopic(ForumTopic topic) {
		return topicMessageRepository.findFirst20ByTopicOrderByIdDesc(topic);
	}
	@Transactional
	public Page<TopicMessage> getMessagesByTopicId(Long topicId,int page) {

		return topicMessageRepository.findByTopicId(topicId,new PageRequest(page,messagesCountPerPage));
	}

	@Transactional()
	public boolean addMessage(IntitaUser user, ForumTopic topic,String body) {
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
	@Transactional
	public Page<TopicMessage> getAllMessagesAndPinFirst(Long topicId,int page){
		  PageRequest pageable = new PageRequest(page, messagesCountPerPage);
		  ForumTopic topic = forumTopicService.getTopic(topicId);
		  TopicMessage firstMessage = topicMessageRepository.findFirstByTopicOrderByDateAsc(topic);
		  if (firstMessage==null)return null;
		  List<TopicMessage> otherMessages =topicMessageRepository.findAllByTopicWhereMessageIdNotEqualOrderByDateAsc(topic.getId(),firstMessage.getId());
		  List<TopicMessage> allMessages = new ArrayList<TopicMessage>();
		  if (firstMessage!=null)
		  allMessages.add(firstMessage);
		  if (otherMessages!=null)
		  allMessages.addAll(otherMessages);		 
		  int max = (messagesCountPerPage*(page+1)>allMessages.size())? allMessages.size(): messagesCountPerPage*(page+1);
		  List<TopicMessage> sublist = allMessages.subList(page*messagesCountPerPage, max);
		  if (page>0){
			//remove last and put author initial message firstly
			//  sublist.remove(sublist.size()-1);
			  sublist.add(0,firstMessage);
		  }
		  Page<TopicMessage> pageObj = new PageImpl<TopicMessage>(sublist,pageable,allMessages.size());
		
		return pageObj;
	}
}
