package com.intita.forum.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.forum.config.CustomAuthenticationProvider;
import com.intita.forum.domain.SessionProfanity;
import com.intita.forum.event.LoginEvent;
import com.intita.forum.event.ParticipantRepository;
import com.intita.forum.models.ConfigParam;
import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumTopic;
import com.intita.forum.models.IntitaUser;
import com.intita.forum.models.Lecture;
import com.intita.forum.models.TopicMessage;
import com.intita.forum.services.ConfigParamService;
import com.intita.forum.services.ForumCategoryService;
import com.intita.forum.services.ForumTopicService;
import com.intita.forum.services.IntitaUserService;
import com.intita.forum.services.LectureService;
import com.intita.forum.services.TopicMessageService;
import com.intita.forum.util.ProfanityChecker;

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
	
	HashMap<String,String> configMap = new HashMap<String,String>();

	@PersistenceContext
	EntityManager entityManager;

	protected Session getCurrentHibernateSession()  {
		return entityManager.unwrap(Session.class);
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
	public String getUsers(Principal principal) throws JsonProcessingException {

		Page<IntitaUser> pageUsers = intitaUserService.getIntitaUsers(1, 15);
		Set<LoginEvent> userList = new HashSet<>();
		for(IntitaUser user : pageUsers)
		{
			userList.add(new LoginEvent(user.getId(),user.getUsername(), user.getAvatar(),participantRepository.isOnline(""+user.getId())));
		}
		return  new ObjectMapper().writeValueAsString(userList);
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
	@RequestMapping(value="/", method = RequestMethod.GET)
	public ModelAndView  getIndex(HttpServletRequest request, @RequestParam(required = false) String before,  Model model,Principal principal) {
		Authentication auth =  authenticationProvider.autorization(authenticationProvider);
		
		//chatLangService.updateDataFromDatabase();
		if(before != null)
		{
			 return new ModelAndView("redirect:"+ before);
		}
		//if(auth != null)
			//addLocolization(model, forumUsersService.getForumUser(auth));
		ModelAndView result = new ModelAndView("index");
		IntitaUser intitaUser = intitaUserService.getIntitaUser(auth);
		Page<ForumCategory> categories = forumCategoryService.getMainCategories(0);
		ArrayList<ForumTopic> lastTopics = new ArrayList<ForumTopic>();
		for (ForumCategory category : categories){
			lastTopics.add(forumCategoryService.getLastTopic(category.getId()));
		}
		result.addObject("username",intitaUser.getNickName());
		result.addObject("categories",categories);
		result.addObject("lastTopics",lastTopics);
		int pagesCount = categories.getTotalPages();
		if(pagesCount<1)pagesCount=1;
		result.addObject("pagesCount",pagesCount);
		result.addObject("currentPage",1);
		return result;
	}
	//@author zinhcuk roman
	@RequestMapping(value="/categories_list", method = RequestMethod.GET)
	public ModelAndView getAllCategories(@RequestParam int page){
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
		return model;
	}
	@RequestMapping(value="/test",method = RequestMethod.GET)
	@ResponseBody 
	public String testMapping(){
		return "test good";
	}
	
	@RequestMapping(value="/view/category/{categoryId}/{page}",method = RequestMethod.GET)
	public ModelAndView viewCategoryById(@PathVariable Long categoryId, @PathVariable int page,Principal principal){
		ModelAndView model = new ModelAndView();
		
		ForumCategory category = forumCategoryService.getCategoryById(categoryId);
		
	
		model.addObject("currentPage",page);
		model.addObject("currentCategory",category);
		model.addObject("categoriesTree",forumCategoryService.getCategoriesTree(category));
		model.addObject("isCategoriesContainer",category.isCategoriesContainer());
		if (category.isCategoriesContainer())
		{
			Page<ForumCategory> categories = forumCategoryService.getSubCategories(categoryId, page-1);
			int pagesCount = categories.getTotalPages();
			if(pagesCount<1)pagesCount=1;
			model.addObject("pagesCount",pagesCount);
			model.addObject("categories",categories);
			ArrayList<ForumTopic> lastTopics = new ArrayList<ForumTopic>();
			for (ForumCategory c : categories){
				lastTopics.add(forumCategoryService.getLastTopic(c.getId()));
			}
			model.addObject("lastTopics",lastTopics);
			model.setViewName("categories_list");
		}
		else{
			Page<ForumTopic> topics = forumTopicService.getAllTopicsSortedByPin(categoryId, page-1);
			int pagesCount = topics.getTotalPages();
			if(pagesCount<1)pagesCount=1;
			ArrayList<TopicMessage> lastMessages = new ArrayList<TopicMessage>();
			for (ForumTopic t : topics){
				TopicMessage lastMessage = topicMessageService.getLastMessageByTopic(t);
				lastMessages.add(lastMessage);
			}
			IntitaUser user = intitaUserService.getIntitaUser(principal);
			model.addObject("lastMessages",lastMessages);
			model.addObject("pagesCount",pagesCount);
			model.addObject("topics",topics);
			model.addObject("isAdmin",intitaUserService.isAdmin(user.getId()));
			model.setViewName("topics_list");
		}
		
		return model;
	}
	@PreAuthorize("@forumCategoryService.checkCategoryAccessToUser(authentication,#categoryId)")
	@RequestMapping(value="/view/category/{categoryId}",method = RequestMethod.GET)
	public ModelAndView viewCategoryById(@PathVariable Long categoryId,Principal principal){
		return viewCategoryById(categoryId,1,principal);
	}
	@PreAuthorize("@forumTopicService.checkTopicAccessToUser(authentication,#topicId)")
	@RequestMapping(value="/view/topic/{topicId}/{page}",method = RequestMethod.GET)
	public ModelAndView viewTopicById(@PathVariable Long topicId, @PathVariable int page){
		ModelAndView model = new ModelAndView("topic_view");
		Page<TopicMessage> messages = topicMessageService.getMessagesByTopicId(topicId, page-1);
		ForumTopic topic = forumTopicService.getTopic(topicId);
		model.addObject("messages",messages);
		int pagesCount = messages.getTotalPages();
		if(pagesCount<1)pagesCount=1;
		model.addObject("pagesCount",pagesCount);
		model.addObject("currentPage",page);
		model.addObject("topic",topic); 
		model.addObject("categoriesTree",forumCategoryService.getCategoriesTree(topic));
		model.addObject("config",configMap);
		return model;
	}
	@PreAuthorize("@forumTopicService.checkTopicAccessToUser(authentication,#topicId)")
	@RequestMapping(value="/view/topic/{topicId}",method = RequestMethod.GET)
	public ModelAndView viewTopicById(@PathVariable Long topicId){	
		return viewTopicById(topicId,1);
	}
	
	@RequestMapping(value="/messages/add/{topicId}",method = RequestMethod.POST)
	public String addNewMessage(@RequestParam("text") String postText,@PathVariable Long topicId,HttpServletRequest request){
		IntitaUser currentUser = intitaUserService.getCurrentIntitaUser();
		ForumTopic topic = forumTopicService.getTopic(topicId);
		TopicMessage message = new TopicMessage(currentUser,topic,postText);
		topicMessageService.addMessage(message);
		 String referer = request.getHeader("Referer");
		    return "redirect:"+ referer;
	}
	@RequestMapping(value="/operations/category/{categoryId}/add_topic",method = RequestMethod.POST)
	public String addTopic(@RequestParam("topic_name") String topicName,@PathVariable Long categoryId,Principal principal,HttpServletRequest request){
		 String referer = request.getHeader("Referer");
		IntitaUser author  = intitaUserService.getIntitaUser(principal);
		if (author == null) return "redirect:"+referer;
		ForumCategory category = forumCategoryService.getCategoryById(categoryId);
		if (author == null) return "redirect:"+referer;
		ForumTopic topic = forumTopicService.addTopic(topicName,category,author);
		if (topic == null) return "redirect:"+referer;
		return "redirect:"+"/view/topic/"+topic.getId();
	}
	@RequestMapping(value="/operations/topic/{topicId}/toggle_pin",method = RequestMethod.POST)
	public String togglePinTopic(@PathVariable Long topicId,Principal principal,HttpServletRequest request){
		 String referer = request.getHeader("Referer");
		IntitaUser user  = intitaUserService.getIntitaUser(principal);
		if (intitaUserService.isAdmin(user.getId())){
			if(!forumTopicService.toggleTopicPin(topicId)){
				return "redirect:"+referer;
			}
		}
		return "redirect:"+referer;
	}
	@RequestMapping(value="/operations/config/update",method = RequestMethod.POST)
	public void refreshConfigParameters()
	{
		List<ConfigParam> config =  configParamService.getParams();
		configMap = ConfigParam.listAsMap(config);
	}
	@PostConstruct
	public void initController(){
		refreshConfigParameters();
	}

	
}