package com.intita.forum.domain;

import java.util.Date;

public class UserActionInfo {
private Long lastCategoryId;
private long lastTopicId;
private long lastActionTime;
public static UserActionInfo forTopic(Long topicId){
	UserActionInfo info = new UserActionInfo();
	info.lastTopicId = topicId;
	info.lastActionTime = new Date().getTime();//current date;
	return info;
}
public static UserActionInfo forCategory(Long categoryId){
	UserActionInfo info = new UserActionInfo();
	info.lastCategoryId = categoryId;
	info.lastActionTime = new Date().getTime();
	return info;
}
public static UserActionInfo forEmptyAction(){
	UserActionInfo info = new UserActionInfo();
	info.lastActionTime = new Date().getTime();
	return info;
}
@Override
public String toString(){
	return "lastCategoryId:"+lastCategoryId+";lastTopicId:"+lastTopicId+";lastActionTime:"+lastActionTime;
}
public long getLastActionTime(){
	return lastActionTime;
}
}
