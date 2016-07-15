package com.intita.forum.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.forum.domain.ForumTreeNode;
import com.intita.forum.models.Course;
import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumCategory.CategoryChildrensType;
import com.intita.forum.models.ForumTopic;
import com.intita.forum.models.IntitaUser;
import com.intita.forum.models.IntitaUser.IntitaUserRoles;
import com.intita.forum.models.Module;
import com.intita.forum.repositories.ForumCategoryRepository;
import com.intita.forum.web.ForumController;

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

	@Autowired
	private IntitaUserService intitaUserService;
	
	private final static Logger log = LoggerFactory.getLogger(ForumController.class);
	

public Page<ForumCategory> getAllCategories(int page){
	return forumCategoryRepository.findAll(new PageRequest(page,categoriesCountForPage)); 
}

public Page<ForumCategory> getMainCategories(int page){
	return forumCategoryRepository.findByCategory(null, new PageRequest(page,categoriesCountForPage));
}

public ForumCategory getCategoryById(Long id){
	return forumCategoryRepository.findOne(id);
}

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
	
	ForumCategory adminCategory = new ForumCategory("Адміністратори","Для адмінчиків",false);
	adminCategory.addRoleDemand(IntitaUserRoles.ADMIN);
	ForumCategory accountantCategory = new ForumCategory("Бухгалтери","Для бухгалтерів",false);
	accountantCategory.addRoleDemand(IntitaUserRoles.ACCOUNTANT);
	ForumCategory teacgersCategory = new ForumCategory("Вчителі","Для вчителів",false);
	teacgersCategory.addRoleDemand(IntitaUserRoles.TEACHER);
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
		ForumCategory moduleCategory = new ForumCategory("Модулі","Обговорення модулів",false);
		moduleCategory.setCategory(category);
		moduleCategory = forumCategoryRepository.save(moduleCategory);
		
		for (Module module : modules){
			ForumCategory c = new ForumCategory(module.getTitleUa(),module.getAlias());
			c.setCategory(moduleCategory);
			forumCategoryRepository.save(c);
		}
		
	}
	
	
}

public LinkedList<ForumTreeNode> getCategoriesTree(ForumCategory lastCategory){
	if (lastCategory==null)return null;
	LinkedList<ForumTreeNode> tree = new LinkedList<ForumTreeNode>();
	ForumCategory parent = lastCategory;
	HashSet<Long> ids = new HashSet<Long>();
	while(parent!=null){
		ForumTreeNode node = new ForumTreeNode(parent.getName(),parent.getId());
		tree.addFirst(node);
		//exit if category met before to prevent infinite cycle
		if (ids.contains(parent.getId()))break;
		ids.add(parent.getId());
		ForumCategory parentCategoryTemp = parent.getCategory();
		if (parentCategoryTemp==null) break; 
		parent=forumCategoryRepository.findOne(parentCategoryTemp.getId());
	}
	return tree;
}
public LinkedList<ForumTreeNode> getCategoriesTree(ForumTopic topic){
	if (topic==null)return null;
	LinkedList<ForumTreeNode> tree = getCategoriesTree(topic.getCategory());
	ForumTreeNode topicNode = new ForumTreeNode(topic.getName(),null);//null id to distinguish categories and topic nodes
	tree.addLast(topicNode);

	return tree;
}

public boolean checkCategoryAccessToUser(Authentication  authentication,Long categoryId){

	ForumCategory category = getCategoryById(categoryId);
	if (category==null) return false;
	Set<IntitaUserRoles> demandedRoles = category.getRolesDemand();
	String id =  (String)authentication.getPrincipal();
	Long longId = Long.parseLong(id);
	IntitaUser currentUser = intitaUserService.getUser(longId);
	if(intitaUserService.hasRoles(currentUser.getId(),demandedRoles)){
		return true;
	}
	return false;
}

}
