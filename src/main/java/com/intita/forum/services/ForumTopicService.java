package com.intita.forum.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.forum.config.CustomAuthenticationProvider;
import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumTopic;
import com.intita.forum.models.IntitaUser;
import com.intita.forum.models.IntitaUser.IntitaUserRoles;
import com.intita.forum.repositories.ForumTopicRepository;

@Service
public class ForumTopicService {
	@Autowired ForumTopicRepository forumTopicRepository;
	@Value("${forum.categoriesOrTopicsCountPerPage}")
	private int topicsCountForPage;
	@Autowired ForumCategoryService forumCategoryService;
	@Autowired IntitaUserService intitaUserService;
	@Autowired private CustomAuthenticationProvider authenticationProvider;
	
	@Transactional
	public Page<ForumTopic> getAllTopics(Long categoryId,int page){
		return forumTopicRepository.findByCategoryId(categoryId,new PageRequest(page,topicsCountForPage)); 
	}
	@Transactional
	public Page<ForumTopic> getAllTopicsSortedByPin(Long categoryId,int page){
		  PageRequest pageable = new PageRequest(page, topicsCountForPage);
		  List<ForumTopic> unPinnedTopics = forumTopicRepository.findByCateogryIdAndSortByPin(categoryId,false);
		  List<ForumTopic> pinnedTopics = forumTopicRepository.findByCateogryIdAndSortByPin(categoryId,true);
		  List<ForumTopic> allTopics = new ArrayList<ForumTopic>();
		  allTopics.addAll(pinnedTopics);
		  allTopics.addAll(unPinnedTopics);		 
		  int max = (topicsCountForPage*(page+1)>allTopics.size())? allTopics.size(): topicsCountForPage*(page+1);
		  Page<ForumTopic> pageObj = new PageImpl<ForumTopic>(allTopics.subList(page*topicsCountForPage, max),pageable,allTopics.size());
		
		return pageObj;
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
		return forumTopicRepository.save(topic);
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
		String id =  (String)authentication.getPrincipal();
		Long longId = Long.parseLong(id);
		IntitaUser currentUser = intitaUserService.getUser(longId);
		if(intitaUserService.hasAllRolesSets(currentUser.getId(),demandsList)){
			return true;
		}
		return false;
	}
}
