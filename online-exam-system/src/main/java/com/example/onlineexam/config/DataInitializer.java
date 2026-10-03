package com.example.onlineexam.config;

import com.example.onlineexam.model.Role;
import com.example.onlineexam.model.User;
import com.example.onlineexam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Creates the first ADMIN account. Public registration can only create STUDENT accounts. */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createAdmin(UserRepository users, PasswordEncoder encoder,
                                  @Value("${app.admin.username}") String username,
                                  @Value("${app.admin.password}") String password) {
        return args -> {
            if (!users.existsByUsername(username)) {
                User admin = new User();
                admin.setUsername(username);
                admin.setPassword(encoder.encode(password));
                admin.setRole(Role.ADMIN);
                users.save(admin);
            }
        };
    }
}
