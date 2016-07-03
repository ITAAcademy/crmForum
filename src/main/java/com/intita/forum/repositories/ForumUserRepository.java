package com.intita.forum.repositories;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.intita.forum.models.ForumTopic;
import com.intita.forum.models.ForumUser;
import com.intita.forum.models.IntitaUser;

public interface ForumUserRepository extends CrudRepository<ForumUser, Long> {
	ForumUser findOneByNickName(String nickName);
	ForumUser findFirstByIntitaUser(IntitaUser user);
	Page<ForumUser> findById(Long id, Pageable pageable);
	ForumUser findById(Long id);
	Page<ForumUser> findAll(Pageable pageable);
	ForumUser findOneByIntitaUser(IntitaUser user);
	List<ForumUser> findFirst10ByIdNotIn(List<Long> users);
	List<ForumUser> findFirst5ByNickNameNotInAndNickNameLike( List<String> users, String login);
	List<ForumUser> findFirst5ByNickNameLike(String nickName);
}
