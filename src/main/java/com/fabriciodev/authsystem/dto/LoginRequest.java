package com.fabriciodev.authsystem.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class LoginRequest {
    private String email;
    private String password;
}

