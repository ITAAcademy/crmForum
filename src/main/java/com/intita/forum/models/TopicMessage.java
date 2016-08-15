package com.intita.forum.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import com.intita.forum.jsonview.Views;


/**
 * 
 * @author Zinchuk Roman
 */
@Entity(name="topic_message")
public class TopicMessage implements Serializable,Comparable<TopicMessage>  {
	
	public TopicMessage(){
		this.date= new Date();
	}
	public TopicMessage(IntitaUser author, ForumTopic topic, TopicMessage topicMessage){	
	this.author = author;
	this.topic = topic;
	this.body = topicMessage.getBody();
	this.date= new Date();
	this.attachedFiles = topicMessage.getAttachedFiles();
	}
	public TopicMessage(IntitaUser author, ForumTopic topic, String body){	
		this.author = author;
		this.topic = topic;
		this.body = body;
		this.date= new Date();
		}
	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;
	
	//@NotBlank
	@ManyToOne(targetEntity = IntitaUser.class, cascade = {CascadeType.REFRESH}, fetch =FetchType.LAZY)
	@NotNull
	@JsonManagedReference
	@JsonView(Views.Public.class)
	@NotFound(action=NotFoundAction.EXCEPTION)
	private IntitaUser author;
	
	@ManyToOne(  fetch = FetchType.LAZY )
	//@NotFound(action=NotFoundAction.IGNORE)
	private ForumTopic topic;
	
	@Size(max=5000)
	@Column
	@JsonView(Views.Public.class)
	private String body;
	
	
	@JsonView(Views.Public.class)
	private ArrayList<String> attachedFiles = new ArrayList<String>();
	
	@Column
	@JsonView(Views.Public.class)
	private Date date;

	public IntitaUser getAuthor() {
		return author;
	}
	public void setAuthor(IntitaUser author) {
		this.author = author;
	}
	public ForumTopic getTopic() {
		return topic;
	}
	public void setTopic(ForumTopic topic) {
		this.topic = topic;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	@Override
	public int compareTo(TopicMessage o) {
		if (o==null)return -1;
		return this.getId().compareTo(o.getId());
	}
	public ArrayList<String> getAttachedFiles() {
		return attachedFiles;
	}
	public void setAttachedFiles(ArrayList<String> attachedFiles) {
		this.attachedFiles = attachedFiles;
	}
	@Transactional
	public boolean isMyAuthor(IntitaUser user)
	{
		return user.equals(getAuthor());
	}
	
	
}
