package com.intita.forum.domain;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intita.forum.web.ForumController;

public class UserSortingCriteria {
public enum ShowItemsCriteria {ALL,ONE_DAY,SEVEN_DAYS,ONE_MONTH};
public enum SortByField {AUTHOR,DATE,TOPIC};
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
	}
	return null;
}

public String getSortingParamNameForClass(Class className){
	if (sortByField==null)return null;
	if (className.getName()=="ForumCategory")
	{
	switch(sortByField){
	case DATE: return "date";
	case TOPIC: return "name";
	default:
		log.error("passed uncorrect field for query sorting !");
		return null;
	}
	}
	else if (className.getName()=="ForumTopic"){
			switch(sortByField){
			case DATE: return "date";
			case TOPIC: return "name";
			case AUTHOR: return "author";
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
	if (className.getName()=="ForumCategory")
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
	else if (className.getName()=="ForumTopic"){
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

}
