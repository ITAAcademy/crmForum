package com.intita.forum.services;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections4.IteratorUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.forum.domain.UserSortingCriteria;
import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumTopic;
import com.intita.forum.models.IntitaUser;
import com.intita.forum.models.TopicMessage;
import com.intita.forum.repositories.TopicMessageRepository;

import utils.CustomDataConverters;

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

	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private ForumCategoryStatisticService forumCategoryStatisticService;

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
		Long topicId = message.getTopic().getId();
		ForumCategory parentCategory = forumTopicService.getTopic(topicId).getCategory();
		if (parentCategory==null) return true;
		ArrayList<Long> res = forumCategoryService.getParentCategoriesIdsIncludeTarget(parentCategory);
		forumCategoryStatisticService.incrementTopicMessagesCount(res);
		//forumCategoryStatisticService.incrementTopicMessagesCount(categoryId);
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
			long timeDelta = now_date.getTime() - msg_date.getTime();
			//System.out.println("time delta:"+timeDelta);
			if(timeDelta < 60000*EDIT_TIME_MINUTES)
				return true;
		}	
		return false;
	}

	@Transactional
	public Page<TopicMessage> getAllMessagesAndPinFirst(Long topicId,int page,UserSortingCriteria sortingCriteria){
		PageRequest pageable = new PageRequest(page, messagesCountPerPage);
		ForumTopic topic = forumTopicService.getTopic(topicId);
		TopicMessage firstMessage = topicMessageRepository.findFirstByTopicOrderByDateAsc(topic);

		if (firstMessage==null)return null;
		List<TopicMessage> otherMessages;
		if (sortingCriteria==null)
			otherMessages=topicMessageRepository.findAllByTopicWhereMessageIdNotEqualOrderByDateAsc(topic.getId(),firstMessage.getId());
		else{
			Session session = sessionFactory.getCurrentSession();
			String sortingParam = sortingCriteria.getSortingParamNameForClass(TopicMessage.class);
			String sortingPart = (sortingParam!=null) ? " ORDER BY m."+sortingParam : "";
			if (sortingPart.length()>0)sortingPart+=" "+sortingCriteria.getOrder();
			String whereParam = sortingCriteria.getDateParamNameForClass(TopicMessage.class);
			String topicIdConditionPrefix = "WHERE m.topic.id = "+topic.getId();
			String firstMessageExcludeConditionAppendix = " AND m.id<>"+firstMessage.getId();
			String wherePart = topicIdConditionPrefix +firstMessageExcludeConditionAppendix+" ";
			if (whereParam!=null)
			wherePart += " AND "+ whereParam ;
			String hql = "SELECT m FROM topic_message m " + " "+wherePart+sortingPart;
			Query query = session.createQuery(hql);
			if (whereParam!=null)
			query.setParameter("dateParam", sortingCriteria.getDateParam());
			otherMessages = query.list();
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
	public Page<TopicMessage> searchByTopicNameAndBodyAndAsAndInCategory (String search,ForumCategory category, int page){
		PageRequest pageable = new PageRequest(page, messagesCountPerPage);
		ArrayList<ForumTopic> array = new ArrayList<>();
		ArrayList<ForumTopic> list = forumCategoryService.getAllInludeSubCategoriesArray(category);
		if(list != null)
			array.addAll(list);
		else
			return null;
		return topicMessageRepository.findByBodyLikeOrTopicNameLikeAndTopicInOrderByDateDesc("%" + search + "%", "%" + search + "%", array, pageable);
	}
	@Transactional
	public Page<TopicMessage> searchByTopicNameAsAndInCategory (String search,ForumCategory category, int page){
		PageRequest pageable = new PageRequest(page, messagesCountPerPage);
		ArrayList<ForumTopic> array = new ArrayList<>();
		ArrayList<ForumTopic> list = forumCategoryService.getAllInludeSubCategoriesArray(category);
		if(list != null)
			array.addAll(list);
		else
			return null;
		return topicMessageRepository.findByTopicNameLikeAndTopicInOrderByDateDesc("%" + search + "%", array, pageable);
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
	
	public int getTotalMessagesCountByTopicsIds(HashSet<Long> topicIds){
		if (topicIds.size()<1) return 0;
		return topicMessageRepository.getMessagesCountInTopics(topicIds);
	}
	public int getMessageCountByTopicId(Long id){
		return topicMessageRepository.getMessagesCountInTopic(id);
	}
}
