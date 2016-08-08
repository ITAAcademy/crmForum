package com.intita.forum.models;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.intita.forum.models.IntitaUser.IntitaUserRoles;
import com.intita.forum.services.IntitaUserService;

/**
 * 
 * @author Nicolas Haiduchok, Zinchuk Roman
 */
@Entity(name="user")
public class IntitaUser implements UserDetails, Serializable,Comparable<IntitaUser>{
	private static final long serialVersionUID = -532710433531902917L;
	public enum IntitaUserRoles  {ADMIN,ACCOUNTANT,STUDENT,TEACHER,USER,TENANT,CONTENT_MANAGER};
	@Transient
	@Autowired
	IntitaUserService intitaUserService;
	@Id
	@GeneratedValue
	private Long id;

	@NotBlank
	@Size(min = 1, max = 255)
	@Column(unique = false,name="email")
	private String login;

	@NotBlank
	@Size(min = 1, max = 100)
	private String password;

	@Column(name="avatar")
	private String avatar;

	@Column(name="nickname")
	private String nickname;

	@Column(name="firstname")
	private String firstName;
	
	@Column(name="secondname")
	private String secondName;


	@Column(name="role")
	private int role;



	/*@OneToMany(mappedBy = "teacher_id", fetch = FetchType.LAZY)
	private List<IntitaConsultation> consultantedConsultation = new ArrayList<>();

	@OneToMany(mappedBy = "user_id", fetch = FetchType.LAZY)
	private List<IntitaConsultation> createdConsultation = new ArrayList<>();
	 */
	//private Permissions permission=Permissions.PERMISSIONS_USER;

	public Long getId() {
		return id;
	}
	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getEmail() {
		return login;
	}

	public void setEmail(String email) {
		this.login = email;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<String> authoritis = intitaUserService.getRolesNames(id);
		return AuthorityUtils.createAuthorityList(authoritis.toArray(new String[authoritis.size()]));
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public static boolean isAnonymous() {
		// Method SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
		// do nothing because anonimus user is considered authorized too
		return "anonymousUser".equals(((IntitaUser)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUsername());
	}

	public IntitaUser(String email_login, String password) {
		this.login = email_login;
		//this.email = email;
		this.password = password;
	}
	public IntitaUser(){
		super();
	}
	public void togglePermission(){
		/* if (permission==Permissions.PERMISSIONS_ADMIN)
			 permission=Permissions.PERMISSIONS_USER;
			 else permission=Permissions.PERMISSIONS_ADMIN;
			if (permission==Permissions.PERMISSIONS_ADMIN)isAdmin=true;
			 else isAdmin=false;*/
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return getLogin();
	}

	public String getNickName() {
		if(firstName != null && !firstName.isEmpty() && secondName != null && !secondName.isEmpty())
			return firstName + " " + secondName;
		
		if(nickname == null || nickname.isEmpty())
			return getLogin();

		return nickname;
	}

	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getFirstName() {
		return firstName;
	}
	public String getFullName(){
		return firstName + secondName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getSecondName() {
		return secondName;
	}
	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}

	@Override
	public int compareTo(IntitaUser o) {
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
		IntitaUser other = (IntitaUser) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	@OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
	private List<TopicMessage> topicMessages = new ArrayList<>();
	
	@OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
	private List<ForumTopic> topics = new ArrayList<>();



	public List<TopicMessage> getTopicMessages() {
		return topicMessages;
	}
	public void setTopicMessages(List<TopicMessage> topicMessages) {
		this.topicMessages = topicMessages;
	}
	public List<ForumTopic> getTopics() {
		return topics;
	}
	public void setTopics(List<ForumTopic> topics) {
		this.topics = topics;
	}
	

}
