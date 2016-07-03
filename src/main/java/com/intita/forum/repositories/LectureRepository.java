package com.intita.forum.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.CrudRepository;

import com.intita.forum.models.Lecture;


@Qualifier("IntitaConf") 
public interface LectureRepository extends CrudRepository<Lecture, Long> {
	public List<Lecture> findFirst5ByTitleUALike(String title);
	public List<Lecture> findFirst5ByImageLike(String image);
	public List<Lecture> findFirst5ByTitleRULike(String title);
	public List<Lecture> findFirst5ByTitleENLike(String title);
	public List<Lecture> findAll();
	public Lecture findOneByTitleUA(String title);
	public Lecture findOneByTitleUALike(String title);	
	public Lecture findOneByTitleRU(String title);
	public Lecture findOneByTitleRULike(String title);	
	public Lecture findOneByTitleEN(String title);
	public Lecture findOneByTitleENLike(String title);
	
	//public Lectures findOneByTitle_ua(String room);
}
