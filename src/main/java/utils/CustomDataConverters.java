package utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.intita.forum.models.ForumTopic;

public class CustomDataConverters {
	public static <T> Page<T> listToPage( List<T> list,int page,int itemsCountForPage){
		  PageRequest pageable = new PageRequest(page, itemsCountForPage);
		  int max = (itemsCountForPage*(page+1)>list.size())? list.size(): itemsCountForPage*(page+1);
		  Page<T> pageObj = new PageImpl<T>(list.subList(page*itemsCountForPage, max),pageable,list.size());	
		return pageObj;
	}
}
