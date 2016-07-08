package com.intita.forum.services;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.forum.models.Course;
import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumCategory.CategoryChildrensType;
import com.intita.forum.models.Module;
import com.intita.forum.repositories.ForumCategoryRepository;

@Service
public class ForumCategoryService {
	@Autowired
	private ForumCategoryRepository forumCategoryRepository;
	@Autowired
	private CourseService courseService;
	
	@Autowired
	private ModuleService moduleService;
	
	@Value("${forum.categoriesOrTopicsCountPerPage}")
	private int categoriesCountForPage;
	
@Transactional	
public Page<ForumCategory> getAllCategories(int page){
	return forumCategoryRepository.findAll(new PageRequest(page,categoriesCountForPage)); 
}
@Transactional
public Page<ForumCategory> getMainCategories(int page){
	return forumCategoryRepository.findByCategory(null, new PageRequest(page,categoriesCountForPage));
}
@Transactional
public ForumCategory getCategoryById(Long id){
	return forumCategoryRepository.findOne(id);
}
@Transactional
public Page<ForumCategory> getSubCategories(Long id,int page){
	ForumCategory rootCategory = forumCategoryRepository.findOne(id);
	CategoryChildrensType childrensType = rootCategory.getCategoryChildrensType();
	switch(childrensType){
	case ChildrenTopic: return null;
	case ChildrenCategory:
		return forumCategoryRepository.findByCategory(new ForumCategory(id),new PageRequest(page,categoriesCountForPage));
	default:
		return null;
	}

}
@PostConstruct
public void initDatabase(){
	if (forumCategoryRepository.count()>0) return;
	initCategoriesByRoles();
	initEducationCategory();
}

public void initCategoriesByRoles(){
	ForumCategory roleCategory = new ForumCategory("Розділ по ролях","для адміністраторів, бухгалтерів, вчителів");
	roleCategory = forumCategoryRepository.save(roleCategory);
	
	ForumCategory adminCategory = new ForumCategory("Адміністратори","Для адмінчиків");
	ForumCategory accountantCategory = new ForumCategory("Бухгалтери","Для бухгалтерів");
	ForumCategory teacgersCategory = new ForumCategory("Вчителі","Для вчителів");
	adminCategory.setCategory(roleCategory);
	accountantCategory.setCategory(roleCategory);
	teacgersCategory.setCategory(roleCategory);
	forumCategoryRepository.save(adminCategory);
	forumCategoryRepository.save(accountantCategory);
	forumCategoryRepository.save(teacgersCategory);
}

public void initEducationCategory(){
	ArrayList<Course> courses = courseService.getAll();
	ForumCategory educationCategory = new ForumCategory("Навчання","Для студентів");
	ForumCategory courseCategory = new ForumCategory("Курси","Обговорення курсів");

	
	educationCategory = forumCategoryRepository.save(educationCategory);
	courseCategory.setCategory(educationCategory);

	courseCategory = forumCategoryRepository.save(courseCategory);


	for (Course course : courses){
		
		ForumCategory category = new ForumCategory(course.getTitleUa(),course.getAlias());
		category.setCategory(courseCategory);
		category = forumCategoryRepository.save(category);
		
		ArrayList<Module> modules = moduleService.getAllFromCourse(course);
		ForumCategory moduleCategory = new ForumCategory("Модулі","Обговорення модулів");
		moduleCategory.setCategory(category);
		moduleCategory = forumCategoryRepository.save(moduleCategory);
		
		for (Module module : modules){
			ForumCategory c = new ForumCategory(module.getTitleUa(),module.getAlias());
			c.setCategory(moduleCategory);
			forumCategoryRepository.save(c);
		}
		
	}
	
	
}

}
