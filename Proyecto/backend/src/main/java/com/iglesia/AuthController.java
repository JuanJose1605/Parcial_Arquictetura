package com.iglesia;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
    ) {}

    public record LoginResponse(
        String token,
        String email,
        String role
    ) {}
}