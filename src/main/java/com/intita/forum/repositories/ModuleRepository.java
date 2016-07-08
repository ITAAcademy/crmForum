package com.intita.forum.repositories;

import java.util.ArrayList;

import org.springframework.data.repository.CrudRepository;

import com.intita.forum.models.Module;

public interface ModuleRepository  extends CrudRepository<Module,Long> {
public ArrayList<Module> findAll();
}
