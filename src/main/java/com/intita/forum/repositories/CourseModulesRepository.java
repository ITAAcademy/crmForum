package com.intita.forum.repositories;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.intita.forum.models.CourseModule;
import com.intita.forum.models.Module;

public interface CourseModulesRepository extends CrudRepository<CourseModule,Long>{
/*FOR EXAMPLE
@Query("select c.id from BotCategory c")
	ArrayList<Long> getAllIds();
	@Query("select c.name from BotCategory c where c.name like %?1%")
	ArrayList<String> getNamesLike(String name,Pageable pageable);
	@Query("select c.id from BotCategory c where c.name like %?1%")
	ArrayList<Long> getIdsWhereNameLike(String name);
	@Query("select c from BotCategory c where c.name like %?1%")
	ArrayList<BotCategory> getBotCategoriesHavingName(String name,Pageable pageable);
 */
	@Query("select m from module m where m.id in (select cm from course_modules cm where idCourse = ?1 ) ")
	ArrayList<Module> finModulesdByCourseId(Long id);
}
