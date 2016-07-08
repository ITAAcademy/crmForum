package com.intita.forum.services;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.intita.forum.models.Lecture;
import com.intita.forum.repositories.LectureRepository;

@Service
public class LectureService {
	
	public static final int UA = 0;
    public static final int RU = 1;
    public static final int EN = 2;
	
@Autowired
LectureRepository lecturesRepository;


@Transactional
public List<Lecture> getAllLectures(){
	return lecturesRepository.findAll();
}

@Transactional
public Lecture getLectureByTitleUA(String title){	
	return lecturesRepository.findOneByTitleUA(title);
}

@Transactional
public Lecture getLectureByTitleRU(String title){	
	return lecturesRepository.findOneByTitleRU(title);
}

@Transactional
public Lecture getLectureByTitleEN(String title){	
	return lecturesRepository.findOneByTitleEN(title);
}
@Transactional
public List<Lecture> getFirstFiveLecturesByTitleUaLike(String title){
	return lecturesRepository.findFirst5ByTitleUALike(title);
}
@Transactional
public List<Lecture> getFirstFiveLecturesByTitleRuLike(String title){
	return lecturesRepository.findFirst5ByTitleRULike(title);
}
@Transactional
public List<Lecture> getFirstFiveLecturesByTitleEnLike(String title){
	return lecturesRepository.findFirst5ByTitleENLike(title);
}
@Transactional
public List<String> getFirstFiveLecturesTitlesByTitleUaLike(String title){
List<Lecture> lectures = lecturesRepository.findFirst5ByTitleUALike(title + "%");
	 List<String> titles = new ArrayList<>();
	 for (int i = 0; i < lectures.size(); i++)
		 titles.add(lectures.get(i).gettitleUA());
	 return titles;
}
@Transactional
public List<String> getFirstFiveLecturesTitlesByTitleRuLike(String title){
	List<Lecture> lectures = lecturesRepository.findFirst5ByTitleRULike(title+ "%");
	 List<String> titles = new ArrayList<>();
	 for (int i = 0; i < lectures.size(); i++)
		 titles.add(lectures.get(i).gettitleRU());
	 return titles;
}
@Transactional
public List<String> getFirstFiveLecturesTitlesByTitleEnLike(String title){
	List<Lecture> lectures = lecturesRepository.findFirst5ByTitleENLike(title + "%" );
	 List<String> titles = new ArrayList<>();
	 for (int i = 0; i < lectures.size(); i++)
		 titles.add(lectures.get(i).gettitleEN());
	 return titles;
}


}