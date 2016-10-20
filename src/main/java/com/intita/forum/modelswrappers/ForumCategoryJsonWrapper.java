package com.intita.forum.modelswrappers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.intita.forum.models.ForumCategory;

public class ForumCategoryJsonWrapper {
	private Long id;	
	private String name;
private String description;
private Date date;
private Long courseModuleId;
private Boolean isCourseCategory;
private ForumCategoryStatisticJsonWrapper statistic;
private boolean isLastTopicAccessible = true;
private ForumTopicJsonWrapper lastTopic;
public ForumCategoryJsonWrapper(ForumCategory category){
	this.id = category.getId();
	this.name = category.getName();
	this.description = category.getDescription();
	this.date = category.getDate();
	this.isCourseCategory = category.getIsCourseCategory();
	this.statistic = new ForumCategoryStatisticJsonWrapper(category.getStatistic());
	this.lastTopic =  category.getLastTopic()==null?null : new ForumTopicJsonWrapper(category.getLastTopic());
}
public Long getId() {
	return id;
}
public void setId(Long id) {
	this.id = id;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public String getDescription() {
	return description;
}
public void setDescription(String description) {
	this.description = description;
}
public Date getDate() {
	return date;
}
public void setDate(Date date) {
	this.date = date;
}
public Long getCourseModuleId() {
	return courseModuleId;
}
public void setCourseModuleId(Long courseModuleId) {
	this.courseModuleId = courseModuleId;
}
public Boolean getIsCourseCategory() {
	return isCourseCategory;
}
public void setIsCourseCategory(Boolean isCourseCategory) {
	this.isCourseCategory = isCourseCategory;
}
public static List<ForumCategoryJsonWrapper> convertList(List<ForumCategory> categories){
	List<ForumCategoryJsonWrapper> result= new ArrayList<ForumCategoryJsonWrapper>();
	for (ForumCategory category : categories)
		result.add(new ForumCategoryJsonWrapper(category));
	return result;
}
public ForumCategoryStatisticJsonWrapper getStatistic() {
	return statistic;
}
public void setStatistic(ForumCategoryStatisticJsonWrapper statistic) {
	this.statistic = statistic;
}
public boolean isLastTopicAccessible() {
	return isLastTopicAccessible;
}
public void setLastTopicAccessible(boolean isAccessible) {
	this.isLastTopicAccessible = isAccessible;
}
public ForumTopicJsonWrapper getLastTopic() {
	return lastTopic;
}
public void setLastTopic(ForumTopicJsonWrapper lastTopic) {
	this.lastTopic = lastTopic;
}

}
