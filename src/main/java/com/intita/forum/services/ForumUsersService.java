package com.intita.forum.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

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
import com.intita.forum.models.IntitaUser;
import com.intita.forum.repositories.ForumUserRepository;

@Service
public class ForumUsersService {

	@Autowired
	private ForumUserRepository forumUsersRepo;
	@Autowired
	private IntitaUserService intitaUserService;

	@PostConstruct
	@Transactional
	public void createAdminUser() {
		System.out.println("admin user created");
		//register("user", "user", "user");

	}

	@Transactional
	public ForumUser getForumUser(Principal principal){
		if (principal==null)return null;
		String chatUserIdStr = principal.getName();
		Long chatUserId = 0L;
		try{
			chatUserId = Long.parseLong(chatUserIdStr);
		}
		catch(NumberFormatException e){
			System.out.println(e);
			return null;
		}
		if(chatUserId < 0)
			return null;
		
		ForumUser user = getForumUser(chatUserId.toString());
		return user;
	}

	@Transactional
	public Page<ForumUser> getForumUsers(int page, int pageSize){
		return forumUsersRepo.findAll(new PageRequest(page-1, pageSize)); 
	}

	@Transactional
	public List<String> getUsersNickNameFist5(String nickName, List<String> excludedNicks){
		List<ForumUser> users = forumUsersRepo.findFirst5ByNickNameNotInAndNickNameLike( excludedNicks, nickName + "%");
		List<String> nickNames = new ArrayList<String>();
		for(int i = 0; i < users.size(); i++)
			nickNames.add(users.get(i).getNickName());
		return nickNames;

	}
	@Transactional
	public List<ForumUser> getUsersFist5(String nickName, List<String> excludedNicks){
		List<ForumUser> users = forumUsersRepo.findFirst5ByNickNameNotInAndNickNameLike( excludedNicks, nickName + "%");
		return users;

	}
	@Transactional
	public ArrayList<ForumUser> getUsers(){
		return (ArrayList<ForumUser>) IteratorUtils.toList(forumUsersRepo.findAll().iterator());
	}
	@Transactional
	public ForumUser getChatUser(Long id){
		return forumUsersRepo.findOne(id);
	}
	@Transactional
	public ForumUser getForumUserFromIntitaId(Long id, boolean createGuest){
		IntitaUser currentUser = intitaUserService.getById(id);
		return getForumUserFromIntitaUser(currentUser, createGuest);
	}
	@Transactional
	public ForumUser getForumUserFromIntitaEmail(String email, boolean createGuest){
		IntitaUser currentUser = intitaUserService.getIntitaUser(email);
		return getForumUserFromIntitaUser(currentUser, createGuest);
	}
	@Transactional
	public ForumUser getForumUserFromIntitaUser(IntitaUser currentUser, boolean createGuest){
		if(currentUser == null  )
		{
			if(createGuest)
				return register("Guest_" + new ShaPasswordEncoder().encodePassword(((Integer)new Random(new Date().getTime()).nextInt()).toString(), new BCryptPasswordEncoder()), null);
			else
				return null;
		}
		ForumUser tempChatUser = forumUsersRepo.findFirstByIntitaUser(currentUser);
		if(tempChatUser == null )
		{
			tempChatUser = register(currentUser.getNickName(), currentUser);
		}
		return tempChatUser;
	}
	@Transactional
	public IntitaUser getIntitaUserFromForumUserId(Long id) {
		ForumUser cUser = getChatUser(id);
		return cUser.getIntitaUser();
	}

	@Transactional
	public ForumUser getForumUser(String nickName) {
		return forumUsersRepo.findOneByNickName(nickName);
	}

	@Transactional(readOnly = false)
	public ForumUser register(String nickName, IntitaUser intitaUser) {
		ForumUser u = new ForumUser(nickName,intitaUser);
		forumUsersRepo.save(u);
		return u;
	}

	@Transactional
	public void updateChatUserInfo(ForumUser u){
		forumUsersRepo.save(u);
	}
	@Transactional
	public void removeUser(Long id){
		forumUsersRepo.delete(id);
	}
	@Transactional
	public List<ForumUser> getChatUsersLike(String nickName){
		return forumUsersRepo.findFirst5ByNickNameLike(nickName + "%");
	}

	/*public ChatUser getById(Long id){
		return usersRepo.findOne(id);
	}*/


}
