package com.springboot.backend.userapp.users_backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import static jakarta.persistence.GenerationType.*;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.springboot.backend.userapp.users_backend.models.IUser;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User implements IUser{

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Column(name = "lastName")
    private String lastName;

    @NotEmpty
    @Email
    private String email;

    @NotBlank
    @Size(min = 4, max = 12)
    private String userName;

    @NotBlank
    private String password;

    @Transient
    //Transient significa que admin no esta mapeado, no es parte de la bbdd
    //atributo propio de la clase
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean admin;

    // Cuando estás trabajando con frameworks como Hibernate (que gestiona la base de datos),
    // puede generar automáticamente algunos campos adicionales en los objetos de tus entidades, como handler y hibernateLazyInitializer.
    // Estos campos son internos y no tienen utilidad para tu API o tus clientes.
    
    //MAPEAMOS USUARIO Y ROL, CON SUS RELACIONES!
    @JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
    // handler -> proxies dinámicos
    // hibernateLazyInitializer ->Este campo aparece cuando tienes relaciones con carga perezosa

    // El atributo fetch especifica cómo se cargarán los datos relacionados cuando consultes una entidad.
    // FetchType.LAZY (Carga perezosa)
    // FetchType.EAGER se carga todo de una sola vez (innecesario)
    @ManyToMany(fetch = FetchType.LAZY)
    //Tabla intermedia
    @JoinTable(name = "users_roles",
            // Define cuál es la columna en la tabla intermedia que conecta con la tabla users.
            joinColumns = { @JoinColumn(name = "user_id") },
            // Define cuál es la columna en la tabla intermedia que conecta con la tabla roles.
            inverseJoinColumns = { @JoinColumn(name = "role_id") },
            // Agrega una restricción de unicidad para asegurarte de que no haya combinaciones duplicadas de user_id y role_id en la tabla intermedia.
            uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "role_id" }) })       
    
    // En User, necesitas inicializar la lista porque los usuarios pueden tener roles asignados
    // y necesitas un contenedor para esas relaciones.       
    private List<Role> roles;

    // Constructor en Java - Inicialización: En el constructor, se inicializa con una lista vacía de roles.
    //sin esa inicializacion povocaria un error.
    public User() {
        this.roles = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
