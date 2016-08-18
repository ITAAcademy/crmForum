package com.intita.forum.domain;

public class TreeNodeStatistic {
private int topicsCount;
private int messagesCount;
public TreeNodeStatistic(){
	
}
public TreeNodeStatistic(int topicsCount,int messagesCount){
	this.topicsCount = topicsCount;
	this.messagesCount = messagesCount;
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

}
