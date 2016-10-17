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
import javax.persistence.OneToOne;
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
@Entity(name="topic_message_updates")
public class TopicMessageUpdateInfo implements Serializable,Comparable<TopicMessageUpdateInfo>  {
	
	public TopicMessageUpdateInfo(){
		
	}
	public TopicMessageUpdateInfo(IntitaUser editor, TopicMessage msg){	
		this.editor = editor;
		this.date= new Date();
		this.msg = msg;
		}
	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;
	
	@OneToOne(fetch =FetchType.LAZY)
	@JsonManagedReference
	@JsonView(Views.Public.class)
	@NotNull
	@NotFound(action=NotFoundAction.EXCEPTION)
	private IntitaUser editor;
	
	@OneToOne(  fetch = FetchType.LAZY )
	//@NotFound(action=NotFoundAction.IGNORE)
	private TopicMessage msg;

	@Column
	@JsonView(Views.Public.class)
	private Date date;

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
	
	public IntitaUser getEditor() {
		return editor;
	}
	public void setEditor(IntitaUser editor) {
		this.editor = editor;
	}
	
	public TopicMessage getMsg() {
		return msg;
	}
	@Override
	public int compareTo(TopicMessageUpdateInfo o) {
		if (o==null)return -1;
		return this.getId().compareTo(o.getId());
	}
}
