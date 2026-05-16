package com.fabriciodev.authsystem.controller;

import com.fabriciodev.authsystem.dto.AuthResponse;
import com.fabriciodev.authsystem.dto.LoginRequest;
import com.fabriciodev.authsystem.dto.RegisterRequest;
import com.fabriciodev.authsystem.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
		if (req.getEmail() == null || req.getPassword() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email and password are required");
		}

		authService.registerUser(req);

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest req) {
		if (req.getEmail() == null || req.getPassword() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email and password are required");
		}

		AuthResponse resp = authService.login(req);

		return ResponseEntity.ok(resp);
	}
}


