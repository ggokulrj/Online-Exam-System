package com.example.onlineexam.service;

import com.example.onlineexam.dto.AuthResponse;
import com.example.onlineexam.dto.LoginRequest;
import com.example.onlineexam.dto.RegisterRequest;
import com.example.onlineexam.exception.ApiException;
import com.example.onlineexam.model.Role;
import com.example.onlineexam.model.User;
import com.example.onlineexam.repository.UserRepository;
import com.example.onlineexam.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public void register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username already taken");
        }
        User user = new User();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(Role.STUDENT); // role is never taken from the request
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
}
