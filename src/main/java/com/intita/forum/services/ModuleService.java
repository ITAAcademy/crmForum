package com.intita.forum.services;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.forum.models.Course;
import com.intita.forum.models.Module;
import com.intita.forum.repositories.CourseModulesRepository;
import com.intita.forum.repositories.ModuleRepository;

@Service
public class ModuleService {
	@Autowired ModuleRepository moduleRepository;
	@Autowired CourseModulesRepository courseModelsRepository;
	
	@Transactional
public ArrayList<Module> getAll(){
	return moduleRepository.findAll();
}
	/**
	 * Problem method
	 * @param course
	 * @return
	 */
	@Transactional
	public ArrayList<Module> getAllFromCourse(Course course){
		return courseModelsRepository.finModulesdByCourseId(course.getId());
	}
	@Transactional
	public ArrayList<Module> getAllFromCourseById(Long id){
		return courseModelsRepository.finModulesdByCourseId(id);
	}
}
