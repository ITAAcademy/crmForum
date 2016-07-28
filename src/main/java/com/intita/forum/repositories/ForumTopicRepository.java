package com.intita.forum.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumTopic;

public interface ForumTopicRepository  extends CrudRepository<ForumTopic, Long> {
	Page<ForumTopic> findAll(Pageable pageable);
	Page<ForumTopic> findByCategory(ForumCategory category,Pageable pageable);
	Page<ForumTopic> findByCategoryId(Long categoryId,Pageable pageable);
	@Query(value = "SELECT t FROM forum_topic t WHERE t.category.id=(:categoryId) AND t.pinned is (:pinned) order by t.date desc")
	List<ForumTopic> findByCateogryIdAndSortByPin(@Param(value = "categoryId")Long categoryId,@Param(value = "pinned")Boolean pinned);
}