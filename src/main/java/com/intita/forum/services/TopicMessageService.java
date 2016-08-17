package com.intita.forum.services;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.*;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumTopic;
import com.intita.forum.models.IntitaUser;
import com.intita.forum.models.TopicMessage;
import com.intita.forum.models.IntitaUser.IntitaUserRoles;
import com.intita.forum.repositories.TopicMessageRepository;

@Service
public class TopicMessageService {

	private static final int EDIT_TIME_MINUTES = 15;

	@Autowired
	private TopicMessageRepository topicMessageRepository;

	@Autowired 
	ForumCategoryService forumCategoryService;

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

	/***
	 * return messages from topic that have date <= dateParam
	 * @param date
	 * @return
	 */
	@Transactional(readOnly=true)
	public Long getMessagesCountByTopicIdAndDateBefore(Long topicId,Date date) {
		return topicMessageRepository.findAllByTopicAndDateBeforeWithLast(topicId,date);
	}
	//source code of page count finded at https://github.com/spring-projects/spring-data-commons/blob/a8db5e3d230f33e86d9d017571ed3ddcb7ad8294/src/main/java/org/springframework/data/domain/PageImpl.java
	public int getPagesCountByTopicIdAndMessageid(Long topicId,Date date){
		Long messagesCount = getMessagesCountByTopicIdAndDateBefore(topicId,date);
		int pagesCount = messagesCount == 0 ? 1 : (int) Math.ceil((double) messagesCount / (double) messagesCountPerPage);		
		return pagesCount;
	}

	@Transactional(readOnly=true)
	public boolean canEdit(IntitaUser user, TopicMessage message) {
		if(user == null || message == null)
			return false;

		if(intitaUserService.isAdmin(user.getId()))
			return true;
		IntitaUser u = message.getAuthor();
		//System.out.println(user.getId() - message.getAuthor().getId());
		if(message.getAuthor().equals(user))
		{
			Date msg_date = message.getDate();
			Date now_date = new Date();
			System.out.println(msg_date.getTime() - now_date.getTime() + 60000*15);
			if(msg_date.getTime() - now_date.getTime() + 60000*EDIT_TIME_MINUTES > 0)
				return true;
		}	
		return false;
	}

	@Transactional
	public Page<TopicMessage> getAllMessagesAndPinFirst(Long topicId,int page){
		PageRequest pageable = new PageRequest(page, messagesCountPerPage);
		ForumTopic topic = forumTopicService.getTopic(topicId);
		TopicMessage firstMessage = topicMessageRepository.findFirstByTopicOrderByDateAsc(topic);

		if (firstMessage==null)return null;
		List<TopicMessage> otherMessages =topicMessageRepository.findAllByTopicWhereMessageIdNotEqualOrderByDateAsc(topic.getId(),firstMessage.getId());
		for (TopicMessage topicMessage : otherMessages) {
			IntitaUser q =  topicMessage.getAuthor();
			System.out.println(q.getLogin());
		}
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

	@Transactional
	public Page<TopicMessage> searchInCategories (ArrayList<ForumCategory> categories, String category, int page){
		PageRequest pageable = new PageRequest(page, messagesCountPerPage);
		ArrayList<ForumTopic> array = new ArrayList<>();
		for (ForumCategory forumCategory : categories) {
			ArrayList<ForumTopic> list = forumCategoryService.getAllInludeSubCategoriesArray(forumCategory);
			if(list != null)
				array.addAll(list);
		}
		return topicMessageRepository.findByBodyLikeAndTopicInOrderByDateDesc("%cu%", array, pageable);
	}
	@Transactional
	public Page<TopicMessage> searchInCategory (ForumCategory categoryObj, String search, int page){
		PageRequest pageable = new PageRequest(page, messagesCountPerPage);
		ArrayList<ForumTopic> array = new ArrayList<>();
		ArrayList<ForumTopic> list = forumCategoryService.getAllInludeSubCategoriesArray(categoryObj);
		if(list != null)
			array.addAll(list);
		else
			return null;

		return topicMessageRepository.findByBodyLikeAndTopicInOrderByDateDesc("%" + search + "%", array, pageable);
	}
	@Transactional
	public Page<TopicMessage> searchInTopic (ForumTopic topic, String search, int page){
		PageRequest pageable = new PageRequest(page, messagesCountPerPage);
		return topicMessageRepository.findByBodyLikeAndTopicOrderByDateDesc("%" + search + "%", topic, pageable);
	}

	@Transactional
	public String getUrl(Long id){
		TopicMessage message = topicMessageRepository.findById(id);
		if(message == null)
			return null;
		ForumTopic topic = message.getTopic();
		if(topic == null)
			return null;

		ArrayList<Long> list = topicMessageRepository.getIdsListByTopic(topic.getId());
		int index = list.lastIndexOf(message.getId());
		if(index != 0)
			index -= 1 + list.size()/(messagesCountPerPage - 1) ;
		index /= messagesCountPerPage  - 1;
		String res = String.format("/view/topic/%s/%s#msg%s", topic.getId(), index + 1, message.getId()); 
		return res;
	}


	public boolean checkPostAccessToUser(Authentication  authentication,Long postId){
		TopicMessage message = getMessage(postId);
		ForumTopic topic = message.getTopic();
		if (topic==null) return false;
		return forumTopicService.checkTopicAccessToUser(authentication, topic.getId());
	}
}
