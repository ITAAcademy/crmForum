package com.intita.forum.services;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;

@Service
public class RedisService {

	/*@Bean
	@Description("Jedis need it")
	public ConfigurationClassPostProcessor importRegistry() {
		return new ConfigurationClassPostProcessor();
	}*/

	private Jedis jedis;
	private JedisPool pool;

	@PostConstruct
	public void AutoUpdate() {
		
		
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(50);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);
		// this(poolConfig, host, port, timeout, password, Protocol.DEFAULT_DATABASE, null);
		pool = new JedisPool(poolConfig, "localhost", 6379, 5000, "1234567");
		try {
			jedis = pool.getResource();
			System.out.println("@@@_REDIS_OK@@@");
		} catch (Exception e) {
			System.out.println("@@@_REDIS_ERR@@@");
			System.out.println(e.getMessage());
		}
	}


	public String getKeyValue(String key) {
		if(!jedis.isConnected())
		{
			//jedis.close();
			jedis.connect();
			return new String();
		}
		try{
		if(jedis != null)
			return  jedis.get(key);
		else
			return new String();
		}
		catch(JedisConnectionException ex)
		{
			/*jedis.close();
			jedis.connect();*/
			jedis = pool.getResource();
			return new String();
		}
	}
	
	public void setValueByKey(String key, String value) {
		if(jedis != null)
			jedis.set(key, value);
	}

}

