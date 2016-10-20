package com.intita.forum.modelswrappers;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.intita.forum.models.ForumCategoryStatistic;

@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
public class ForumCategoryStatisticJsonWrapper {
	private Long id;
	private int topicsCount;
	private int messagesCount;
	private int categoriesCount;
	public ForumCategoryStatisticJsonWrapper(ForumCategoryStatistic statistic){
		this.id = statistic.getId();
		this.topicsCount = statistic.getTopicsCount();
		this.messagesCount = statistic.getMessagesCount();
		this.categoriesCount = statistic.getCategoriesCount();
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
}
