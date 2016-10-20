package com.intita.forum.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
import com.intita.forum.domain.UserSortingCriteria;
import com.intita.forum.models.Course;
import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumCategoryStatistic;
import com.intita.forum.models.ForumTopic;
import com.intita.forum.models.IntitaUser;
import com.intita.forum.models.IntitaUser.IntitaUserRoles;
import com.intita.forum.models.Module;
import com.intita.forum.modelswrappers.ForumCategoryJsonWrapper;
import com.intita.forum.repositories.ForumCategoryRepository;

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
	
	@Autowired
	private ForumCategoryStatisticService forumCategoryStatisticService;
	
	private final static Logger log = LoggerFactory.getLogger(ForumCategoryService.class);
	
	@Value("${forum.categoriesOrTopicsCountPerPage}")
	private int topicsCountForPage;
	@Autowired
	private SessionFactory sessionFactory;
	
	final private Long EDUCATIONAL_CATEGORY_ID = 1L;

@Transactional
public ForumCategory update(ForumCategory category){
	return forumCategoryRepository.save(category);
}
public Page<ForumCategory> getAllCategories(int page){
	return forumCategoryRepository.findAll(new PageRequest(page,categoriesCountForPage)); 
}
public ArrayList<ForumCategory> getAllCategories(){
	return forumCategoryRepository.findAll(); 
}
@Transactional
public ArrayList<Long> getAllCategoriesIds(){
	return forumCategoryRepository.getAllCategoriesIds();
}

public Page<ForumCategory> getMainCategories(int page){
	return forumCategoryRepository.findByCategory(null, new PageRequest(page,categoriesCountForPage));
}
public ArrayList<ForumCategory> getMainCategories(){
	return forumCategoryRepository.findByCategory(null);
}
@Transactional
public ForumCategory getCategoryById(Long id){
	if (id==null) return null;
	return forumCategoryRepository.findOne(id);
}
/**
 * Receive list of categories and remove unaccessible categories (for given user) from it
 * @param pageObj
 * @param user
 * @param page
 * @return
 */
public Page<ForumCategory> filterNotAccesibleCategories(Page<ForumCategory> pageObj,Authentication auth,int page){
	if (pageObj==null)return null;
	List<ForumCategory> pageContent = pageObj.getContent();
	List<ForumCategory> accessibleCategoriesList = filterNotAccesibleCategories(pageContent,auth);
	for (ForumCategory category : pageContent){
		if(checkCategoryAccessToAuthentication(auth, category.getId())){
			accessibleCategoriesList.add(category);	
		}
	}
	return CustomDataConverters.listToPage(accessibleCategoriesList, page, topicsCountForPage);
}

/**
 * Receive list of categories and remove unaccessible categories (for given user) from it
 * @param pageObj
 * @param user
 * @param page
 * @return
 */
public ArrayList<ForumCategory> filterNotAccesibleCategories(List<ForumCategory> pageContent,Authentication auth){
	if (pageContent==null)return null;
	ArrayList<ForumCategory> accessibleCategoriesList = new ArrayList<ForumCategory>();
	for (ForumCategory category : pageContent){
		if(checkCategoryAccessToAuthentication(auth, category.getId())){
			accessibleCategoriesList.add(category);	
		}
	}
	return accessibleCategoriesList;
}
/**
 * Don't use it . To slow ...
 **/
public HashSet<Long> filterNotAccesibleCategoriesIds(HashSet<Long> categoriesIds,Authentication authentication){
	if (categoriesIds==null)return new HashSet<Long>();
	if (authentication==null) return new HashSet<Long>();
	HashSet<Long> accessibleCategoriesList = new HashSet<>();
	for (Long categoryId : categoriesIds){
		if(checkCategoryAccessToAuthentication(authentication, categoryId)){
			accessibleCategoriesList.add(categoryId);	
		}
	}
	return accessibleCategoriesList;
}
/*public List<ForumCategory> filterNotAccesibleCategories(Page<ForumCategory> pageObj,IntitaUser user){
	List<ForumCategory> pageContent = pageObj.getContent();
	return filterNotAccesibleCategories(pageContent,user);
}*/
/**
 * Return childrens of Category sorted by some fields depends on UserSortingCriteria
 * @param parentCategoryId
 * @param user
 * @param sortingCriteria
 * @param page
 * @return 
 */
@Transactional
public ArrayList<ForumCategory> getSubCategoriesList(Long parentCategoryId,Authentication auth,UserSortingCriteria sortingCriteria,Integer page){
	long startTime = new Date().getTime();
	if (parentCategoryId==null) return new ArrayList<ForumCategory>();
	ForumCategory rootCategory = forumCategoryRepository.findOne(parentCategoryId);
	List<ForumCategory> result = null;
		if (sortingCriteria==null){
			if (page!=null)
		result = forumCategoryRepository.findByCategory(rootCategory, new PageRequest(page,categoriesCountForPage)).getContent();
			else result = forumCategoryRepository.findByCategory(rootCategory);
		}
		else{
		
			Session session = sessionFactory.getCurrentSession();
			String sortingParam = sortingCriteria.getSortingParamNameForClass(ForumCategory.class);
			String sortingPart = (sortingParam!=null) ? " ORDER BY c."+sortingParam : "";
			if (sortingPart.length()>0)sortingPart+=" "+sortingCriteria.getOrder();
			String whereParam = sortingCriteria.getDateParamNameForClass(ForumCategory.class);
			String wherePart = "WHERE c.category.id = "+parentCategoryId +" ";
			if (whereParam!=null)
			wherePart += " AND "+ whereParam ;
			String hql = "SELECT c FROM forum_category c " + " "+wherePart+sortingPart;
			Query query = session.createQuery(hql);
			if (whereParam!=null)
			query.setParameter("dateParam", sortingCriteria.getDateParam());
			query.setFirstResult(page*categoriesCountForPage);
			query.setMaxResults(categoriesCountForPage);
			result = query.list();
		}
		long delta = new Date().getTime() - startTime;
		ArrayList<ForumCategory> accessibleCategories = filterNotAccesibleCategories(result,auth);
		log.info("#a312a delta:"+delta);
	return accessibleCategories;
}
/**
 * generate map with categories, last topics and their accesibilities
 * @param auth
 * @param categoryId
 * @param categoriesSortingCriteria
 * @return map with keys "categories","lastTopics","lastTopicsAccessibility"
 */
@Transactional
public List<ForumCategoryJsonWrapper> getCategoriesWrapped(Authentication auth,Long categoryId,UserSortingCriteria categoriesSortingCriteria,int page){
	List<ForumCategoryJsonWrapper>  categoriesWrapped = new ArrayList<ForumCategoryJsonWrapper>();
	ArrayList<ForumCategory> categories = null;
	if (categoryId!=null)categories = getSubCategoriesList(categoryId,auth,categoriesSortingCriteria,page);
	else categories = getMainCategories();
	for (ForumCategory c : categories){
		ForumCategoryJsonWrapper categoryWrapper = new ForumCategoryJsonWrapper(c);
		boolean isTopicRoomAccessible = checkTopicAccessToAuthentication(auth,c.getLastTopic());
		categoryWrapper.setLastTopicAccessible(isTopicRoomAccessible);
		categoriesWrapped.add(categoryWrapper);
	}
return categoriesWrapped;	
}

/**
 * SLOW !!!. Return page with children of category sorted by some fields depends on UserSortingCriteria
 * @param id
 * @param user
 * @param sortingCriteria
 * @return
 */
@Transactional
public Page<ForumCategory> getSubCategoriesSinglePage(Long id,Authentication auth,UserSortingCriteria sortingCriteria){
List<ForumCategory> subCategoriesList = getSubCategoriesList(id,auth,sortingCriteria,null);
if (subCategoriesList==null) 
	subCategoriesList = new ArrayList<ForumCategory>();
Page<ForumCategory> result = CustomDataConverters.listToPage(subCategoriesList,0,subCategoriesList.size());
return result;
}
/**
 * Return specific page with childrens of category sorted by some fields depends on UserSortingCriteria
 * @param id
 * @param page
 * @param user
 * @param sortingCriteria
 * @return
 */
@Transactional
public Page<ForumCategory> getSubCategoriesPage(Long id,int page,Authentication auth,UserSortingCriteria sortingCriteria){
List<ForumCategory> subCategoriesList = getSubCategoriesList(id,auth,sortingCriteria,null);
if (subCategoriesList==null) 
	subCategoriesList = new ArrayList<ForumCategory>();
Page<ForumCategory> result = CustomDataConverters.listToPage(subCategoriesList,page,categoriesCountForPage,subCategoriesList.size());
return result;
}
/**
 * return childrens of categorie without any custom sorting
 * @param id
 * @return
 */
@Transactional
public ArrayList<ForumCategory> getSubCategories(ForumCategory rootCategory){
	return forumCategoryRepository.findByCategory(rootCategory);
}

@Transactional
public ArrayList<ForumTopic> getAllInludeSubCategoriesArray(ForumCategory rootCategory){
	ArrayList<ForumTopic> array = new ArrayList<>();
		ArrayList<ForumCategory> t_array = new ArrayList<>(rootCategory.getCategories());
		for (ForumCategory forumCategory : t_array) {
			ArrayList<ForumTopic> tt_array = getAllInludeSubCategoriesArray(forumCategory);
			if(tt_array != null)
				array.addAll(tt_array);
		}
	return array;
}
/**
 * Generate basic root categoires and add new categorie for each course to them if it's not added before.
 * 
 */
@Transactional
public void updateCategoriesFromCourses(){
	initEducationCategory();//must be called first to set proper Id for educational category
	initCategoriesByRoles();
	
}

/**
 * add basic categories for role-based access
 */
@Transactional
public void initCategoriesByRoles(){
	final String ROLES_CATEGORY_NAME = "Розділ по ролях";
	final String ROLES_ADMINISTRATORS_CATEGORY_NAME = "Адміністратори";
	final String ROLES_ACCOUNTANTS_CATEGORY_NAME = "Бухгалтери";
	final String ROLES_TEACHERS_CATEGORY_NAME = "Вчителі";
	
	final String ROLES_STUDENTS_CATEGORY_NAME = "Студенти";
	final String ROLES_TENANTS_CATEGORY_NAME = "Тенанти";
	final String ROLES_CONTENT_MANAGER_CATEGORY_NAME = "Контент менеджери";
	
	final String ROLES_TRAINERS_CATEGORY_NAME = "Тренери";
	final String ROLES_CONSULTANTS_CATEGORY_NAME = "Консультанти";
	
	/*ForumCategory roleCategory = null;
	ArrayList<ForumCategory> categories = forumCategoryRepository.findByNameAndCategoryIdWhereDateEqualMinDate(ROLES_CATEGORY_NAME,
			null);
	if (categories.size()>0){
		roleCategory = categories.get(0);
	}
	if (roleCategory == null)
		roleCategory = ForumCategory.createInstance(ROLES_CATEGORY_NAME,"для адміністраторів, бухгалтерів, вчителів",true);
	roleCategory = forumCategoryRepository.save(roleCategory);*/
	ForumCategory roleCategory = getOrCreateForumCategory(ROLES_CATEGORY_NAME,null,"для адміністраторів, бухгалтерів, вчителів",true);	
	//ForumCategory adminCategory = ForumCategory.createInstance(ROLES_ADMINISTRATORS_CATEGORY_NAME,"Для адмінчиків",false);
	ForumCategory adminCategory = getOrCreateForumCategory(ROLES_ADMINISTRATORS_CATEGORY_NAME,roleCategory,"Для адмінчиків",false,IntitaUserRoles.ADMIN);
	//ForumCategory accountantCategory = ForumCategory.createInstance(ROLES_ACCOUNTANTS_CATEGORY_NAME,"Для бухгалтерів",false);
	ForumCategory accountantCategory =  getOrCreateForumCategory(ROLES_ACCOUNTANTS_CATEGORY_NAME,roleCategory,"Для бухгалтерів",false,IntitaUserRoles.ACCOUNTANT);
	//ForumCategory teacgersCategory =  ForumCategory.createInstance(ROLES_TEACHERS_CATEGORY_NAME,"Для вчителів",false);
	ForumCategory teacgersCategory =  getOrCreateForumCategory(ROLES_TEACHERS_CATEGORY_NAME,roleCategory,"Для вчителів",false,IntitaUserRoles.TEACHER);
	ForumCategory studentsCategory =  getOrCreateForumCategory(ROLES_STUDENTS_CATEGORY_NAME,roleCategory,"Для студентів",false,IntitaUserRoles.STUDENT);
	ForumCategory tenantsCategory =  getOrCreateForumCategory(ROLES_TENANTS_CATEGORY_NAME,roleCategory,"Для тенантів",false,IntitaUserRoles.TENANT);
	ForumCategory conentsManagersCategory =  getOrCreateForumCategory(ROLES_CONTENT_MANAGER_CATEGORY_NAME,roleCategory,"Для контент менеджерів",false,IntitaUserRoles.CONTENT_MANAGER);
	ForumCategory trainersCategory =  getOrCreateForumCategory(ROLES_TRAINERS_CATEGORY_NAME,roleCategory,"Для тренерів",false,IntitaUserRoles.TRAINER);
	ForumCategory consultantsCategory =  getOrCreateForumCategory(ROLES_CONSULTANTS_CATEGORY_NAME,roleCategory,"Для консультантів",false,IntitaUserRoles.CONSULTANT);
}
/**
 * add new category to database basic on course data
 * @param course
 * @param parentCategory
 */
@Transactional
public void saveCourseAsCategory(Course course,ForumCategory parentCategory){		

	ForumCategory category =  getForumCategoryByCourseOrModule(course.getId(),true);
	if (category == null)
	{
		category  = ForumCategory.createInstanceForCourse(course.getTitleUa(),course.getAlias(),course.getId());
		category.setCategory(parentCategory);
		category = forumCategoryRepository.save(category);
		ForumCategoryStatistic statistic = new ForumCategoryStatistic();
		statistic=forumCategoryStatisticService.save(statistic);
		category.setStatistic(statistic);
	}
		ArrayList<Module> modules = moduleService.getAllFromCourse(course);
		for (Module module : modules){
			ForumCategory c = getForumCategoryByCourseOrModule(module.getId(),false);
			if (c == null){
			//module isn't already excist
			c = ForumCategory.createInstanceForModule(module.getTitleUa(),module.getAlias(),module.getId());
			c  = forumCategoryRepository.save(c);
			c.setCategory(category);
			ForumCategoryStatistic statistic = new ForumCategoryStatistic();
			statistic=forumCategoryStatisticService.save(statistic);
			c.setStatistic(statistic);
			
			}
		}	
}
/**
 * Retreive ForumCategory which create from course with given id
 * @param id
 * @param isCourse
 * @return
 */
@Transactional
public ForumCategory getForumCategoryByCourseOrModule(Long id,boolean isCourse){
	List<ForumCategory> list = forumCategoryRepository.findByCourseOrModuleIdAndIsCourseCategory(id,isCourse);
	if (list.size()>0) return list.get(0);
	return null;
}
/**
 * Find catagory by name in parent category and if not exist - create new one
 * @param name
 * @param parent
 * @param description
 * @param containSubCategories
 * @param role
 * @return
 */
@Transactional
public ForumCategory getOrCreateForumCategory(String name,ForumCategory parent,String description,
		boolean containSubCategories,IntitaUserRoles role){
	//return getOrCreateForumCategory(name,parent,description,null,null,containSubCategories);
	ForumCategory category = null;
	Long parentId = (parent==null) ? null : parent.getId();
	ForumCategory parentCategory = null;
	if (parentId!=null)parentCategory = forumCategoryRepository.findOne(parentId);
	ArrayList<ForumCategory> categories = null;
	if (parentId == null)
	categories = forumCategoryRepository.findByNameAndCategoryIdIsNull(name);
	else categories = forumCategoryRepository.findByNameAndCategoryId(name,parentId);
	if (categories.size()>0){
		category = categories.get(0);
	}
	if (category == null)
	{
		category = ForumCategory.createInstance(name,description,containSubCategories);
		if (role!=null)
		category.addRoleDemand(role);
		category.setCategory(parentCategory);
		ForumCategoryStatistic statistic = new ForumCategoryStatistic();
		statistic=forumCategoryStatisticService.save(statistic);
		category.setStatistic(statistic);
	}
	category = forumCategoryRepository.save(category);
	return category;
}
@Transactional
public ForumCategory getOrCreateForumCategory(String name,ForumCategory parent,String description,
		boolean containSubCategories){
	return getOrCreateForumCategory(name, parent,description,containSubCategories,null);
}

@Transactional
public void initEducationCategory(){
	final String EDUCATION_CATEGORY_NAME = "Навчання";
	final String COURSE_CATEGORY_NAME = "Курси";
	ArrayList<Course> courses = courseService.getAll();
	//if categories doesn't excist we create them and store id Database
	ForumCategory educationCategory =forumCategoryRepository.findOne(EDUCATIONAL_CATEGORY_ID);
	if (educationCategory==null){
		educationCategory = getOrCreateForumCategory(EDUCATION_CATEGORY_NAME, null, "Для студентів",true);
	educationCategory.setId(EDUCATIONAL_CATEGORY_ID);
	educationCategory = forumCategoryRepository.save(educationCategory);
	}
	ForumCategory courseCategory =   getOrCreateForumCategory(COURSE_CATEGORY_NAME, educationCategory, "Обговорення курсів",true);

	for (Course c : courses){
		saveCourseAsCategory(c,courseCategory);
	}
}

public LinkedList<ForumTreeNode> getCategoriesTree(ForumCategory lastCategory){
	if (lastCategory==null)return new LinkedList<ForumTreeNode>();
	long timeMs = new Date().getTime();
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
	long deltaMs = new Date().getTime() - timeMs;
	return tree;
}
/**
 * Return list of roles demanded for every category in tree from rootCategory 
 * to topic with topicId
 * @param topicId
 * @return
 */
@Transactional
public LinkedList<ForumCategory> getAllSubCategories(ForumCategory category,HashSet<Long> idsParam){
	if (category==null)return new LinkedList<ForumCategory>();
	LinkedList<ForumCategory> tree = new LinkedList<ForumCategory>();
	HashSet<Long> ids = idsParam;
	if(ids==null)ids =  new HashSet<Long>();
	List<ForumCategory> subcategories =  category.getCategories();
	for (ForumCategory c: subcategories){
		if (ids.contains(c.getId()))continue;
		ids.add(c.getId());
		tree.addLast(c);;
	}
	/*for (ForumCategory subCategoryTmp : subcategories){
		//ForumCategory subCategory =forumCategoryRepository.findOne(subCategoryTmp.getId());
		LinkedList<ForumCategory> subSubs = getAllSubCategories(subCategoryTmp,ids);
		//if (subSubs==null)return tree;
		for (ForumCategory subSub : subSubs){
			if (ids.contains(subSub.getId()))continue;
			tree.addLast(subSub);
			ids.add(subSub.getId());
		}
	}*/
	return tree;
}
/**
 * Need to fix . To slow ...
 **/
@Transactional
public HashSet<Long> getAllSubCategoriesIds(Long categoryId,HashSet<Long> idsParam){
	ForumCategory category = forumCategoryRepository.findOne(categoryId);
	return getAllSubCategoriesIds(category,idsParam);
}
@Transactional
public HashSet<Long> getSubCategoriesIds(ForumCategory category){
	return forumCategoryRepository.findSubCategoriesIdsByCategory(category.getId());
}
@Transactional
public HashSet<Long> getAllSubCategoriesIds(ForumCategory category,HashSet<Long> idsParam){
	if (category==null)return new HashSet<Long>();
	HashSet<Long> ids = idsParam;
	if(ids==null)ids =  new HashSet<Long>();
	List<ForumCategory> subcategories = category.getCategories();
	for (ForumCategory c: subcategories){
		if (ids.contains(c.getId()))continue;
		ids.add(c.getId());
	}
	for (ForumCategory subCategoryTmp : subcategories){
		ForumCategory subCategory =forumCategoryRepository.findOne(subCategoryTmp.getId());
		HashSet<Long> subSubs = getAllSubCategoriesIds(subCategory,ids);
		if (subSubs==null) return ids;
		for (Long subSub : subSubs){
			if (ids.contains(subSub))continue;
			ids.add(subSub);
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

@Transactional
public void updateLastTopic(Long categoryId,ForumTopic topic){
	ForumCategory category = forumCategoryRepository.findOne(categoryId);
	ArrayList<Long> categoriesToUpdateLastTopic = getParentCategoriesIds(category);
	categoriesToUpdateLastTopic.add(category.getId());
	forumCategoryRepository.updateLastTopic(categoriesToUpdateLastTopic,topic);
	
}
/**
 * Update lastTopic field in every category to make them relevant
 */
@Transactional
public void updateLastTopics(){
	ArrayList<ForumCategory> lastTopics = forumCategoryRepository.findByCategoryIsNull();
	for (ForumCategory category : lastTopics)//get root categories;
	{
		ForumTopic lastTopic = processLastTopic(category.getId(), null);
		if (lastTopic != null){
			log.info("last topic for category:"+category.getId()+":"+lastTopic.getName());
		}
		else log.info("last topic for category:"+category.getId()+":null");
	}
}
@Transactional
public void updateLastTopic(ForumCategory category,ForumTopic topic){
	ArrayList<Long> categoriesToUpdateLastTopic = getParentCategoriesIds(category);
	categoriesToUpdateLastTopic.add(category.getId());
	forumCategoryRepository.updateLastTopic(categoriesToUpdateLastTopic,topic);
	
}
public List<ForumCategory> getParentCategories(ForumCategory category){
	List<ForumCategory> parentCategories = new ArrayList<>();
	if (category==null || category.getCategory()==null) return parentCategories;
	ForumCategory parent = category.getCategory();
	while (parent!=null){
		parentCategories.add(parent);
		parent = parent.getCategory();
	}
	return parentCategories;	
}
public ArrayList<Long> getParentCategoriesIds(ForumCategory category){
	ArrayList<Long> parentCategoriesIds = new ArrayList<>();
	if (category==null || category.getCategory()==null) return parentCategoriesIds;
	ForumCategory parent = category.getCategory();
	while (parent!=null){
		parentCategoriesIds.add(parent.getId());
		parent = parent.getCategory();
	}
	return parentCategoriesIds;	
}
@Transactional
public ArrayList<Long> getParentCategoriesIdsIncludeTarget(ForumCategory category){
	ArrayList<Long> categories = getParentCategoriesIds(category);
	categories.add(category.getId());
	return categories;
}
@Transactional
public boolean checkCategoryAccessToAuthentication(Authentication  authentication,Long categoryId){

	if (authentication==null || categoryId==null) return false;
	ForumCategory category = getCategoryById(categoryId);
	if (category==null) return false;
	LinkedList<Set<IntitaUserRoles>> demandedRoles = getDemandsForCategory(categoryId);
	if(intitaUserService.hasAllRolesSetsByAuthentication(authentication,demandedRoles)){
		return true;
	}
	return false;
}
@Transactional
public boolean checkTopicAccessToAuthentication(Authentication authentication,ForumTopic topic){
	if (topic==null) return false;
	if (topic.getCategory()==null) return true; // root category is accesible for all users
	return checkCategoryAccessToAuthentication(authentication,topic.getCategory().getId());
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
public ForumTopic getLastTopic(Long categoryId,IntitaUser user){
	ForumCategory category = forumCategoryRepository.findOne(categoryId);
	return category.getLastTopic();
}
@Transactional
public List<ForumCategoryStatistic> getCategoriesStatistic(List<ForumCategory> categories){	
	List<ForumCategoryStatistic> categoriesStatistic = new ArrayList<ForumCategoryStatistic>();
	for (ForumCategory c : categories){
	categoriesStatistic.add(c.getStatistic());
	}
	return categoriesStatistic;
}
/**
 * process last topic in category and set category topic value to processed result
 * @param categoryId
 * @param currentLastTopic
 * @return
 */
@Transactional
public ForumTopic processLastTopic(ForumCategory category,ForumTopic currentLastTopic){
	List<ForumCategory> subcategories = getSubCategories(category);
	ForumTopic lastTopicInThisCategory = null;
	lastTopicInThisCategory = forumTopicService.getLastTopic(category.getId());
	for (ForumCategory subCategory : subcategories){
		ForumTopic processedLastTopic = processLastTopic(subCategory,lastTopicInThisCategory);
		if (processedLastTopic != null)
		lastTopicInThisCategory = processedLastTopic;
	}
	if (lastTopicInThisCategory!=null)
	updateLastTopic(category.getId(), lastTopicInThisCategory );
	ForumTopic topicAfterCurrentLastTopic = null;
	boolean isLastTopicInThisCategoryIsLaterThanParentCategoryTopic = 
			lastTopicInThisCategory!=null && currentLastTopic!=null &&
			(lastTopicInThisCategory.getDate().getTime() > currentLastTopic.getDate().getTime());
	if (isLastTopicInThisCategoryIsLaterThanParentCategoryTopic)
		topicAfterCurrentLastTopic = lastTopicInThisCategory;
	return topicAfterCurrentLastTopic;
}
/**
 * process last topic in category and set category last topic value to processed result
 * @param categoryId
 * @param currentLastTopic
 * @return
 */
@Transactional
public ForumTopic processLastTopic(Long categoryId,ForumTopic currentLastTopic){
	ForumCategory category = forumCategoryRepository.findOne(categoryId);
	return processLastTopic(category,currentLastTopic);
}

@Transactional
public ArrayList<ForumCategory> getRootCategories(){
	return forumCategoryRepository.findByCategoryIsNull();
}


}
