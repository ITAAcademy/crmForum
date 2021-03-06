package com.intita.forum.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.forum.config.CustomAuthenticationProvider;
import com.intita.forum.domain.UserSortingCriteria;
import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumTopic;
import com.intita.forum.models.IntitaUser;
import com.intita.forum.models.IntitaUser.IntitaUserRoles;
import com.intita.forum.models.ForumCategoryStatistic;
import com.intita.forum.repositories.ForumTopicRepository;

import utils.CustomDataConverters;

@Service
public class ForumTopicService {
	@Value("${forum.categoriesOrTopicsCountPerPage}")
	private int topicsCountForPage;
	@Autowired ForumTopicRepository forumTopicRepository;
	@Autowired ForumCategoryService forumCategoryService;
	@Autowired TopicMessageService topicMessageService;
	@Autowired IntitaUserService intitaUserService;
	@Autowired private CustomAuthenticationProvider authenticationProvider;
	@Autowired private ForumCategoryStatisticService forumCategoryStatisticService;
	@Autowired 
	private SessionFactory sessionFactory;
	private final static Logger log = LoggerFactory.getLogger(ForumTopicService.class);
	
	@Transactional
	public ForumTopic getLastTopicAfterDate(Long categoryId, Date paramDate){
		//if (paramDate == null) 
		List<ForumTopic> topics = forumTopicRepository.findInCategoryWhereDateGreaterThenParamDateSortByDateDesc(categoryId,paramDate);
		ForumTopic lastTopic  = topics.size()>0 ? topics.get(0) : null;
		return lastTopic;
	}
	@Transactional
	public Page<ForumTopic> getAllTopics(Long categoryId,int page){
		return forumTopicRepository.findByCategoryId(categoryId,new PageRequest(page,topicsCountForPage)); 
	}
	@Transactional
	public List<ForumTopic> getAllTopics(Long categoryId){
		return forumTopicRepository.findByCategoryId(categoryId);
	}
	@Transactional
	public HashSet<Long> getAllTopicsIds(Long categoryId){
		return forumTopicRepository.findAllIdsByCategory(categoryId);
	}
	@Transactional
	public ForumTopic getLastTopic(Long categoryId){
		List<ForumTopic> lastTopics = forumTopicRepository.findInCategorySortByDateDesc(categoryId);
		ForumTopic lastTopic = lastTopics.size() > 0 ? lastTopics.get(0) : null;
		return lastTopic;
	}
	@Transactional
	public Page<ForumTopic> getAllTopicsSortedByPin(Long categoryId,int page,UserSortingCriteria sortingCriteria){
		 if (sortingCriteria==null){
		List<ForumTopic> unPinnedTopics = forumTopicRepository.findByCateogryIdAndSortByPin(categoryId,false);
		  List<ForumTopic> pinnedTopics = forumTopicRepository.findByCateogryIdAndSortByPin(categoryId,true);
		  List<ForumTopic> allTopics = new ArrayList<ForumTopic>();
		  allTopics.addAll(pinnedTopics);
		  allTopics.addAll(unPinnedTopics);		 
		 return CustomDataConverters.listToPage(allTopics, page, topicsCountForPage);
		 }
		 else{
			 {
					Session session = sessionFactory.getCurrentSession();
					String sortingParam = sortingCriteria.getSortingParamNameForClass(ForumTopic.class);
					String defaultOrder = "ORDER BY t.pinned desc";
					String sortingPart = (sortingParam!=null) ? defaultOrder+",  t."+sortingParam : defaultOrder;
					if (sortingPart.length()>0)sortingPart+=" "+sortingCriteria.getOrder();
					String whereParam = sortingCriteria.getDateParamNameForClass(ForumTopic.class);
					String wherePart = "WHERE t.category.id = "+categoryId +" ";
					if (whereParam!=null)
					wherePart += " AND "+ whereParam ;
					String hql = "SELECT t FROM forum_topic t " + " "+wherePart+sortingPart;
					Query query = session.createQuery(hql);
					if (whereParam!=null)
					query.setParameter("dateParam", sortingCriteria.getDateParam());
					List<ForumTopic> resultList = query.list();
					Page<ForumTopic> result = CustomDataConverters.listToPage(resultList,page,topicsCountForPage);
					return  result;
				}
		 }
	}
	@Transactional
	public ForumTopic getTopic(Long topicId){
		return forumTopicRepository.findOne(topicId);
	}
	@Transactional
	public boolean toggleTopicPin(Long topicId){
		ForumTopic topic = forumTopicRepository.findOne(topicId);
		if (topic==null) return false;
		boolean isPinned = topic.isPinned();
		topic.setPinned(!isPinned);
		return true;
	}
	@Transactional
	public ForumTopic addTopic(ForumTopic topic){
		return forumTopicRepository.save(topic);
	}
	@Transactional
	public ForumTopic addTopic(String name,ForumCategory category,IntitaUser author){
		ForumTopic topic = new ForumTopic();
		topic.setName(name);
		topic.setAuthor(author);
		topic.setCategory(category);
		topic = forumTopicRepository.save(topic);
		forumCategoryService.updateLastTopic(category, topic);
		ArrayList<Long> res = forumCategoryService.getParentCategoriesIdsIncludeTarget(topic.getCategory());
		forumCategoryStatisticService.incrementTopicsCount(res);
		return topic ;
	}
	public LinkedList<Set<IntitaUserRoles>> getDemandsForTopic(Long topicId){
		if (topicId==null)return null;
		ForumTopic topic = getTopic(topicId);
		if (topic==null) return null;
		LinkedList<Set<IntitaUserRoles>> roles = new LinkedList<Set<IntitaUserRoles>>();
		ForumCategory parent = topic.getCategory();
		HashSet<Long> ids = new HashSet<Long>();
		while(parent!=null){
			Set<IntitaUserRoles> rolesSet = parent.getRolesDemand();
			roles.add(rolesSet);
			//exit if category met before to prevent infinite cycle
			if (ids.contains(parent.getId()))break;
			ids.add(parent.getId());
			ForumCategory parentCategoryTemp = parent.getCategory();
			if (parentCategoryTemp==null) break; 
			parent=forumCategoryService.getCategoryById(parentCategoryTemp.getId());
		}
		return roles;
	}
	public boolean checkTopicAccessToUser(Authentication  authentication,Long topicId){
		ForumTopic topic = getTopic(topicId);
		if (topic==null) return false;
		LinkedList<Set<IntitaUserRoles>> demandsList = getDemandsForTopic(topicId);
		//authentication = authenticationProvider.autorization(authenticationProvider);
		IntitaUser currentUser = (IntitaUser) authentication.getPrincipal();
		if(intitaUserService.hasAllRolesSets(currentUser.getId(),demandsList)){
			return true;
		}
		return false;
	}
	@Transactional
	public HashSet<Long> getAllSubTopicsIdsByCategoriesIds(ForumCategory category,HashSet<Long> cachedAllSubcategories){
		
		HashSet<Long> categories = cachedAllSubcategories != null ? cachedAllSubcategories : forumCategoryService.getSubCategoriesIds(category);
		HashSet<Long> currentCategoryTopics =  forumTopicRepository.findAllIdsByCategory(category.getId());
		if (categories==null || categories.size()<1){
			return currentCategoryTopics;
		}
			//categories = new HashSet<Long>();
		//categories.add(category.getId());
		HashSet<Long> topicIds = forumTopicRepository.findIdsByCategoryIds(categories);
		topicIds.addAll(currentCategoryTopics);
		return topicIds;
	}
	@Transactional
	public HashSet<Long> getAllSubTopicsIdsByCategories(ForumCategory category,List<ForumCategory> cachedAllSubcategories){
		if (cachedAllSubcategories==null) return getAllSubTopicsIdsByCategoriesIds(category,null);
		HashSet<Long> cachedAllSubcategoriesIds = new HashSet<Long>();
		for (ForumCategory categorie : cachedAllSubcategories){
			cachedAllSubcategoriesIds.add(categorie.getId());
		}
		return getAllSubTopicsIdsByCategoriesIds(category,cachedAllSubcategoriesIds);
	}
	
	public List<ForumCategoryStatistic> getTopicsStatisticByIds(List<Long> topicsIds){
		
		List<ForumCategoryStatistic> topicsStatistic = new ArrayList<ForumCategoryStatistic>();
		for (Long topicId : topicsIds){
			int messagesCount = topicMessageService.getMessageCountByTopicId(topicId);
			topicsStatistic.add(new ForumCategoryStatistic(0,0,messagesCount));
		}
		return topicsStatistic;
	}
	public List<ForumCategoryStatistic> getTopicsStatistic(List<ForumTopic> topics){
		
		List<ForumCategoryStatistic> topicsStatistic = new ArrayList<ForumCategoryStatistic>();
		for (ForumTopic topic : topics){
			int messagesCount = topicMessageService.getMessageCountByTopicId(topic.getId());
			topicsStatistic.add(new ForumCategoryStatistic(0,0,messagesCount));
		}
		return topicsStatistic;
	}

}
