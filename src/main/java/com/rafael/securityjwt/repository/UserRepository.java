package com.rafael.securityjwt.repository;

import com.rafael.securityjwt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad User.
 *
 * Al extender JpaRepository obtenemos automaticamente metodos CRUD:
 * save(), findById(), findAll(), delete(), etc.
 *
 * Spring Data JPA genera la implementacion automaticamente a partir
 * del nombre de los metodos (Query Methods). No necesitamos escribir SQL.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su username.
     * Spring Data traduce esto a: SELECT * FROM users WHERE username = ?
     * Retorna Optional porque el usuario podria no existir.
     */
    Optional<User> findByUsername(String username);

    /**
     * Verifica si existe un usuario con ese username.
     * Spring Data traduce esto a: SELECT COUNT(*) > 0 FROM users WHERE username = ?
     * Util para validar antes de registrar un usuario nuevo.
     */
    boolean existsByUsername(String username);
}
