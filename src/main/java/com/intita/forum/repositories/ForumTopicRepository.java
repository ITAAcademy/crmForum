package com.intita.forum.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumTopic;

public interface ForumTopicRepository  extends CrudRepository<ForumTopic, Long> {
	Page<ForumTopic> findAll(Pageable pageable);
	Page<ForumTopic> findByCategory(ForumCategory category,Pageable pageable);
	Page<ForumTopic> findByCategoryId(Long categoryId,Pageable pageable);
	
}