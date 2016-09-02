package com.intita.forum.repositories;

import java.util.ArrayList;
import java.util.Date;
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
	Page<ForumCategory> findByCategoryAndDate(ForumCategory category, Pageable pageable);
	Page<ForumCategory> findByCategoryOrderByDate(ForumCategory category,Pageable pageable);
	Page<ForumCategory> findByCategoryOrderByName(ForumCategory category,String name, Pageable pageable);
	ArrayList<ForumCategory> findByCategory(ForumCategory category);
	@Query(value = "SELECT t FROM forum_topic t WHERE date = ( select max ( date ) FROM forum_topic t WHERE t.category.id IN (:categoriesIds))")
	ForumTopic getLastTopic(@Param(value = "categoriesIds") HashSet<Long> categoriesIds);
	int countByCourseModuleIdAndIsCourseCategory(Long id,Boolean isCourseCategory);
	@Query(value = "SELECT c from forum_category c WHERE c.name = (:name) AND c.date = (select MIN(date) from forum_category)")
	ArrayList<ForumCategory> findFirstByNameWhereDateEqualMinDate(@Param(value = "name") String name);
	@Query(value = "SELECT COUNT(c) from forum_category c WHERE c.courseModuleId = (:courseOrModuleId) AND c.isCourseCategory = (:isCourseCategory)")
	int countByCourseOrModuleIdAndIsCourseCategory(@Param(value = "courseOrModuleId") Long courseOrModuleId,@Param(value="isCourseCategory") boolean isCourseCategory);
	@Query(value = "SELECT c from forum_category c WHERE c.courseModuleId = (:courseOrModuleId) AND c.isCourseCategory = (:isCourseCategory)")
	ArrayList<ForumCategory> findByCourseOrModuleIdAndIsCourseCategory(@Param(value = "courseOrModuleId") Long courseOrModuleId,@Param(value="isCourseCategory") boolean isCourseCategory);
	ArrayList<ForumCategory> findByNameAndCategoryId(String name,Long categoryId);
	ArrayList<ForumCategory> findByNameAndCategoryIdIsNull(String name);
	@Query(value = "SELECT COUNT(c) from forum_category c WHERE c.name = (:name) AND c.category.id = (:categoryId)")
	int countByNameAndCategoryId(@Param(value = "name") String name,@Param(value = "categoryId") Long categoryId);
	@Query(value = "SELECT COUNT(c) from forum_category c WHERE (name = (:name) AND courseModuleId = (:courseOrModuleId) AND date = (select MIN(date) from forum_category))")
	int countByNameAndCourseOrModuleId(@Param(value = "name") String name,@Param(value = "courseOrModuleId") Long courseOrModuleId);
	@Query(value = "SELECT c from forum_category c WHERE c.name = (:name) AND c.courseModuleId = (:courseOrModuleId) AND c.date = (select MIN(date) from forum_category)")
	ArrayList<ForumCategory> findFirstByNameAndCourseOrModuleIdWhereDateEqualMinDate(@Param(value = "name") String name,@Param(value = "courseOrModuleId") Long courseOrModuleId);
}

