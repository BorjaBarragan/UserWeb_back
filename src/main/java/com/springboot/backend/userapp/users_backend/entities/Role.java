package com.springboot.backend.userapp.users_backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")

public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // El constructor vacío es obligatorio para que JPA funcione correctamente.
    // El constructor parametrizado es opcional pero muy útil para simplificar la creación de objetos.
    
    //Si no tuvieramos este constructor (ni otro vacío), obtendríamos un error en tiempo de ejecución
    // cuando JPA intente cargar datos de la base de datos.
    
    public Role() {
    }

    public Role(String name) {
        this.name = name;
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

}
