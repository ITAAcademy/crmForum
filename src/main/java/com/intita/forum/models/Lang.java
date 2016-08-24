package com.intita.forum.models;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * @author Zinchuk Roman
 */
@Entity(name="forum_lang")
public class Lang implements Serializable,Comparable<IntitaUser> {

	@Id
	@GeneratedValue
	private Long id;

	@NotBlank
	@Size(min = 1, max = 255)
	@Column(unique = false)
	private String lang;

	@NotBlank
	@Column(unique = false)
	private String map;

	@Override
	public int compareTo(IntitaUser arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	public Long getId() {
		return id;
	}

	public String getLang() {
		return lang;
	}

	public String getMap() {
		return map;
	}
}
