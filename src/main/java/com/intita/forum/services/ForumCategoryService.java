package com.intita.forum.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.intita.forum.models.ForumCategory;
import com.intita.forum.models.ForumCategory.CategoryChildrensType;
import com.intita.forum.repositories.ForumCategoryRepository;

@Service
public class ForumCategoryService {
	@Autowired
	private ForumCategoryRepository forumCategoryRepository;
	
	final int SINGLE_PAGE_RESULTS_COUNT = 20;
	
public Page<ForumCategory> getAllCategories(int page){
	return forumCategoryRepository.findAll(new PageRequest(page,SINGLE_PAGE_RESULTS_COUNT)); 
}
public Page<ForumCategory> getSubCategories(Long id,int page){
	ForumCategory rootCategory = forumCategoryRepository.findOne(id);
	CategoryChildrensType childrensType = rootCategory.getCategoryChildrensType();
	switch(childrensType){
	case ChildrenTopic: return null;
	case ChildrenCategory:
		return forumCategoryRepository.findByParentCategory(new ForumCategory(id),new PageRequest(page,SINGLE_PAGE_RESULTS_COUNT));
	default:
		return null;
	}

}
}
