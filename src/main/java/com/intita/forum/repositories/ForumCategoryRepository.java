package com.intita.forum.repositories;

import java.util.ArrayList;
import java.util.HashSet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumTopic;

public interface ForumCategoryRepository  extends CrudRepository<ForumCategory, Long> {
	Page<ForumCategory> findAll(Pageable pageable);
	Page<ForumCategory> findByCategory(ForumCategory category, Pageable pageable);
	ArrayList<ForumCategory> findByCategory(ForumCategory category);
	@Query(value = "SELECT t FROM forum_topic t WHERE date = ( select max ( date ) FROM forum_topic t WHERE t.category.id IN (:categoriesIds))")
	ForumTopic getLastTopic(@Param(value = "categoriesIds") HashSet<Long> categoriesIds);
}
