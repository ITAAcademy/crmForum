package com.intita.forum.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity(name="module")
public class Module {
	@Id
	@NotNull
	@Column(name="module_ID")
	private Long id;
	
	@Column(name="title_ru")
	private String titleRu;
	@Column(name="title_en")
	private String titleEn;
	@Column(name="title_ua")
	private String titleUa;
	private String alias;
	private String language;
	@Column(name="lesson_count")
	private Integer lessonCount;
	@Column(name="module_price")
	private float modulePrice;
	@Column(name="for_whom")
	private String forWhom;
	@Column(name="what_you_learn")
	private String whatYouLearn;
	@Column(name="what_you_get")
	private String whatYouGet;
	@Column(name="module_img")
	private String moduleImg;
	private String level;
	@Column(name="hours_in_day")
	private String hoursInDay;
	@Column(name="days_in_week")
	private Integer daysInWeek;
	private Integer rating;
	@Column(name="module_number")
	private Integer moduleNumber;
	private boolean cancelled;
	private boolean status;
	@Column(name="price_offline")
	private float priceOffline;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public String getTitleUa() {
		return titleUa;
	}
	public void setTitleUa(String titleUa) {
		this.titleUa = titleUa;
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
	public Integer getLessonCount() {
		return lessonCount;
	}
	public void setLessonCount(Integer lessonCount) {
		this.lessonCount = lessonCount;
	}
	public float getModulePrice() {
		return modulePrice;
	}
	public void setModulePrice(float modulePrice) {
		this.modulePrice = modulePrice;
	}
	public String getForWhom() {
		return forWhom;
	}
	public void setForWhom(String forWhom) {
		this.forWhom = forWhom;
	}
	public String getWhatYouLearn() {
		return whatYouLearn;
	}
	public void setWhatYouLearn(String whatYouLearn) {
		this.whatYouLearn = whatYouLearn;
	}
	public String getWhatYouGet() {
		return whatYouGet;
	}
	public void setWhatYouGet(String whatYouGet) {
		this.whatYouGet = whatYouGet;
	}
	public String getModuleImg() {
		return moduleImg;
	}
	public void setModuleImg(String moduleImg) {
		this.moduleImg = moduleImg;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getHoursInDay() {
		return hoursInDay;
	}
	public void setHoursInDay(String hoursInDay) {
		this.hoursInDay = hoursInDay;
	}
	public Integer getDaysInWeek() {
		return daysInWeek;
	}
	public void setDaysInWeek(Integer daysInWeek) {
		this.daysInWeek = daysInWeek;
	}
	public Integer getRating() {
		return rating;
	}
	public void setRating(Integer rating) {
		this.rating = rating;
	}
	public Integer getModuleNumber() {
		return moduleNumber;
	}
	public void setModuleNumber(Integer moduleNumber) {
		this.moduleNumber = moduleNumber;
	}
	public boolean isCancelled() {
		return cancelled;
	}
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public float getPriceOffline() {
		return priceOffline;
	}
	public void setPriceOffline(float priceOffline) {
		this.priceOffline = priceOffline;
	}
	
}
