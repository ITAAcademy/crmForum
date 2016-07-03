package com.intita.forum.repositories;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.intita.forum.models.ForumTopic;
import com.intita.forum.models.ForumUser;
import com.intita.forum.models.TopicMessage;

@Qualifier("IntitaConf") 
public interface TopicMessageRepository  extends CrudRepository<TopicMessage, Long>{
	  Page<TopicMessage> findById(Long id, Pageable pageable);
	  ArrayList<TopicMessage> findAll(Pageable pageable);
	  ArrayList<TopicMessage> findByAuthor(ForumUser author);
	  ArrayList<TopicMessage> findByTopic(ForumTopic topic);
	  ArrayList<TopicMessage> findFirst20ByTopicOrderByIdDesc(ForumTopic topic);
	  
	  ArrayList<TopicMessage> findFirst10ByTopicAndDateAfter(ForumTopic topic, Date date);
	  ArrayList<TopicMessage> findFirst10ByTopicAndDateBeforeOrderByIdDesc(ForumTopic topic, Date date);
	  ArrayList<TopicMessage> findAllByTopicAndDateAfter(ForumTopic topic, Date date);
	  ArrayList<TopicMessage> findAllByTopicAndDateAfterAndAuthorNot(ForumTopic topic, Date date, ForumUser user);
	  Long countByTopicAndDateAfterAndAuthorNot(ForumTopic topic, Date date, ForumUser user);
	  List<TopicMessage> findAllByDateAfterAndAuthorNot( Date date, ForumUser user);
	  Set<TopicMessage> findAllByAuthorNot(ForumUser user);
	  ArrayList<TopicMessage> findAllByDateAfter(Date date);
	  TopicMessage findFirstByTopicOrderByDateDesc(ForumTopic topic);
}
