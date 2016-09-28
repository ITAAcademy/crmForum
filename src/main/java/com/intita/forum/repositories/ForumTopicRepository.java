package com.intita.forum.repositories;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	List<ForumTopic> findByCategoryId(Long categoryId);
	@Query(value="SELECT t.id FROM forum_topic t WHERE t.category.id=(:categoryId)")
	HashSet<Long> findAllIdsByCategory(@Param(value="categoryId")Long categoryId);
	@Query(value = "SELECT t FROM forum_topic t WHERE t.category.id=(:categoryId) AND t.pinned is (:pinned) order by t.date desc")
	List<ForumTopic> findByCateogryIdAndSortByPin(@Param(value = "categoryId")Long categoryId,@Param(value = "pinned")Boolean pinned);
	@Query(value = "SELECT COUNT(t) FROM forum_topic t WHERE t.category.id in (:categoryIds)")
	int countByCategoryIds(@Param(value = "categoryIds")List<Long> categoryIds);
	@Query(value = "SELECT t.id FROM forum_topic t WHERE t.category.id in (:categoryIds)")
	HashSet<Long> findIdsByCategoryIds(@Param(value = "categoryIds")Set<Long> categoryIds);
	@Query(value = "SELECT t FROM forum_topic t WHERE category.id=:categoryId AND t.date > :paramDate order by t.date desc")
	List<ForumTopic> findInCategoryWhereDateGreaterThenParamDateSortByDateDesc(@Param(value="categoryId") Long categoryId,@Param(value="paramDate")Date paramDate);
	@Query(value = "SELECT t FROM forum_topic t WHERE category.id=:categoryId order by t.date desc")
	List<ForumTopic> findInCategorySortByDateDesc(@Param(value="categoryId") Long categoryId);
}