package com.intita.forum.domain;

import java.util.Date;

public class UserActionInfo {
private long lastCategoryId;
private long lastTopicId;
private long lastActionTime;
public static UserActionInfo forTopic(long topicId){
	UserActionInfo info = new UserActionInfo();
	info.lastTopicId = topicId;
	info.lastActionTime = new Date().getTime();//current date;
	return info;
}
public static UserActionInfo forCategory(long categoryId){
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
