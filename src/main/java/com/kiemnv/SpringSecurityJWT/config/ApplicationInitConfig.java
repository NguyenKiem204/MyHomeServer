package com.kiemnv.SpringSecurityJWT.config;

import com.kiemnv.SpringSecurityJWT.entity.Role;
import com.kiemnv.SpringSecurityJWT.entity.User;
import com.kiemnv.SpringSecurityJWT.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitConfig {
    private PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                var roles = new HashSet<String>();
                roles.add(Role.ADMIN.name());
                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .email("nkiem347@gmail.com")
                        .roles(Set.of(Role.ADMIN))
                        .build();
                userRepository.save(user);
                log.warn("admin user has bean create with default password: admin, please change it");
            }
        };
    }

}

