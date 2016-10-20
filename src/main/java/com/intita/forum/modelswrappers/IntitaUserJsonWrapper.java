package com.intita.forum.modelswrappers;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.intita.forum.models.IntitaUser;
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
public class IntitaUserJsonWrapper {
	private Long id;
	private String login;
	private String password;
	private String avatar;
	private String nickname;
	private String firstName;
	private String secondName;
	private int role;
	public IntitaUserJsonWrapper(IntitaUser user){
		this.id = user.getId();
		this.login = user.getLogin();
		this.password = user.getPassword();
		this.avatar = user.getAvatar();
		this.nickname = user.getNickname();
		this.firstName = user.getFirstName();
		this.secondName = user.getSecondName();
		this.role = user.getRole();
	}
	public Long getId() {
		return id;
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
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
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
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getSecondName() {
		return secondName;
	}
	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}
	public int getRole() {
		return role;
	}
	public void setRole(int role) {
		this.role = role;
	}

}
