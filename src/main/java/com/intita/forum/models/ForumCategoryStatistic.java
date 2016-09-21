package com.intita.forum.models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity(name="forum_category_statistic")
public class ForumCategoryStatistic {
	@Id
	@GeneratedValue
	private Long id;
private int topicsCount;
private int messagesCount;
private int categoriesCount;

@OneToOne(mappedBy="statistic")
ForumCategory category;
public ForumCategory getCategory() {
	return category;
}
public void setCategory(ForumCategory category) {
	this.category = category;
}
public ForumCategoryStatistic(){
	
}
public ForumCategoryStatistic(int topicsCount,int categoriesCount,int messagesCount){
	this.topicsCount = topicsCount;
	this.messagesCount = messagesCount;
	this.categoriesCount = categoriesCount;
}
public int getTopicsCount() {
	return topicsCount;
}
public void setTopicsCount(int topicsCount) {
	this.topicsCount = topicsCount;
}
public int getMessagesCount() {
	return messagesCount;
}
public void setMessagesCount(int messagesCount) {
	this.messagesCount = messagesCount;
}
public int getCategoriesCount() {
	return categoriesCount;
}
public void setCategoriesCount(int categoriesCount) {
	this.categoriesCount = categoriesCount;
}
public Long getId() {
	return id;
}
public void setId(Long id) {
	this.id = id;
}

}
