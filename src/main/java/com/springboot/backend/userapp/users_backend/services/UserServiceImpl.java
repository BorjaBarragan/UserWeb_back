package com.springboot.backend.userapp.users_backend.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.backend.userapp.users_backend.entities.User;
import com.springboot.backend.userapp.users_backend.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService {
   
    // Repositorio que interactúa con la base de datos
    private UserRepository repository;

    // Inyección de dependencias a través del constructor
    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }
    //En vez del constructor podemos hacerlo asi : 
    // @Autowired 
    // private UserRepository repository;
 
    //Recordar! ->
    // 1-Visibilidad metodo (public)
    // 2-Tipo de retorno (Optional<User>, List<User>)
    // 3-Nombre del metodo
    // 4-Parametros del metodo
    // 5- return si no es void repositorio.metodo(parametro)
    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
       // return (List) this.repository.findAll();
       return (List<User>) this.repository.findAll(); 
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(@NonNull Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public User save(User user) {
        return this.repository.save(user);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        this.repository.deleteById(id);
    }

}
