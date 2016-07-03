package com.intita.forum.event;

import java.util.concurrent.ConcurrentSkipListMap;

import org.springframework.stereotype.Component;


/**
 * 
 * @author Nicolas
 */

@Component
public class ParticipantRepository {

	public ConcurrentSkipListMap<String,Integer> getActiveSessions() {
		return activeSessions;
	}

	private ConcurrentSkipListMap<String,Integer> activeSessions = new ConcurrentSkipListMap<String,Integer>();
	//put null to distinguish WS from LP sessions. We need no last action time for Web Sockets
	public void add(String forumId) {
		if (activeSessions.containsKey(forumId)){
			int presenceIndex = activeSessions.get(forumId);
			activeSessions.put(forumId, presenceIndex+1);
			//System.out.println("presence increased to:"+(presenceIndex+1));
		}
		else{
	activeSessions.put(forumId,1);
	System.out.println("presence added with start value:"+1);
		}
		
	}

	public boolean isOnline(String forumId) {
		boolean containsKey = activeSessions.containsKey(forumId);
		Integer getId = activeSessions.get(forumId);
		
		boolean online = containsKey &&  getId > 0;
		//System.out.println("isOnline "+chatId+" ? "+ online);
		return online;
	}

	public void removeParticipant(String forumId) {
		if (activeSessions.containsKey(forumId)){
			int presenceIndex = activeSessions.get(forumId);
			if (presenceIndex<=0)
			{
				activeSessions.remove(forumId);
				System.out.println("presence removed");
			}
			else
			{
			activeSessions.put(forumId, presenceIndex-1);
			//System.out.println("presence decreased to:"+(presenceIndex-1));
			}
		}
	}
}
