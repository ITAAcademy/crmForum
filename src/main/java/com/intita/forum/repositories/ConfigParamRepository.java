package com.intita.forum.repositories;

import org.springframework.data.repository.CrudRepository;

import com.intita.forum.models.ConfigParam;

public interface ConfigParamRepository extends CrudRepository<ConfigParam, Long> {
ConfigParam findFirstByParam(String param);
}
