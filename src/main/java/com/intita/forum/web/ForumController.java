package com.intita.forum.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.kefirsf.bb.BBProcessorFactory;
import org.kefirsf.bb.ConfigurationFactory;
import org.kefirsf.bb.TextProcessor;
import org.kefirsf.bb.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.forum.config.CustomAuthenticationProvider;
import com.intita.forum.domain.ForumTreeNode;
import com.intita.forum.domain.ForumTreeNode.TreeNodeType;
import com.intita.forum.domain.SessionProfanity;
import com.intita.forum.domain.UserActionInfo;
import com.intita.forum.domain.UserSortingCriteria;
import com.intita.forum.domain.UserSortingCriteria.ShowItemsCriteria;
import com.intita.forum.domain.UserSortingCriteria.SortByField;
import com.intita.forum.event.LoginEvent;
import com.intita.forum.event.ParticipantRepository;
import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumTopic;
import com.intita.forum.models.IntitaUser;
import com.intita.forum.models.Lecture;
import com.intita.forum.models.TopicMessage;
import com.intita.forum.models.ForumCategoryStatistic;
import com.intita.forum.services.ConfigParamService;
import com.intita.forum.services.ForumCategoryService;
import com.intita.forum.services.ForumCategoryStatisticService;
import com.intita.forum.services.ForumLangService;
import com.intita.forum.services.ForumTopicService;
import com.intita.forum.services.IntitaUserService;
import com.intita.forum.services.LectureService;
import com.intita.forum.services.TopicMessageService;
import com.intita.forum.util.CustomPrettyTime;
import com.intita.forum.util.ProfanityChecker;

import utils.CustomDataConverters;

/**
 * Controller that handles WebSocket chat messages
 * 
 * @author Zinchuk Roman
 */
@Service
@Controller
public class ForumController {

	@Autowired
	ConfigParamService configParamService;
	private final static Logger log = LoggerFactory.getLogger(ForumController.class);

	@Autowired private ProfanityChecker profanityFilter;

	@Autowired private SessionProfanity profanity;

	@Autowired private ParticipantRepository participantRepository;

	@Autowired private CustomAuthenticationProvider authenticationProvider;

	@Autowired private IntitaUserService intitaUserService;
	@Autowired private TopicMessageService userMessageService;

	@Autowired private LectureService lectureService;
	@Autowired private ForumCategoryService forumCategoryService;
	@Autowired private ForumTopicService forumTopicService;
	@Autowired private TopicMessageService topicMessageService;
	@Autowired private ForumLangService forumLangService;
	@Autowired private ForumCategoryStatisticService forumCategoryStatisticService;

	@PersistenceContext
	EntityManager entityManager;

	private static final String KEFIRCONFIG_PATH = "bbcode/kefirconfig.xml";

	private static int MAXIMAL_USER_INACTIVE_TIME_MINUTES = 2; 
	private TextProcessor bbCodeProcessor = null;
	private BBProcessorFactory processorFactory;

	private HashMap<Long,UserActionInfo> onlineUsersActivity = new HashMap<Long,UserActionInfo>() ;
	@PostConstruct
	private void initTextProcessoe() {
		processorFactory = BBProcessorFactory.getInstance();
		refreshConfigParameters();
		forumCategoryService.updateCategoriesFromCourses();
		//forumCategoryStatisticService.createEmptyCategoriesStatisticForAllCategories();
		forumCategoryStatisticService.updateAllCategoriesStatistic();
		log.info("all categories statistic updated");
		forumCategoryService.updateLastTopics();
	}
	public TextProcessor getTextProcessorInstance(HttpServletRequest request){
		if(bbCodeProcessor == null)//recreate bbCode processor?
		{
			Configuration configuration  = ConfigurationFactory.getInstance().createFromResource(KEFIRCONFIG_PATH);
			HashMap<String, CharSequence> map = new HashMap<>(configuration.getParams());

			map.put("targetURL", request.getContextPath());

			configuration.setParams(map);
			bbCodeProcessor = processorFactory.create(configuration);
		}
		return bbCodeProcessor;
	}

	private final static ObjectMapper mapper = new ObjectMapper();

	private final ConcurrentHashMap<String, ArrayList<Object>> infoMap = new ConcurrentHashMap<>();

	public void addFieldToInfoMap(String key, Object value)
	{
		ArrayList<Object> listElm = infoMap.get(key);
		if(listElm == null)
		{
			listElm = new ArrayList<>();
			infoMap.put(key, listElm);
		}
		listElm.add(value);
	}	


	/********************
	 * GET CHAT USERS LIST FOR TEST
	 *******************/
	@RequestMapping(value = "/chat/users", method = RequestMethod.POST)
	@ResponseBody
	public String getUsers(Authentication principal) throws JsonProcessingException {

		Page<IntitaUser> pageUsers = intitaUserService.getIntitaUsers(1, 15);
		Set<LoginEvent> userList = new HashSet<>();
		for(IntitaUser user : pageUsers)
		{
			userList.add(new LoginEvent(user.getId(),user.getUsername(), user.getAvatar(),participantRepository.isOnline(""+user.getId())));
		}
		return  new ObjectMapper().writeValueAsString(userList);
	}

	@RequestMapping(value = "/operations/category/update_all_from_courses", method = RequestMethod.GET)
	public String updateCategoriesFromCourses(HttpServletRequest request){
		String referer = request.getHeader("Referer");
		forumCategoryService.updateCategoriesFromCourses();
		if (referer==null) return "redirect:/";
		return "redirect:"+referer;
	}

	@RequestMapping(value = "/chat/lectures/getfivelike/", method = RequestMethod.POST)
	@ResponseBody
	public ArrayList<Lecture> getLecturesLike(@RequestBody String title) throws JsonProcessingException {
		List<Lecture> lecturesList = new ArrayList<Lecture>();

		int lang = getCurrentLangInt();

		if (lang == lectureService.EN)
			lecturesList = lectureService.getFirstFiveLecturesByTitleEnLike(title);
		else
			if (lang == lectureService.RU)
				lecturesList = lectureService.getFirstFiveLecturesByTitleRuLike(title);
		if (lang == lectureService.UA)
			lecturesList = lectureService.getFirstFiveLecturesByTitleUaLike(title);	

		return  new ArrayList<Lecture>(lecturesList);
	}

	@RequestMapping(value="/chat/lectures/get_five_titles_like/", method = RequestMethod.GET)
	@ResponseBody
	public String getLecturesTitlesLike(@RequestParam String title) throws JsonProcessingException {


		List<String> lecturesList = new ArrayList<>();		
		int lang = getCurrentLangInt();

		if (lang == lectureService.EN)
			lecturesList = lectureService.getFirstFiveLecturesTitlesByTitleEnLike(title);
		else
			if (lang == lectureService.RU)
				lecturesList = lectureService.getFirstFiveLecturesTitlesByTitleRuLike(title);
		if (lang == lectureService.UA)
			lecturesList = lectureService.getFirstFiveLecturesTitlesByTitleUaLike(title);	

		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = mapper.writeValueAsString(lecturesList);
		return jsonInString;
	}

	/*
	 * Out from room
	 */


	public int getCurrentLangInt()
	{
		String lang = getCurrentLang();
		if (lang.equals(("ua")))
			return 0;
		if (lang.equals(("ru")))
			return 1;
		return 2;
	}
	public static String getCurrentLang()
	{
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpSession session = attr.getRequest().getSession(false);
		String lg;
		if(session != null)
			lg = (String) session.getAttribute("chatLg");
		else
			lg = "ua";
		if(lg == null)
			return "ua";
		return lg;
	}
	@RequestMapping(value="/operations/leave_site", method = RequestMethod.POST)
	public void userLeaveSiteMapping(){
		//TODO 
	}

	@RequestMapping(value="/login", method = RequestMethod.GET)
	public ModelAndView  getLoginPage(HttpServletRequest request, @RequestParam(required = false) String before,  Model model,Authentication principal) {
		Authentication auth =  authenticationProvider.autorization(authenticationProvider);

		//chatLangService.updateDataFromDatabase();
		if(before != null)
		{
			return new ModelAndView("redirect:"+ before);
		}
		return new ModelAndView("redirect:/");
	}

	@RequestMapping(value="/", method = RequestMethod.GET)
	public ModelAndView  getIndex(RedirectAttributes redirectAttributes,HttpServletRequest request,HttpServletResponse response, @RequestParam(required = false) String before,  Model model,Authentication principal) {
		Authentication auth =  authenticationProvider.autorization(authenticationProvider);
		return viewCategoryById(redirectAttributes,null,null,1,request,response,auth,null,null);
	}

	@RequestMapping(value="/test",method = RequestMethod.GET)
	@ResponseBody 
	public String testMapping(){
		return "test good";
	}
	/*******************************
	 * Category @RequestMapping
	 ******************************/
	@PreAuthorize("@forumCategoryService.checkCategoryAccessToAuthentication(authentication,#categoryId)")
	@RequestMapping(value="/view/category/{categoryId}/{pageOfTopic}",method = RequestMethod.GET)
	public ModelAndView viewCategoryByIdMapping(RedirectAttributes redirectAttributes, @RequestParam(required = false) String search,
			@PathVariable Long categoryId, @PathVariable int pageOfTopic, HttpServletRequest request,HttpServletResponse response, Authentication auth){
		return viewCategoryById(redirectAttributes,search,categoryId,pageOfTopic,request,response,auth,null,null);
	}
	public ModelAndView viewCategoryById(RedirectAttributes redirectAttributes, String search,  Long categoryId, 
			int pageOfTopic, HttpServletRequest request,HttpServletResponse response, 
			Authentication auth,UserSortingCriteria categoriesSortingCriteria, UserSortingCriteria topicsSortingCriteria){
		long beginTime = new Date().getTime();
		long totalFuncTime = beginTime;
		int currentLogString = 0;
		if(search != null)
		{
			redirectAttributes.addAttribute("searchvalue", search);
			redirectAttributes.addAttribute("type", SearchType.CATEGORY);
			return new ModelAndView("redirect:" + "/view/search/" + categoryId + "/1");
		}
		totalFuncTime = new Date().getTime() - beginTime;

		IntitaUser user = (IntitaUser) auth.getPrincipal();
		ModelAndView model = new ModelAndView();
		ForumCategory category = forumCategoryService.getCategoryById(categoryId);
		if (user!=null){		
			model.addObject("user", (IntitaUser)auth.getPrincipal());
			onlineUsersActivity.put(user.getId(), UserActionInfo.forCategory(categoryId));
		}
		totalFuncTime = new Date().getTime() - beginTime;

		model.addObject("currentCategory",category);		
		model.addObject("categoriesTree",forumCategoryService.getCategoriesTree(category));
		CustomPrettyTime p = new CustomPrettyTime(new Locale(getCurrentLang()));
		model.addObject("prettyTime",p);
		model.addObject("config",configParamService.getCachedConfigMap());
		model.setViewName("category_view");
		model.addObject("isAdmin",intitaUserService.isAdmin(user.getId()));
		model.addObject("bbcode", getTextProcessorInstance(request));
		totalFuncTime = new Date().getTime() - beginTime;
		//Categories model configuring
		if (categoriesSortingCriteria==null){	
			categoriesSortingCriteria = UserSortingCriteria.loadFromCookie(ForumCategory.class, request);
		}
		else{
			categoriesSortingCriteria.saveToCookie(ForumCategory.class, request,response);
		}
		totalFuncTime = new Date().getTime() - beginTime;
		ModelMap categoriesModelMap = new ModelMap();
		Page<ForumCategory> categories = null;
		if (categoryId!=null)categories = forumCategoryService.getSubCategoriesSinglePage(categoryId,auth,categoriesSortingCriteria);
		else categories = forumCategoryService.getMainCategories(0);
		totalFuncTime = new Date().getTime() - beginTime;
		categoriesModelMap.addAttribute("items",categories);
		ArrayList<ForumTopic> lastTopics = new ArrayList<ForumTopic>();
		for (ForumCategory c : categories){
			lastTopics.add(c.getLastTopic());
		}
		totalFuncTime = new Date().getTime() - beginTime;
		categoriesModelMap.addAttribute("lastTopics",lastTopics);
		categoriesModelMap.addAttribute("statistic",forumCategoryService.getCategoriesStatistic(categories.getContent()));
		categoriesModelMap.addAttribute("sortingCriteria",categoriesSortingCriteria.convertToOrdinal());
		categoriesModelMap.addAttribute("sortingMenu",UserSortingCriteria.getSortingMenuData(ForumCategory.class,forumLangService.getLocalization()));
		model.addObject("categoriesModel",categoriesModelMap);
		// Topics model configuring
		if (topicsSortingCriteria==null){	
			topicsSortingCriteria = UserSortingCriteria.loadFromCookie(ForumTopic.class, request);
		}
		else{
			topicsSortingCriteria.saveToCookie(ForumTopic.class,request, response);
		}
		totalFuncTime = new Date().getTime() - beginTime;
		Page<ForumTopic> topics = forumTopicService.getAllTopicsSortedByPin(categoryId, pageOfTopic-1,topicsSortingCriteria);
		int pagesCount = topics.getTotalPages();
		if(pagesCount<1)pagesCount=1;
		ArrayList<TopicMessage> lastMessages = new ArrayList<TopicMessage>();
		for (ForumTopic t : topics){
			TopicMessage lastMessage = topicMessageService.getLastMessageByTopic(t);
			lastMessages.add(lastMessage);
		}
		totalFuncTime = new Date().getTime() - beginTime;
		ModelMap topicsModelMap = new ModelMap();
		topicsModelMap.addAttribute("lastMessages",lastMessages);
		topicsModelMap.addAttribute("items",topics);

		List<ForumTopic> pageTopics = topics.getContent();
		topicsModelMap.addAttribute("statistic",forumTopicService.getTopicsStatistic(pageTopics));
		topicsModelMap.addAttribute("sortingCriteria",topicsSortingCriteria.convertToOrdinal());
		topicsModelMap.addAttribute("sortingMenu",UserSortingCriteria.getSortingMenuData(ForumTopic.class,forumLangService.getLocalization()));
		totalFuncTime = new Date().getTime() - beginTime;
		model.addObject("currentPage",pageOfTopic);
		model.addObject("pagesCount",pagesCount);
		model.addObject("topicsModel",topicsModelMap);
		totalFuncTime = new Date().getTime() - beginTime;
		return model;
	}
	@PreAuthorize("@forumCategoryService.checkCategoryAccessToAuthentication(authentication,#categoryId)")
	@RequestMapping(value="/view/category/{categoryId}",method = RequestMethod.GET)
	public ModelAndView viewCategoryByIdMapping(RedirectAttributes redirectAttributes, @RequestParam(required = false) String search,@PathVariable Long categoryId, HttpServletRequest requset,HttpServletResponse response, Authentication principal){
		return viewCategoryById(redirectAttributes, search, categoryId, 1, requset,response, principal,null,null);
	}
	@PreAuthorize("@forumCategoryService.checkCategoryAccessToAuthentication(authentication,#categoryId)")
	@RequestMapping(value="/view/category/{categoryId}",method = RequestMethod.POST)
	public ModelAndView viewCategoryByIdMappingPost(@RequestParam Map<String,String> requestParams,RedirectAttributes redirectAttributes,@PathVariable Long categoryId, HttpServletRequest request,HttpServletResponse response, 
			Authentication principal){
		boolean isCategoriesCriteriaSet = true, isTopicsCriteriaSet = true;
		String search = requestParams.get("search");
		Integer categoriesWhere = null,categoriesSort=null,topicsWhere=null,topicsSort=null;
		Boolean categoriesOrder=null,topicsOrder=null;
		try{
			categoriesWhere = Integer.parseInt(requestParams.get("categories_where"));
			categoriesSort = Integer.parseInt(requestParams.get("categories_sort"));
			categoriesOrder = Integer.parseInt(requestParams.get("categories_order")) > 0 ;
		}
		catch(NumberFormatException e){
			log.error(e.getMessage());
			isCategoriesCriteriaSet = false;
		}
		try{
			topicsWhere = Integer.parseInt(requestParams.get("topics_where"));
			topicsSort = Integer.parseInt(requestParams.get("topics_sort"));
			topicsOrder = Integer.parseInt(requestParams.get("topics_order")) > 0;
		}
		catch(NumberFormatException e){
			log.error(e.getMessage());
			isTopicsCriteriaSet = false;
		}

		UserSortingCriteria categoriesCriteria = null,topicsCriteria=null;
		if (isCategoriesCriteriaSet)
			categoriesCriteria = new UserSortingCriteria(ShowItemsCriteria.fromInteger(categoriesWhere),SortByField.fromInteger(categoriesSort),categoriesOrder);
		if (isTopicsCriteriaSet)
			topicsCriteria = new UserSortingCriteria(ShowItemsCriteria.fromInteger(topicsWhere),SortByField.fromInteger(topicsSort),topicsOrder);


		return viewCategoryById(redirectAttributes, search, categoryId, 1, request,response, principal,categoriesCriteria,topicsCriteria);
	}
	/******************************
	 * REDIRECT @RequestMapping 
	 ******************************/
	@RequestMapping(value="/redirect/message/{msgId}",method = RequestMethod.GET)
	public ModelAndView redirectToMsg(@PathVariable("msgId") Long msgId, HttpServletRequest request, Authentication auth){
		return new ModelAndView("redirect:" + topicMessageService.getUrl(msgId)); 
	}
	/******************************
	 * SEARCH @RequestMapping 
	 ******************************/
	class SearchType{
		public final static int CATEGORY = 1 << 0; 
		public final static int TOPIC = 1 << 1;
		public final static int CATEGORY_NAME = 1 << 2;
	}
	@PreAuthorize("@forumCategoryService.checkCategoryAccessToAuthentication(authentication,#categoryId)")
	@RequestMapping(value="/view/search/{categoryId}/{page}",method = RequestMethod.GET)
	public ModelAndView viewSearch(@RequestParam(name="searchvalue") String searchValue,@RequestParam(name="type") int type, @PathVariable Long categoryId, @PathVariable int page, HttpServletRequest request, Authentication auth){
		ModelAndView model = new ModelAndView("topic_view");
		Page<TopicMessage> messages = null;
		LinkedList<ForumTreeNode> tree = null;
		String t = request.getQueryString();

		if((SearchType.CATEGORY & type) == 1 || (SearchType.CATEGORY_NAME & type) == 1 || !(SearchType.TOPIC == type))
		{
			ForumCategory category = forumCategoryService.getCategoryById(categoryId);
			if((SearchType.CATEGORY_NAME | SearchType.CATEGORY) == type)
			{
				messages = topicMessageService.searchByTopicNameAndBodyAndAsAndInCategory(searchValue, category, 0);
			}
			else
				if(SearchType.CATEGORY == type)
					messages = topicMessageService.searchInCategory(category, searchValue, 0);
				else
					if(SearchType.CATEGORY_NAME ==  type)
						messages = topicMessageService.searchByTopicNameAsAndInCategory(searchValue, category, 0);
			tree = forumCategoryService.getCategoriesTree(category);
		}else
		{
			ForumTopic topic = forumTopicService.getTopic(categoryId);
			messages = topicMessageService.searchInTopic(topic, searchValue, 0);
			tree = forumCategoryService.getCategoriesTree(topic);
		}
		tree.add(new ForumTreeNode("Пошук", TreeNodeType.OTHER));
		model.addObject("categoriesTree", tree);

		int pagesCount = 0;
		if (messages!=null){
			model.addObject("messages",messages);
			pagesCount = messages.getTotalPages();
		}
		if(pagesCount<1)pagesCount=1;
		model.addObject("pagesCount",pagesCount);
		model.addObject("currentPage",page);
		IntitaUser user = (IntitaUser)auth.getPrincipal();
		if(user !=null){
			onlineUsersActivity.put(user.getId(), UserActionInfo.forCategory(categoryId));
			model.addObject("user", user);
		}
		CustomPrettyTime p = new CustomPrettyTime(new Locale(getCurrentLang()));
		model.addObject("prettyTime",p);

		//  System.out.println(p.format(new Date()));
		model.addObject("config",configParamService.getCachedConfigMap());
		model.addObject("bbcode", getTextProcessorInstance(request));

		Map<Long, Boolean> canEditMap = new HashMap<>();
		if(messages != null)
			for (TopicMessage topicMessage : messages) {
				canEditMap.put(topicMessage.getId(), topicMessageService.canEdit((IntitaUser)auth.getPrincipal(), topicMessage));
			}

		model.addObject("canEditMap", canEditMap);
		model.addObject("blockSearch", true);
		model.addObject("paginationLink", "/view/search/" + categoryId + "/");
		model.addObject("onlineUsers", onlineUsersActivity);


		return model;
	}



	/*******************************
	 * TOPIC @RequestMapping
	 ******************************/
	@PreAuthorize("@forumTopicService.checkTopicAccessToUser(authentication,#topicId)")
	@RequestMapping(value="/view/topic/{topicId}/{page}",method = RequestMethod.GET)
	public ModelAndView viewTopicByIdMapping(RedirectAttributes redirectAttributes, 
			@RequestParam(required = false) String search, @PathVariable Long topicId,
			@PathVariable int page, HttpServletRequest request,HttpServletResponse response, Authentication auth){
		return viewTopicById(redirectAttributes,search,topicId,page,request,response,auth,null);
	}
	@PreAuthorize("@forumTopicService.checkTopicAccessToUser(authentication,#topicId)")
	@RequestMapping(value="/view/topic/{topicId}/{page}",method = RequestMethod.POST)
	public ModelAndView viewTopicByIdMappingPostWithPage(RedirectAttributes redirectAttributes, 
			@RequestParam(required = false) String search, @PathVariable Long topicId,
			@PathVariable int page, HttpServletRequest request,HttpServletResponse response, Authentication auth,
			@RequestParam(required = false) int where,@RequestParam(required = false) int sort,
			@RequestParam(required = false) Boolean order){
		UserSortingCriteria criteria = new UserSortingCriteria(ShowItemsCriteria.fromInteger(where),SortByField.fromInteger(sort),order);
		return viewTopicById(redirectAttributes,search,topicId,page,request,response,auth,criteria);
	}
	@PreAuthorize("@forumTopicService.checkTopicAccessToUser(authentication,#topicId)")
	@RequestMapping(value="/view/topic/{topicId}",method = RequestMethod.POST)
	public ModelAndView viewTopicByIdMappingPost(RedirectAttributes redirectAttributes, 
			@RequestParam(required = false) String search, @PathVariable Long topicId,HttpServletRequest request, HttpServletResponse response,Authentication auth,
			@RequestParam(required = false) int where,@RequestParam(required = false) int sort,
			@RequestParam(required = false) Boolean order){
		return viewTopicByIdMappingPostWithPage(redirectAttributes,search,topicId,1,request,response,auth,where,sort,order);
	}
	public ModelAndView viewTopicById(RedirectAttributes redirectAttributes,  String search,  Long topicId,  int page, HttpServletRequest request,HttpServletResponse response, Authentication auth,UserSortingCriteria sortingCriteria){
		if(search != null)
		{
			redirectAttributes.addAttribute("searchvalue", search);
			redirectAttributes.addAttribute("type", SearchType.TOPIC);
			return new ModelAndView("redirect:" + "/view/search/" + topicId + "/1");
		}
		ModelAndView model = new ModelAndView("topic_view");
		if (sortingCriteria==null){	
			sortingCriteria = UserSortingCriteria.loadFromCookie(TopicMessage.class, request);
		}
		else{
			sortingCriteria.saveToCookie(TopicMessage.class,request, response);
		}
		model.addObject("sortingCriteria",sortingCriteria.convertToOrdinal());
		model.addObject("sortingMenu",UserSortingCriteria.getSortingMenuData(TopicMessage.class,forumLangService.getLocalization()));
		Page<TopicMessage> messages = topicMessageService.getAllMessagesAndPinFirst(topicId, page-1,sortingCriteria);
		ForumTopic topic = forumTopicService.getTopic(topicId);
		int pagesCount = 0;
		if (messages!=null){
			model.addObject("messages",messages);
			pagesCount = messages.getTotalPages();
		}
		if(pagesCount<1)pagesCount=1;
		model.addObject("pagesCount",pagesCount);
		model.addObject("currentPage",page);
		model.addObject("topic",topic);
		IntitaUser user = (IntitaUser)auth.getPrincipal();
		if (user!=null){
			model.addObject("user", user);
			onlineUsersActivity.put(user.getId(), UserActionInfo.forTopic(topicId));
		}
		CustomPrettyTime p = new CustomPrettyTime(new Locale(getCurrentLang()));
		model.addObject("prettyTime",p);

		//  System.out.println(p.format(new Date()));
		model.addObject("categoriesTree",forumCategoryService.getCategoriesTree(topic));
		model.addObject("config",configParamService.getCachedConfigMap());
		model.addObject("bbcode", getTextProcessorInstance(request));

		Map<Long, Boolean> canEditMap = new HashMap<>();
		if(messages != null)
			for (TopicMessage topicMessage : messages) {
				canEditMap.put(topicMessage.getId(), topicMessageService.canEdit((IntitaUser)auth.getPrincipal(), topicMessage));
			}
		model.addObject("onlineUsers", onlineUsersActivity);
		model.addObject("canEditMap", canEditMap);
		model.addObject("paginationLink", "/view/topic/" + topicId + "/");
		model.addObject("localization",forumLangService.getLocalizationMap());

		return model;
	}
	@Scheduled(fixedRate = 120000)
	public void reportCurrentTime() {
		Iterator<Long> i = onlineUsersActivity.keySet().iterator();
		while (i.hasNext() ){
			Long key = i.next();
			UserActionInfo info = onlineUsersActivity.get(key);
			long currentTime = new Date().getTime();
			long delta =  currentTime - info.getLastActionTime();
			long minutesDelta = CustomDataConverters.millisecondsToMinutes(delta);
			if (minutesDelta>=MAXIMAL_USER_INACTIVE_TIME_MINUTES){
				i.remove();
			}
		}
	}

	@RequestMapping(value="/operations/clearcookie/sorting_config",method = RequestMethod.GET)
	public String clearCookiesForSorting( @RequestParam(value="itemType") String itemType,HttpServletResponse response,HttpServletRequest request){
		String referer = request.getHeader("Referer");
		String classPrefix = "com.intita.forum.models.";
		String className = classPrefix+itemType;
		Class itemClass = null;
		try {
			itemClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			log.error("class not found:"+className);
			return "redirect:"+referer;
		}
		UserSortingCriteria.removeFromCookie(itemClass,request,response);
		return "redirect:"+referer;
		//Cookie cookie = new Cookie
	}
	@PreAuthorize("@forumTopicService.checkTopicAccessToUser(authentication,#topicId)")
	@RequestMapping(value="/view/topic/{topicId}",method = RequestMethod.GET)
	public ModelAndView viewTopicByIdMapping(RedirectAttributes redirectAttributes, @RequestParam(required = false) String search, @PathVariable Long topicId, HttpServletRequest request,HttpServletResponse response, Authentication auth){	
		return viewTopicById(redirectAttributes, search, topicId, 1, request,response, auth,null);
	}

	@PreAuthorize("@topicMessageService.checkPostAccessToUser(authentication,#postId)")
	@RequestMapping(value="/view/post/{postId}",method = RequestMethod.GET)
	public String viewPostById(@PathVariable Long postId, HttpServletRequest request, Authentication auth) throws Exception{	
		TopicMessage post = topicMessageService.getMessage(postId);
		if (post==null)throw new Exception("post not found");
		ForumTopic topic = post.getTopic();
		if (topic==null)throw new Exception("post haven't topic");
		int pages = topicMessageService.getPagesCountByTopicIdAndMessageid(topic.getId(), post.getDate());
		Long topicId = topic.getId();
		//ModelAndView result = viewTopicById(topicId, pages, request, auth);
		return "redirect:/view/topic/"+topicId+"/"+pages+"#msg"+post.getId();
	}


	@ResponseBody
	@PreAuthorize("@forumTopicService.checkTopicAccessToUser(authentication,#topicId)")
	@RequestMapping(value="/messages/add/{topicId}",method = RequestMethod.POST)
	public ResponseEntity<Map<String,String>> addNewMessage(@RequestParam("text") String postText,@PathVariable Long topicId,HttpServletRequest request, Authentication auth){
		String referer = request.getHeader("Referer");
		if (postText.length()==0)
			return new ResponseEntity<Map<String,String>>(HttpStatus.BAD_REQUEST);
		IntitaUser currentUser = intitaUserService.getCurrentIntitaUser();
		if(currentUser.isAnonymous())
			return new ResponseEntity<Map<String,String>>(HttpStatus.UNAUTHORIZED);
		ForumTopic topic = forumTopicService.getTopic(topicId);
		TopicMessage message = new TopicMessage(currentUser,topic,postText);
		topicMessageService.addMessage(message);
		Page<TopicMessage> messages = topicMessageService.getAllMessagesAndPinFirst(topicId, 0,null);
		HashMap<String,String> messageMap = new HashMap<String,String>();
		messageMap.put("id", topicId.toString());
		messageMap.put("page", ""+messages.getTotalPages());
		messageMap.put("topic", topicId.toString());
		return new ResponseEntity<Map<String,String>>(messageMap,HttpStatus.OK);//"redirect:/view/topic/" + topicId + "/" + messages.getTotalPages();//go to last				
	}
	public void updateCategoriesStatistic(){
		List<ForumCategory> categories = forumCategoryService.getAllCategories();
		for(ForumCategory categorie : categories){
			//TODO
		}
	}



	@ResponseBody
	@PreAuthorize("@forumCategoryService.checkCategoryAccessToAuthentication(authentication,#categoryId)")
	@RequestMapping(value="/operations/category/{categoryId}/add_topic",method = RequestMethod.POST)
	public ResponseEntity<Long> addTopic(@RequestParam(value = "topic_name") String topicName,@RequestParam(value = "topic_text") String topicText,@PathVariable Long categoryId,Authentication auth,HttpServletRequest request){
		if (topicName==null || topicName.length()<=0 || topicText==null || topicText.length()<=0) return new ResponseEntity<Long>(HttpStatus.BAD_REQUEST);
		String referer = request.getHeader("Referer");
		IntitaUser author  = (IntitaUser) auth.getPrincipal();
		if (author == null || author.isAnonymous()) return new ResponseEntity<Long>(HttpStatus.UNAUTHORIZED);
		ForumCategory category = forumCategoryService.getCategoryById(categoryId);
		if (category == null) return new ResponseEntity<Long>(HttpStatus.BAD_REQUEST);
		ForumTopic topic = forumTopicService.addTopic(topicName,category,author);
		if (topic == null) return new ResponseEntity<Long>(HttpStatus.BAD_REQUEST);
		addNewMessage(topicText, topic.getId(), request, auth);
		return new ResponseEntity<Long>(topic.getId(),HttpStatus.OK);//"redirect:"+"/view/topic/"+
	}

	@ResponseBody
	@PreAuthorize("@forumCategoryService.checkCategoryAccessToAuthentication(authentication,#categoryId)")
	@RequestMapping(value="/operations/category/{categoryId}/add_category",method = RequestMethod.POST)
	public ResponseEntity<Long> addCategory(@RequestParam(value = "category_name") String categoryName, @PathVariable Long categoryId,Authentication auth,HttpServletRequest request){
		if (categoryName==null || categoryName.length()<=0) return new ResponseEntity<Long>(HttpStatus.BAD_REQUEST);
		String referer = request.getHeader("Referer");
		IntitaUser author  = (IntitaUser) auth.getPrincipal();
		if (author == null || author.isAnonymous()) return new ResponseEntity<Long>(HttpStatus.UNAUTHORIZED);
		ForumCategory category = forumCategoryService.getCategoryById(categoryId);
		if (category == null) return new ResponseEntity<Long>(HttpStatus.BAD_REQUEST);

		ForumCategory nCategory = forumCategoryService.update(new ForumCategory());
		ForumCategoryStatistic statistic = forumCategoryStatisticService.save(new ForumCategoryStatistic());
		statistic.setCategory(nCategory);
		nCategory.setStatistic(statistic);//???
		nCategory.setName(categoryName);
		nCategory.setCategory(category);
		forumCategoryService.update(nCategory);
		forumCategoryStatisticService.save(statistic);//???

		if (nCategory == null || statistic == null) return new ResponseEntity<Long>(HttpStatus.BAD_REQUEST);
		return new ResponseEntity<Long>(nCategory.getId(),HttpStatus.OK);//"redirect:"+"/view/topic/"+
	}

	@PreAuthorize("@forumTopicService.checkTopicAccessToUser(authentication,#topicId)")
	@RequestMapping(value="/operations/topic/{topicId}/toggle_pin",method = RequestMethod.POST)
	public String togglePinTopic(@PathVariable Long topicId,Authentication auth,HttpServletRequest request){
		String referer = request.getHeader("Referer");
		IntitaUser user  = (IntitaUser) auth.getPrincipal();
		if (intitaUserService.isAdmin(user.getId())){
			if(!forumTopicService.toggleTopicPin(topicId)){
				return "redirect:"+referer;
			}
		}
		return "redirect:"+referer;
	}

	@ResponseBody
	@RequestMapping(value="/operations/message/{messageId}/get",method = RequestMethod.POST,produces = "text/plain;charset=UTF-8")
	public ResponseEntity<String> getMsg(@PathVariable("messageId") Long msgID, Authentication auth,HttpServletRequest request){
		IntitaUser user = (IntitaUser) auth.getPrincipal();
		if(user.isAnonymous())
			return new ResponseEntity<String>("null",HttpStatus.OK);//need return code
		TopicMessage msg = topicMessageService.getMessage(msgID);
		if(msg == null /*|| !msg.getAuthor().equals(user)*/)
			return new ResponseEntity<String>("null",HttpStatus.OK);//need return code
		String messageBody = msg.getBody();
		return new ResponseEntity<String>(messageBody,HttpStatus.OK);
	}
	@ResponseBody
	@RequestMapping(value="/operations/message/{messageId}/update",method = RequestMethod.POST)
	public ResponseEntity<String> updateMsg(@PathVariable("messageId") Long msgID,@RequestParam("msg_body") String body,Authentication auth,HttpServletRequest request){
		IntitaUser user = (IntitaUser) auth.getPrincipal();
		if(body==null || body.length()<1)
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		if(user == null || IntitaUser.isAnonymous())
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		TopicMessage msg = topicMessageService.getMessage(msgID);
		if(msg == null /*|| !msg.getAuthor().equals(user)*/)
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		if(!topicMessageService.canEdit(user, msg))
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		topicMessageService.updateMessage(msg, user, body);
		return new ResponseEntity<String>(HttpStatus.OK);
	}

	@RequestMapping(value="/operations/config/update",method = RequestMethod.POST)
	public void refreshConfigParameters()
	{
		configParamService.refreshCachedConfigFromDb();
	}




}