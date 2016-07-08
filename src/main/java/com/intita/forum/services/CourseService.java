package com.intita.forum.services;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.forum.models.Course;
import com.intita.forum.repositories.CourseRepository;

@Service
public class CourseService {
	@Autowired
	CourseRepository courseRepository;
	@Transactional
public ArrayList<Course> getAll(){
	return courseRepository.findAll();
}
}
