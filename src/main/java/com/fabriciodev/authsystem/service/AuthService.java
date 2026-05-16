package com.fabriciodev.authsystem.service;

import com.fabriciodev.authsystem.domain.entity.Role;
import com.fabriciodev.authsystem.domain.entity.User;
import com.fabriciodev.authsystem.domain.enums.RoleType;
import com.fabriciodev.authsystem.dto.AuthResponse;
import com.fabriciodev.authsystem.dto.LoginRequest;
import com.fabriciodev.authsystem.dto.RegisterRequest;
import com.fabriciodev.authsystem.repository.RoleRepository;
import com.fabriciodev.authsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public void registerUser(RegisterRequest req) {
        if (userRepository.findByEmail(req.getEmail()) != null) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setName(req.getName());
        user.setEnabled(Boolean.TRUE);

        // Ensure ROLE_USER exists
        Role userRole = roleRepository.findByName(RoleType.USER);
        if (userRole == null) {
            userRole = new Role();
            userRole.setName(RoleType.USER);
            userRole.setDescription("Default user role");
            roleRepository.save(userRole);
        }

        user.setRoles(Collections.singletonList(userRole));
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail());
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        String access = tokenService.generateAccessToken(user);
        String refresh = tokenService.generateRefreshToken(user);

        return new AuthResponse(access, refresh);
    }

}
