package com.intita.forum.domain;

import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumTopic;
import com.intita.forum.models.TopicMessage;
import com.intita.forum.util.CookieHelper;
import com.intita.forum.web.ForumController;

public class UserSortingCriteria {
	private final static  String forumCategoryClassSimpleName = ForumCategory.class.getSimpleName();
	private final static String forumTopicClassSimpleName = ForumTopic.class.getSimpleName();
	private final static String topicMessageClassSimpleName = TopicMessage.class.getSimpleName();
public enum ShowItemsCriteria {ALL,ONE_DAY,SEVEN_DAYS,ONE_MONTH,ONE_YEAR;
	public static ShowItemsCriteria fromInteger(int x) {
        switch(x) {
        case 0:
            return ALL;
        case 1:
            return ONE_DAY;
        case 2:
            return SEVEN_DAYS;
        case 3:
            return ONE_MONTH;
        case 4:
        	return ONE_YEAR;
        }
        return null;
    }
	public static HashMap<Integer,String> toHashMap(){
		HashMap<Integer,String> datePick = new HashMap<Integer,String>();
		datePick.put(ShowItemsCriteria.ALL.ordinal(),"all_messages");
		datePick.put(ShowItemsCriteria.ONE_DAY.ordinal(), "one_day");
		datePick.put(ShowItemsCriteria.SEVEN_DAYS.ordinal(), "seven_days");
		datePick.put(ShowItemsCriteria.ONE_MONTH.ordinal(), "one_month");
		datePick.put(ShowItemsCriteria.ONE_YEAR.ordinal(), "one_year");
		return datePick;
	}
	
};
public enum SortByField {AUTHOR,DATE,TOPIC;
	public static SortByField fromInteger(int x) {
    switch(x) {
    case 0:
        return AUTHOR;
    case 1:
        return DATE;
    case 2:
        return TOPIC;
    }
    return null;
}
	public static HashMap<Integer,String> toHashMap(){
		HashMap<Integer,String> datePick = new HashMap<Integer,String>();
		datePick.put(SortByField.AUTHOR.ordinal(),"author");
		datePick.put(SortByField.DATE.ordinal(), "date");
		datePick.put(SortByField.TOPIC.ordinal(), "topic");
		return datePick;
	}	
};
private ShowItemsCriteria showItemsCriteria;
private SortByField sortByField;
private Boolean isAscend;
public static HashMap<Integer,String> orderOptionsToHashMap(){
	HashMap<Integer,String> result = new HashMap<Integer,String>();
	result.put(0, "by_descend");
	result.put(1, "by_ascend");
	return result;
}

private final static Logger log = LoggerFactory.getLogger(ForumController.class);
public ShowItemsCriteria getShowItemsCriteria() {
	return showItemsCriteria;
}
public void setShowItemsCriteria(ShowItemsCriteria showItemsCriteria) {
	this.showItemsCriteria = showItemsCriteria;
}
public SortByField getSortByField() {
	return sortByField;
}
public void setSortByField(SortByField sortByField) {
	this.sortByField = sortByField;
}
public Boolean isAscend() {
	return isAscend;
}
public void setAscend(Boolean isAscend) {
	this.isAscend = isAscend;
}
public UserSortingCriteria(ShowItemsCriteria showItemsCriteria, SortByField sortByField,Boolean isAscend ){
	this.showItemsCriteria=showItemsCriteria;
	this.sortByField=sortByField;
	this.isAscend=isAscend;
}
public UserSortingCriteria( ){
	this.showItemsCriteria=ShowItemsCriteria.ALL;
	this.sortByField=SortByField.DATE;
	this.isAscend=true ;
}
public boolean isFullCriteriasSet(){
	return showItemsCriteria != null && sortByField != null && isAscend != null; 
}
public boolean isShowItemsCriteriaSet(){
	if (showItemsCriteria==null)return false;
	return true;
}
public boolean isSortByFieldSet(){
	if (sortByField==null)return false;
	return true;	
}
public boolean isAscendSet(){
	if (isAscend==null)return false;
	return true;	
}
public String getOrder(){
	if(isAscend) return "ASC";
	else return "DESC";
}
/**
 * generate date param for ShowItemsCriteria;
 * @return
 */
public Date getDateParam(){
	if (showItemsCriteria==null) return null;
	switch(showItemsCriteria){
	case ONE_DAY:
		Date dateBefore1Day = DateUtils.addDays(new Date(),-1);
		return dateBefore1Day;
	case SEVEN_DAYS:
		Date dateBefore7Days = DateUtils.addDays(new Date(),-7);
		return dateBefore7Days;
	case ONE_MONTH:
		Date dateBefore30Days = DateUtils.addDays(new Date(),-30);
		return dateBefore30Days;
	case ONE_YEAR:
		Date dateBefore365Days = DateUtils.addDays(new Date(),-365);
		return dateBefore365Days;
	}
	return null;
}

public String getSortingParamNameForClass(Class className){
	if (sortByField==null)return null;
	if (className.getSimpleName().equals(forumCategoryClassSimpleName))
	{
	switch(sortByField){
	case DATE: return "date";
	case TOPIC: return "name";
	default:
		log.error("passed uncorrect field for query sorting !");
		return null;
	}
	}
	else if (className.getSimpleName().equals(forumTopicClassSimpleName)){
			switch(sortByField){
			case DATE: return "date";
			case TOPIC: return "name";
			case AUTHOR: return "author.firstName";
			default:
				log.error("passed uncorrect field for query sorting !");
				return null;
			}
		}
	else if (className.getSimpleName().equals(topicMessageClassSimpleName)){
		switch(sortByField){
		case DATE: return "date";
		case AUTHOR: return "author.firstName";
		default:
			log.error("passed uncorrect field for query sorting !");
			return null;
		}
	}
	else {
		log.error("unsuported class");
		return null;
	}
	
}
public String getDateParamNameForClass(Class className){
	if (showItemsCriteria==null)return null;
	if (className.getSimpleName().equals(forumCategoryClassSimpleName) ||
			className.getSimpleName().equals(forumTopicClassSimpleName) ||
			className.getSimpleName().equals(topicMessageClassSimpleName))
	{
	switch(showItemsCriteria){
	case ALL: return null;//we needn't any where clause in this case;
	case ONE_DAY:
	case ONE_MONTH:
	case SEVEN_DAYS:
		return "date > :dateParam";
	default:
		log.error("passed uncorrect field for query sorting !");
		return null;
	}
	}
	else {
		log.error("unsuported class");
		return null;
	}
	
}
public void saveToCookie(Class classObj,HttpServletRequest request, HttpServletResponse response){
		String className = classObj.getSimpleName();
		if (!className.equals(forumCategoryClassSimpleName) && !className.equals(forumTopicClassSimpleName) &&
				!className.equals(topicMessageClassSimpleName)){
			log.error("class must be one of those:"+forumCategoryClassSimpleName+","+forumTopicClassSimpleName
					+","+topicMessageClassSimpleName);
			return;
		}
		CookieHelper.saveCookie(className+"sorting_where_condition", showItemsCriteria.toString(), 1000,request, response);
		CookieHelper.saveCookie(className+"sorting_condition", sortByField.toString(), 1000,request, response);
		CookieHelper.saveCookie(className+"sorting_order_is_ascend", isAscend.toString(), 1000,request, response);		
}
public static void removeFromCookie(Class classObj,HttpServletRequest request, HttpServletResponse response){
	String className = classObj.getSimpleName();
	if (!className.equals(forumCategoryClassSimpleName) && !className.equals(forumTopicClassSimpleName) &&
			!className.equals(topicMessageClassSimpleName)){
		log.error("class must be one of those:"+forumCategoryClassSimpleName+","+forumTopicClassSimpleName
				+","+topicMessageClassSimpleName);
		return;
	}
	CookieHelper.saveCookie(className+"sorting_where_condition", null, 0,request, response);
	CookieHelper.saveCookie(className+"sorting_condition", null, 0,request, response);
	CookieHelper.saveCookie(className+"sorting_order_is_ascend", null, 0,request, response);
}
public static UserSortingCriteria loadFromCookie(Class classObj,HttpServletRequest request){
	String className = classObj.getSimpleName();
	if (!className.equals(forumCategoryClassSimpleName) && !className.equals(forumTopicClassSimpleName) 
			&& !className.equals(topicMessageClassSimpleName) ){
		log.error("class must be ForumCategory or ForumTopic");
		return null;
	}
	UserSortingCriteria userCriteria = new UserSortingCriteria();
	String whereCondition = CookieHelper.getCookieValue(className+"sorting_where_condition", request);
	if (whereCondition!=null && whereCondition.length()>0)
		userCriteria.setShowItemsCriteria(ShowItemsCriteria.valueOf(whereCondition));
	String sortingCondition = CookieHelper.getCookieValue(className+"sorting_condition", request);
	if (sortingCondition!=null && sortingCondition.length()>0)
		userCriteria.setSortByField(SortByField.valueOf(sortingCondition));
	String sortingOrderIsAscendString = CookieHelper.getCookieValue(className+"sorting_order_is_ascend", request);
	if (sortingOrderIsAscendString!=null && sortingOrderIsAscendString.length()>0){
		if (sortingOrderIsAscendString=="true")userCriteria.setAscend(true);
		else userCriteria.setAscend(false);
	}
	return userCriteria;
}
/***
 * for Thymleaf, enum values replaced by ordinal
 * @return
 */

public HashMap<String,Integer> convertToOrdinal(){
	HashMap<String,Integer> map = new HashMap<String,Integer>();
	map.put("where", showItemsCriteria.ordinal());
	map.put("sort", sortByField.ordinal());
	map.put("order", isAscend ? 1 : 0);
	return map;
	
	
}
/***
 * return map in map where first level map represent <select> items
 * and second level map represent <option> items
 * @param classObj determine which data to be included in map e.g 
 * ForumCategory can't be sorted by author because it hasn't author; 
 * @return
 */
public static HashMap<String,HashMap<Integer, String>> getSortingMenuData(Class classObj){
	HashMap<String,HashMap<Integer, String>> result = new HashMap<String,HashMap<Integer, String>>();
	String className = classObj.getSimpleName();
	if (!className.equals(forumCategoryClassSimpleName) && !className.equals(forumTopicClassSimpleName) &&
			!className.equals(topicMessageClassSimpleName)){
		log.error("class must be one of those:"+forumCategoryClassSimpleName+","+forumTopicClassSimpleName
				+","+topicMessageClassSimpleName);
		return null;
	}
	result.put("where", ShowItemsCriteria.toHashMap());
	result.put("sort", SortByField.toHashMap());
	result.put("order", orderOptionsToHashMap());
	return result;
	
	
}

}
