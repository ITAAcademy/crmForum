package com.intita.forum.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumTopic;
import com.intita.forum.repositories.ForumTopicRepository;

@Service
public class ForumTopicService {
	@Autowired ForumTopicRepository forumTopicRepository;
	@Value("${forum.categoriesOrTopicsCountPerPage}")
	private int topicsCountForPage;
	@Transactional
	public Page<ForumTopic> getAllTopics(Long categoryId,int page){
		return forumTopicRepository.findByCategoryId(categoryId,new PageRequest(page,topicsCountForPage)); 
	}
	@Transactional
	public ForumTopic getTopic(Long topicId){
		return forumTopicRepository.findOne(topicId);
	}
}
