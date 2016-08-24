package com.intita.forum.services;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.forum.models.ConfigParam;
import com.intita.forum.repositories.ConfigParamRepository;


@Service
public class ConfigParamService {
	@Autowired
	ConfigParamRepository configParamRepo;
	@Transactional
public ConfigParam getParam(String param){
	return configParamRepo.findFirstByParam(param);
}
	public List<ConfigParam> getParams(){
		return (List<ConfigParam>) configParamRepo.findAll();
	}
	 HashMap<String,String> cachedConfigMap = new HashMap<String,String>();
	public HashMap<String, String> getCachedConfigMap() {
		return cachedConfigMap;
	}
	public void setCachedConfigMap(HashMap<String, String> cachedConfigMap) {
		this.cachedConfigMap = cachedConfigMap;
	}
	@Transactional
	public void refreshCachedConfigFromDb(){
		List<ConfigParam> config =  getParams();
		cachedConfigMap = ConfigParam.listAsMap(config);
	}
}
