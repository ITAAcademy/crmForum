package com.intita.forum.domain;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intita.forum.models.ForumCategory;
import com.intita.forum.util.CookieHelper;
import com.intita.forum.web.ForumController;

public class UserSortingCriteria {
	private String forumCategoryClassSimpleName = ForumCategory.class.getSimpleName();
	private String forumTopicClassSimpleName = ForumCategory.class.getSimpleName();
	private String topicMessageClassSimpleName = ForumCategory.class.getSimpleName();
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
    }};
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
}};
private ShowItemsCriteria showItemsCriteria;
private SortByField sortByField;
private Boolean isAscend;
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
	if (className.getName().equals("com.intita.forum.models.ForumCategory"))
	{
	switch(sortByField){
	case DATE: return "date";
	case TOPIC: return "name";
	default:
		log.error("passed uncorrect field for query sorting !");
		return null;
	}
	}
	else if (className.getName().equals("com.intita.forum.models.ForumTopic")){
			switch(sortByField){
			case DATE: return "date";
			case TOPIC: return "name";
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
public String getWhereParamNameForClass(Class className){
	if (showItemsCriteria==null)return null;
	if (className.getName().equals("com.intita.forum.models.ForumCategory"))
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
	else if (className.getName().equals("com.intita.forum.models.ForumTopic")){
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
public void saveToCookie(Class classObj,HttpServletResponse response){
		String className = classObj.getSimpleName();
		if (className!=forumCategoryClassSimpleName && className!=forumTopicClassSimpleName &&
				className != topicMessageClassSimpleName){
			log.error("class must be one of those:"+forumCategoryClassSimpleName+","+forumTopicClassSimpleName
					+","+topicMessageClassSimpleName);
			return;
		}
		CookieHelper.saveCookie(className+"sorting_where_condition", showItemsCriteria.toString(), 1000, response);
		CookieHelper.saveCookie(className+"sorting_condition", sortByField.toString(), 1000, response);
		CookieHelper.saveCookie(className+"sorting_order_is_ascend", isAscend.toString(), 1000, response);		
}
public static UserSortingCriteria loadFromCookie(Class classObj,HttpServletRequest request){
	String className = classObj.getSimpleName();
	if (className!="ForumCategory" && className!="ForumTopic" ){
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

}
