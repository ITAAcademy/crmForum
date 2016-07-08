package com.intita.forum.repositories;

import java.util.ArrayList;

import org.springframework.data.repository.CrudRepository;

import com.intita.forum.models.Course;

public interface CourseRepository  extends CrudRepository<Course, Long>{
ArrayList<Course> findAll();
}
