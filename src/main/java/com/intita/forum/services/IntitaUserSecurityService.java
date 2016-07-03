package com.intita.forum.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.forum.models.IntitaUser;
import com.intita.forum.repositories.IntitaUserRepository;

@Service
public class IntitaUserSecurityService implements UserDetailsService {
	
	@Autowired
	private IntitaUserRepository usersRepo;
	
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		
		IntitaUser user = usersRepo.findByLogin(username);
		//User user = usersRepo.findByLogin(username);

		if(user == null) {
			throw new UsernameNotFoundException("User with login " + username + " was not found");
		}
		
		return user;
	}
}