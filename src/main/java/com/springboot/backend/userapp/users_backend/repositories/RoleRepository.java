package com.springboot.backend.userapp.users_backend.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.springboot.backend.userapp.users_backend.entities.Role;


public interface RoleRepository extends CrudRepository <Role,Long>{

    Optional <Role> findByName(String name);

}
