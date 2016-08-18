package com.intita.forum.domain;

import java.util.HashMap;

public class ForumTreeNode {
	public enum TreeNodeType{
		CATEGORY, TOPIC, OTHER
	}
	
	private Long id;
	private String name;
	private TreeNodeType type;
	private HashMap<String,String> data;
	
	public ForumTreeNode(){

	}
	public ForumTreeNode(String name){
		this.name = name;
		type = TreeNodeType.CATEGORY;
	}
	public ForumTreeNode(String name, TreeNodeType type){
		this.name = name;
		this.type = type;
	}
	public ForumTreeNode(String name,Long id){
		this.name = name;
		this.id=id;
		type = TreeNodeType.CATEGORY;
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
	public TreeNodeType getType() {
		return type;
	}
	public void setType(TreeNodeType type) {
		this.type = type;
	}
}
