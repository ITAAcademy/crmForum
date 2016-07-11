package com.intita.forum.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity(name="course_modules")
public class CourseModule {

@Id
@NotNull
@GeneratedValue
Long id;
@Column(name="id_course")
Long idCourse;
@Column(name="id_module")
Long moduleId;
Integer order;
@Column(name="mandatory_modules")
Integer mandatoryModules;
@Column(name="price_in_course")
Integer priceInCourse;
public Long getId() {
	return id;
}
public void setId(Long id) {
	this.id = id;
}
public Long getIdCourse() {
	return idCourse;
}
public void setIdCourse(Long idCourse) {
	this.idCourse = idCourse;
}
public Long getModuleId() {
	return moduleId;
}
public void setModuleId(Long moduleId) {
	this.moduleId = moduleId;
}
public Integer getOrder() {
	return order;
}
public void setOrder(Integer order) {
	this.order = order;
}
public Integer getMandatoryModules() {
	return mandatoryModules;
}
public void setMandatoryModules(Integer mandatoryModules) {
	this.mandatoryModules = mandatoryModules;
}
public Integer getPriceInCourse() {
	return priceInCourse;
}
public void setPriceInCourse(Integer priceInCourse) {
	this.priceInCourse = priceInCourse;
}
}
