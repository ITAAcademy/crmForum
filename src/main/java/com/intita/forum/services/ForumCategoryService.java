package com.intita.forum.services;

import java.util.ArrayList;
import java.util.Date;
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
import com.intita.forum.domain.UserSortingCriteria;
import com.intita.forum.models.Course;
import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumTopic;
import com.intita.forum.models.IntitaUser;
import com.intita.forum.models.IntitaUser.IntitaUserRoles;
import com.intita.forum.models.Module;
import com.intita.forum.models.ForumCategoryStatistic;
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
	
	@Autowired
	private ForumCategoryStatisticService forumCategoryStatisticService;
	
	private final static Logger log = LoggerFactory.getLogger(ForumCategory.class);
	
	@Value("${forum.categoriesOrTopicsCountPerPage}")
	private int topicsCountForPage;
	@Autowired
	private SessionFactory sessionFactory;
	
	final private Long EDUCATIONAL_CATEGORY_ID = 1L;

@Transactional
public void update(ForumCategory category){
	forumCategoryRepository.save(category);
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
@Transactional
public ForumCategory getCategoryById(Long id){
	return forumCategoryRepository.findOne(id);
}
public Page<ForumCategory> filterNotAccesibleCategories(Page<ForumCategory> pageObj,IntitaUser user,int page){
	if (pageObj==null)return null;
	List<ForumCategory> pageContent = pageObj.getContent();
	List<ForumCategory> accessibleCategoriesList = filterNotAccesibleCategories(pageContent,user);
	for (ForumCategory category : pageContent){
		if(checkCategoryAccessToUser(user, category.getId())){
			accessibleCategoriesList.add(category);	
		}
	}
	return CustomDataConverters.listToPage(accessibleCategoriesList, page, topicsCountForPage);
}
public List<ForumCategory> filterNotAccesibleCategories(List<ForumCategory> pageContent,IntitaUser user){
	if (pageContent==null)return null;
	ArrayList<ForumCategory> accessibleCategoriesList = new ArrayList<ForumCategory>();
	for (ForumCategory category : pageContent){
		if(checkCategoryAccessToUser(user, category.getId())){
			accessibleCategoriesList.add(category);	
		}
	}
	return accessibleCategoriesList;
}
/**
 * Need to fix . To slow ...
 **/
public HashSet<Long> filterNotAccesibleCategoriesIds(HashSet<Long> categoriesIds,IntitaUser user){
	if (categoriesIds==null)return new HashSet<Long>();
	if (user==null) return new HashSet<Long>();
	HashSet<Long> accessibleCategoriesList = new HashSet<>();
	for (Long categoryId : categoriesIds){
		if(checkCategoryAccessToUser(user, categoryId)){
			accessibleCategoriesList.add(categoryId);	
		}
	}
	return accessibleCategoriesList;
}
public List<ForumCategory> filterNotAccesibleCategories(Page<ForumCategory> pageObj,IntitaUser user){
	List<ForumCategory> pageContent = pageObj.getContent();
	return filterNotAccesibleCategories(pageContent,user);
}
@Transactional
public List<ForumCategory> getSubCategoriesList(Long id,IntitaUser user,UserSortingCriteria sortingCriteria,Integer page){
	ForumCategory rootCategory = forumCategoryRepository.findOne(id);
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
			String wherePart = "WHERE c.category.id = "+id +" ";
			if (whereParam!=null)
			wherePart += " AND "+ whereParam ;
			String hql = "SELECT c FROM forum_category c " + " "+wherePart+sortingPart;
			Query query = session.createQuery(hql);
			if (whereParam!=null)
			query.setParameter("dateParam", sortingCriteria.getDateParam());
			result = query.list();
		}
	return filterNotAccesibleCategories(result,user);
}
@Transactional
public Page<ForumCategory> getSubCategoriesSinglePage(Long id,IntitaUser user,UserSortingCriteria sortingCriteria){
List<ForumCategory> subCategoriesList = getSubCategoriesList(id,user,sortingCriteria,null);
if (subCategoriesList==null) 
	subCategoriesList = new ArrayList<ForumCategory>();
Page<ForumCategory> result = CustomDataConverters.listToPage(subCategoriesList,0,subCategoriesList.size());
return result;
}

@Transactional
public Page<ForumCategory> getSubCategoriesPage(Long id,int page,IntitaUser user,UserSortingCriteria sortingCriteria){
List<ForumCategory> subCategoriesList = getSubCategoriesList(id,user,sortingCriteria,null);
if (subCategoriesList==null) 
	subCategoriesList = new ArrayList<ForumCategory>();
Page<ForumCategory> result = CustomDataConverters.listToPage(subCategoriesList,page,categoriesCountForPage,subCategoriesList.size());
return result;
}
@Transactional
public ArrayList<ForumCategory> getSubCategories(Long id){
	ForumCategory rootCategory = forumCategoryRepository.findOne(id);
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
@Transactional
public void updateCategoriesFromCourses(){
	initEducationCategory();//must be called first to set proper Id for educational category
	initCategoriesByRoles();
	
}

@Transactional
public void initCategoriesByRoles(){
	final String ROLES_CATEGORY_NAME = "Розділ по ролях";
	final String ROLES_ADMINISTRATORS_CATEGORY_NAME = "Адміністратори";
	final String ROLES_ACCOUNTANTS_CATEGORY_NAME = "Бухгалтери";
	final String ROLES_TEACHERS_CATEGORY_NAME = "Вчителі";
	
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
}
@Transactional
public void saveCourseAsCategory(Course course,ForumCategory parentCategory){		

	ForumCategory category =  getForumCategoryByCourseOrModule(course.getId(),true);
	if (category == null)
	{
		category  = ForumCategory.createInstanceForCourse(course.getTitleUa(),course.getAlias(),course.getId());
		category.setCategory(parentCategory);
		category = forumCategoryRepository.save(category);
	}
		ArrayList<Module> modules = moduleService.getAllFromCourse(course);
		for (Module module : modules){
			ForumCategory c = getForumCategoryByCourseOrModule(module.getId(),false);
			if (c == null){
			//module isn't already excist
			c = ForumCategory.createInstanceForModule(module.getTitleUa(),module.getAlias(),module.getId());
			c.setCategory(category);
			ForumCategoryStatistic statistic = new ForumCategoryStatistic();
			statistic=forumCategoryStatisticService.save(statistic);
			c.setStatistic(statistic);
			forumCategoryRepository.save(c);
			}
		}	
}
@Transactional
public ForumCategory getForumCategoryByCourseOrModule(Long id,boolean isCourse){
	List<ForumCategory> list = forumCategoryRepository.findByCourseOrModuleIdAndIsCourseCategory(id,true);
	if (list.size()>0) return list.get(0);
	return null;
}

@Transactional
public ForumCategory getOrCreateForumCategory(String name,ForumCategory parent,String description,boolean containSubCategories,IntitaUserRoles role){
	//return getOrCreateForumCategory(name,parent,description,null,null,containSubCategories);
	ForumCategory roleCategory = null;
	Long parentId = (parent==null) ? null : parent.getId();
	ForumCategory parentCategory = null;
	if (parentId!=null)parentCategory = forumCategoryRepository.findOne(parentId);
	ArrayList<ForumCategory> categories = null;
	if (parentId == null)
	categories = forumCategoryRepository.findByNameAndCategoryIdIsNull(name);
	else categories = forumCategoryRepository.findByNameAndCategoryId(name,parentId);
	if (categories.size()>0){
		roleCategory = categories.get(0);
	}
	if (roleCategory == null)
	{
		roleCategory = ForumCategory.createInstance(name,description,containSubCategories);
		if (role!=null)
		roleCategory.addRoleDemand(role);
		roleCategory.setCategory(parentCategory);
		ForumCategoryStatistic statistic = new ForumCategoryStatistic();
		statistic=forumCategoryStatisticService.save(statistic);
		roleCategory.setStatistic(statistic);
	}
	roleCategory = forumCategoryRepository.save(roleCategory);
	return roleCategory;
}
@Transactional
public ForumCategory getOrCreateForumCategory(String name,ForumCategory parent,String description,
		boolean containSubCategories){
	return getOrCreateForumCategory(name, parent,description,containSubCategories,null);
}
//IntitaUserRoles

/*@Transactional
public ForumCategory getOrCreateForumCategory(String name,ForumCategory parent,String description,
		Long courseOrModuleId,Boolean derivedFromCourse,boolean containSubCategories ){
	
	ForumCategory targetCategory = null;
	ArrayList<ForumCategory> categoriesTemp;
	boolean isDerivedFromCourseOrCategory = derivedFromCourse!=null && courseOrModuleId !=null;
	// if category created for module or course already excist
	if(isDerivedFromCourseOrCategory){
	if (forumCategoryRepository.countByCourseModuleIdAndIsCourseCategory(
			courseOrModuleId,derivedFromCourse)==0){
			if (derivedFromCourse)
		targetCategory = ForumCategory.createInstanceForCourse(name,description,courseOrModuleId);
		else
			targetCategory = ForumCategory.createInstanceForModule(name,description,courseOrModuleId);	
			
			ForumCategoryStatistic statistic = new ForumCategoryStatistic();
			statistic=forumCategoryStatisticService.save(statistic);
				targetCategory.setStatistic(statistic);
				
		targetCategory.setCategory(parent);
		targetCategory = forumCategoryRepository.save(targetCategory);
	}
	else{
			categoriesTemp = forumCategoryRepository.findFirstByNameAndCourseOrModuleIdWhereDateEqualMinDate(name, courseOrModuleId);
		if (categoriesTemp != null && categoriesTemp.size()>0){
			targetCategory = categoriesTemp.get(0);
		}
	}
	}
	else{
	//categoriesTemp = forumCategoryRepository.findFirstByNameWhereDateEqualMinDate(name);
		targetCategory = ForumCategory.createInstance(name, description, containSubCategories);
		targetCategory.setCategory(parent);
		targetCategory = forumCategoryRepository.save(targetCategory);
	}
	
	return targetCategory;
}
*/
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
	if (lastCategory==null)return null;
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

public LinkedList<ForumCategory> getAllSubCategories(ForumCategory category,HashSet<Long> idsParam){
	if (category==null)return null;
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
/**
 * Need to fix . To slow ...
 **/
@Transactional
public HashSet<Long> getAllSubCategoriesIds(Long categoryId,HashSet<Long> idsParam){
	ForumCategory category = forumCategoryRepository.findOne(categoryId);
	return getAllSubCategoriesIds(category,idsParam);
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
public void updateLastTopic(ForumCategory c,ForumTopic topic){
	ForumCategory category = forumCategoryRepository.findOne(c.getId());
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
public ForumTopic getLastTopic(Long categoryId,IntitaUser user){
	ForumCategory category = forumCategoryRepository.findOne(categoryId);
	return category.getLastTopic();
	/*long startTime = new Date().getTime();
	long delta = new Date().getTime() - startTime;
	int currentLogStringIndex = 0;
	ForumCategory category = forumCategoryRepository.findOne(categoryId);
	if (category==null) return null;
	delta = new Date().getTime() - startTime;
	HashSet<Long> categoriesIds = getAllSubCategoriesIds(category,null);
	delta = new Date().getTime() - startTime;
	if (categoriesIds==null){
		categoriesIds = new HashSet<Long>();
	}
	else{
		categoriesIds = filterNotAccesibleCategoriesIds(categoriesIds,user);
	}
	delta = new Date().getTime() - startTime;
	categoriesIds.add(category.getId());
	ForumTopic lastTopic = forumCategoryRepository.getLastTopic(categoriesIds);
	delta = new Date().getTime() - startTime;
	delta = new Date().getTime() - startTime;
	return lastTopic;
	*/
}
@Transactional
public List<ForumCategoryStatistic> getCategoriesStatistic(List<ForumCategory> categories){	
	List<ForumCategoryStatistic> categoriesStatistic = new ArrayList<ForumCategoryStatistic>();
	for (ForumCategory c : categories){
	categoriesStatistic.add(getCategorieStatistic(c));
	}
	return categoriesStatistic;
}
@Transactional
public ForumCategoryStatistic getCategorieStatistic(ForumCategory category){
	/*HashSet<Long> allSubcategoriesIds = getAllSubCategoriesIds(category,null);
	int categoriesCount = allSubcategoriesIds.size();
	HashSet<Long> topicsIds = forumTopicService.getAllSubTopicsIds(category,allSubcategoriesIds);
	int messagesCount = topicMessageService.getTotalMessagesCountByTopicsIds(topicsIds);*/	
	ForumCategoryStatistic statistic = category.getStatistic();
	return statistic;
}


}
