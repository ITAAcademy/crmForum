package com.intita.forum.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@Entity(name="forum_category_statistic")
public class ForumCategoryStatistic {
	@Id
	@GeneratedValue
	private Long id;
private int topicsCount;
private int messagesCount;
private int categoriesCount;

/**
 * WARNING. Maybe error hiding
 */
//@NotFound(action = NotFoundAction.IGNORE)
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
public void add (ForumCategoryStatistic statistic){
	this.topicsCount += statistic.topicsCount;
	this.messagesCount += statistic.messagesCount;
	this.categoriesCount += statistic.categoriesCount;
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
@Override
public String toString(){
	return "categories:"+categoriesCount+" topics:"+topicsCount+" messages:"+messagesCount;
}

}
