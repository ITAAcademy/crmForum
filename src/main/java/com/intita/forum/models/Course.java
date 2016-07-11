package com.intita.forum.models;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity(name="course")
public class Course {
	@Id
	@NotNull
	@GeneratedValue
	@Column(name="course_ID")
	private Long id;
	
	private String alias;
	private String language;
	
	@Column(name="title_ua")
	private String titleUa;
	@Column(name="title_ru")
	private String titleRu;
	@Column(name="title_en")
	private String titleEn;
	
	private Integer level;
	private Date start;
	private Integer status;
	private Integer modulesCount;
	
	@Column(name="course_price")
	private Float coursePrise;
	@Column(name="for_whom_ua")
	private String forWhomUa;
	@Column(name="what_you_learn_ua")
	private String whatYouLearUa;
	@Column(name="what_you_get_ua")
	private String whatYouGetUa;
	@Column(name="for_whom_ru")
	private String forWhomRu;
	@Column(name="what_you_learn_ru")
	private String whatYouLearnRu;
	@Column(name="what_you_get_ru")
	private String whatYouGetRu;
	@Column(name="for_whom_en")
	private String forWhomEn;
	@Column(name="what_you_learn_en")
	private String whatYouLearnEn;
	@Column(name="what_you_get_en")
	private String whatYouGetEn;
	@Column(name="course_img")
	private String courseImg;
	private Integer rating;
	private boolean cancelled;
	@Column(name="course_number")
	private Integer courseNumber;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getTitleUa() {
		return titleUa;
	}
	public void setTitleUa(String titleUa) {
		this.titleUa = titleUa;
	}
	public String getTitleRu() {
		return titleRu;
	}
	public void setTitleRu(String titleRu) {
		this.titleRu = titleRu;
	}
	public String getTitleEn() {
		return titleEn;
	}
	public void setTitleEn(String titleEn) {
		this.titleEn = titleEn;
	}
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getModulesCount() {
		return modulesCount;
	}
	public void setModulesCount(Integer modulesCount) {
		this.modulesCount = modulesCount;
	}
	public Float getCoursePrise() {
		return coursePrise;
	}
	public void setCoursePrise(Float coursePrise) {
		this.coursePrise = coursePrise;
	}
	public String getForWhomUa() {
		return forWhomUa;
	}
	public void setForWhomUa(String forWhomUa) {
		this.forWhomUa = forWhomUa;
	}
	public String getWhatYouLearUa() {
		return whatYouLearUa;
	}
	public void setWhatYouLearUa(String whatYouLearUa) {
		this.whatYouLearUa = whatYouLearUa;
	}
	public String getWhatYouGetUa() {
		return whatYouGetUa;
	}
	public void setWhatYouGetUa(String whatYouGetUa) {
		this.whatYouGetUa = whatYouGetUa;
	}
	public String getForWhomRu() {
		return forWhomRu;
	}
	public void setForWhomRu(String forWhomRu) {
		this.forWhomRu = forWhomRu;
	}
	public String getWhatYouLearnRu() {
		return whatYouLearnRu;
	}
	public void setWhatYouLearnRu(String whatYouLearnRu) {
		this.whatYouLearnRu = whatYouLearnRu;
	}
	public String getWhatYouGetRu() {
		return whatYouGetRu;
	}
	public void setWhatYouGetRu(String whatYouGetRu) {
		this.whatYouGetRu = whatYouGetRu;
	}
	public String getForWhomEn() {
		return forWhomEn;
	}
	public void setForWhomEn(String forWhomEn) {
		this.forWhomEn = forWhomEn;
	}
	public String getWhatYouLearnEn() {
		return whatYouLearnEn;
	}
	public void setWhatYouLearnEn(String whatYouLearnEn) {
		this.whatYouLearnEn = whatYouLearnEn;
	}
	public String getWhatYouGetEn() {
		return whatYouGetEn;
	}
	public void setWhatYouGetEn(String whatYouGetEn) {
		this.whatYouGetEn = whatYouGetEn;
	}
	public String getCourseImg() {
		return courseImg;
	}
	public void setCourseImg(String courseImg) {
		this.courseImg = courseImg;
	}
	public Integer getRating() {
		return rating;
	}
	public void setRating(Integer rating) {
		this.rating = rating;
	}
	public boolean isCancelled() {
		return cancelled;
	}
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	public Integer getCourseNumber() {
		return courseNumber;
	}
	public void setCourseNumber(Integer courseNumber) {
		this.courseNumber = courseNumber;
	}
	
	
}
