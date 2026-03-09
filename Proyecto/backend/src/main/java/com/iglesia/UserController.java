package com.iglesia;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public UserResponse createClient(@RequestBody CreateUserRequest request) {
        return userService.createClient(request);
    }

    public record CreateUserRequest(
        @Email @NotBlank String email,
        @NotBlank String password
    ) {}

    public record UserResponse(
        Long id,
        String email,
        String role
    ) {
        public static UserResponse from(AppUser user) {
            return new UserResponse(user.getId(), user.getEmail(), user.getRole().name());
        }
    }
}