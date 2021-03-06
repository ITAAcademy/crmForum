package com.intita.forum.repositories;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumTopic;
import com.intita.forum.models.IntitaUser;
import com.intita.forum.models.TopicMessage;

@Qualifier("IntitaConf") 
public interface TopicMessageRepository  extends CrudRepository<TopicMessage, Long>{
	  TopicMessage findById(Long id);
	  Page<TopicMessage> findAll(Pageable pageable);
	  
	  @Query(value = "SELECT m FROM topic_message m WHERE m.id <> (:messageId) and topic.id = (:topicId) order by date asc")
	  ArrayList<TopicMessage> findAllByTopicWhereMessageIdNotEqualOrderByDateAsc(@Param(value = "topicId")Long topicId,@Param(value = "messageId")Long messageid);
	  @Query(value = "SELECT count(*) FROM topic_message m WHERE topic.id = (:topicId) and date <= (:date)")
	  Long findAllByTopicAndDateBeforeWithLast(@Param(value = "topicId")Long topicId,@Param(value = "date") Date date);
	  
	  ArrayList<TopicMessage> findByAuthor(IntitaUser author);
	  Page<TopicMessage> findByTopic(ForumTopic topic,Pageable page);
	  Page<TopicMessage> findByTopicId(Long topicId,Pageable page);
	  ArrayList<TopicMessage> findFirst20ByTopicOrderByIdDesc(ForumTopic topic);
	  
	  ArrayList<TopicMessage> findFirst10ByTopicAndDateAfter(ForumTopic topic, Date date);
	  ArrayList<TopicMessage> findFirst10ByTopicAndDateBeforeOrderByIdDesc(ForumTopic topic, Date date);
	  ArrayList<TopicMessage> findAllByTopicAndDateAfter(ForumTopic topic, Date date);
	  ArrayList<TopicMessage> findAllByTopicAndDateAfterAndAuthorNot(ForumTopic topic, Date date, IntitaUser user);
	  Long countByTopicAndDateAfterAndAuthorNot(ForumTopic topic, Date date, IntitaUser user);
	  List<TopicMessage> findAllByDateAfterAndAuthorNot( Date date, IntitaUser user);
	  Set<TopicMessage> findAllByAuthorNot(IntitaUser user);
	  ArrayList<TopicMessage> findAllByDateAfter(Date date);
	  TopicMessage findFirstByTopicOrderByDateDesc(ForumTopic topic);
	  TopicMessage findFirstByTopicOrderByDateAsc(ForumTopic topic);
	  
	  Page<TopicMessage> findByBodyLikeAndTopicInOrderByDateDesc(String like, ArrayList<ForumTopic> topics,Pageable page);
	  Page<TopicMessage> findByBodyLikeOrTopicNameLikeAndTopicInOrderByDateDesc(String like1, String like2, ArrayList<ForumTopic> topics,Pageable page);
	  Page<TopicMessage> findByTopicNameLikeAndTopicInOrderByDateDesc(String like, ArrayList<ForumTopic> topics,Pageable page);
	  Page<TopicMessage> findByBodyLikeAndTopicOrderByDateDesc(String like, ForumTopic topic,Pageable page);
	  Page<TopicMessage> findByTopicNameLikeOrderByDateDesc(String like,Pageable page);
	  
	  @Query(value = "SELECT u.id FROM topic_message u WHERE topic.id = :id order by date asc")
	  ArrayList<Long> getIdsListByTopic(@Param("id")Long topicId);
	  
	  @Query(value = "SELECT count(u) FROM topic_message u WHERE topic.id in :topicsIds")
	  int getMessagesCountInTopics(@Param("topicsIds") HashSet<Long> topicsIds);
	  @Query(value = "SELECT count(u) FROM topic_message u WHERE topic.id = :topicIds")
	  int getMessagesCountInTopic(@Param("topicIds") Long topicIds);
}
