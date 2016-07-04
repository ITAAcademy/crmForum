package com.intita.forum.models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity(name="forum_category")
public class ForumCategory {
	public ForumCategory(){
		
	}
	@Id
	@GeneratedValue
	private Long id;
	
	public enum CategoryChildrensType {ChildrenCategory,ChildrenTopic};
private CategoryChildrensType categoryChildrensType;
private String name;
private String description;


@ManyToOne( fetch = FetchType.LAZY)
private ForumCategory parentCategory;

public ForumCategory getParentCategory() {
	return parentCategory;
}
public void setParentCategory(ForumCategory parentCategory) {
	this.parentCategory = parentCategory;
}
@OneToMany(mappedBy = "parentCategory", fetch = FetchType.LAZY)
private List<ForumCategory> categories;

@OneToMany(mappedBy = "parentCategory", fetch = FetchType.LAZY)
private List<ForumTopic> topics;

public CategoryChildrensType getCategoryChildrensType() {
	return categoryChildrensType;
}
public void setCategoryChildrensType(CategoryChildrensType categoryChildrensType) {
	this.categoryChildrensType = categoryChildrensType;
}
public String getName() {
	return name;
}
public void setName(String categoryName) {
	this.name = categoryName;
}
public List<ForumCategory> getCategories() {
	return categories;
}
public void setCategories(List<ForumCategory> categories) {
	this.categories = categories;
}
public List<ForumTopic> getTopics() {
	return topics;
}
public void setTopics(List<ForumTopic> topics) {
	this.topics = topics;
}
public String getDescription() {
	return description;
}
public void setDescription(String description) {
	this.description = description;
}
public ForumCategory(Long id){
	this.id=id;
}
public Long getId() {
	return id;
}
public void setId(Long id) {
	this.id = id;
}

}
