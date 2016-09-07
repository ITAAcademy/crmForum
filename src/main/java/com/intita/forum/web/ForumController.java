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
import com.intita.forum.services.ConfigParamService;
import com.intita.forum.services.ForumCategoryService;
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
	public ModelAndView  getIndex(HttpServletRequest request, @RequestParam(required = false) String before,  Model model,Authentication principal) {
		Authentication auth =  authenticationProvider.autorization(authenticationProvider);
		
		//chatLangService.updateDataFromDatabase();
		if(before != null)
		{
			return new ModelAndView("redirect:"+ before);
		}
		//if(auth != null)
		//addLocolization(model, forumUsersService.getForumUser(auth));
		ModelAndView result = new ModelAndView("categories_list");
		IntitaUser intitaUser = (IntitaUser) auth.getPrincipal();
		Page<ForumCategory> categories = forumCategoryService.getMainCategories(0);
		ArrayList<ForumTopic> lastTopics = new ArrayList<ForumTopic>();
		for (ForumCategory category : categories){
			lastTopics.add(forumCategoryService.getLastTopic(category.getId()));
		}
		if (intitaUser!=null){
			result.addObject("username",intitaUser.getNickName());
			onlineUsersActivity.put(intitaUser.getId(), UserActionInfo.forEmptyAction());
		}
		result.addObject("categories",categories);
		result.addObject("lastTopics",lastTopics);
		int pagesCount = categories.getTotalPages();
		if(pagesCount<1)pagesCount=1;
		result.addObject("pagesCount",pagesCount);
		result.addObject("currentPage",1);
		CustomPrettyTime p = new CustomPrettyTime(new Locale(getCurrentLang()));
		result.addObject("prettyTime",p);
		result.addObject("config",configParamService.getCachedConfigMap());
		result.addObject("user", (IntitaUser)auth.getPrincipal());
		result.addObject("blockSearch", true);
		List<ForumCategory> pageCategories = categories.getContent();
		result.addObject("statistic",forumCategoryService.getCategoriesStatistic(pageCategories));
		return result;
	}
	//@author zinhcuk roman
	@RequestMapping(value="/categories_list", method = RequestMethod.GET)
	public ModelAndView getAllCategories(@RequestParam int page, Authentication auth){
		ModelAndView model = new ModelAndView("categories_list");
		Page<ForumCategory> categoriesPage = forumCategoryService.getMainCategories(page-1);
		ArrayList<ForumTopic> lastTopics = new ArrayList<ForumTopic>();
		for (ForumCategory category : categoriesPage){
			lastTopics.add(forumCategoryService.getLastTopic(category.getId()));
		}
		model.addObject("categories",categoriesPage);
		model.addObject("lastTopics",lastTopics);
		int pagesCount = categoriesPage.getTotalPages();
		if(pagesCount<1)pagesCount=1;
		model.addObject("pagesCount",pagesCount);
		model.addObject("currentPage",page);
		CustomPrettyTime p = new CustomPrettyTime(new Locale(getCurrentLang()));
		model.addObject("prettyTime",p);
		IntitaUser user = (IntitaUser)auth.getPrincipal();
		if (user!=null){
		model.addObject("user", user);
		onlineUsersActivity.put(user.getId(), UserActionInfo.forEmptyAction());
		}
		List<ForumCategory> pageCategories = categoriesPage.getContent();
		model.addObject("statistic",forumCategoryService.getCategoriesStatistic(pageCategories));
		return model;
	}
	@RequestMapping(value="/test",method = RequestMethod.GET)
	@ResponseBody 
	public String testMapping(){
		return "test good";
	}
	/*******************************
	 * Category @RequestMapping
	 ******************************/
	@PreAuthorize("@forumCategoryService.checkCategoryAccessToUser(authentication,#categoryId)")
	@RequestMapping(value="/view/category/{categoryId}/{page}",method = RequestMethod.GET)
	public ModelAndView viewCategoryByIdMapping(RedirectAttributes redirectAttributes, @RequestParam(required = false) String search,
			@PathVariable Long categoryId, @PathVariable int page, HttpServletRequest request,HttpServletResponse response, Authentication auth){
		return viewCategoryById(redirectAttributes,search,categoryId,page,request,response,auth,null);
	}
	public ModelAndView viewCategoryById(RedirectAttributes redirectAttributes, String search,  Long categoryId, 
			 int page, HttpServletRequest request,HttpServletResponse response, Authentication auth,UserSortingCriteria sortingCriteria){
		if(search != null)
		{
			redirectAttributes.addAttribute("searchvalue", search);
			redirectAttributes.addAttribute("type", SearchType.CATEGORY);
			return new ModelAndView("redirect:" + "/view/search/" + categoryId + "/1");
		}
		
		IntitaUser user = (IntitaUser) auth.getPrincipal();
		ModelAndView model = new ModelAndView();
		ForumCategory category = forumCategoryService.getCategoryById(categoryId);
		model.addObject("currentPage",page);
		model.addObject("currentCategory",category);
		model.addObject("categoriesTree",forumCategoryService.getCategoriesTree(category));
		model.addObject("isCategoriesContainer",category.isCategoriesContainer());
		CustomPrettyTime p = new CustomPrettyTime(new Locale(getCurrentLang()));
		model.addObject("prettyTime",p);
		model.addObject("config",configParamService.getCachedConfigMap());

		if (category.isCategoriesContainer())
		{
			if (sortingCriteria==null){	
				sortingCriteria = UserSortingCriteria.loadFromCookie(ForumCategory.class, request);
			}
			else{
				sortingCriteria.saveToCookie(ForumCategory.class, request,response);
			}
			Page<ForumCategory> categories = forumCategoryService.getSubCategoriesPage(categoryId, page-1,user,sortingCriteria);
			int pagesCount = categories.getTotalPages();
			if(pagesCount<1)pagesCount=1;
			model.addObject("pagesCount",pagesCount);
			model.addObject("categories",categories);
			ArrayList<ForumTopic> lastTopics = new ArrayList<ForumTopic>();
			for (ForumCategory c : categories){
				lastTopics.add(forumCategoryService.getLastTopic(c.getId()));
			}
			model.addObject("lastTopics",lastTopics);
			List<ForumCategory> pageCategories = categories.getContent();
			model.addObject("statistic",forumCategoryService.getCategoriesStatistic(pageCategories));
			model.setViewName("categories_list");
			model.addObject("sortingCriteria",sortingCriteria.convertToOrdinal());
			model.addObject("sortingMenu",UserSortingCriteria.getSortingMenuData(ForumCategory.class,forumLangService.getLocalization()));

		}
		else{
			if (sortingCriteria==null){	
				sortingCriteria = UserSortingCriteria.loadFromCookie(ForumTopic.class, request);
			}
			else{
				sortingCriteria.saveToCookie(ForumTopic.class,request, response);
			}
			Page<ForumTopic> topics = forumTopicService.getAllTopicsSortedByPin(categoryId, page-1,sortingCriteria);
			int pagesCount = topics.getTotalPages();
			if(pagesCount<1)pagesCount=1;
			ArrayList<TopicMessage> lastMessages = new ArrayList<TopicMessage>();
			for (ForumTopic t : topics){
				TopicMessage lastMessage = topicMessageService.getLastMessageByTopic(t);
				lastMessages.add(lastMessage);
			}

			model.addObject("lastMessages",lastMessages);
			model.addObject("pagesCount",pagesCount);
			model.addObject("topics",topics);
			model.addObject("isAdmin",intitaUserService.isAdmin(user.getId()));
			List<ForumTopic> pageTopics = topics.getContent();
			model.addObject("statistic",forumTopicService.getTopicsStatistic(pageTopics));
			model.setViewName("topics_list");
			model.addObject("sortingCriteria",sortingCriteria.convertToOrdinal());
			model.addObject("sortingMenu",UserSortingCriteria.getSortingMenuData(ForumTopic.class,forumLangService.getLocalization()));
		}
		model.addObject("bbcode", getTextProcessorInstance(request));
		if (user!=null){		
		model.addObject("user", (IntitaUser)auth.getPrincipal());
		onlineUsersActivity.put(user.getId(), UserActionInfo.forCategory(categoryId));
		}

		return model;
	}
	@PreAuthorize("@forumCategoryService.checkCategoryAccessToUser(authentication,#categoryId)")
	@RequestMapping(value="/view/category/{categoryId}",method = RequestMethod.GET)
	public ModelAndView viewCategoryByIdMapping(RedirectAttributes redirectAttributes, @RequestParam(required = false) String search,@PathVariable Long categoryId, HttpServletRequest requset,HttpServletResponse response, Authentication principal){
		return viewCategoryById(redirectAttributes, search, categoryId, 1, requset,response, principal,null);
	}
	@PreAuthorize("@forumCategoryService.checkCategoryAccessToUser(authentication,#categoryId)")
	@RequestMapping(value="/view/category/{categoryId}",method = RequestMethod.POST)
	public ModelAndView viewCategoryByIdMappingPost(RedirectAttributes redirectAttributes, @RequestParam(required = false) String search,@PathVariable Long categoryId, HttpServletRequest requset,HttpServletResponse response, Authentication principal,@RequestParam(required = false) int where,@RequestParam(required = false) int sort,@RequestParam(required = false) Boolean order){
		UserSortingCriteria criteria = new UserSortingCriteria(ShowItemsCriteria.fromInteger(where),SortByField.fromInteger(sort),order);
		return viewCategoryById(redirectAttributes, search, categoryId, 1, requset,response, principal,criteria);
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
	@PreAuthorize("@forumCategoryService.checkCategoryAccessToUser(authentication,#categoryId)")
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
	      log.info("removing offline users from online users list...");
	      Iterator<Long> i = onlineUsersActivity.keySet().iterator();
	      while (i.hasNext() ){
	    	  Long key = i.next();
	    	  UserActionInfo info = onlineUsersActivity.get(key);
	    	  long currentTime = new Date().getTime();
	    	  long delta =  currentTime - info.getLastActionTime();
	    	  long minutesDelta = CustomDataConverters.millisecondsToMinutes(delta);
	    	  //log.info(key+" "+info + " "+minutesDelta);
	    	  if (minutesDelta>=MAXIMAL_USER_INACTIVE_TIME_MINUTES){
	    		  i.remove();
	    		  //log.info("removed");
	    	  }
	      }
	      log.info("...done");
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


	@ResponseBody
	@PreAuthorize("@forumCategoryService.checkCategoryAccessToUser(authentication,#categoryId)")
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
	@RequestMapping(value="/operations/message/{messageId}/get",method = RequestMethod.POST)
	public String getMsg(@PathVariable("messageId") Long msgID, Authentication auth,HttpServletRequest request){
		IntitaUser user = (IntitaUser) auth.getPrincipal();
		if(user.isAnonymous())
			return "null";//need return code
		TopicMessage msg = topicMessageService.getMessage(msgID);
		if(msg == null /*|| !msg.getAuthor().equals(user)*/)
			return "null";//need return code
		return msg.getBody();
	}
	@ResponseBody
	@RequestMapping(value="/operations/message/{messageId}/update",method = RequestMethod.POST)
	public ResponseEntity<String> getMsg(@PathVariable("messageId") Long msgID,@RequestParam("msg_body") String body,Authentication auth,HttpServletRequest request){
		IntitaUser user = (IntitaUser) auth.getPrincipal();
		if(body==null || body.length()<1)
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		if(user == null || user.isAnonymous())
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		TopicMessage msg = topicMessageService.getMessage(msgID);
		if(msg == null /*|| !msg.getAuthor().equals(user)*/)
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		if(!topicMessageService.canEdit(user, msg))
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		msg.setBody(body);
		topicMessageService.addMessage(msg);
		return new ResponseEntity<String>(HttpStatus.OK);
	}

	@RequestMapping(value="/operations/config/update",method = RequestMethod.POST)
	public void refreshConfigParameters()
	{
	configParamService.refreshCachedConfigFromDb();
	}




}