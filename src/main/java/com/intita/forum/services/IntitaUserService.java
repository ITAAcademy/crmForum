package com.intita.forum.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.forum.models.IntitaUser;
import com.intita.forum.models.IntitaUser.IntitaUserRoles;
import com.intita.forum.repositories.IntitaUserRepository;

@Service
public class IntitaUserService {
	@Autowired
	private IntitaUserRepository usersRepo;

	@PostConstruct
	@Transactional
	public void createAdminUser() {
		System.out.println("admin user created");
		//register("user", "user", "user");

	}

	@Transactional
	public Page<IntitaUser> getIntitaUsers(int page, int pageSize){
		return usersRepo.findAll(new PageRequest(page-1, pageSize)); 

	}
	
	@Transactional
	public IntitaUser getIntitaUser(Principal principal){
		if (principal==null) return null;
		IntitaUser u = (IntitaUser)principal;
		String intitaUserIdStr = ((IntitaUser)principal).getId().toString();
		Long intitaUserId = 0L;
				try{
					intitaUserId = Long.parseLong(intitaUserIdStr);
				}
		catch(NumberFormatException e){
		System.out.println(e);
		return null;
		}
		IntitaUser user = usersRepo.findOne(intitaUserId);
		return user;
	}

	@Transactional
	public List<String> getUsersEmailsFist5(String login, List<Long> logins){
		List<IntitaUser> users = usersRepo.findFirst5ByIdNotInAndLoginLike( logins, login + "%");
		List<String> emails = new ArrayList<String>();
		for(int i = 0; i < users.size(); i++)
			emails.add(users.get(i).getEmail());
		return emails;

	}
	
	@Transactional
	public List<String> getUsersEmailsFist5(String login){
		List<IntitaUser> users = usersRepo.findFirst5ByLoginLike(login + "%");
		//System.out.println("FFFFFFFFFFFFFFFFFFF  " + login + " " + users);
		List<String> emails = new ArrayList<String>();
		for(int i = 0; i < users.size(); i++)
			emails.add(users.get(i).getEmail());
		return emails;

	}
	
	
	@Transactional
	public ArrayList<IntitaUser> getUsers(){
		return (ArrayList<IntitaUser>) IteratorUtils.toList(usersRepo.findAll().iterator()); 
	}
	@Transactional
	public IntitaUser getUser(Long id){
		return usersRepo.findOne(id);
	}

	@Transactional
	public IntitaUser getIntitaUser(String login) {
		return usersRepo.findByLogin(login);
	}

	@Transactional(readOnly = false)
	public void register(String login, String pass) {
		String passHash = new BCryptPasswordEncoder().encode(pass);
		//String passHash = pass;
		IntitaUser u = new IntitaUser(login.toLowerCase(), passHash);

		usersRepo.save(u);
	}

	@Transactional(readOnly = false)
	public void togglePermissionById(Long id){
		IntitaUser u = getById(id);
		u.togglePermission();
		usersRepo.save(u);
	}
	@Transactional
	public void updateUserInfo(IntitaUser u){
		usersRepo.save(u);
	}

	@Transactional
	public void removeUser(Long id){
		usersRepo.delete(id);
	}
	@Transactional
	public IntitaUser getById(Long id){
		return usersRepo.findFisrtById(id);
	}
	@Transactional
	public boolean isAdmin(Long id){
		if(id != null && usersRepo.findInAdminTable(id) != null)
			return true;
		return false;
	}
	@Transactional
	public boolean isTenant(Long id){
		if(usersRepo.findInTenantTable(id) != null)
			return true;
		return false;
	}
	@Transactional
	public Set<IntitaUserRoles> getRoles(Long id){
		Set<IntitaUserRoles> roles = new HashSet<IntitaUserRoles>();
		roles.add(IntitaUserRoles.USER);
		if(usersRepo.findInAdminTable(id) != null)
			roles.add(IntitaUserRoles.ADMIN);
		if(usersRepo.findInTenantTable(id) != null)
		roles.add(IntitaUserRoles.TENANT);
		if(usersRepo.findInContentManagerTable(id) != null)
			roles.add(IntitaUserRoles.CONTENT_MANAGER);
		if(usersRepo.findInTeachersTable(id) != null)
			roles.add(IntitaUserRoles.TEACHER);
		if(usersRepo.findInAccountantTable(id) != null)
			roles.add(IntitaUserRoles.ACCOUNTANT);
		if(usersRepo.findInStudentTable(id) != null)
			roles.add(IntitaUserRoles.STUDENT);
		return roles;
	}

	public Set<String> getRolesNames(Long id){
		Set<IntitaUserRoles> roles = getRoles(id);
		Set<String> stringRoles = new HashSet<String>();
		for (IntitaUserRoles role : roles){
			stringRoles.add(role.name());
		}
		return stringRoles;
	}
	
	@Transactional
	public boolean isContentManager(Long id){
		if(usersRepo.findInContentManagerTable(id) != null)
			return true;
		return false;
	}
	@Transactional
	public boolean isTeacher(Long id){
		if(usersRepo.findInTeachersTable(id) != null)
			return true;
		return false;
	}
	@Transactional
	public boolean isAccounant(Long id){
		if(usersRepo.findInAccountantTable(id) != null)
			return true;
		return false;
	}
	@Transactional
	public boolean isStudent(Long id){
		if(usersRepo.findInStudentTable(id) != null)
			return true;
		return false;
	}
	
	public IntitaUser getCurrentIntitaUser() {
		if (SecurityContextHolder.getContext().getAuthentication()==null) return null;
		return (IntitaUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	public static Long getCurrentIntitaUserId() {
		String idStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Long id = Long.parseLong(idStr);
		return id;
	}
	
	public boolean hasAnyRoles(Long userId,Set<IntitaUserRoles> demandedRoles){
		if (demandedRoles==null || demandedRoles.size()<1) return true;
		Set<IntitaUserRoles> userRoles = getRoles(userId);
		for(IntitaUserRoles role : demandedRoles){
			if (userRoles.contains(role))return true;			
		}
		return false;
	}
	public boolean hasAllRolesSets(Long userId,LinkedList<Set<IntitaUserRoles>> demandedRoles){
		if (demandedRoles==null || demandedRoles.size()<1) return true;
		Set<IntitaUserRoles> userRoles = getRoles(userId);
		boolean demandedListSatisfied = true;
		for (Set<IntitaUserRoles> demandedSet : demandedRoles ){
			if (demandedSet.size()<1)return true;// no demand, so all demands satisfied=)
			boolean demandedSetSatisfied = false;
			for(IntitaUserRoles role : demandedSet){
				if (userRoles.contains(role))demandedSetSatisfied = true;
				break;
			}
			if (!demandedSetSatisfied){
				demandedListSatisfied = false;
				break;
			}
		}		
		return demandedListSatisfied;
	}
	

}

