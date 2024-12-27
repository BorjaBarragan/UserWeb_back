package com.springboot.backend.userapp.users_backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.backend.userapp.users_backend.entities.Role;
import com.springboot.backend.userapp.users_backend.entities.User;
import com.springboot.backend.userapp.users_backend.models.IUser;
import com.springboot.backend.userapp.users_backend.models.UserRequest;
import com.springboot.backend.userapp.users_backend.repositories.RoleRepository;
import com.springboot.backend.userapp.users_backend.repositories.UserRepository;

//Recordar que los servicios actuan como capas intermedias entre controladores y repositorios.
@Service
public class UserServiceImpl implements UserService {

    // 1. sin constructor y con @Autowired
    // @Autowired
    // private UserRepository repository
    // 2. con constructor

    private UserRepository repository;

    private PasswordEncoder passwordEncoder;

    private RoleRepository roleRepository;

    // Inyección de dependencias a través del constructor
    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    // Recordar! ->
    // 1-Visibilidad metodo (public)
    // 2-Tipo de retorno (Optional<User>, List<User>)
    // 3-Nombre del metodo
    // 4-Parametros del metodo
    // 5- return si no es void repositorio.metodo(parametro)

    @Override
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return this.repository.findAll(pageable);
    }

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
        user.setRoles(getRoles(user));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return this.repository.save(user);
    }

    @Override
    @Transactional
    public Optional<User> update(UserRequest user, Long id) {
        // Busca el usuario por ID
        Optional<User> userOptional = repository.findById(id);

        // Si el usuario está presente, lo actualiza con los nuevos datos
        if (userOptional.isPresent()) {
            User userDb = userOptional.get(); // Obtiene el usuario de la base de datos
            userDb.setName(user.getName()); // Actualiza el nombre
            userDb.setLastName(user.getLastName()); // Actualiza el apellido
            userDb.setEmail(user.getEmail()); // Actualiza el email
            userDb.setUserName(user.getUserName()); // Actualiza el nombre de usuario

            userDb.setRoles(getRoles(user));
            return Optional.of(repository.save(userDb));
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public Long deleteById(Long id) {
        this.repository.deleteById(id);
        return id;
    }

    private List<Role> getRoles(IUser user) {
        List<Role> roles = new ArrayList<>();
        // 1. Buscar el rol "ROLE_USER" en la base de datos
        Optional<Role> optionalRoleUser = roleRepository.findByName("ROLE_USER");
        // 2. Si el rol existe, añadirlo a la lista de roles del usuario
        optionalRoleUser.ifPresent(role -> roles.add(role));

        if (user.isAdmin()) {
            Optional<Role> optionalRoleAdmin = roleRepository.findByName("ROLE_ADMIN");
            // 2. Si el rol existe, añadirlo a la lista de roles del usuario
            optionalRoleAdmin.ifPresent(role -> roles.add(role));
        }
        return roles;
    }
}
