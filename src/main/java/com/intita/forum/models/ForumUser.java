package com.intita.forum.models;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonView;
import com.intita.forum.jsonview.Views;

/**
 * 
 * @author Zinchuk Roman
 */
@Entity(name="forum_user")
public class ForumUser implements Serializable,Comparable<ForumUser> {
	@Autowired
	@Transient
    private SessionFactory factory;
	
	public enum Permissions{PERMISSIONS_ADMIN,PERMISSIONS_USER};

	@Id
	@GeneratedValue
	private Long id;

	@OneToOne(fetch = FetchType.EAGER)
	private IntitaUser intitaUser;

	@JsonView(Views.Public.class)
	@Size(min = 0, max = 50)
	private String nickName;

	@OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<ForumTopic> rooms = new HashSet<>();

	@OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<TopicMessage> messages = new ArrayList<>();
	
	public Set<ForumTopic> getRootRooms() {
		return rooms;
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
		ForumUser other = (ForumUser) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public ForumUser(){

	}
	public ForumUser(String nickName, IntitaUser intitaUser){
		this.nickName=nickName;
		this.intitaUser=intitaUser;
	}
	public ForumUser(IntitaUser intitaUser){
		this.nickName=intitaUser.getLogin();
		this.intitaUser=intitaUser;
	}
	public ForumUser(Long id){
		setId(id);
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public IntitaUser getIntitaUser() {
		return intitaUser;
	}
	public void setIntitaUser(IntitaUser intitaUser) {
		this.intitaUser = intitaUser;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	
	public Principal getPrincipal()
	{
		return new Principal() {
			
			@Override
			public String getName() {
				return getId().toString();
			}
		};
	}
	
	@Override
	public int compareTo(ForumUser o) {
		if (o==null)return -1;
		return this.getId().compareTo(o.getId());
	}
	public Permissions getPermission() {
		return Permissions.PERMISSIONS_USER;
	}

	public void setPermission(Permissions permission) {
		//this.permission = permission;
		/*if (permission==Permissions.PERMISSIONS_ADMIN)isAdmin=true;
		else isAdmin=false;*/
	}

}

