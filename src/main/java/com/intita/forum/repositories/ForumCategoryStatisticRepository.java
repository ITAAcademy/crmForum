package com.intita.forum.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.intita.forum.models.ForumCategoryStatistic;

public interface ForumCategoryStatisticRepository  extends CrudRepository<ForumCategoryStatistic, Long> {
	@Modifying
	@Query(value = "UPDATE forum_category_statistic s set messagesCount = messagesCount + 1 where s.category.id = :categoryId")
	public void incrementMessagesCount(@Param(value="categoryId") Long categoryId);
	
	@Modifying
	@Query(value = "UPDATE forum_category_statistic s set topicsCount = topicsCount + 1 where s.category.id = :categoryId")
	public void incremenTopicsCount(@Param(value="categoryId") Long categoryId);
	
	@Modifying
	@Query(value = "UPDATE forum_category_statistic s set categoriesCount = categoriesCount + 1 where s.category.id = :categoryId")
	public void incrementCategoriesCount(@Param(value="categoryId") Long categoryId);
	
	//
	
	@Modifying
	@Query(value = "UPDATE forum_category_statistic s SET s.messagesCount = s.messagesCount + 1 where s.category.id in :categories")
	public void incrementMessagesCount(@Param(value="categories") List<Long> categories);
	
	@Modifying
	@Query(value = "UPDATE forum_category_statistic s SET s.topicsCount = s.topicsCount + 1 where s.category.id in :categories")
	public void incremenTopicsCount(@Param(value="categories") List<Long> categories);
	
	@Modifying
	@Query(value = "UPDATE forum_category_statistic s SET s.categoriesCount = s.categoriesCount + 1 where s.category.id in :categories")
	public void incrementCategoriesCount(@Param(value="categories") List<Long> categories);
	
	//
	
	@Modifying
	@Query(value = "UPDATE forum_category_statistic s set messagesCount = :value where s.category.id = :categoryId")
	public void setMessagesCount(@Param(value="categoryId") Long categoryId,@Param(value="value") int value);
	
	@Modifying
	@Query(value = "UPDATE forum_category_statistic s set topicsCount = :value  where s.category.id = :categoryId")
	public void setTopicsCount(@Param(value="categoryId") Long categoryId,@Param(value="value") int value);
	
	@Modifying
	@Query(value = "UPDATE forum_category_statistic s set categoriesCount = :value  where s.category.id = :categoryId")
	public void setCategoriesCount(@Param(value="categoryId") Long categoryId,@Param(value="value") int value);
	
	@Modifying
	@Query(value = "UPDATE forum_category_statistic s set s.categoriesCount = :countOfCategories, s.messagesCount = :countOfMessages, s.topicsCount = :countOfTopics where s.category.id = :categoryId" )
	public void setStatistic(@Param(value="categoryId") Long categoryId,
			@Param(value="countOfCategories") int countOfCategories, @Param(value="countOfMessages")int countOfMessages,
			@Param(value="countOfTopics") int countOfTopics);
	
}
