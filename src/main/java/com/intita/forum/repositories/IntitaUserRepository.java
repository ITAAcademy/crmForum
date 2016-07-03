package com.intita.forum.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.intita.forum.models.IntitaUser;


//http://docs.spring.io/spring-data/jpa/docs/1.4.3.RELEASE/reference/html/jpa.repositories.html
@Qualifier("IntitaConf") 
public interface IntitaUserRepository extends CrudRepository<IntitaUser, Long> {
   IntitaUser findByLogin(String login);
  //User findByEmail(String email);
   IntitaUser findFisrtById(Long id);
  Page<IntitaUser> findById(Long id, Pageable pageable);
  Page<IntitaUser> findAll(Pageable pageable);
  List<IntitaUser> findFirst10ByIdNotIn(List<Long> users);
//  List<User> findFirst5ByLoginAndByPassword( String users, String login);
  List<IntitaUser> findFirst5ByIdNotInAndLoginLike( List<Long> users, String login);
  List<IntitaUser> findFirst5ByLoginLike(String login);
  //
  @Query(value = "SELECT * FROM user_admin WHERE id_user = ?1 AND ((start_date <= NOW() AND end_date >= NOW()) OR end_date IS NULL) LIMIT 1", nativeQuery = true)
  Object findInAdminTable(String userId);
  
  @Query(value = "SELECT * FROM user_tenant WHERE chat_user_id = ?1 AND ((start_date <= NOW() AND end_date >= NOW()) OR end_date IS NULL) LIMIT 1", nativeQuery = true)
  Object findInTenantTable(String userId);
 
}