package com.rafael.securityjwt.service;

import com.rafael.securityjwt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementacion de UserDetailsService de Spring Security.
 *
 * Este servicio es el PUENTE entre nuestra base de datos y Spring Security.
 * Spring Security no sabe como acceder a nuestra BD, asi que le proporcionamos
 * este servicio para que pueda cargar los datos del usuario cuando los necesite.
 *
 * Se usa internamente por el AuthenticationManager durante el login
 * y por el JwtAuthenticationFilter para validar tokens.
 *
 * @RequiredArgsConstructor: Lombok genera un constructor con los campos final,
 * lo que permite la inyeccion de dependencias por constructor (recomendado por Spring).
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carga un usuario desde la BD y lo convierte al formato que Spring Security entiende.
     *
     * Spring Security trabaja con la interfaz UserDetails, no con nuestra entidad User.
     * Por eso convertimos nuestra entidad a un objeto UserDetails con:
     * - username
     * - password (hasheado)
     * - authorities (roles del usuario con prefijo ROLE_)
     *
     * El prefijo "ROLE_" es una convencion de Spring Security.
     * Cuando en SecurityConfig escribimos hasRole("ADMIN"), internamente
     * busca la authority "ROLE_ADMIN".
     *
     * @param username nombre de usuario a buscar
     * @return UserDetails con la info del usuario para Spring Security
     * @throws UsernameNotFoundException si el usuario no existe en la BD
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
