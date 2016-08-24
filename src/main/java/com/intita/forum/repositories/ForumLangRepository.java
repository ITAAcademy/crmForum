package com.intita.forum.repositories;

import org.springframework.data.repository.CrudRepository;

import com.intita.forum.models.Lang;

//@Qualifier("IntitaConf") 
public interface ForumLangRepository extends CrudRepository<Lang, Long> {

}
