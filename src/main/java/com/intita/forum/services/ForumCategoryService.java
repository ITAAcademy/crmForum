package com.intita.forum.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
import com.intita.forum.domain.ForumTreeNode.TreeNodeType;
import com.intita.forum.domain.TreeNodeStatistic;
import com.intita.forum.domain.UserSortingCriteria;
import com.intita.forum.models.Course;
import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumCategory.CategoryChildrensType;
import com.intita.forum.models.ForumTopic;
import com.intita.forum.models.IntitaUser;
import com.intita.forum.models.IntitaUser.IntitaUserRoles;
import com.intita.forum.models.Module;
import com.intita.forum.repositories.ForumCategoryRepository;
import com.intita.forum.web.ForumController;

import utils.CustomDataConverters;

@Service
public class ForumCategoryService {
	@Autowired
	private ForumCategoryRepository forumCategoryRepository;
	@Autowired
	private CourseService courseService;
	@Autowired
	private TopicMessageService topicMessageService;
	
	@Autowired
	private ModuleService moduleService;
	
	@Value("${forum.categoriesOrTopicsCountPerPage}")
	private int categoriesCountForPage;

	@Autowired
	private IntitaUserService intitaUserService;
	
	@Autowired
	private ForumTopicService forumTopicService;
	
	private final static Logger log = LoggerFactory.getLogger(ForumController.class);
	
	@Value("${forum.categoriesOrTopicsCountPerPage}")
	private int topicsCountForPage;
	@Autowired
	private SessionFactory sessionFactory;
	

public Page<ForumCategory> getAllCategories(int page){
	return forumCategoryRepository.findAll(new PageRequest(page,categoriesCountForPage)); 
}

public Page<ForumCategory> getMainCategories(int page){
	return forumCategoryRepository.findByCategory(null, new PageRequest(page,categoriesCountForPage));
}

public ForumCategory getCategoryById(Long id){
	return forumCategoryRepository.findOne(id);
}
public Page<ForumCategory> filterNotAccesibleCategories(Page<ForumCategory> pageObj,IntitaUser user,int page){
	if (pageObj==null)return null;
	List<ForumCategory> pageContent = pageObj.getContent();
	ArrayList<ForumCategory> accessibleCategoriesList = new ArrayList<ForumCategory>();
	for (ForumCategory category : pageContent){
		if(checkCategoryAccessToUser(user, category.getId())){
			accessibleCategoriesList.add(category);	
		}
	}
	return CustomDataConverters.listToPage(accessibleCategoriesList, page, topicsCountForPage);
}
public List<ForumCategory> filterNotAccesibleCategories(Page<ForumCategory> pageObj,IntitaUser user){
	List<ForumCategory> pageContent = pageObj.getContent();
	ArrayList<ForumCategory> accessibleCategoriesList = new ArrayList<ForumCategory>();
	for (ForumCategory category : pageContent){
		if(checkCategoryAccessToUser(user, category.getId())){
			accessibleCategoriesList.add(category);	
		}
	}
	return accessibleCategoriesList;
}
@Transactional
public Page<ForumCategory> getSubCategories(Long id,int page,IntitaUser user,UserSortingCriteria sortingCriteria){
	ForumCategory rootCategory = forumCategoryRepository.findOne(id);
	CategoryChildrensType childrensType = rootCategory.getCategoryChildrensType();
	Page<ForumCategory> result;
	switch(childrensType){
	case ChildrenTopic: result = null;
	break;
	case ChildrenCategory:
		if (sortingCriteria==null)
		result = forumCategoryRepository.findByCategory(rootCategory, new PageRequest(page,categoriesCountForPage));
		else{
			Session session = sessionFactory.getCurrentSession();
			String sortingParam = sortingCriteria.getSortingParamNameForClass(ForumCategory.class);
			String sortingPart = (sortingParam!=null) ? " ORDER BY c."+sortingParam : "";
			if (sortingPart.length()>0)sortingPart+=" "+sortingCriteria.getOrder();
			String whereParam = sortingCriteria.getWhereParamNameForClass(ForumCategory.class);
			String wherePart = "WHERE c.category.id = "+id +" ";
			if (whereParam!=null)
			wherePart += " AND "+ whereParam ;
			String hql = "SELECT c FROM forum_category c " + " "+wherePart+sortingPart;
			Query query = session.createQuery(hql);
			if (whereParam!=null)
			query.setParameter("dateParam", sortingCriteria.getDateParam());
			List<ForumCategory> resultList = query.list();
			result =  CustomDataConverters.listToPage(resultList,page,categoriesCountForPage);
		}
		
		break;
	default:
		result = null;
	}
	return filterNotAccesibleCategories(result,user,page);
}

public ArrayList<ForumCategory> getSubCategories(Long id){
	ForumCategory rootCategory = forumCategoryRepository.findOne(id);
	CategoryChildrensType childrensType = rootCategory.getCategoryChildrensType();
	switch(childrensType){
	case ChildrenTopic: return null;
	case ChildrenCategory:
		return forumCategoryRepository.findByCategory(rootCategory);
	default:
		return null;
	}
}
@Transactional
public ArrayList<ForumTopic> getAllInludeSubCategoriesArray(ForumCategory rootCategory){
	ArrayList<ForumTopic> array = new ArrayList<>();
	CategoryChildrensType childrensType = rootCategory.getCategoryChildrensType();
	switch(childrensType){
	case ChildrenTopic: array.addAll(rootCategory.getTopics());
	case ChildrenCategory:
		ArrayList<ForumCategory> t_array = new ArrayList<>(rootCategory.getCategories());
		for (ForumCategory forumCategory : t_array) {
			ArrayList<ForumTopic> tt_array = getAllInludeSubCategoriesArray(forumCategory);
			if(tt_array != null)
				array.addAll(tt_array);
		}
	}
	return array;
}

@PostConstruct
public void initDatabase(){
	if (forumCategoryRepository.count()>0) return;
	initCategoriesByRoles();
	initEducationCategory();
}

public void initCategoriesByRoles(){
	ForumCategory roleCategory = new ForumCategory("Розділ по ролях","для адміністраторів, бухгалтерів, вчителів",true);
	roleCategory = forumCategoryRepository.save(roleCategory);
	
	ForumCategory adminCategory = new ForumCategory("Адміністратори","Для адмінчиків",false,true);
	adminCategory.addRoleDemand(IntitaUserRoles.ADMIN);
	ForumCategory accountantCategory = new ForumCategory("Бухгалтери","Для бухгалтерів",false,true);
	accountantCategory.addRoleDemand(IntitaUserRoles.ACCOUNTANT);
	ForumCategory teacgersCategory = new ForumCategory("Вчителі","Для вчителів",false,true);
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
	ForumCategory educationCategory = new ForumCategory("Навчання","Для студентів",true);
	ForumCategory courseCategory = new ForumCategory("Курси","Обговорення курсів",true);

	
	educationCategory = forumCategoryRepository.save(educationCategory);
	courseCategory.setCategory(educationCategory);

	courseCategory = forumCategoryRepository.save(courseCategory);


	for (Course course : courses){
		
		ForumCategory category = new ForumCategory(course.getTitleUa(),course.getAlias(),true);
		category.setCategory(courseCategory);
		category = forumCategoryRepository.save(category);
		
		ArrayList<Module> modules = moduleService.getAllFromCourse(course);
		ForumCategory moduleCategory = new ForumCategory("Модулі","Обговорення модулів",false,true);
		moduleCategory.setCategory(category);
		moduleCategory = forumCategoryRepository.save(moduleCategory);
		
		for (Module module : modules){
			ForumCategory c = new ForumCategory(module.getTitleUa(),module.getAlias(),true);
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
/**
 * Return list of roles demanded for every category in tree from rootCategory 
 * to topic with topicId
 * @param topicId
 * @return
 */

public LinkedList<ForumCategory> getAllSubCategories(ForumCategory category,HashSet<Long> idsParam){
	if (category==null || !category.isCategoriesContainer())return null;
	LinkedList<ForumCategory> tree = new LinkedList<ForumCategory>();
	HashSet<Long> ids = idsParam;
	if(ids==null)ids =  new HashSet<Long>();
	List<ForumCategory> subcategories =  category.getCategories();
	for (ForumCategory c: subcategories){
		if (ids.contains(c.getId()))continue;
		ids.add(c.getId());
		tree.addLast(c);;
	}
	for (ForumCategory subCategoryTmp : subcategories){
		ForumCategory subCategory =forumCategoryRepository.findOne(subCategoryTmp.getId());
		LinkedList<ForumCategory> subSubs = getAllSubCategories(subCategory,ids);
		if (subSubs==null)return tree;
		for (ForumCategory subSub : subSubs){
			if (ids.contains(subSub.getId()))continue;
			tree.addLast(subSub);
			ids.add(subSub.getId());
		}
	}
	return tree;
}
public HashSet<Long> getAllSubCategoriesIds(ForumCategory category,HashSet<Long> idsParam){
	if (category==null || !category.isCategoriesContainer())return null;
	HashSet<Long> ids = idsParam;
	if(ids==null)ids =  new HashSet<Long>();
	List<ForumCategory> subcategories = category.getCategories();
	for (ForumCategory c: subcategories){
		if (ids.contains(c.getId()))continue;
		ids.add(c.getId());
	}
	for (ForumCategory subCategoryTmp : subcategories){
		ForumCategory subCategory =forumCategoryRepository.findOne(subCategoryTmp.getId());
		LinkedList<ForumCategory> subSubs = getAllSubCategories(subCategory,ids);
		if (subSubs==null) return ids;
		for (ForumCategory subSub : subSubs){
			if (ids.contains(subSub.getId()))continue;
			ids.add(subSub.getId());
		}
	}
	return ids;
}

public LinkedList<ForumTreeNode> getCategoriesTree(ForumTopic topic){
	if (topic==null)return null;
	LinkedList<ForumTreeNode> tree = getCategoriesTree(topic.getCategory());
	ForumTreeNode topicNode = new ForumTreeNode(topic.getName(), TreeNodeType.TOPIC);//null id to distinguish categories and topic nodes
	topicNode.setId(topic.getId());
	tree.addLast(topicNode);

	return tree;
}

public boolean checkCategoryAccessToUser(Authentication  authentication,Long categoryId){

	IntitaUser currentUser =  (IntitaUser) authentication.getPrincipal();
	return checkCategoryAccessToUser(currentUser,categoryId);
}
public boolean checkCategoryAccessToUser(IntitaUser  user,Long categoryId){
if (user==null || categoryId==null) return false;
	ForumCategory category = getCategoryById(categoryId);
	if (category==null) return false;
	LinkedList<Set<IntitaUserRoles>> demandedRoles = getDemandsForCategory(categoryId);
	if(intitaUserService.hasAllRolesSets(user.getId(),demandedRoles)){
		return true;
	}
	return false;
}
public LinkedList<Set<IntitaUserRoles>> getDemandsForCategory(Long categoryId){
	if (categoryId==null)return null;
	ForumCategory category = getCategoryById(categoryId);
	if (category==null) return null;
	LinkedList<Set<IntitaUserRoles>> roles = new LinkedList<Set<IntitaUserRoles>>();
	ForumCategory parent = category;
	HashSet<Long> ids = new HashSet<Long>();
	while(parent!=null){
		Set<IntitaUserRoles> rolesSet = parent.getRolesDemand();
		roles.add(rolesSet);
		//exit if category met before to prevent infinite cycle
		if (ids.contains(parent.getId()))break;
		ids.add(parent.getId());
		ForumCategory parentCategoryTemp = parent.getCategory();
		if (parentCategoryTemp==null) break; 
		parent=getCategoryById(parentCategoryTemp.getId());
	}
	return roles;
}

public void removeAllAutogeneratedCategoriesAndTopics(){
	
}
public ForumTopic getLastTopic(Long categoryId){
	ForumCategory category = forumCategoryRepository.findOne(categoryId);
	if (category==null) return null;
	HashSet<Long> categoriesIds = getAllSubCategoriesIds(category,null);
	if (categoriesIds==null){
		categoriesIds = new HashSet<Long>();
	}
	categoriesIds.add(category.getId());
	ForumTopic lastTopic = forumCategoryRepository.getLastTopic(categoriesIds);
	return lastTopic;

}

public List<TreeNodeStatistic> getCategoriesStatistic(List<ForumCategory> categories){
	
	List<TreeNodeStatistic> categoriesStatistic = new ArrayList<TreeNodeStatistic>();
	for (ForumCategory c : categories){
	HashSet<Long> topicsIds = forumTopicService.getAllSubTopicsIds(c);
	int messagesCount = topicMessageService.getTotalMessagesCountByTopicsIds(topicsIds);
	TreeNodeStatistic statistic = new TreeNodeStatistic(topicsIds.size(),messagesCount);
	categoriesStatistic.add(statistic);
	}
	return categoriesStatistic;
}


}
