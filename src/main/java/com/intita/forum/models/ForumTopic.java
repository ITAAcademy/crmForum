package com.intita.forum.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import com.intita.forum.jsonview.Views;


/**
 * 
 * @author Zinchuk Roman
 */
@Entity(name="forum_topic")
public class ForumTopic implements Serializable,Comparable<ForumTopic> {
	@ManyToOne( fetch = FetchType.LAZY)
	private ForumCategory category;
	
	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;

	@JsonView(Views.Public.class)
	private boolean active = true;
	
	private Date date;

	@NotBlank
	@Size(min = 1, max = 255)
	@Column(unique = false)
	@JsonView(Views.Public.class)
	private String name;
	//Pined topics showed at top
	private boolean pinned;

	@NotNull
	@JsonView(Views.Public.class)
	private short type;
	
	private Boolean isAutoGenerated;

	@ManyToOne( fetch = FetchType.LAZY)
	private IntitaUser author;

	@OneToMany(mappedBy = "topic", fetch = FetchType.LAZY)
	private List<TopicMessage> topicMessages = new ArrayList<>();

	public List<TopicMessage> getTopicMessages() {
		return topicMessages;
	}

	public ForumTopic()
	{
		this.date = new Date();
	}
	public ForumTopic(long id)
	{
		this.id = id;
		this.date = new Date();
	}
	public IntitaUser getAuthor() {
		return author;
	}


	public void setAuthor(IntitaUser autor) {
		this.author = autor;
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
	public static List<String> getForumTopicNames(Iterable<ForumTopic> topics){
		List<String> result = new ArrayList<String>();
		for (ForumTopic forumTopic : topics){
			result.add(forumTopic.getName());
		}
		return result;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return "ForumTopic ";
	}
	public short getType() {
		return type;
	}
	public void setType(short type) {
		this.type = type;
	}

	@Override
	public int compareTo(ForumTopic o) {
		if (o==null)return -1;
		return this.getId().compareTo(o.getId());
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ForumTopic other = (ForumTopic) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	public ForumCategory getCategory() {
		return category;
	}

	public void setCategory(ForumCategory category) {
		this.category = category;
	}

	public Boolean getIsAutoGenerated() {
		return isAutoGenerated;
	}

	public void setIsAutoGenerated(Boolean isAutoGenerated) {
		this.isAutoGenerated = isAutoGenerated;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isPinned() {
		return pinned;
	}

	public void setPinned(boolean isPinned) {
		this.pinned = isPinned;
	}
}
