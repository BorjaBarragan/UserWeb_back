package com.springboot.backend.userapp.users_backend.services;

import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;

import com.springboot.backend.userapp.users_backend.entities.User;

public interface UserService {

    List<User> findAll();

    Optional <User> findById(@NonNull Long id);

    User save(User user);

    void deleteById(Long id);
}
