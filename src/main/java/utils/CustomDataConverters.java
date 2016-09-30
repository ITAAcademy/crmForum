package utils;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

public class CustomDataConverters {
	private final static Logger log = LoggerFactory.getLogger(CustomDataConverters.class);
	public static <T> Page<T> listToPage( List<T> list,int page,int itemsCountForPage,Integer customElementsCount){
		
		  int max = (itemsCountForPage*(page+1)>list.size())? list.size(): itemsCountForPage*(page+1);
		  int totalSize = (customElementsCount== null) ? list.size() : customElementsCount;
		  PageRequest pageable = new PageRequest(page, (totalSize==0) ? 1 : totalSize);
		  Page<T> pageObj =  new PageImpl<T>(list.subList(page*itemsCountForPage, max),pageable,totalSize);


		return pageObj;
	}

	public static <T> Page<T> listToPage( List<T> list,int page,int itemsCountForPage){
		Page<T> result = listToPage(list,page,itemsCountForPage,null);
		return result;
	}
	public static long millisecondsToMinutes(long ms){
		long seconds = ms / 1000;
		long minutes = seconds / 60;
		return minutes;
	}
}
