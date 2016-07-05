package com.intita.forum.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.forum.config.CustomAuthenticationProvider;
import com.intita.forum.domain.SessionProfanity;
import com.intita.forum.event.LoginEvent;
import com.intita.forum.event.ParticipantRepository;
import com.intita.forum.jsonview.Views;
import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumCategory.CategoryChildrensType;
import com.intita.forum.models.ForumTopic;
import com.intita.forum.models.IntitaUser;
import com.intita.forum.models.Lecture;
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

	@RequestMapping(value="/get_room_messages", method = RequestMethod.GET)
	@ResponseBody
	public String  getRoomMessages(@RequestParam Long roomId, Principal principal) throws JsonProcessingException {
		mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
		return mapper.writerWithView(Views.Public.class).writeValueAsString(userMessageService.getMessagesByTopicId(roomId));
	}
	
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
		result.addObject("username",intitaUser.getNickName());
		result.addObject("categories",categories);
		result.addObject("pagesCount",categories.getTotalPages());
		result.addObject("currentPage",1);
		return result;
	}
	//@author zinhcuk roman
	@RequestMapping(value="/categories_list", method = RequestMethod.GET)
	public ModelAndView getAllCategories(@RequestParam int page){
		ModelAndView model = new ModelAndView("categories_list");
		Page<ForumCategory> categoriesPage = forumCategoryService.getMainCategories(page-1);
		model.addObject("categories",categoriesPage);
		model.addObject("pagesCount",categoriesPage.getTotalPages());
		model.addObject("currentPage",page);
		return model;
	}
	@RequestMapping(value="/test",method = RequestMethod.GET)
	@ResponseBody 
	public String testMapping(){
		return "test good";
	}
	
	@RequestMapping(value="/view/category/{categoryId}/{page}",method = RequestMethod.GET)
	public ModelAndView viewCategoryById(@PathVariable Long categoryId, @PathVariable int page){
		ModelAndView model = new ModelAndView("categories_list");
		
		ForumCategory category = forumCategoryService.getCategoryById(categoryId);
		
	
		model.addObject("currentPage",page);
		model.addObject("currentCategory",categoryId);
		model.addObject("isCategoriesContainer",category.isCategoriesContainer());
		if (category.isCategoriesContainer())
		{
		Page<ForumCategory> categories = forumCategoryService.getSubCategories(categoryId, page-1);
		model.addObject("pagesCount",categories.getTotalPages());
		model.addObject("categories",categories);
		}
		else{
			Page<ForumTopic> topics = forumTopicService.getAllTopics(categoryId, page);
			model.addObject("pagesCount",topics.getTotalPages());
			model.addObject("topics",topics);
		}
		
		return model;
	}
	@RequestMapping(value="/view/category/{categoryId}",method = RequestMethod.GET)
	public ModelAndView viewCategoryById(@PathVariable Long categoryId){
		return viewCategoryById(categoryId,1);
	}
	@RequestMapping(value="/view/topic/{topicId}/{page}",method = RequestMethod.GET)
	public ModelAndView viewTopicById(@PathVariable Long topicId, @PathVariable int page){
		ModelAndView model = new ModelAndView("topics_list");
		Page<ForumCategory> categories = forumTopicService.getAllTopics(categoryId, page)(topicId, page-1);
		model.addObject("categories",categories);
		model.addObject("pagesCount",categories.getTotalPages());
		model.addObject("currentPage",page);
		model.addObject("currentCategory",categoryId);
		return model;
	}

}