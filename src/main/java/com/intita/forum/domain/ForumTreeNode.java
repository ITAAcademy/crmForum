package com.intita.forum.domain;

import java.util.HashMap;

public class ForumTreeNode {
private Long id;
private String name;
private HashMap<String,String> data;
public ForumTreeNode(){
	
}
public ForumTreeNode(String name){
	this.name = name;
}
public ForumTreeNode(String name,Long id){
	this.name = name;
	this.id=id;
}
public Long getId() {
	return id;
}
public void setId(Long id) {
	this.id = id;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public HashMap<String, String> getData() {
	return data;
}
public void setData(HashMap<String, String> data) {
	this.data = data;
}
}
