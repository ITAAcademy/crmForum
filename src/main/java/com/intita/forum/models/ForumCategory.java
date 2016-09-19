package com.intita.forum.models;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.intita.forum.models.IntitaUser.IntitaUserRoles;

@Entity(name="forum_category")
public class ForumCategory {
	public ForumCategory(){

	}
	@Id
	@GeneratedValue
	private Long id;
	
private String name;
private String description;
private Date date;
private Long courseModuleId;
private Boolean isCourseCategory;
@OneToOne
private ForumTopic lastTopic;
@OneToOne
private ForumCategoryStatistic statistic;
@ElementCollection(targetClass = IntitaUserRoles.class)
@Enumerated(EnumType.STRING)
@CollectionTable(name="forum_categories_roles",joinColumns = {@JoinColumn(name="category_id")}) // use default join column name
@Column( name="role_demand", nullable=false ) 
private Set<IntitaUserRoles> rolesDemand = new HashSet<IntitaUserRoles>();

public static ForumCategory createInstance(String name,String description,boolean containSubCategories){
	ForumCategory instance = new ForumCategory();
	instance.name=name;
	instance.description=description;
	instance.date = new Date();
	return instance;
}

public static ForumCategory createInstanceForCourse(String name,String description,Long courseId){
	ForumCategory instance = new ForumCategory();
	instance.name=name;
	instance.description=description;
	instance.date = new Date();
	instance.courseModuleId = courseId;
	instance.isCourseCategory=true;
	return instance;
}

public static ForumCategory createInstanceForModule(String name,String description,Long moduleId){
	ForumCategory instance = new ForumCategory();
	instance.name=name;
	instance.description=description;
	instance.date = new Date();
	instance.courseModuleId = moduleId;
	instance.isCourseCategory=false;
	return instance;
}

@ManyToOne( fetch = FetchType.LAZY)
private ForumCategory category;

public ForumCategory getCategory() {
	return category;
}
public void setCategory(ForumCategory category) {
	this.category = category;
}
@OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
private List<ForumCategory> categories;

@OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
private List<ForumTopic> topics;

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
public Set<IntitaUserRoles> getRolesDemand() {
	return rolesDemand;
}
public void setRolesDemand(Set<IntitaUserRoles> rolesDemand) {
	this.rolesDemand = rolesDemand;
}
public void addRoleDemand(IntitaUserRoles role){
	this.rolesDemand.add(role);
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

public ForumTopic getLastTopic() {
	return lastTopic;
}

public void setLastTopic(ForumTopic lastTopic) {
	this.lastTopic = lastTopic;
}

public ForumCategoryStatistic getStatistic() {
	return statistic;
}

public void setStatistic(ForumCategoryStatistic statistic) {
	this.statistic = statistic;
}


}
