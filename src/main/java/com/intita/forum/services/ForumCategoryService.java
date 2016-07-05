package com.intita.forum.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	
	@Value("${forum.categoriesOrTopicsCountPerPage}")
	private int categoriesCountForPage;
	
	
public Page<ForumCategory> getAllCategories(int page){
	return forumCategoryRepository.findAll(new PageRequest(page,categoriesCountForPage)); 
}
public Page<ForumCategory> getMainCategories(int page){
	return forumCategoryRepository.findByCategory(null, new PageRequest(page,categoriesCountForPage));
}
public ForumCategory getCategoryById(Long id){
	return forumCategoryRepository.findOne(id);
}

public Page<ForumCategory> getSubCategories(Long id,int page){
	ForumCategory rootCategory = forumCategoryRepository.findOne(id);
	CategoryChildrensType childrensType = rootCategory.getCategoryChildrensType();
	switch(childrensType){
	case ChildrenTopic: return null;
	case ChildrenCategory:
		return forumCategoryRepository.findByCategory(new ForumCategory(id),new PageRequest(page,categoriesCountForPage));
	default:
		return null;
	}

}
}
