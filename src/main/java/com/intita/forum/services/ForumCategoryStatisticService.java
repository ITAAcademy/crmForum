package com.intita.forum.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumCategoryStatistic;
import com.intita.forum.models.ForumTopic;
import com.intita.forum.repositories.ForumCategoryStatisticRepository;


@Service
public class ForumCategoryStatisticService {
	private final static Logger log = LoggerFactory.getLogger(ForumCategoryStatisticService.class);
	@Autowired ForumCategoryStatisticRepository statisticRepository;
	@Autowired ForumCategoryService forumCategoryService;
	@Autowired ForumTopicService forumTopicService;
	@Autowired TopicMessageService topicMessagesService;
public void incrementTopicsCount(Long categoryId){
	ForumCategory category = forumCategoryService.getCategoryById(categoryId);
	ForumCategoryStatistic statistic = category.getStatistic();
	int topicsCount = statistic.getTopicsCount();
	statistic.setTopicsCount(topicsCount+1);
	statisticRepository.save(statistic);
}

public void incrementCategoriesCount(Long categoryId){
	ForumCategory category = forumCategoryService.getCategoryById(categoryId);
	ForumCategoryStatistic statistic = category.getStatistic();
	int categoriesCount = statistic.getCategoriesCount();
	statistic.setCategoriesCount(categoriesCount+1);
	statisticRepository.save(statistic);
}
@Transactional
public void incrementTopicMessagesCount(Long categoryId){
	ForumCategory category = forumCategoryService.getCategoryById(categoryId);
	ForumCategoryStatistic statistic = category.getStatistic();
	int messagesCount = statistic.getMessagesCount();
	statistic.setMessagesCount(messagesCount+1);
	statisticRepository.save(statistic);
}
@Transactional
public void incrementTopicsCount(List<Long> categories){
	for (Long categoryId : categories){
		incrementTopicsCount(categoryId);
	}
}
@Transactional
public void incrementCategoriesCount(List<Long> categories){
	for (Long categoryId : categories){
		incrementCategoriesCount(categoryId);
	}
}
@Transactional
public void incrementTopicMessagesCount(List<Long> categories){
	for (Long categoryId : categories){
		incrementTopicMessagesCount(categoryId);
	}
}
@Transactional
public void setTopicsCount(Long categoryId, int count ){
	statisticRepository.setTopicsCount(categoryId,count);
}
@Transactional
public void setCategoriesCount(Long categoryId,int count){
	statisticRepository.setCategoriesCount(categoryId,count);
}
@Transactional
public void setMessagesCount(Long categoryId,int count){
	statisticRepository.setMessagesCount(categoryId,count);
}
/**
 * Creates Empty statistic to be filled by {@link #updateCategorieStatistic(Long categoryId) updateCategorieStatistic}
 */
@Transactional
public void createEmptyCategoriesStatisticForAllCategories(){
	ArrayList<Long> categories = forumCategoryService.getAllCategoriesIds();
	long startTime = new Date().getTime();
	long endTime = startTime;
	for (Long categoryId : categories){
		ForumCategory category = forumCategoryService.getCategoryById(categoryId);
		endTime = new Date().getTime() - startTime;
		ForumCategoryStatistic statistic = new ForumCategoryStatistic();
		statistic = statisticRepository.save(statistic);
		category.setStatistic(statistic);
		forumCategoryService.update(category);
	}
	
}

/**
 * Update all categories statistic and return ids of updated categories;
 * @param categoryId
 */
@Transactional
public void updateAllCategoriesStatistic(){
	ArrayList<ForumCategory> categories = forumCategoryService.getRootCategories();
	for (ForumCategory category : categories)
		updateCategoriesStatistic(category);		
}
/*@Transactional
public void updateCategorieStatistic(Long categoryId){
	long startTime = new Date().getTime();
	long endTime = startTime;
	HashSet<Long> categories = forumCategoryService.getAllSubCategoriesIds(categoryId, null);
	int categoriesCount = categories.size();
	ForumCategory category= forumCategoryService.getCategoryById(categoryId);
	HashSet<Long> topics = forumTopicService.getAllSubTopicsIds(category,categories);
	int messagesCount = topicMessagesService.getTotalMessagesCountByTopicsIds(topics);
	ForumCategoryStatistic statistic = category.getStatistic();
	if (statistic == null) 
		statistic = new ForumCategoryStatistic();
	statistic.setCategory(category);
	statistic.setCategoriesCount(categoriesCount);
	statistic.setTopicsCount(topics.size());
	statistic.setMessagesCount(messagesCount);
	statistic = statisticRepository.save(statistic);
	category.setStatistic(statistic);
	forumCategoryService.update(category);
	endTime = new Date().getTime();
	long deltaTime = endTime - startTime;
	//statisticRepository.setStatistic(categoryId,categoriesCount, 0,0);
}*/
@Transactional
public ForumCategoryStatistic updateCategoriesStatistic(ForumCategory category){
	List<ForumCategory> subcategories = forumCategoryService.getSubCategories(category);
	HashSet<Long> topics = forumTopicService.getAllTopicsIds(category.getId());
	int messagesCount = topicMessagesService.getTotalMessagesCountByTopicsIds(topics);
	int topicsCount = topics == null ? 0 : topics.size();
	int categoriesCount = subcategories == null ? 0 : subcategories.size();
	ForumCategoryStatistic totalForumCategoryStatistic =  category.getStatistic();
	//check if category already has statistic mapping, create new if false and set topics,categories and messages count 
	if (totalForumCategoryStatistic==null){
		totalForumCategoryStatistic = new ForumCategoryStatistic(topicsCount,categoriesCount,messagesCount);
		totalForumCategoryStatistic = statisticRepository.save(totalForumCategoryStatistic);
	}
	else{
		totalForumCategoryStatistic.setTopicsCount(topicsCount);
		totalForumCategoryStatistic.setCategoriesCount(categoriesCount);
		totalForumCategoryStatistic.setMessagesCount(messagesCount);
	}
	//	
	
	for (ForumCategory subCategory : subcategories){
		ForumCategoryStatistic statistic = updateCategoriesStatistic(subCategory);
		totalForumCategoryStatistic.add(statistic);
	}
	//totalForumCategoryStatistic.setCategory(category);
	
	category.setStatistic(totalForumCategoryStatistic);
	//log.info("statistic:"+totalForumCategoryStatistic);
	//log.info("Category statistic saved("+category.getName()+"):"+totalForumCategoryStatistic);
	return totalForumCategoryStatistic;
	
}

@Transactional
public ForumCategoryStatistic save(ForumCategoryStatistic statistic){
	return statisticRepository.save(statistic);
}

}
