package com.intita.forum.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.forum.models.ForumUser;
import com.intita.forum.models.ForumUser.Permissions;
import com.intita.forum.models.IntitaUser;
import com.intita.forum.repositories.IntitaUserRepository;

@Service
public class IntitaUserService {
	@Autowired
	private IntitaUserRepository usersRepo;
	
	@Autowired
	private ForumUsersService forumUsersService;

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
		String forumUserIdStr = principal.getName();
		Long forumUserId = 0L;
				try{
					forumUserId = Long.parseLong(forumUserIdStr);
				}
		catch(NumberFormatException e){
		System.out.println(e);
		return null;
		}
		IntitaUser user = forumUsersService.getIntitaUserFromForumUserId(forumUserId);
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
	public IntitaUser getIntitaUserFromForumUser(Long chatUserId){
		ForumUser forumUser= forumUsersService.getChatUser(chatUserId);
		if (forumUser==null) return null;
		return forumUser.getIntitaUser();
		
	}

	@Transactional
	public IntitaUser getIntitaUser(String login) {
		return usersRepo.findByLogin(login);
	}

	@Transactional(readOnly = false)
	public void register(String login, String email, String pass) {
		String passHash = new BCryptPasswordEncoder().encode(pass);
		//String passHash = pass;
		IntitaUser u = new IntitaUser(login, email.toLowerCase(), passHash);

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
	public boolean isAdmin(String id){
		if(usersRepo.findInAdminTable(id) != null)
			return true;
		return false;
	}
	@Transactional
	public boolean isTenant(String id){
		if(usersRepo.findInTenantTable(id) != null)
			return true;
		return false;
	}

}

