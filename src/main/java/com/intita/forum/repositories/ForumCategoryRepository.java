package com.intita.forum.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.intita.forum.models.ForumCategory;

public interface ForumCategoryRepository  extends CrudRepository<ForumCategory, Long> {
	Page<ForumCategory> findAll(Pageable pageable);
	Page<ForumCategory> findByCategory(ForumCategory category, Pageable pageable);
	
}
